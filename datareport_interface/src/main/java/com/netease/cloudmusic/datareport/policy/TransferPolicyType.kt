package com.netease.cloudmusic.datareport.policy

import androidx.annotation.IntDef

/**
 * 事件转移的类型
 */
class TransferPolicyType {
    companion object{
        const val TYPE_TARGET_VIEW = 1 //把事件转移到确定的view上面
        const val TYPE_FIND_UP_OID = 2 //向上查找oid，如果没有oid向上查找最近的一个点，然后转移
        const val TYPE_FIND_DOWN_OID = 3 //向下查找oid，如果没有oid向下查找最近的一个点，然后转移
    }
}

@IntDef(
    TransferPolicyType.TYPE_TARGET_VIEW,
    TransferPolicyType.TYPE_FIND_UP_OID,
    TransferPolicyType.TYPE_FIND_DOWN_OID
)
@kotlin.annotation.Retention(AnnotationRetention.SOURCE)
annotation class TransferType {
}