package com.netease.cloudmusic.datareport.vtree.traverse;

import android.view.View;

/**
 * 定义一个视图被遍历的监听，只要视图被遍历到了，就会通知出去
 */
public interface OnViewTraverseListener {

    void onViewVisited(View view);
}
