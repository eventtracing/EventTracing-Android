package com.netease.cloudmusic.datareport.report.exception

interface ExceptionObserver {
    fun onException(info: IErrorInfo)
}