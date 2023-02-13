package com.netease.cloudmusic.datareport.utils.timer;

public interface IDurationTimer {
    /**
     * 开始计时
     */
    void startTimer();

    /**
     * 结束计时
     */
    void stopTimer();

    /**
     * 获取停留时长。一次性消费数据，获取之后会被立马重置。
     *
     * @return 停留时长
     */
    long getDuration();


    void reset();
}
