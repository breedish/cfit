package com.mtvi.cfit;

import com.google.common.base.Objects;

/**
 * Cfit control properties.
 * @author voitaua
 */
@SuppressWarnings("unused")
public final class CfitProperties {

    /** Default value for quantity of workers.*/
    private static final int DEFAULT_WORKERS_NUMBER = 40;
    /** Value of performance threshold in percents.*/
    private static final int DEFAULT_PERFORMANCE_THRESHOLD = 10;
    /**
     *  Default value of max number of items displayed in each sub report section. Applies only to
     *  {@link com.mtvi.cfit.comparison.report.ReportBuilder.Format#HTML} report format.
     */
    private static final int DEFAULT_MAX_SUB_REPORT_ITEMS = 100;
    /**
     *  Default value of max size of items displayed in each sub report section in bytes. Applies only to
     *  {@link com.mtvi.cfit.comparison.report.ReportBuilder.Format#HTML} report format.
     */
    private static final int DEFAULT_MAX_SUB_REPORT_ITEMS_SIZE = 10 * 1024 * 1024;

    /** Extra/Additional request parameters to add for each query.*/
    private String extraReqParams;
    /** Defined max value of performance diff value between average execution times.*/
    private Integer performanceThreshold = DEFAULT_PERFORMANCE_THRESHOLD;
    /** Rc artifact version.*/
    private String rcArtifactVersion;
    /** Original artifact version.*/
    private String originalArtifactVersion;
    /** Number of workers that will be used for queries execution.*/
    private int workersNumber = DEFAULT_WORKERS_NUMBER;
    /** Value of max total size of items for sub report.*/
    private int subReportItemsSize = DEFAULT_MAX_SUB_REPORT_ITEMS_SIZE;
    /** Value of max total items for sub report.*/
    private int subReportItemNumber = DEFAULT_MAX_SUB_REPORT_ITEMS;

    public String getExtraReqParams() {
        return extraReqParams;
    }

    public void setExtraReqParams(String extraReqParams) {
        this.extraReqParams = extraReqParams;
    }

    public Integer getPerformanceThreshold() {
        return performanceThreshold;
    }

    public void setPerformanceThreshold(Integer performanceThreshold) {
        this.performanceThreshold = performanceThreshold;
    }

    public String getRcArtifactVersion() {
        return rcArtifactVersion;
    }

    public void setRcArtifactVersion(String rcArtifactVersion) {
        this.rcArtifactVersion = rcArtifactVersion;
    }

    public String getOriginalArtifactVersion() {
        return originalArtifactVersion;
    }

    public void setOriginalArtifactVersion(String originalArtifactVersion) {
        this.originalArtifactVersion = originalArtifactVersion;
    }

    public int getWorkersNumber() {
        return workersNumber;
    }

    public void setWorkersNumber(int workersNumber) {
        this.workersNumber = workersNumber;
    }

    public int getSubReportItemsSize() {
        return subReportItemsSize;
    }

    public int getSubReportItemNumber() {
        return subReportItemNumber;
    }

    public void setSubReportItemsSize(int subReportItemsSize) {
        this.subReportItemsSize = subReportItemsSize;
    }

    public void setSubReportItemNumber(int subReportItemNumber) {
        this.subReportItemNumber = subReportItemNumber;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("originalArtifactVersion", originalArtifactVersion)
            .add("rcArtifactVersion", rcArtifactVersion)
            .add("performanceThreshold", performanceThreshold)
            .add("extraReqParams", extraReqParams)
            .add("workersNumber", workersNumber)
            .toString();
    }
}
