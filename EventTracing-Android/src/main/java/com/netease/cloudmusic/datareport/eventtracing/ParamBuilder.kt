package com.netease.cloudmusic.datareport.eventtracing

import com.netease.cloudmusic.datareport.operator.DataReport
import com.netease.cloudmusic.datareport.provider.IViewDynamicParamsProvider

/**
 * 自定参数构架类
 */
class ParamBuilder internal constructor(viewNode: Any?) {

    private val node: Any? = viewNode

    companion object {
        const val DATA_ID = "s_cid"
        const val DATA_TYPE = "s_ctype"
        const val DATA_ALG = "s_calg"
        const val DATA_TRACE_ID = "s_ctraceid"
        const val DATA_ALG_INFO = "s_calginfo"
        const val DATA_ID_ER = "s_cid"
        const val DATA_LOG_INFO = "s_cloginfo"
        const val DATA_TRP = "s_ctrp" // transparent 透传

        //============= 组合参数，用来和 DATA_ID 与 DATA_ALG 拼接在一起
        const val DATA_TYPE_USER = "user"
        const val DATA_TYPE_PLAYLIST = "playlist"
        const val DATA_TYPE_VOICE = "voice"
        const val DATA_TYPE_VOICE_LIST = "voicelist"
        const val DATA_TYPE_MV = "mv"
        const val DATA_TYPE_SUBJECT = "subject"
        const val DATA_TYPE_EVENT = "event"
        const val DATA_TYPE_ALBUM = "album"
        const val DATA_TYPE_BANNER = "banner"
        const val DATA_TYPE_SONG = "song"
        const val DATA_TYPE_ACTIVITY = "activity"
        const val DATA_TYPE_SHOW = "show"
        const val DATA_TYPE_ARTIST = "artist"
        const val DATA_TYPE_MUSIC_FESTIVAL = "music_festival"
        const val DATA_TYPE_SHORT_VIDEO = "short_video"
        const val DATA_TYPE_AD = "ad"
        const val DATA_TYPE_SONG_ORDER = "song_order"
        const val DATA_TYPE_MLOG = "mlog"
        const val DATA_TYPE_MLOG_TOPIC = "mlog_topic"
        const val DATA_TYPE_CIRCLE_DEMO = "circle_demo"
        const val DATA_TYPE_CIRCLE_TREE_HOLE = "circle_tree_hole"

        const val DATA_TOID = "s_toid"
        const val DATA_STYLE = "s_style"
        const val DATA_POSITION = "s_position"
        const val DATA_URL = "s_url"
        const val DATA_TARGET = "s_target"
        const val DATA_TITLE = "s_title"
        const val DATA_ACTION = "s_action"
        const val DATA_ACTION_TYPE = "s_actiontype"
        const val DATA_LIVE_ID = "s_liveid"
        const val DATA_STATUS = "s_status"
        const val DATA_MODULE = "s_module"

        const val DATA_DEVICE = "s_device"
        const val DATA_RESOLUTION = "s_resolution"
        const val DATA_CARRIER = "s_carrier"
        const val DATA_NETWORK = "s_network"
        const val DATA_MSG = "s_msg"
        const val DATA_SCREEN_STATUS = "s_screen_status"
        const val DATA_FLOWFREE = "s_flowfree"
        const val DATA_CODE = "s_code"
        const val DATA_ISOWNER = "s_isowner"
        const val DATA_SUBTITLE = "s_subtitle"
        const val DATA_SCENE = "s_scene"
        const val DATA_TARGET_ID = "s_targetid"
        const val DATA_LABEL = "s_label"
        const val DATA_BUSINESS = "s_business"
        const val DATA_CALLUSERID = "s_calluserid"
        const val DATA_COLUMN = "s_column"
        const val DATA_NAME = "s_name"
        const val DATA_ANCHORID = "s_anchorid"
        const val DATA_TIME = "s_time"
        const val DATA_LIVE_URL = "s_liveurl"
        const val DATA_LIVE_TYPE = "s_live_type"
        const val DATA_RECOMMEND_LIVEID = "s_recommend_liveid"
        const val DATA_LIVE_ROOMNO = "s_liveRoomNo"

        fun params(viewNode: Any?): ParamBuilder {
            return params(viewNode, true)
        }

        fun params(viewNode: Any?, clear: Boolean): ParamBuilder {
            val bean = ParamBuilder(viewNode)
            if (clear) {
                bean.clear()
            }
            return bean
        }
    }

    fun putBIId(value: Any?): ParamBuilder {
        DataReport.getInstance().setCustomParams(node, DATA_ID, value ?: "")
        return this
    }

    /**
     * transparent 透传
     */
    fun putBITransparent(value: Any?): ParamBuilder {
        DataReport.getInstance().setCustomParams(node, DATA_TRP, value ?: "")
        return this
    }

