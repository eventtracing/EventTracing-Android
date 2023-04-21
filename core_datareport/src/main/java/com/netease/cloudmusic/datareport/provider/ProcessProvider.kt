package com.netease.cloudmusic.datareport.provider

import android.content.*
import android.database.Cursor
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import com.netease.cloudmusic.datareport.inner.DataReportInner
import com.netease.cloudmusic.datareport.report.refer.PreReferStorage
import com.netease.cloudmusic.datareport.report.refer.QueueList
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

/**
 * 该ContentProvider 主要用来支持跨进程的SP
 */
class ProcessProvider : ContentProvider() {

    companion object {
        private const val QUERY_GET = 1
        private const val QUERY_GET_ALL = 2
        private const val QUERY_CONTAINS = 3

        const val ACTION_PREFERENCES_CHANGE = "com.netease.cloudmusic.datareport.provider.PREFERENCES_CHANGE"
        const val EXTRA_NAME = "name"
        const val EXTRA_KEYS = "keys"

        const val KEYS = "keys"

        private const val TAG = "ProcessProvider"

        fun jsonArrayToStringSet(array: JSONArray): HashSet<String> {
            val set = HashSet<String>()
            try {
                for (i in array.length() - 1 downTo 0) {
                    set.add(array.getString(i))
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            return set
        }

        fun stringSetToJSONArray(set: Set<String>): JSONArray {
            val array = JSONArray()
            for (s in set) {
                array.put(s)
            }
            return array
        }
    }

    private var mMemoryStorage: HashMap<String, HashMap<String, Any>> = HashMap()

    private val mListeners: HashMap<String, HashMap<String?, Int>> = HashMap()

    private val mUriMatcher: UriMatcher = UriMatcher(UriMatcher.NO_MATCH)

    override fun onCreate(): Boolean {
        val authority: String = ProcessContract.getAuthority()
        mUriMatcher.addURI(authority, "*/*/" + ProcessContract.QUERY_GET_ALL, QUERY_GET_ALL)
        mUriMatcher.addURI(authority, "*/*/" + ProcessContract.QUERY_GET, QUERY_GET)
        mUriMatcher.addURI(authority, "*/*/" + ProcessContract.QUERY_CONTAINS, QUERY_CONTAINS)
        registerAction()
        return true
    }

    private fun registerAction() {
        PreReferStorage.registerPreferenceAction()
        QueueList.registerPreferenceAction()
        DataReportInner.registerPreferenceAction()
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        if (values == null) {
            return null
        }
        val name = uri.pathSegments[0]
        val keys = values.getAsString(KEYS)
        try {
            val keyArray: JSONArray? = if (TextUtils.isEmpty(keys)) null else JSONArray(keys)
            synchronized(mListeners) {
                var listeners = mListeners[name]
                if (listeners == null) {
                    listeners = HashMap()
                    if (keyArray == null) {
                        listeners[null] = 1
                    } else {
                        for (i in keyArray.length() - 1 downTo 0) {
                            listeners[keyArray.getString(i)] = 1
                        }
                    }
                    mListeners.put(name, listeners)
                } else {
                    if (keyArray == null) {
                        val count = listeners[null]
                        if (count == null) {
                            listeners.put(null, 1)
                        } else {
                            listeners.put(null, count + 1)
                        }
                    } else {
                        for (i in keyArray.length() - 1 downTo 0) {
                            val key: String = keyArray.getString(i)
                            val count = listeners[key]
                            if (count == null) {
                                listeners[key] = 1
                            } else {
                                listeners[key] = count + 1
                            }
                        }
                    }
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return null
    }

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? {
        var cursor: Cursor? = null
        val paths = uri.pathSegments
        val name = paths[0]
        val inMemory = paths[1].toInt() == ProcessPreferences.MODE_IN_MEMORY
        when (mUriMatcher.match(uri)) {
            QUERY_GET -> {
                when (sortOrder?.toInt() ?: 0) {
                    ProcessPreferences.TYPE_STRING -> {
                        val string: String?
                        if (inMemory) { string = getFromMemoryPreferences(name, projection!![0]) as String? } else {
                            synchronized(this){ string = context!!.getSharedPreferences(name, Context.MODE_PRIVATE).getString(projection!![0], null)} }
                        cursor = ProcessCursor(if (string == null) 0 else 1, string)
                    }
                    ProcessPreferences.TYPE_STRING_SET -> {
                        val set: Set<String>?
                        if (inMemory) { set = getFromMemoryPreferences(name, projection!![0]) as Set<String>? } else {
                            synchronized(this){ set = context!!.getSharedPreferences(name, Context.MODE_PRIVATE).getStringSet(projection!![0], null)} }
                        var setJsonStr: String? = null
                        if (set != null) {
                            setJsonStr = stringSetToJSONArray(set).toString()
                        }
                        cursor = ProcessCursor(if (set == null) 0 else 1, setJsonStr)
                    }
                    ProcessPreferences.TYPE_INT -> {
                        val intVal: Int
                        if (inMemory) {
                            val valObj: Any? = getFromMemoryPreferences(name, projection!![0])
                            intVal = if (valObj == null) selection!!.toInt() else valObj as Int
                        } else {
                            synchronized(this){ intVal = context!!.getSharedPreferences(name, Context.MODE_PRIVATE).getInt(projection!![0], selection!!.toInt())}
                        }
                        cursor = ProcessCursor(1, intVal)
                    }
                    ProcessPreferences.TYPE_LONG -> {
                        val longVal: Long
                        if (inMemory) {
                            val valObj: Any? = getFromMemoryPreferences(name, projection!![0])
                            longVal = if (valObj == null) selection!!.toLong() else valObj as Long
                        } else {
                            synchronized(this){ longVal = context!!.getSharedPreferences(name, Context.MODE_PRIVATE).getLong(projection!![0], selection!!.toLong())}
                        }
                        cursor = ProcessCursor(1, longVal)
                    }
                    ProcessPreferences.TYPE_FLOAT -> {
                        val floatVal: Float
                        if (inMemory) {
                            val valObj: Any? = getFromMemoryPreferences(name, projection!![0])
                            floatVal = if (valObj == null) selection!!.toFloat() else valObj as Float
                        } else {
                            synchronized(this){ floatVal = context!!.getSharedPreferences(name, Context.MODE_PRIVATE).getFloat(projection!![0], selection!!.toFloat())}
                        }
                        cursor = ProcessCursor(1, floatVal)
                    }
                    ProcessPreferences.TYPE_BOOLEAN -> {
                        val booleanVal: Boolean
                        if (inMemory) {
                            val valObj: Any? = getFromMemoryPreferences(name, projection!![0])
                            booleanVal = if (valObj == null) java.lang.Boolean.parseBoolean(selection) else valObj as Boolean
                        } else {
                            synchronized(this){ booleanVal = context!!.getSharedPreferences(name, Context.MODE_PRIVATE).getBoolean(projection!![0], java.lang.Boolean.parseBoolean(selection))}
                        }
                        cursor = ProcessCursor(1, if (booleanVal) 1 else 0)
                    }
                }
            }
            QUERY_GET_ALL -> {
                var map: Map<String, *>
                if (inMemory) {
                    synchronized(this) { map = HashMap(getMemoryPreferences(name)) }
                } else {
                    synchronized(this) { map = context!!.getSharedPreferences(name, Context.MODE_PRIVATE).all}
                }
                val json = JSONObject()
                try {
                    for ((key, value) in map) {
                        when (value) {
                            null -> {
                                json.put(key, JSONObject.NULL)
                            }
                            is String -> {
                                val array = JSONArray()
                                array.put(ProcessPreferences.TYPE_STRING)
                                array.put(value)
                                json.put(key, array)
                            }
                            is Set<*> -> {
                                val array = JSONArray()
                                array.put(ProcessPreferences.TYPE_STRING_SET)
                                array.put(stringSetToJSONArray(value as Set<String>))
                                json.put(key, array)
                            }
                            is Int -> {
                                val array = JSONArray()
                                array.put(ProcessPreferences.TYPE_INT)
                                array.put(value)
                                json.put(key, array)
                            }
                            is Long -> {
                                val array = JSONArray()
                                array.put(ProcessPreferences.TYPE_LONG)
                                array.put(value)
                                json.put(key, array)
                            }
                            is Float -> {
                                val array = JSONArray()
                                array.put(ProcessPreferences.TYPE_FLOAT)
                                array.put(value)
                                json.put(key, array)
                            }
                            is Boolean -> {
                                val array = JSONArray()
                                array.put(ProcessPreferences.TYPE_BOOLEAN)
                                array.put(value)
                                json.put(key, array)
                            }
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                cursor = ProcessCursor(1, json.toString())
            }
            QUERY_CONTAINS -> {
                var contains: Boolean
                if (inMemory) {
                    synchronized(this) { contains = getMemoryPreferences(name).containsKey(projection!![0]) }
                } else {
                    synchronized(this) { contains = context!!.getSharedPreferences(name, Context.MODE_PRIVATE).contains(projection!![0]) }
                }
                cursor = ProcessCursor(1, if (contains) 1 else 0)
            }
        }
        return cursor
    }

    @Synchronized
    private fun getFromMemoryPreferences(name: String, key: Any): Any? {
        return getMemoryPreferences(name)[key]
    }

    private fun getMemoryPreferences(name: String): HashMap<String, Any> {
        var memoryPreferences = mMemoryStorage[name]
        if (memoryPreferences == null) {
            memoryPreferences = HashMap()
            mMemoryStorage[name] = memoryPreferences
        }
        return memoryPreferences
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        val paths = uri.pathSegments
        val name = paths[0]
        val inMemory = paths[1].toInt() == ProcessPreferences.MODE_IN_MEMORY
        val clear = java.lang.Boolean.parseBoolean(uri.getQueryParameter(ProcessContract.PARAM_CLEAR))
        val modifiedKeys = ArrayList<String>()
        if (inMemory) {
            synchronized(this) {
                val memoryPreferences = getMemoryPreferences(name)
                if (clear) {
                    memoryPreferences.clear()
                }
                for ((key, value) in values!!.valueSet()) {
                    if (value == null) {
                        memoryPreferences.remove(key)
                    } else {
                        memoryPreferences[key] = value
                    }
                    modifiedKeys.add(key)
                }
                if (selectionArgs != null) {
                    try {
                        val stringSetValueArray = JSONArray(selection)
                        for (i in selectionArgs.indices) {
                            memoryPreferences[selectionArgs[i]] = jsonArrayToStringSet(stringSetValueArray.getJSONArray(i))
                            modifiedKeys.add(selectionArgs[i])
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
            }
        } else {
            synchronized(this) {
                val syncAction = uri.getQueryParameter(ProcessContract.PARAM_SYNC_ACTION)

                val innerSp = context!!.getSharedPreferences(name, Context.MODE_PRIVATE)
                val editor = innerSp.edit()
                if (clear) {
                    editor.clear()
                }

                if (syncAction != null) {
                    ProcessUpdateManager.invokeAction(syncAction, innerSp, editor, values)?.let {
                        modifiedKeys.addAll(it)
                    }
                }

                for ((key, value) in values!!.valueSet()) {
                    when (value) {
                        null -> {
                            editor.remove(key)
                        }
                        is String -> {
                            editor.putString(key, value as String)
                        }
                        is Int -> {
                            editor.putInt(key, value)
                        }
                        is Long -> {
                            editor.putLong(key, value)
                        }
                        is Float -> {
                            editor.putFloat(key, value)
                        }
                        is Boolean -> {
                            editor.putBoolean(key, value)
                        }
                    }
                    modifiedKeys.add(key)
                }
                if (selectionArgs != null) {
                    try {
                        val stringSetValueArray = JSONArray(selection)
                        for (i in selectionArgs.indices) {
                            editor.putStringSet(selectionArgs[i], jsonArrayToStringSet(stringSetValueArray.getJSONArray(i)))
                            modifiedKeys.add(selectionArgs[i])
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
                if (java.lang.Boolean.parseBoolean(uri.getQueryParameter(ProcessContract.PARAM_IMMEDIATELY))) {
                    editor.commit()
                } else {
                    editor.apply()
                }
            }
        }

        //notify listeners
        if (modifiedKeys.size > 0) {
            var keySet: HashSet<String?>? = null
            synchronized(mListeners) {
                val listeners = mListeners[name]
                if (listeners != null) {
                    keySet = HashSet(listeners.keys)
                }
            }
            if (keySet != null) {
                if (!keySet!!.contains(null)) {
                    modifiedKeys.retainAll(keySet!!)
                }
                if (modifiedKeys.size > 0) {
                    val context = context
                    if (context != null) {
                        val intent = Intent(ACTION_PREFERENCES_CHANGE)
                        intent.setPackage(context.packageName)
                        intent.putExtra(EXTRA_NAME, name)
                        intent.putStringArrayListExtra(EXTRA_KEYS, modifiedKeys)
                        try {
                            context.sendBroadcast(intent)
                        } catch (e: RuntimeException) {
                            Log.e(TAG, "", e)
                        }
                    }
                }
            }
        }

        return 0
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String?>?): Int {
        val name = uri.pathSegments[0]
        synchronized(mListeners) {
            val listeners = mListeners[name]
            if (listeners != null) {
                val keys: Array<String?> = selectionArgs ?: arrayOf<String?>(null)
                for (key in keys) {
                    var count = listeners[key]
                    if (count != null) {
                        count -= 1
                        if (count > 0) {
                            listeners[key] = count
                        } else {
                            listeners.remove(key)
                        }
                    }
                }
                if (listeners.size == 0) {
                    mListeners.remove(name)
                }
            }
        }
        return 0
    }

    override fun getType(uri: Uri): String? {
        return null
    }


}