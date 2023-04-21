package com.netease.cloudmusic.datareport.notifier;

import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.netease.cloudmusic.datareport.data.ReusablePool;

/**
 * View复用事件的事件通知者
 * 比如RecyclerView或者listView的item复用
 */
public class ViewReuseNotifier implements IEventNotifier {
    private ViewGroup mParentView;
    private View mView;
    private long mItemId = RecyclerView.NO_ID;

    public void init(ViewGroup parentView, View view, long itemId) {
        mParentView = parentView;
        mView = view;
        mItemId = itemId;
    }

    @Override
    public int getReuseType() {
        return ReusablePool.TYPE_VIEW_REUSE;
    }

    @Override
    public void notifyEvent(IEventListener listener) {
        listener.onViewReused(mParentView, mView, mItemId);
    }

    @Override
    public void reset() {
        mParentView = null;
        mView = null;
        mItemId = -1;
    }
}
