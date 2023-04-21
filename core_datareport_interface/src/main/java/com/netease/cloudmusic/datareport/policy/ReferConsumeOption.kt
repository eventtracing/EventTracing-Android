package com.netease.cloudmusic.datareport.policy

/**
 * refer选项的枚举
 */
enum class ReferConsumeOption {

    /**
     * 不消费任何refer
     */
    CONSUME_NONE,

    /**
     * 只消费子页面产生的refer
     */
    CONSUME_SUB_PAGE_REFER,

    /**
     * 消费事件产生的refer
     */
    CONSUME_EVENT_REFER,

    /**
     * 消费所有的refer
     */
    CONSUME_ALL
}