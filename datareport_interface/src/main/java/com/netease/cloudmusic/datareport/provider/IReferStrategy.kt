package com.netease.cloudmusic.datareport.provider

interface IReferStrategy {

    /**
     * 自定义构建的scm
     * 注意：修改了方法签名， 返回的数据是一个 Pair，first还是 scm，second 是一个boolean 表示当前的数据是否有数据做了encode操作的
     */
    fun buildScm(params: Map<String, Any>?): Pair<String, Boolean>

    /**
     * 返回多级refer的层级
     */
    fun mutableReferLength(): Int

}