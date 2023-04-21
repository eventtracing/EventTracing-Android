package com.netease.cloudmusic.datareport;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.netease.cloudmusic.datareport.policy.ReportPolicy;
import com.netease.cloudmusic.datareport.provider.IAppVisitProvider;
import com.netease.cloudmusic.datareport.provider.IDynamicParamsProvider;
import com.netease.cloudmusic.datareport.provider.IFormatter;
import com.netease.cloudmusic.datareport.provider.ILogger;
import com.netease.cloudmusic.datareport.provider.IReferStrategy;
import com.netease.cloudmusic.datareport.provider.IReporter;

import java.util.HashSet;
import java.util.regex.Pattern;

/**
 * 参数配置类
 */
public class Configuration {

    private static final Builder DEFAULT_BUILDER = new Builder();

    private static volatile Configuration sDefaultInstance;

    private boolean mDataCollectEnable;

    private final boolean mDebugMode;
    private final ReportPolicy mReportPolicy;

    private final Long mExposureMinTime;
    private final ILogger mLogger;
    private final IFormatter mFormatter;
    private final IReporter mReporter;
    private final IDynamicParamsProvider mDynamicParamsProvider;
    private final IDynamicParamsProvider mSyncDynamicParamsProvider;
    private final IAppVisitProvider mAppVisitProvider;
    private final HashSet<String> hsReferOidList;
    private final IReferStrategy mReferStrategy;
    private final String versionInfo;

    private final Pattern patternCustomEvent;
    private final Pattern patternGlobalKey;
    private final Pattern patternCustomKey;

    private final boolean elementExposureEnd;
    private final boolean aopScrollEnable;

    public static Configuration getDefault() {
        if (sDefaultInstance == null) {
            synchronized (Configuration.class) {
                if (sDefaultInstance == null) {
                    sDefaultInstance = new Configuration();
                }
            }
        }
        return sDefaultInstance;
    }

    public static Builder builder() {
        return new Builder();
    }

    Configuration() {
        this(DEFAULT_BUILDER);
    }

    private Configuration(Builder builder) {

        mDataCollectEnable = builder.mDataCollectEnable;

        mDebugMode = builder.mDebugMode;
        mReportPolicy = builder.mReportPolicy;

        mLogger = builder.mLogger;
        mFormatter = builder.mFormatter;
        mReporter = builder.mReporter;
        mDynamicParamsProvider = builder.mDynamicParamsProvider;
        mSyncDynamicParamsProvider = builder.mSyncDynamicParamsProvider;

        mAppVisitProvider = builder.mAppVisitProvider;
        hsReferOidList = builder.hsReferOidList;
        mExposureMinTime = builder.mExposureMinTime;
        mReferStrategy = builder.mReferStrategy;

        patternCustomEvent = builder.patternCustomEvent == null ? null : Pattern.compile(builder.patternCustomEvent);
        patternCustomKey = builder.patternCustomKey == null ? null : Pattern.compile(builder.patternCustomKey);
        patternGlobalKey = builder.patternGlobalKey == null ? null : Pattern.compile(builder.patternGlobalKey);

        elementExposureEnd = builder.elementExposureEnd;
        aopScrollEnable = builder.aopScrollEnable;
        versionInfo = builder.versionInfo;
    }

    public IReferStrategy getReferStrategy() {
        return mReferStrategy;
    }

    public void setDataCollectEnable(boolean dataCollectEnable) {
        mDataCollectEnable = dataCollectEnable;
    }

    /**
     * 设置是否开启可视化调试工具
     * @param isDebug
     */
    public void setDebugUI(boolean isDebug) {

    }

    public boolean isAopScrollEnable() {
        return aopScrollEnable;
    }

    public boolean isElementExposureEnd(){
        return elementExposureEnd;
    }

    public boolean isDebugUIEnable() {
        return false;
    }

    public boolean isDataCollectEnable() {
        return mDataCollectEnable;
    }

    public boolean isDebugMode() {
        return mDebugMode;
    }

    public ReportPolicy getReportPolicy() {
        return mReportPolicy;
    }

    public ILogger getLogger() {
        return mLogger;
    }

    @NonNull
    public IFormatter getFormatter() {
        return mFormatter;
    }

    public IReporter getReporter() {
        return mReporter;
    }

    @Nullable
    public Long getExposureMinTime() {
        return mExposureMinTime;
    }

    public IDynamicParamsProvider getDynamicParamsProvider() {
        return mDynamicParamsProvider;
    }

    public IDynamicParamsProvider getSyncDynamicParamsProvider() {
        return mSyncDynamicParamsProvider;
    }

    public HashSet<String> getHsReferOidList() {
        return hsReferOidList;
    }

    public Pattern getPatternCustomEvent() {
        return patternCustomEvent;
    }

