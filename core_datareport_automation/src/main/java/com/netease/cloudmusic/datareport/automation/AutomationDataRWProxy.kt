package com.netease.cloudmusic.datareport.automation

import android.view.View
import com.netease.cloudmusic.datareport.data.DataRWProxy
import com.netease.cloudmusic.datareport.inner.InnerKey
import com.netease.cloudmusic.datareport.vtree.VTreeManager
import com.netease.cloudmusic.datareport.vtree.bean.VTreeNode
import com.netease.cloudmusic.datareport.vtree.getView

class AutomationDataRWProxy private constructor(private val targetView: View) {

    companion object {
        fun with(any: Any): AutomationDataRWProxy? {
            val view = getView(any) ?: return null
            return AutomationDataRWProxy(view)
        }

        fun getDataEntity(view: View, createIfNull: Boolean): AutomationEntity? {
            val obj = view.getTag(R.id.key_automation_id)
            if (obj is AutomationEntity) {
                return obj
            }
            if (createIfNull) {
                val entity = AutomationEntity()
                view.setTag(R.id.key_automation_id, entity)
                return entity
            }
            return null
        }
    }


    fun setOid(oid: String): AutomationDataRWProxy {
        getDataEntity(targetView, true)?.apply {
            this.oid = oid
            this.spm = null
        }
        return this
    }

    fun setPos(pos: Int): AutomationDataRWProxy {
        getDataEntity(targetView, true)?.apply {
            this.pos = pos
            this.spm = null
        }
        return this
    }

    fun getSpm(): String? {

        VTreeManager.getCurrentVTreeInfo()?.treeMap?.get(targetView)?.let {
            return getVTreeSpm(it)
        }

        val spmItem = getSpmItem(targetView) ?: return null
        val parent = targetView.parent
        if (parent is View) {
            getSpmInner(parent)
        } else {
            null
        }?.let {
            return "${spmItem}|${it}"
        }
        return spmItem
    }

    private fun getVTreeSpm(vTreeNode: VTreeNode): String {
        val spmBuilder = StringBuilder()
        var currentNode = vTreeNode
        while (currentNode.parentNode != null) {
            val oid = currentNode.getOid()
            val pos = currentNode.getPos()
            spmBuilder.append(oid)
            pos?.let {
                spmBuilder.append(":").append(pos)
            }
            spmBuilder.append("|")
            currentNode = currentNode.parentNode!!
        }
        return if (spmBuilder.isEmpty()) {
            ""
        } else {
            spmBuilder.substring(0, spmBuilder.length - 1)
        }
    }

    private fun getSpmInner(view: View): String? {
        val automationEntity = getDataEntity(view, false)
        automationEntity?.spm?.let {
            return it
        }

        VTreeManager.getCurrentVTreeInfo()?.treeMap?.get(view)?.let {
            return getVTreeSpm(it).apply {
                if(this != "") {
                    view.removeOnAttachStateChangeListener(AutomationAttachListener)
                    view.addOnAttachStateChangeListener(AutomationAttachListener)
                    getDataEntity(view, true)?.spm = this
                }
            }
        }

        val parent = view.parent
        val pSpm = if (parent is View) {
            getSpmInner(parent)
        } else {
            null
        }

        val spmItem = getSpmItem(view)
        val spm = if (spmItem != null && pSpm != null) {
            "${spmItem}|${pSpm}"
        } else spmItem ?: pSpm

        if (spm != null) {
            view.removeOnAttachStateChangeListener(AutomationAttachListener)
            view.addOnAttachStateChangeListener(AutomationAttachListener)
            getDataEntity(view, true)?.spm = spm
        }

        return spm
    }

    private fun getSpmItem(view: View): String? {

        return DataRWProxy.getDataEntity(view, false)?.let {
            val oid = it.pageId ?: it.elementId
            val pos = it.innerParams[InnerKey.VIEW_POSITION]
            if (oid != null && pos != null) {
                return "${oid}:${pos}"
            } else if (oid != null) {
                return oid
            } else {
                null
            }
        } ?: getDataEntity(view, false)?.let {
            val oid = it.oid
            val pos = it.pos
            if (oid != null && pos != null) {
                return "${oid}:${pos}"
            } else if (oid != null) {
                return oid
            } else {
                null
            }
        }
    }
}