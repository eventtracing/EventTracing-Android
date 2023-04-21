package com.netease.cloudmusic.datareport.provider

import android.content.*
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.TextUtils
import android.util.Log
import android.util.Pair
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

/**
 * 用于多进程存储的sp
 */
class ProcessPreferences private constructor(private val mContext: Context, private val mName: String, private val mMode: Int): SharedPreferences {

    companion object {

        const val WHAT_UPDATE = 1
        const val WHAT_INSERT = 2
        const val WHAT_DELETE = 3

        const val MODE_DEFAULT = 0
        const val MODE_IN_MEMORY = 1

        const val TYPE_STRING = 1
        const val TYPE_STRING_SET = 2
        const val TYPE_INT = 3
        const val TYPE_LONG = 4
        const val TYPE_FLOAT = 5
        const val TYPE_BOOLEAN = 6

        private val sPrefers = HashMap<String, ProcessPreferences>()

        private const val TAG = "TreasurePreferences"

        fun getInstance(context: Context, name: String): ProcessPreferences{
            return getInstance(context, name, MODE_DEFAULT)
        }

        fun getInstance(context: Context, name: String, mode: Int): ProcessPreferences {
            synchronized(sPrefers) {
                var sp = sPrefers[name]
                if (sp == null) {
                    sp = ProcessPreferences(context, name, mode)
                    sPrefers.put(name, sp)
                }
                return sp
            }
        }
    }

    private val mListeners = WeakHashMap<SharedPreferences.OnSharedPreferenceChangeListener, ArrayList<String>>()
    private var mPreferencesChangeReceiver: BroadcastReceiver? = null

    private val mHandler: RetryHandler = RetryHandler(Looper.getMainLooper())

    private fun buildUri(path: String, params: HashMap<String, String>?): Uri {
        val builder = ProcessContract.getAuthorityUri().buildUpon()
        builder.appendPath(mName).appendPath(mMode.toString() + "").appendPath(path)
        if (params != null) {
            for ((key, value) in params) {
                builder.appendQueryParameter(key, value)
            }
        }
        return builder.build()
    }