    /**
     * 需要加密的id的设置
     */
    fun putBIIdEncryption(value: Any?): ParamBuilder {
        DataReport.getInstance().setCustomParams(node, DATA_ID_ER, value ?: "")
        return this
    }

    fun putBIToId(value: Any?): ParamBuilder {
        DataReport.getInstance().setCustomParams(node, DATA_TOID, value ?: "")
        return this
    }

    fun putBIAlg(value: Any?): ParamBuilder {
        DataReport.getInstance().setCustomParams(node, DATA_ALG, value ?: "")
        return this
    }

    fun putBITraceId(value: Any?): ParamBuilder {
        DataReport.getInstance().setCustomParams(node, DATA_TRACE_ID, value ?: "")
        return this
    }

    fun putBIAlgInfo(value: Any?): ParamBuilder {
        DataReport.getInstance().setCustomParams(node, DATA_ALG_INFO, value ?: "")
        return this
    }

    fun putBILogInfo(value: Any?): ParamBuilder {
        DataReport.getInstance().setCustomParams(node, DATA_LOG_INFO, value ?: "")
        return this
    }


    fun putBIStyle(value: Any?): ParamBuilder {
        DataReport.getInstance().setCustomParams(node, DATA_STYLE, value ?: "")
        return this
    }

    fun putBIUrl(value: Any?): ParamBuilder {
        DataReport.getInstance().setCustomParams(node, DATA_URL, value ?: "")
        return this
    }

    fun putBIType(value: Any?): ParamBuilder {
        DataReport.getInstance().setCustomParams(node, DATA_TYPE, value ?: "")
        return this
    }

    fun putBITarget(value: Any?): ParamBuilder {
        DataReport.getInstance().setCustomParams(node, DATA_TARGET, value ?: "")
        return this
    }

    fun putBITitle(value: Any?): ParamBuilder {
        DataReport.getInstance().setCustomParams(node, DATA_TITLE, value ?: "")
        return this
    }

    fun putBIAction(value: Any?): ParamBuilder {
        DataReport.getInstance().setCustomParams(node, DATA_ACTION, value ?: "")
        return this
    }

    fun putBIActionType(value: Any?): ParamBuilder {
        DataReport.getInstance().setCustomParams(node, DATA_ACTION_TYPE, value ?: "")
        return this
    }

    fun putBILiveId(value: Any?): ParamBuilder {
        DataReport.getInstance().setCustomParams(node, DATA_LIVE_ID, value ?: "")
        return this
    }

    fun putBIPosition(value: Any?): ParamBuilder {
        DataReport.getInstance().setCustomParams(node, DATA_POSITION, value ?: "")
        if (value is Int) {
            DataReport.getInstance().setPosition(node, value)
        } else if (value is String) {
            try {
                DataReport.getInstance().setPosition(node, value.toInt())
            } catch (e: NumberFormatException) {
                e.printStackTrace()
            }
        }
        return this
    }

    fun putBIStatus(value: Any?): ParamBuilder {
        DataReport.getInstance().setCustomParams(node, DATA_STATUS, value ?: "")
        return this
    }

    fun putBIModule(value: Any?): ParamBuilder {
        DataReport.getInstance().setCustomParams(node, DATA_MODULE, value ?: "")
        return this
    }

    fun putBIDevice(value: Any?): ParamBuilder {
        DataReport.getInstance().setCustomParams(node, DATA_DEVICE, value ?: "")
        return this
    }

    fun putBIResolution(value: Any?): ParamBuilder {
        DataReport.getInstance().setCustomParams(node, DATA_RESOLUTION, value ?: "")
        return this
    }

    fun putBICarrier(value: Any?): ParamBuilder {
        DataReport.getInstance().setCustomParams(node, DATA_CARRIER, value ?: "")
        return this
    }

    fun putBINetwork(value: Any?): ParamBuilder {
        DataReport.getInstance().setCustomParams(node, DATA_NETWORK, value ?: "")
        return this
    }

    fun putBIMsg(value: Any?): ParamBuilder {
        DataReport.getInstance().setCustomParams(node, DATA_MSG, value ?: "")
        return this
    }

    fun putBIScreenStatus(value: Any?): ParamBuilder {
        DataReport.getInstance().setCustomParams(node, DATA_SCREEN_STATUS, value ?: "")
        return this
    }

    fun putBIFlowFree(value: Any?): ParamBuilder {
        DataReport.getInstance().setCustomParams(node, DATA_FLOWFREE, value ?: "")
        return this
    }

    fun putBICode(value: Any?): ParamBuilder {
        DataReport.getInstance().setCustomParams(node, DATA_CODE, value ?: "")
        return this
    }

    fun putBIIsOwner(value: Any?): ParamBuilder {
        DataReport.getInstance().setCustomParams(node, DATA_ISOWNER, value ?: "")
        return this
    }

