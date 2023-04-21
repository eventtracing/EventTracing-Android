package com.netease.cloudmusic.datareport.inject.fragment;

import androidx.fragment.app.Fragment;

public class ReportAndroidXFragment extends Fragment {

    public ReportAndroidXFragment() {
        super();
    }

    public ReportAndroidXFragment(int contentLayoutId) {
        super(contentLayoutId);
    }

    @Override
    public void onResume() {
        super.onResume();
        AndroidXFragmentCollector.onResume(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        AndroidXFragmentCollector.onPause(this);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        AndroidXFragmentCollector.onHiddenChanged(this, hidden);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        AndroidXFragmentCollector.setUserVisibleHint(this, isVisibleToUser);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        AndroidXFragmentCollector.onDestroyView(this);
    }
}