    private fun getLocalContentProvider(contentResolver: ContentResolver, uri: Uri): Pair<ContentProviderClient?, ContentProvider?> {
        var client: ContentProviderClient? = null
        var provider: ContentProvider? = null
        try {
            client =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) contentResolver.acquireUnstableContentProviderClient(uri)
                else contentResolver.acquireContentProviderClient(uri)
            provider = client?.localContentProvider
            return Pair(if (provider != null) client else null, provider)
        } catch (t: Throwable) {
            Log.e(TAG, "", t)
        } finally {
            if (provider == null) {
                releaseClientSilently(client)
            }
        }
        return Pair(null, null)
    }

    private fun releaseClientSilently(client: ContentProviderClient?) {
        if (client != null) {
            try {
                client.release()
            } catch (ignored: Throwable) {
            }
        }
    }

    private fun closeCursorSilently(cursor: Cursor?) {
        if (cursor != null) {
            try {
                cursor.close()
            } catch (ignored: Throwable) {
            }
        }
    }

    private fun registerChangeReceiver() {
        if (mPreferencesChangeReceiver == null) {
            mPreferencesChangeReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val name = intent.getStringExtra(ProcessProvider.EXTRA_NAME)
                    if (mName == name) {
                        val modifiedKeys = intent.getStringArrayListExtra(ProcessProvider.EXTRA_KEYS)
                        if (modifiedKeys != null) {
                            val listeners = ArrayList<Pair<SharedPreferences.OnSharedPreferenceChangeListener, String>>()
                            synchronized(mListeners) {
                                for ((key1, keys) in mListeners) {
                                    for (i in modifiedKeys.indices.reversed()) {
                                        val key = modifiedKeys[i]
                                        if (keys == null || keys.contains(key)) {
                                            listeners.add(Pair(key1, key))
                                        }
                                    }
                                }
                            }
                            for (i in listeners.indices.reversed()) {
                                val pair = listeners[i]
                                pair.first.onSharedPreferenceChanged(this@ProcessPreferences, pair.second)
                            }
                        }
                    }
                }
            }
            mContext.registerReceiver(mPreferencesChangeReceiver, IntentFilter(ProcessProvider.ACTION_PREFERENCES_CHANGE))
        }
    }

    private fun insert(uri: Uri, contentValues: ContentValues, rCount: Int) {
        var retryCount = rCount
        var client: ContentProviderClient? = null
        try {
            val contentResolver = mContext.contentResolver
            val clientAndProvider = getLocalContentProvider(contentResolver, uri)
            client = clientAndProvider.first
            val localProvider = clientAndProvider.second
            if (localProvider != null) {
                localProvider.insert(uri, contentValues)
            } else {
                contentResolver.insert(uri, contentValues)
            }
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "", e)
            retryCount++
            if (retryCount <= 100) {
                val message = e.message
                if (!TextUtils.isEmpty(message) && message!!.toLowerCase().contains("content://${ProcessContract.getAuthority()}")) {
                    mHandler.sendMessage(mHandler.obtainMessage(WHAT_INSERT, retryCount, 0, arrayOf(uri, contentValues)))
                }
            }
        } finally {
            releaseClientSilently(client)
        }
    }

    private fun delete(uri: Uri, keys: Array<String>?, rCount: Int) {
        var retryCount = rCount
        var client: ContentProviderClient? = null
        try {
            val contentResolver = mContext.contentResolver
            val clientAndProvider = getLocalContentProvider(contentResolver, uri)
            client = clientAndProvider.first
            val localProvider = clientAndProvider.second
            if (localProvider != null) {
                localProvider.delete(uri, null, keys)
            } else {
                contentResolver.delete(uri, null, keys)
            }
        } catch (e: java.lang.IllegalArgumentException) {
            Log.e(TAG, "", e)
            retryCount++
            if (retryCount <= 100) {
                val message = e.message
                if (!TextUtils.isEmpty(message) && message!!.toLowerCase().contains("content://${ProcessContract.getAuthority()}")) {
                    mHandler.sendMessage(mHandler.obtainMessage(WHAT_DELETE, retryCount, 0, arrayOf(uri, keys)))
                }
            }
        } finally {
            releaseClientSilently(client)
        }
    }

    override fun contains(key: String?): Boolean {
        var client: ContentProviderClient? = null
        var cursor: Cursor? = null
        try {
            val uri = buildUri(ProcessContract.QUERY_CONTAINS, null)
            val contentResolver = mContext.contentResolver
            val clientAndProvider = getLocalContentProvider(contentResolver, uri)
            client = clientAndProvider.first
            val localProvider = clientAndProvider.second
            cursor = if (localProvider != null)
                localProvider.query(uri, arrayOf(key!!), null, null, null)
            else contentResolver.query(uri, arrayOf(key!!), null, null, null)
            if (cursor != null && cursor.moveToNext()) {
                return cursor.getInt(0) == 1
            }
        } catch (t: Throwable) {
            Log.e(TAG, "", t)
        } finally {
            closeCursorSilently(cursor)
            releaseClientSilently(client)
        }
        return false
    }

    override fun getBoolean(key: String?, defValue: Boolean): Boolean {
        var client: ContentProviderClient? = null
        var cursor: Cursor? = null
        try {
            val uri = buildUri(ProcessContract.QUERY_GET, null)
            val contentResolver = mContext.contentResolver
            val clientAndProvider = getLocalContentProvider(contentResolver, uri)
            client = clientAndProvider.first
            val localProvider = clientAndProvider.second
            cursor = if (localProvider != null) localProvider.query(uri, arrayOf(key!!), defValue.toString(), null, TYPE_BOOLEAN.toString() + ""
            ) else contentResolver.query(uri, arrayOf(key!!), defValue.toString(), null, TYPE_BOOLEAN.toString() + "")
            if (cursor != null && cursor.moveToNext()) {
                return cursor.getInt(0) == 1
            }
        } catch (t: Throwable) {
            Log.e(TAG, "", t)
        } finally {
            closeCursorSilently(cursor)
            releaseClientSilently(client)
        }
        return defValue
    }

    /**
     * This method is expensive.
     * You should use {@link #registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener, ArrayList keys)} instead to achieve performance.
     * */
    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        if (listener == null) {
            return
        }
        synchronized(mListeners) {
            mListeners[listener] = null
            registerChangeReceiver()
        }
        val uri = buildUri(ProcessContract.REGISTER, null)
        val values = ContentValues()
        values.put(ProcessProvider.KEYS, null as String?)
        insert(uri, values, 0)
    }

    /**
     * @param listener The callback that will run. listener需要被强引用，否则可能因为被回收了无法回调
     * @param keys The keys that be listened.
     */
    fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?, keys: ArrayList<String>?) {
        if (listener == null || keys == null || keys.size == 0) {
            return
        }
        synchronized(mListeners) {
            mListeners[listener] = keys
            registerChangeReceiver()
        }
        val uri = buildUri(ProcessContract.REGISTER, null)
        val values = ContentValues()
        values.put(ProcessProvider.KEYS, JSONArray(keys).toString())
        insert(uri, values, 0)
    }


    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        if (listener == null) {
            return
        }
        var keys: Array<String>? = null
        synchronized(mListeners) {
            val keyList = mListeners.remove(listener)
            if (keyList != null) {
                keys = keyList.toTypedArray()
            }
            if (mListeners.size == 0) {
                mContext.unregisterReceiver(mPreferencesChangeReceiver)
                mPreferencesChangeReceiver = null
            }
        }
        val uri = buildUri(ProcessContract.UNREGISTER, null)
        delete(uri, keys, 0)
    }

    override fun getInt(key: String?, defValue: Int): Int {
        var client: ContentProviderClient? = null
        var cursor: Cursor? = null
        try {
            val uri = buildUri(ProcessContract.QUERY_GET, null)
            val contentResolver = mContext.contentResolver
            val clientAndProvider = getLocalContentProvider(contentResolver, uri)
            client = clientAndProvider.first
            val localProvider = clientAndProvider.second
            cursor = if (localProvider != null) localProvider.query(uri, arrayOf(key!!), defValue.toString(), null, TYPE_INT.toString()
            ) else contentResolver.query(uri, arrayOf(key!!), defValue.toString(), null, TYPE_INT.toString())
            if (cursor != null && cursor.moveToNext()) {
                return cursor.getInt(0)
            }
        } catch (t: Throwable) {
            Log.e(TAG, "", t)
        } finally {
            closeCursorSilently(cursor)
            releaseClientSilently(client)
        }
        return defValue
    }

    override fun getAll(): MutableMap<String, *> {
        val map = HashMap<String, Any?>()
        var client: ContentProviderClient? = null
        var cursor: Cursor? = null
        try {
            val uri = buildUri(ProcessContract.QUERY_GET_ALL, null)
            val contentResolver = mContext.contentResolver
            val clientAndProvider = getLocalContentProvider(contentResolver, uri)
            client = clientAndProvider.first
            val localProvider = clientAndProvider.second
            cursor = if (localProvider != null) localProvider.query(uri, null, null, null, null
            ) else contentResolver.query(uri, null, null, null, null)
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    val json = JSONObject(cursor.getString(0))
                    val it = json.keys()
                    while (it.hasNext()) {
                        val key = it.next()
                        if (json.isNull(key)) {
                            map[key] = null
                        } else {
                            val array = json.getJSONArray(key)
                            when (array.getInt(0)) {
                                TYPE_STRING -> map[key] = array.getString(1)
                                TYPE_STRING_SET -> map[key] = ProcessProvider.jsonArrayToStringSet(array.getJSONArray(1))
                                TYPE_INT -> map[key] = array.getInt(1)
                                TYPE_LONG -> map[key] = array.getLong(1)
                                TYPE_FLOAT -> map[key] = array.getDouble(1).toFloat()
                                TYPE_BOOLEAN -> map[key] = array.getBoolean(1)
                            }
                        }
                    }
                }
            }
        } catch (t: Throwable) {
            Log.e(TAG, "", t)
        } finally {
            closeCursorSilently(cursor)
            releaseClientSilently(client)
        }
        return map
    }

    override fun edit(): SharedPreferences.Editor {
        return TreasureEditor()
    }

    override fun getLong(key: String?, defValue: Long): Long {
        var client: ContentProviderClient? = null
        var cursor: Cursor? = null
        try {
            val uri = buildUri(ProcessContract.QUERY_GET, null)
            val contentResolver = mContext.contentResolver
            val clientAndProvider = getLocalContentProvider(contentResolver, uri)
            client = clientAndProvider.first
            val localProvider = clientAndProvider.second
            cursor = if (localProvider != null) localProvider.query(uri, arrayOf(key!!), defValue.toString(), null, TYPE_LONG.toString()
            ) else contentResolver.query(uri, arrayOf(key!!), defValue.toString(), null, TYPE_LONG.toString())
            if (cursor != null && cursor.moveToNext()) {
                return cursor.getLong(0)
            }
        } catch (t: Throwable) {
            Log.e(TAG, "", t)
        } finally {
            closeCursorSilently(cursor)
            releaseClientSilently(client)
        }
        return defValue
    }

    override fun getFloat(key: String?, defValue: Float): Float {
        var client: ContentProviderClient? = null
        var cursor: Cursor? = null
        try {
            val uri = buildUri(ProcessContract.QUERY_GET, null)
            val contentResolver = mContext.contentResolver
            val clientAndProvider = getLocalContentProvider(contentResolver, uri)
            client = clientAndProvider.first
            val localProvider = clientAndProvider.second
            cursor = if (localProvider != null) localProvider.query(uri, arrayOf(key!!), defValue.toString(), null, TYPE_FLOAT.toString()
            ) else contentResolver.query(uri, arrayOf(key!!), defValue.toString(), null, TYPE_FLOAT.toString())
            if (cursor != null && cursor.moveToNext()) {
                return cursor.getFloat(0)
            }
        } catch (t: Throwable) {
            Log.e(TAG, "", t)
        } finally {
            closeCursorSilently(cursor)
            releaseClientSilently(client)
        }
        return defValue
    }

    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String> {
        var client: ContentProviderClient? = null
        var cursor: Cursor? = null
        try {
            val uri = buildUri(ProcessContract.QUERY_GET, null)
            val contentResolver = mContext.contentResolver
            val clientAndProvider = getLocalContentProvider(contentResolver, uri)
            client = clientAndProvider.first
            val localProvider = clientAndProvider.second
            cursor = if (localProvider != null) localProvider.query(uri, arrayOf(key!!), null, null, TYPE_STRING_SET.toString()
            ) else contentResolver.query(uri, arrayOf(key!!), null, null, TYPE_STRING_SET.toString())
            if (cursor != null && cursor.moveToNext()) {
                return ProcessProvider.jsonArrayToStringSet(JSONArray(cursor.getString(0)))
            }
        } catch (t: Throwable) {
            Log.e(TAG, "", t)
        } finally {
            closeCursorSilently(cursor)
            releaseClientSilently(client)
        }
        return defValues!!
    }

    override fun getString(key: String?, defValue: String?): String? {
        var client: ContentProviderClient? = null
        var cursor: Cursor? = null
        try {
            val uri = buildUri(ProcessContract.QUERY_GET, null)
            val contentResolver = mContext.contentResolver
            val clientAndProvider = getLocalContentProvider(contentResolver, uri)
            client = clientAndProvider.first
            val localProvider = clientAndProvider.second
            cursor = if (localProvider != null) localProvider.query(uri, arrayOf(key!!), null, null, TYPE_STRING.toString()
            ) else contentResolver.query(uri, arrayOf(key!!), null, null, TYPE_STRING.toString())
            if (cursor != null && cursor.moveToNext()) {
                return cursor.getString(0)
            }
        } catch (t: Throwable) {
            Log.e(TAG, "", t)
        } finally {
            closeCursorSilently(cursor)
            releaseClientSilently(client)
        }
        return defValue
    }


    internal inner class TreasureEditor : SharedPreferences.Editor {
        private val mModified: MutableMap<String, Any?> = HashMap()
        private var mClear = false
        private var syncAction: String? = null

        fun forSyncAction(action: String): TreasureEditor {
            synchronized(this) {
                syncAction = action
                return this
            }
        }

        override fun putString(key: String, value: String?): SharedPreferences.Editor {
            synchronized(this) {
                mModified[key] = value
                return this
            }
        }

        override fun putStringSet(key: String, values: Set<String>?): SharedPreferences.Editor {
            synchronized(this) {
                mModified[key] = values?.let { HashSet(it) }
                return this
            }
        }

        override fun putInt(key: String, value: Int): SharedPreferences.Editor {
            synchronized(this) {
                mModified[key] = value
                return this
            }
        }

        override fun putLong(key: String, value: Long): SharedPreferences.Editor {
            synchronized(this) {
                mModified[key] = value
                return this
            }
        }

        override fun putFloat(key: String, value: Float): SharedPreferences.Editor {
            synchronized(this) {
                mModified[key] = value
                return this
            }
        }

        override fun putBoolean(key: String, value: Boolean): SharedPreferences.Editor {
            synchronized(this) {
                mModified[key] = value
                return this
            }
        }

        override fun remove(key: String): SharedPreferences.Editor {
            synchronized(this) {
                mModified[key] = null
                return this
            }
        }

        override fun clear(): SharedPreferences.Editor {
            synchronized(this) {
                mClear = true
                return this
            }
        }

        override fun commit(): Boolean {
            update(true)
            return true
        }

        override fun apply() {
            update(false)
        }

        private fun update(immediately: Boolean) {
            synchronized(this) {
                val contentValues = ContentValues()
                val stringSetKeyList = ArrayList<String>()
                val stringSetValueArray = JSONArray()
                for ((key, value) in mModified) {
                    when (value) {
                        null -> {
                            contentValues.putNull(key)
                        }
                        is String -> {
                            contentValues.put(key, value as String?)
                        }
                        is HashSet<*> -> {
                            stringSetKeyList.add(key)
                            stringSetValueArray.put(ProcessProvider.stringSetToJSONArray(value as Set<String>))
                        }
                        is Int -> {
                            contentValues.put(key, value as Int?)
                        }
                        is Long -> {
                            contentValues.put(key, value as Long?)
                        }
                        is Float -> {
                            contentValues.put(key, value as Float?)
                        }
                        is Boolean -> {
                            contentValues.put(key, value as Boolean?)
                        }
                    }
                }
                val params = HashMap<String, String>()
                params[ProcessContract.PARAM_CLEAR] = mClear.toString()
                params[ProcessContract.PARAM_IMMEDIATELY] = immediately.toString()
                syncAction?.let {
                    params[ProcessContract.PARAM_SYNC_ACTION] = it
                }
                val uri: Uri = buildUri(ProcessContract.UPDATE, params)
                updateImpl(uri, contentValues, stringSetValueArray.toString(), if (stringSetKeyList.size > 0) stringSetKeyList.toTypedArray() else null, 0)
            }
        }
    }

    private fun updateImpl(uri: Uri, contentValues: ContentValues, stringSetValue: String, stringSetKey: Array<String>?, rCount: Int) {
        var retryCount = rCount
        var client: ContentProviderClient? = null
        try {
            val contentResolver = mContext.contentResolver
            val clientAndProvider = getLocalContentProvider(contentResolver, uri)
            client = clientAndProvider.first
            val localProvider = clientAndProvider.second
            if (localProvider != null) {
                localProvider.update(uri, contentValues, stringSetValue, stringSetKey)
            } else {
                contentResolver.update(uri, contentValues, stringSetValue, stringSetKey)
            }
        } catch (e: IllegalStateException) {
            Log.e(TAG, "", e)
        } catch (e: SecurityException) {
            Log.e(TAG, "", e)
        } catch (e: java.lang.IllegalArgumentException) {
            Log.e(TAG, "", e)
            retryCount++
            if (retryCount <= 100) {
                val message = e.message
                if (!TextUtils.isEmpty(message) && message!!.toLowerCase().contains("content://${ProcessContract.getAuthority()}")) {
                    mHandler.sendMessage(mHandler.obtainMessage(WHAT_UPDATE, retryCount, 0, arrayOf(uri, contentValues, stringSetValue, stringSetKey)))
                }
            }
        } finally {
            releaseClientSilently(client)
        }
    }

    internal inner class RetryHandler constructor(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                WHAT_UPDATE -> {
                    val info = msg.obj as Array<*>
                    updateImpl(info[0] as Uri, info[1] as ContentValues, info[2] as String, info[3] as? Array<String>?, msg.arg1)
                }
                WHAT_INSERT -> {
                    val info = msg.obj as Array<*>
                    insert(info[0] as Uri, info[1] as ContentValues, msg.arg1)
                }
                WHAT_DELETE -> {
                    val info = msg.obj as Array<*>
                    delete(info[0] as Uri, info[1] as? Array<String>?, msg.arg1)
                }
            }
        }
    }
}