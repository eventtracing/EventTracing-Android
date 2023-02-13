package com.netease.cloudmusic.datareport.scroller;

import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.appbar.AppBarLayout;
import com.netease.cloudmusic.datareport.R;
import com.netease.cloudmusic.datareport.event.ScrollEventObserver;
import com.netease.cloudmusic.datareport.vtree.traverse.OnViewTraverseListener;
import com.netease.cloudmusic.datareport.utils.Log;
import com.netease.cloudmusic.datareport.notifier.DefaultEventListener;
import com.netease.cloudmusic.datareport.inject.EventCollector;
import com.netease.cloudmusic.datareport.notifier.IEventListener;
import com.netease.cloudmusic.datareport.inner.DataReportInner;
import com.netease.cloudmusic.datareport.utils.UIUtils;
import com.netease.cloudmusic.datareport.vtree.traverse.VTreeTraverser;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * 负责辅助所有列表等可滑动视图的滚动监听
 */
abstract class ScrollStateObserver extends RecyclerView.OnScrollListener implements AbsListView.OnScrollListener, OnViewTraverseListener, View.OnAttachStateChangeListener, AppBarLayout.OnOffsetChangedListener {

    private static final String TAG = "ScrollStateObserver";

    private final WeakHashMap<ViewPager, ViewPager.OnPageChangeListener> mViewPagerListeners = new WeakHashMap<>();
    private final Set<View> mScrollingViews = Collections.newSetFromMap(new WeakHashMap<View, Boolean>());

    @SuppressWarnings("FieldCanBeLocal")
    private final IEventListener mEventListener = new ScrollEventListener();

    ScrollStateObserver() {
        EventCollector.getInstance().registerEventListener(mEventListener);
        VTreeTraverser.INSTANCE.setListener(this);
    }

    /**
     * 各种滑动视图滑动停止的回调
     */
    protected abstract void onIdle(View source);

    protected abstract void onScrollUpdate(View source);

    boolean isScrolling() {
        return mScrollingViews.size() > 0;
    }

    /**
     * 注入RecyclerView的滚动监听
     */
    public void inject(RecyclerView recyclerView) {
        recyclerView.removeOnScrollListener(this);
        recyclerView.removeOnAttachStateChangeListener(this);
        recyclerView.addOnScrollListener(this);
        recyclerView.addOnAttachStateChangeListener(this);
    }

    public void inject(AppBarLayout appBarLayout) {
        appBarLayout.removeOnOffsetChangedListener(this);
        appBarLayout.addOnOffsetChangedListener(this);
    }

    /**
     * 注入ListView的滚动监听
     * 这里要先判断listView是否已经设置过监听，没有才设置
     * 如果设置过，这个监听会被替换掉，滚动停止时会通过EventCollector将滚动状态发布出来
     */
    public void inject(AbsListView listView) {
        Object object = UIUtils.getListScrollListener(listView);
        if (object == null) {
            listView.removeOnAttachStateChangeListener(this);
            listView.setOnScrollListener(this);
            listView.addOnAttachStateChangeListener(this);
        }
    }

    /**
     * 注入ViewPager的滚动监听
     * 这里需要维护一个Map，因为ViewPager的监听回调中没有将ViewPager本身暴露出来，导致我们不知道是谁发生了滚动
     */
    public void inject(ViewPager viewPager) {
        ViewPager.OnPageChangeListener listener = mViewPagerListeners.get(viewPager);
        if (listener == null) {
            listener = new PageChangeListenerImpl(viewPager);
            mViewPagerListeners.put(viewPager, listener);
            viewPager.removeOnAttachStateChangeListener(this);
            viewPager.addOnPageChangeListener(listener);
            viewPager.addOnAttachStateChangeListener(this);
        }
    }

