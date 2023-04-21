package com.netease.cloudmusic.datareport.notifier;

import androidx.recyclerview.widget.RecyclerView;

import com.netease.cloudmusic.datareport.data.ReusablePool;

public class RecyclerViewScrollPositionNotifier implements IEventNotifier {

    private RecyclerView mView;

    public void init(RecyclerView view) {
        mView = view;
    }

    @Override
    public int getReuseType() {
        return ReusablePool.TYPE_RECYCLER_VIEW_SCROLL_POSITION;
    }

    @Override
    public void notifyEvent(IEventListener listener) {
        listener.onRecyclerViewScrollPosition(mView);
    }

    @Override
    public void reset() {
        mView = null;
    }
}
