package com.netease.cloudmusic.datareport.vtree.traverse;

import android.graphics.Rect;

/**
 * 父节点信息类
 */
public class AncestorInfo {

    /**
     * 父节点本身的可见区域
     */
    public Rect visibleRect = new Rect();

    /**
     * 父节点实际的区域，不可见的部分也包含在内，用以子View的计算
     */
    public Rect actualRect = new Rect();

    /**
     * 子View的限制区域，注意和父节点本身的可见区域可以不同
     * visibleRect就是父节点本身所在屏幕上的位置
     * restrictedRect则是父节点能提供给子View摆放的位置，可以超出父节点本身的rect
     */
    public Rect restrictedRect = new Rect();

    /**
     * 父视图的scrollX 结合父视图的rect计算自己的left
     */
    public int scrollX = 0;

    /**
     * 父视图的scrollY 结合父视图的rect计算自己的top
     */
    public int scrollY = 0;

    /**
     * 父视图的clipChildren
     */
    public boolean clipChildren = true;

    public AncestorInfo getClone() {
        AncestorInfo cloneInfo = new AncestorInfo();
        cloneInfo.visibleRect.set(visibleRect);
        cloneInfo.actualRect.set(actualRect);
        cloneInfo.restrictedRect.set(restrictedRect);
        cloneInfo.scrollX = scrollX;
        cloneInfo.scrollY = scrollY;
        cloneInfo.clipChildren = clipChildren;
        return cloneInfo;
    }
}
