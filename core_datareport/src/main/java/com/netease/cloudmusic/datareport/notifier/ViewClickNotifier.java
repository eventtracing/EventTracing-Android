package com.netease.cloudmusic.datareport.notifier;

import android.view.View;

import com.netease.cloudmusic.datareport.data.ReusablePool;

/**
 * 点击事件的事件通知者
 */
public class ViewClickNotifier implements IEventNotifier {
    private View mView;

    public void init(View view) {
        mView = view;
    }

    @Override
    public int getReuseType() {
        return ReusablePool.TYPE_VIEW_CLICK;
    }

    @Override
    public void notifyEvent(IEventListener listener) {
        listener.onViewClick(mView);
    }

    @Override
    public void reset() {
        mView = null;
    }
}
