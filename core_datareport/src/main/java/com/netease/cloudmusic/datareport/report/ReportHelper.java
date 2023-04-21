package com.netease.cloudmusic.datareport.report;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import com.netease.cloudmusic.datareport.data.DataRWProxy;
import com.netease.cloudmusic.datareport.inner.InnerKey;
import com.netease.cloudmusic.datareport.policy.ReportPolicy;
import com.netease.cloudmusic.datareport.inner.DataReportInner;
import com.netease.cloudmusic.datareport.vtree.bean.VTreeNode;


/**
 * 上报帮助类
 */
public class ReportHelper {

    /**
     * 上报策略是否允许上报点击
     */
    public static boolean reportClick(@Nullable View view) {
        if (emptyId(view)) {
            return false;
        }
        return getReportPolicy(view).reportClick;
    }

    /**
     * 上报策略是否允许上报曝光
     */
    static boolean reportExposure(@Nullable View view) {
        if (emptyId(view)) {
            return false;
        }
        return getReportPolicy(view).reportExposure;
    }

    static boolean reportExposure(VTreeNode vTreeNode) {
        ReportPolicy policy = (ReportPolicy) vTreeNode.getInnerParam(InnerKey.VIEW_REPORT_POLICY);
        if (policy == null) {
            policy = DataReportInner.getInstance().getConfiguration().getReportPolicy();
        }
        if (policy == null) {
            return true;
        }
        return policy.reportExposure;
    }

    static boolean reportElementExposureEnd(VTreeNode vTreeNode) {
        Object flag = vTreeNode.getInnerParam(InnerKey.VIEW_ELEMENT_EXPOSURE_END);
        if (flag instanceof Boolean) {
            return (boolean) flag;
        } else {
            return DataReportInner.getInstance().getConfiguration().isElementExposureEnd();
        }
    }

    /**
     * 判断数据是否有有效的元素id
     */
    private static boolean emptyId(@Nullable View view) {
        return view == null || (TextUtils.isEmpty(DataRWProxy.getPageId(view)) && TextUtils.isEmpty(DataRWProxy.getElementId(view)));
    }

    @NonNull
    private static ReportPolicy getReportPolicy(View view) {

        ReportPolicy policy = (ReportPolicy) DataRWProxy.getInnerParam(view, InnerKey.VIEW_REPORT_POLICY);
        if (policy == null) {
            policy = DataReportInner.getInstance().getConfiguration().getReportPolicy();
        }
        return policy == null ? ReportPolicy.REPORT_POLICY_ALL : policy;
    }
}
