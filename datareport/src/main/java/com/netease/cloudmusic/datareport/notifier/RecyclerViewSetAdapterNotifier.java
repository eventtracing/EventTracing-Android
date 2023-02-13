package com.netease.cloudmusic.datareport.notifier;

import androidx.recyclerview.widget.RecyclerView;

import com.netease.cloudmusic.datareport.data.ReusablePool;

/**
 * RecyclerView设置adapter事件的事件通知者
 */
public class RecyclerViewSetAdapterNotifier implements IEventNotifier {
    private RecyclerView mView;

    public void init(RecyclerView view) {
        mView = view;
    }

    @Override
    public int getReuseType() {
        return ReusablePool.TYPE_RECYCLER_VIEW_SET_ADAPTER;
    }

    @Override
    public void notifyEvent(IEventListener listener) {
        listener.onSetRecyclerViewAdapter(mView);
    }

    @Override
    public void reset() {
        mView = null;
    }
}
