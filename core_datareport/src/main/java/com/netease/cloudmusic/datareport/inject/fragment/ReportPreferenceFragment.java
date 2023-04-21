package com.netease.cloudmusic.datareport.inject.fragment;

import android.preference.PreferenceFragment;

public class ReportPreferenceFragment extends PreferenceFragment {

    @Override
    public void onResume() {
        super.onResume();
        FragmentCollector.onResume(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        FragmentCollector.onPause(this);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        FragmentCollector.onHiddenChanged(this, hidden);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        FragmentCollector.setUserVisibleHint(this, isVisibleToUser);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        FragmentCollector.onDestroyView(this);
    }
}