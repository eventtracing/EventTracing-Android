package com.netease.cloudmusic.datareport.inject.dialog;

import android.app.Dialog;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.netease.cloudmusic.datareport.inject.EventCollector;

public class ReportDialog extends Dialog {
    public ReportDialog(@NonNull Context context) {
        super(context);
    }

    public ReportDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected ReportDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
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
