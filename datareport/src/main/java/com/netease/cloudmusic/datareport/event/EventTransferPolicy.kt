package com.netease.cloudmusic.datareport.event

import android.view.View
import com.netease.cloudmusic.datareport.policy.TransferPolicyType.Companion.TYPE_FIND_DOWN_OID
import com.netease.cloudmusic.datareport.policy.TransferPolicyType.Companion.TYPE_FIND_UP_OID
import com.netease.cloudmusic.datareport.policy.TransferPolicyType.Companion.TYPE_TARGET_VIEW
import com.netease.cloudmusic.datareport.policy.TransferType
import com.netease.cloudmusic.datareport.vtree.getChildByOid
import com.netease.cloudmusic.datareport.vtree.getOidChild
import com.netease.cloudmusic.datareport.vtree.getOidParents
import com.netease.cloudmusic.datareport.vtree.getParentByOid
import java.lang.ref.WeakReference

/**
 * 事件对于view的转移策略
 */
class EventTransferPolicy(@TransferType type: Int, oid: String?, targetView: View?) {

    @TransferType
    private val mType = type

    private val mTargetView: WeakReference<View>? = if(targetView != null) WeakReference(targetView) else null

    private val mOid: String? = oid

    @TransferType
    fun getType(): Int {
        return mType
    }

    fun getOid(): String? {
        return mOid
    }

    fun getTargetView(view: View): View? {
        when (mType) {
            TYPE_FIND_DOWN_OID -> {
                val oid = mOid
                return if (oid != null) {
                    getChildByOid(view, oid)
                } else {
                    getOidChild(view)
                }
            }
            TYPE_FIND_UP_OID -> {
                val oid = mOid
                return if (oid != null) {
                    getParentByOid(view, oid)
                } else {
                    getOidParents(view)
                }
            }
            TYPE_TARGET_VIEW -> {
                return mTargetView?.get()
            }
        }
        return null
    }
}