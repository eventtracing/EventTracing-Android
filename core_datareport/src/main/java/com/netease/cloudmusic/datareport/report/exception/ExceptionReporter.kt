package com.netease.cloudmusic.datareport.report.exception

/**
 * 错误日志上报，统一收口
 * 不再抛出异常
 */
object ExceptionReporter {

    private val observerList = mutableListOf<ExceptionObserver>()

    fun reportError(info: IErrorInfo) {
        observerList.forEach {
            it.onException(info)
        }
    }

    fun addObserver(observer: ExceptionObserver) {
        observerList.add(observer)
    }

    fun removeObserver(observer: ExceptionObserver) {
        observerList.add(observer)
    }
}