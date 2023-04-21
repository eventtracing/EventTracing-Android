package com.netease.cloudmusic.datareport.policy;

/**
 * 上报策略
 */
public enum ReportPolicy {

    /**
     * 都不上报
     */
    REPORT_POLICY_NONE(false, false),

    /**
     * 只上报曝光
     */
    REPORT_POLICY_EXPOSURE(false, true),

    /**
     * 只上报点击
     */
    REPORT_POLICY_CLICK(true, false),

    /**
     * 都上报
     */
    REPORT_POLICY_ALL(true, true);

    public final boolean reportClick;
    public final boolean reportExposure;

    ReportPolicy(boolean reportClick, boolean reportExposure) {
        this.reportClick = reportClick;
        this.reportExposure = reportExposure;
    }
}
