package com.netease.cloudmusic.datareport.vtree.traverse;

import android.graphics.RectF;

import com.netease.cloudmusic.datareport.utils.SafeList;

/**
 * 曝光检测中，使用到的中间数据
 */
public class DetectionData {

    public final static double NO_EXPOSURE = -1.0;

    /**
     * 辅助变量，用来记录临时的位置信息，避免反复创建
     */
    public final RectF helperRectF = new RectF();

    public final int[] location = new int[2];

    /**
     * 递归过程中，记录某个节点所有的父节点信息，直到根节点
     */
    public final SafeList<AncestorInfo> ancestorsInfo = new SafeList<AncestorInfo>(20) {
        @Override
        public AncestorInfo initValue() {
            return new AncestorInfo();
        }
    };

    /**
     * 递归过程中，记录某个节点是否曝光，主要用于后序遍历时的通知
     */
    public final SafeList<Double> exposureRate = new SafeList<Double>(20) {
        @Override
        public Double initValue() {
            return NO_EXPOSURE;
        }
    };
}
