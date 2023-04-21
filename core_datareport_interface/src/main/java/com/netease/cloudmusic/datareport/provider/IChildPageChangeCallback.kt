package com.netease.cloudmusic.datareport.provider

/**
 *
 */
interface IChildPageChangeCallback {
    fun onChildPageOidChange(childPageSpm: String?, childPageOid: String?, isProcessForeground: Boolean)
}