package com.netease.cloudmusic.datareport.inject.fragment;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;

import com.netease.cloudmusic.datareport.inject.dialog.ReportDialog;

public class ReportDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new ReportDialog(getActivity(), getTheme());
    }
}
