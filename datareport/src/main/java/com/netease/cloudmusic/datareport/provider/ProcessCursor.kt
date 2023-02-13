package com.netease.cloudmusic.datareport.provider

import android.database.AbstractCursor
import android.database.CursorWindow

internal class ProcessCursor(private val mCount: Int, private val mValue: Any?): AbstractCursor() {

    companion object{
        private const val FIELD_TYPE_NULL = 0
        private const val FIELD_TYPE_INTEGER = 1
        private const val FIELD_TYPE_FLOAT = 2
        private const val FIELD_TYPE_STRING = 3
        private const val FIELD_TYPE_BLOB = 4
    }

    override fun getLong(column: Int): Long {
        if (mValue is Int) {
            return mValue.toLong()
        }
        return mValue as? Long ?: 0
    }

    override fun getCount(): Int {
        return mCount
    }

    override fun getColumnNames(): Array<String> {
        return arrayOf("value")
    }

    override fun getShort(column: Int): Short {
        return mValue as? Short ?: 0
    }

    override fun getFloat(column: Int): Float {
        return mValue as? Float ?: 0f
    }

    override fun getDouble(column: Int): Double {
        if (mValue is Float) {
            return mValue.toDouble()
        }
        return mValue as? Double ?: 0.0
    }

    override fun isNull(column: Int): Boolean {
        return mValue == null
    }

    override fun getInt(column: Int): Int {
        return mValue as? Int ?: 0
    }

    override fun getString(column: Int): String? {
        return mValue as? String?
    }

    override fun getType(column: Int): Int {
        if (mValue is String) {
            return FIELD_TYPE_STRING
        } else if (mValue is Int || mValue is Long) {
            return FIELD_TYPE_INTEGER
        } else if (mValue is Float || mValue is Double) {
            return FIELD_TYPE_FLOAT
        }
        return FIELD_TYPE_NULL
    }

    override fun fillWindow(pos: Int, window: CursorWindow) {
        var position = pos
        if (position < 0 || position >= count) {
            return
        }
        val oldPos = getPosition()
        val numColumns = columnCount
        window.clear()
        window.startPosition = position
        window.setNumColumns(numColumns)
        if (moveToPosition(position)) {
            rowloop@ do {
                if (!window.allocRow()) {
                    break
                }
                for (i in 0 until numColumns) {
                    val type = getType(i)
                    val success: Boolean
                    success = when (type) {
                        FIELD_TYPE_NULL -> window.putNull(position, i)
                        FIELD_TYPE_INTEGER -> window.putLong(getLong(i), position, i)
                        FIELD_TYPE_FLOAT -> window.putDouble(getDouble(i), position, i)
                        FIELD_TYPE_BLOB -> {
                            val value = getBlob(i)
                            if (value != null) window.putBlob(value, position, i) else window.putNull(position, i)
                        }
                        FIELD_TYPE_STRING -> {
                            val value = getString(i)
                            if (value != null) window.putString(value, position, i) else window.putNull(position, i)
                        }
                        else -> {
                            val value = getString(i)
                            if (value != null) window.putString(value, position, i) else window.putNull(position, i)
                        }
                    }
                    if (!success) {
                        window.freeLastRow()
                        break@rowloop
                    }
                }
                position += 1
            } while (moveToNext())
        }
        moveToPosition(oldPos)
    }
}