    fun putBISubtitle(value: Any?): ParamBuilder {
        DataReport.getInstance().setCustomParams(node, DATA_SUBTITLE, value ?: "")
        return this
    }

    fun putBIScene(value: Any?): ParamBuilder {
        DataReport.getInstance().setCustomParams(node, DATA_SCENE, value ?: "")
        return this
    }

    fun putBITargetId(value: Any?): ParamBuilder {
        DataReport.getInstance().setCustomParams(node, DATA_TARGET_ID, value ?: "")
        return this
    }

    fun putBILabel(value: Any?): ParamBuilder {
        DataReport.getInstance().setCustomParams(node, DATA_LABEL, value ?: "")
        return this
    }

    fun putBIBusiness(value: Any?): ParamBuilder {
        DataReport.getInstance().setCustomParams(node, DATA_BUSINESS, value ?: "")
        return this
    }

    fun putBICallUserId(value: Any?): ParamBuilder {
        DataReport.getInstance().setCustomParams(node, DATA_CALLUSERID, value ?: "")
        return this
    }

    fun putBIColumn(value: Any?): ParamBuilder {
        DataReport.getInstance().setCustomParams(node, DATA_COLUMN, value ?: "")
        return this
    }

    fun putBIName(value: Any?): ParamBuilder {
        DataReport.getInstance().setCustomParams(node, DATA_NAME, value ?: "")
        return this
    }

    fun putBIAnchorId(value: Any?): ParamBuilder {
        DataReport.getInstance().setCustomParams(node, DATA_ANCHORID, value ?: "")
        return this
    }

    fun putBITime(value: Any?): ParamBuilder {
        DataReport.getInstance().setCustomParams(node, DATA_TIME, value ?: "")
        return this
    }

    fun putBILiveUrl(value: Any?): ParamBuilder {
        DataReport.getInstance().setCustomParams(node, DATA_LIVE_URL, value ?: "")
        return this
    }

    fun putBILiveType(value: Any?): ParamBuilder {
        DataReport.getInstance().setCustomParams(node, DATA_LIVE_TYPE, value ?: "")
        return this
    }

    fun putBIRecommendLiveId(value: Any?): ParamBuilder {
        DataReport.getInstance().setCustomParams(node, DATA_RECOMMEND_LIVEID, value ?: "")
        return this
    }

    fun putBILiveRoomNo(value: Any?): ParamBuilder {
        DataReport.getInstance().setCustomParams(node, DATA_LIVE_ROOMNO, value ?: "")
        return this
    }

    fun putBICustomParam(key: String, value: Any?): ParamBuilder {
        DataReport.getInstance().setCustomParams(node, key, value ?: "")
        return this
    }

    fun putBICustomParams(map: Map<String, Any?>): ParamBuilder {
        DataReport.getInstance().setCustomParams(node, map)
        return this
    }

    fun putBICustomParams(block: ((map: MutableMap<String, Any?>) -> Unit)): ParamBuilder {
        val map = mutableMapOf<String, Any?>()
        block.invoke(map)
        putBICustomParams(map)
        return this
    }

    /**
     * 设置动态的参数
     * 注意：内部只会对改回调的对象进行弱引用
     * @param provider 动态参数的回调
     */
    fun putDynamicParams(provider: IViewDynamicParamsProvider): ParamBuilder {
        DataReport.getInstance().setDynamicParams(node, provider)
        return this
    }

    /**
     * 给对应的事件参数设置对象级别的数据，通过动态钩子的形式添加
     * @param eventIds 对应事件的id的列表
     * @param provider 回调的钩子
     */
    fun addEventParamsCallback(eventIds: Array<String>, provider: IViewDynamicParamsProvider): ParamBuilder {
        DataReport.getInstance().addEventParamsCallback(node, eventIds, provider)
        return this
    }

    /**
     * 给点击事件参数设置对象级别的数据，通过动态钩子的形式添加
     * @param provider 回调的钩子
     */
    fun addClickParamsCallback(provider: IViewDynamicParamsProvider): ParamBuilder {
        DataReport.getInstance().setClickParamsCallback(node, provider)
        return this
    }

    fun clear(): ParamBuilder {
        DataReport.getInstance().resetCustomParams(node)
        val position = DataReport.getInstance().getInnerPosition(node)
        if (position != null) {
            DataReport.getInstance().setCustomParams(node, DATA_POSITION, position)
        }
        return this
    }

    /**
     * 多场景的组合
     * @param id 该场景的id
     * @param alg 该场景的alg
     * @param type 场景类型，必须为非空。传入的值参考 DATA_TYPE_*
     */
    fun putTypeResource(id: String?, alg: String?, type: String): ParamBuilder {
        id?.let { DataReport.getInstance().setCustomParams(node, "${DATA_ID}_${type}", it) }
        alg?.let { DataReport.getInstance().setCustomParams(node, "${DATA_ALG}_${type}", it) }
        return this
    }
}