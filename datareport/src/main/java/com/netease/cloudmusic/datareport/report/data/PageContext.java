package com.netease.cloudmusic.datareport.report.data;

import com.netease.cloudmusic.datareport.report.InnerReportKeyKt;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * 页面上下文信息
 */
public class PageContext implements IContext{

    private final int pageStep;
    //这个actSeq主要是为了保存page曝光的时候，当前的actSeq，主要用于容器refer的生成
    private int actSeq;
    private final long exposureTime;
    private final String pgRefer;
    private final String psRefer;

    public PageContext(int pageStep, long exposureTime) {
        this(pageStep, exposureTime, null, null);
    }

    public PageContext(int pageStep, long exposureTime, String pgRefer, String psRefer) {
        this.pageStep = pageStep;
        this.exposureTime = exposureTime;
        this.pgRefer = pgRefer;
        this.psRefer = psRefer;
    }

    @NotNull
    @Override
    public Map<String, Object> getParams() {
        Map<String, Object> map = new HashMap<>();
        map.put(InnerReportKeyKt.PAGE_STEP_KEY, pageStep);
        if (pgRefer != null && pgRefer.length() > 0) {
            map.put(InnerReportKeyKt.PAGE_REFER_KEY, pgRefer);
        }
        if (psRefer != null && psRefer.length() > 0) {
            map.put(InnerReportKeyKt.PS_REFER_KEY, psRefer);
        }
        return map;
    }

    public int getPageStep() {
        return pageStep;
    }

    public void setActSeq(int actSeq) {
        this.actSeq = actSeq;
    }

    public int getActSeq() {
        return actSeq;
    }

    public String getPgRefer() {
        return pgRefer;
    }

    public String getPsRefer() {
        return psRefer;
    }

    @Override
    public long getExposureTimes() {
        return exposureTime;
    }
}