    /**
     *
     * @param view 滑动的view
     * @param newState view滑动的新的状态
     * @param stopState 这个view停止滑动的code是啥，用来比较
     */
    private void onScrollEvent(View view, int newState, int stopState) {
        ScrollInfo info = (ScrollInfo) view.getTag(R.id.key_scroll_id);
        if (info != null && info.getScrollEventEnable()) {
            if (info.getScrollState() != newState) {
                if (info.getScrollState() == stopState) { //开始滑动
                    info.setDestinationX(view.getScrollX());
                    info.setDestinationY(view.getScrollY());
                } else if (newState == stopState) { //停止滑动
                    int currentX = view.getScrollX();
                    int currentY = view.getScrollY();
                    info.setOffsetX(currentX - info.getDestinationX());
                    info.setOffsetY(currentY - info.getDestinationY());
                    info.setDestinationX(currentX);
                    info.setDestinationY(currentY);
                    ScrollEventObserver.INSTANCE.onScrollEvent(view, info);
                }
                info.setScrollState(newState);
            }
        }
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        if (DataReportInner.getInstance().isDebugMode()) {
            Log.d(TAG, "AppBarLayout.onOffsetChanged: verticalOffset = " + verticalOffset);
        }
        addAppBarLayoutDrawListenerForOnce(appBarLayout);
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
        if (DataReportInner.getInstance().isDebugMode()) {
            Log.d(TAG, "RecyclerView.onScrollStateChanged: newState = " + newState);
        }

        setScrollStateChange(recyclerView, newState, RecyclerView.SCROLL_STATE_IDLE);

        updateScrollingView(recyclerView, newState != RecyclerView.SCROLL_STATE_IDLE);
        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
            onIdle(recyclerView);
        }
    }

    private void setScrollStateChange(View view, int newState, int stopState) {
        ScrollInfo info = (ScrollInfo) view.getTag(R.id.key_scroll_id);
        if (info != null && info.getScrollEventEnable()) {
            if (info.getScrollState() != newState) {
                if (info.getScrollState() == stopState) { //开始滑动
                    info.setOffsetY(info.getDestinationY());
                    info.setOffsetX(info.getDestinationX());
                } else if (newState == stopState) { //停止滑动
                    info.setOffsetX(info.getDestinationX() - info.getOffsetX());
                    info.setOffsetY(info.getDestinationY() - info.getOffsetY());
                    if (view instanceof AbsListView) {
                        info.setMode(ScrollInfo.CELL);
                    }
                    ScrollEventObserver.INSTANCE.onScrollEvent(view, info);
                }
                info.setScrollState(newState);
            }
        }
    }

    private void setScrollInfo(View view, int dx, int dy) {
        ScrollInfo info = (ScrollInfo) view.getTag(R.id.key_scroll_id);
        if (info != null && info.getScrollEventEnable()) {
            info.setDestinationX(info.getDestinationX() + dx);
            info.setDestinationY(info.getDestinationY() + dy);
        }
    }

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        setScrollInfo(recyclerView, dx, dy);
        addRecyclerDrawListenerForOnce(recyclerView);
    }

    private void addRecyclerDrawListenerForOnce(RecyclerView recyclerView) {
        mRecyclerDrawListener.setView(recyclerView);
        recyclerView.getViewTreeObserver().addOnPreDrawListener(mRecyclerDrawListener);
    }

    private void addListViewDrawListenerForOnce(AbsListView listView) {
        mListViewDrawListener.setView(listView);
        listView.getViewTreeObserver().addOnPreDrawListener(mListViewDrawListener);
    }

    private void addAppBarLayoutDrawListenerForOnce(AppBarLayout appBarLayout) {
        mAppBarLayoutDrawListener.setView(appBarLayout);
        appBarLayout.getViewTreeObserver().addOnPreDrawListener(mAppBarLayoutDrawListener);
    }

    private final MyPreDrawListener mRecyclerDrawListener = new MyPreDrawListener();
    private final MyPreDrawListener mListViewDrawListener = new MyPreDrawListener();
    private final MyPreDrawListener mAppBarLayoutDrawListener = new MyPreDrawListener();

    private class MyPreDrawListener implements ViewTreeObserver.OnPreDrawListener{
        private WeakReference<View> view = null;

        public void setView(View view) {
            this.view = new WeakReference<>(view);
        }
        @Override
        public boolean onPreDraw() {
            View v = view.get();
            if (v != null) {
                onScrollUpdate(v);
                v.getViewTreeObserver().removeOnPreDrawListener(this);
            }
            return true;
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView listView, int scrollState) {
        if (DataReportInner.getInstance().isDebugMode()) {
            Log.d(TAG, "AbsListView.onScrollStateChanged: scrollState = " + scrollState);
        }
        setScrollStateChange(listView, scrollState, AbsListView.OnScrollListener.SCROLL_STATE_IDLE);
        updateScrollingView(listView, scrollState != AbsListView.OnScrollListener.SCROLL_STATE_IDLE);
        if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
            onIdle(listView);
        }
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        ScrollInfo info = (ScrollInfo) view.getTag(R.id.key_scroll_id);
        if (info != null && info.getScrollEventEnable()) {
            info.setDestinationX(0);
            info.setDestinationY(firstVisibleItem);
        }
        addListViewDrawListenerForOnce(view);
    }

    @Override
    public void onViewVisited(View view) {
        if (view instanceof AbsListView) {
            inject((AbsListView) view);
            return;
        }

        if (view instanceof RecyclerView) {
            inject((RecyclerView) view);
            return;
        }

        if (view instanceof ViewPager) {
            inject((ViewPager) view);
            return;
        }

        if (view instanceof AppBarLayout) {
            inject((AppBarLayout) view);
        }
    }

    private void updateScrollingView(View view, boolean isScrolling) {
        if (isScrolling) {
            mScrollingViews.add(view);
        } else {
            mScrollingViews.remove(view);
        }
    }

    /**
     * 监听各种setAdapter事件，在setAdapter时注入滚动监听
     */
    private class ScrollEventListener extends DefaultEventListener {

        @Override
        public void onSetRecyclerViewAdapter(RecyclerView recyclerView) {
            inject(recyclerView);
        }

        @Override
        public void onSetViewPagerAdapter(ViewPager viewPager) {
            inject(viewPager);
        }

        @Override
        public void onListScrollStateChanged(AbsListView listView, int scrollState) {
            if (DataReportInner.getInstance().isDebugMode()) {
                Log.d(TAG, "onListScrollStateChanged: scrollState=" + scrollState);
            }
            onScrollStateChanged(listView, scrollState);
        }

        @Override
        public void onListScrolled(AbsListView listView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            onScroll(listView, firstVisibleItem, visibleItemCount, totalItemCount);
        }
    }

    private class PageChangeListenerImpl implements ViewPager.OnPageChangeListener {

        private WeakReference<ViewPager> mViewPagerRef;

        PageChangeListenerImpl(ViewPager viewPager) {
            mViewPagerRef = new WeakReference<>(viewPager);
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (DataReportInner.getInstance().isDebugMode()) {
                Log.d(TAG, "ViewPager.onPageScrollStateChanged: state = " + state);
            }
            ViewPager viewPager = mViewPagerRef.get();
            if (viewPager == null) {
                return;
            }
            onScrollEvent(viewPager, state, ViewPager.SCROLL_STATE_IDLE);
            updateScrollingView(viewPager, state != ViewPager.SCROLL_STATE_IDLE);
            if (state == ViewPager.SCROLL_STATE_IDLE) {
                onIdle(viewPager);
            }
        }
    }

    @Override
    public void onViewAttachedToWindow(View v) {
    }

    @Override
    public void onViewDetachedFromWindow(View v) {
        if (v instanceof RecyclerView) {
            onScrollStateChanged((RecyclerView) v, RecyclerView.SCROLL_STATE_IDLE);
        } else if (v instanceof AbsListView) {
            onScrollStateChanged((AbsListView) v, AbsListView.OnScrollListener.SCROLL_STATE_IDLE);
        } else if (v instanceof ViewPager) {
            ViewPager.OnPageChangeListener listener = mViewPagerListeners.get(v);
            if (listener != null) {
                listener.onPageScrollStateChanged(ViewPager.SCROLL_STATE_IDLE);
            }
        }
    }
}
