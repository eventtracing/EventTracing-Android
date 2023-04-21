package com.netease.cloudmusic.datareport.report

/**
 * 事件的类型code
 */
const val INNER_EVENT_CODE = "_eventcode"

/**
 * 每次冷启动生成的唯一ID
 */
const val SESSION_ID = "_sessid"

/**
 * 超级位置模型，
 */
const val SPM_KEY = "_spm"
/**
 * 超级资源模型，
 */
const val SCM_KEY = "_scm"

/**
 * 无node关联的自定义事件的refer相关的spm
 */
const val REFER_SPM_KEY = "_refer_spm"

/**
 * 无node关联的自定义事件的refer相关的scm
 */
const val REFER_SCM_KEY = "_refer_scm"

/**
 * 曝光页面或者元素的唯一id
 */
const val OID_KEY = "_oid"

/**
 * 页面深度
 */
const val PAGE_STEP_KEY = "_pgstep"

/**
 * 互动深度
 */
const val ACTIOIN_SEQ_KEY = "_actseq"

/**
 * 页面曝光来源
 */
const val PAGE_REFER_KEY = "_pgrefer"

/**
 * 页面曝光来源
 */
const val PS_REFER_KEY = "_psrefer"

/**
 * 上层所有的元素信息
 */
const val ELEMENT_LIST = "_elist"

/**
 * 上层所有的页面信息
 */
const val PAGE_LIST = "_plist"

/**
 * 上次启动app的sessionId
 */
const val SIDE_REFER = "_sidrefer"

/**
 * 消费起始公参
 */
const val HS_REFER = "_hsrefer"

const val GLOBAL_DB_REFER = "g_dprefer"

/**
 * 相对于父亲的位置信息
 */
const val POS_KEY = "_pos"

/**
 * 需要跳转的页面的oid
 */
const val TO_OID = "_toid"

/**
 * 曝光时间
 */
const val EXPOSURE_DURATION = "_duration"

/**
 * 停留时长
 * 从App曝光到App反曝光经过的时间，单位毫秒
 */
const val REPORT_KEY_LVTM = "_duration"

/**
 * 停留时长(支持多进程）
 * 从App曝光到App反曝光经过的时间，单位毫秒
 */
const val REPORT_KEY_LVTM_HEART = "_heart_duration"

/**
 * 上报的时间
 */
const val UPLOAD_TIME = "logtime"

/**
 * 生成的 最终数据finaldata中是否使用了加密，如果使用了的话，需要加一下下面的标记位来标示
 */
const val FLAG_ER = "_scm_er"

/**
 * temp变量的KEY，临时把一个node存下来，在主线层转换
 */
const val CURRENT_NODE_TEMP_KEY = "current_node_temp_key"

const val CURRENT_EVENT_PARAMS = "current_event_params"

/**
 * 自定义全局上报的时候，没有view，但是需要有一个 _refer_type
 */
const val REFER_TYPE = "_refer_type"

/**
 * 上报的时候携带的上下文信息
 */
const val REPORT_CONTEXT = "_rpc_source"

/**
 * 当前的构建树的可见比例
 */
const val EXPOSURE_RATIO = "_ratio"

fun isContainsInnerKeysAndNullCheck(map: MutableMap<String?, Any?>?): String? {
    map?.let {
        val iterator = it.iterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            when {
                item.key == null -> {
                    iterator.remove()
                }
                item.value == null -> {
                    item.setValue("")
                }
                else -> {
                    isContainsInnerKeys(item.key)?.let { key -> return key }
                }
            }
        }
    }
    return null
}

fun isContainsInnerKeys(key: String?): String? {
    return if (key == OID_KEY || key == POS_KEY || key == PAGE_REFER_KEY || key == PS_REFER_KEY || key == PAGE_STEP_KEY) {
        key
    } else {
        null
    }
}
