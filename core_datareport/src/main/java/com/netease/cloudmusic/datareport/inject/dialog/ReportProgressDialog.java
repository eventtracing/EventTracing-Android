package com.netease.cloudmusic.datareport.inject.dialog;

import android.app.ProgressDialog;
import android.content.Context;

import androidx.annotation.NonNull;

import com.netease.cloudmusic.datareport.inject.EventCollector;

public class ReportProgressDialog extends ProgressDialog {
    public ReportProgressDialog(@NonNull Context context) {
        super(context);
    }

    public ReportProgressDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        EventCollector.getInstance().onDialogFocusChanged(this, hasFocus);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventCollector.getInstance().onDialogStop(this);
    }
}