    public Pattern getPatternGlobalKey() {
        return patternGlobalKey;
    }

    public Pattern getPatternCustomKey() {
        return patternCustomKey;
    }

    public IAppVisitProvider getAppVisitProvider() {
        return mAppVisitProvider;
    }

    public String getVersionInfo() {
        return versionInfo;
    }

    public static class Builder {
        private boolean mDataCollectEnable = true;
        private boolean mDebugMode = false;
        private ReportPolicy mReportPolicy = ReportPolicy.REPORT_POLICY_ALL;

        private ILogger mLogger;
        private Long mExposureMinTime;
        private IFormatter mFormatter;
        private IReporter mReporter;

        private IDynamicParamsProvider mDynamicParamsProvider;
        private IDynamicParamsProvider mSyncDynamicParamsProvider;
        private IAppVisitProvider mAppVisitProvider;
        private HashSet<String> hsReferOidList = new HashSet<>();
        private IReferStrategy mReferStrategy;

        private String patternCustomEvent;
        private String patternGlobalKey;
        private String patternCustomKey;

        private String versionInfo = "";

        private boolean elementExposureEnd = false;
        private boolean aopScrollEnable = false;

        /**
         * 是否打开埋点
         */
        public Builder defaultDataCollectEnable(boolean defaultDataCollectEnable) {
            this.mDataCollectEnable = defaultDataCollectEnable;
            return this;
        }

        /**
         * 是否是调试模式
         */
        public Builder debugMode(boolean debugMode) {
            this.mDebugMode = debugMode;
            return this;
        }

        /**
         * 全局的上报策略
         */
        public Builder reportPolicy(ReportPolicy elementReportPolicy) {
            this.mReportPolicy = elementReportPolicy;
            return this;
        }

        /**
         * 设置日志输出的接口实现
         */
        public Builder provideLogger(ILogger logger) {
            this.mLogger = logger;
            return this;
        }

        public Builder setExposureMinTime(long time) {
            this.mExposureMinTime = time;
            return this;
        }

        public Builder provideReferStrategy(IReferStrategy referStrategy) {
            this.mReferStrategy = referStrategy;
            return this;
        }

        /**
         * 设置埋点格式输出的接口实现
         */
        public Builder provideFormatter(IFormatter formatter) {
            this.mFormatter = formatter;
            return this;
        }

        public Builder setElementExposureEnd(boolean enable) {
            elementExposureEnd = enable;
            return this;
        }

        public Builder setAOPScrollEnable(boolean enable) {
            aopScrollEnable = enable;
            return this;
        }

        /**
         * 设置埋点上报的接口实现
         */
        public Builder provideReporter(IReporter reporter) {
            mReporter = reporter;
            return this;
        }

        /**
         * 设置全局公共参数的钩子
         * 在主线程被回调
         */
        public Builder provideDynamicParams(IDynamicParamsProvider dynamicParamsProvider) {
            mDynamicParamsProvider = dynamicParamsProvider;
            return this;
        }

        /**
         * 设置全局公共参数的钩子
         * 注意：此方法与provideDynamicParams的区别在于此方法是在埋点线程同步执行的
         */
        public Builder provideSyncDynamicParams(IDynamicParamsProvider dynamicParamsProvider) {
            mSyncDynamicParamsProvider = dynamicParamsProvider;
            return this;
        }

        /**
         * app打开的来源，没有用，暂时保留
         */
        public Builder provideAppVisit(IAppVisitProvider appVisitProvider) {
            mAppVisitProvider = appVisitProvider;
            return this;
        }

        /**
         * 设置hsrefer的列表
         */
        public Builder provideHsReferOidList(HashSet<String> oidList) {
            hsReferOidList.addAll(oidList);
            return this;
        }

        /**
         * 设置自定义事件的正则表达式，用来过滤非法的自定义事件
         * @param customEventPattern
         * @return
         */
        public Builder setCustomEventPattern(String customEventPattern) {
            this.patternCustomEvent = customEventPattern;
            return this;
        }

        /**
         * 设置公参的正则表达式，用来过滤非法的公参key
         * @param globalKeyPattern
         * @return
         */
        public Builder setGlobalKeyPattern(String globalKeyPattern) {
            this.patternGlobalKey = globalKeyPattern;
            return this;
        }

        /**
         * 设置自定义参数的正则表达式，用来过滤非法的自定义参数
         * @param customKeyPattern
         * @return
         */
        public Builder setCustomKeyPattern(String customKeyPattern) {
            this.patternCustomKey = customKeyPattern;
            return this;
        }

        public Builder setVersionInfo(String versionInfo) {
            this.versionInfo = versionInfo;
            return this;
        }

        public Configuration build() {
            Configuration configuration = new Configuration(this);
            return configuration;
        }

    }

}
