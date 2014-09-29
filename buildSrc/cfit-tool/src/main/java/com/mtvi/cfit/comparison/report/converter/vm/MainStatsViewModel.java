package com.mtvi.cfit.comparison.report.converter.vm;

import com.mtvi.cfit.comparison.report.ComparisonReport;
import com.mtvi.cfit.comparison.report.ComparisonStats;
import com.mtvi.cfit.comparison.report.PerformanceStatistic;
import com.mtvi.cfit.comparison.report.ReportState;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Class MainStatsViewModel.
 *
 * @author zenind
 */
public class MainStatsViewModel implements ReportViewModel {
    /** Template name.*/
    private static final String TEMPLATE_NAME = "report-main.html";
    /** Source.*/
    private final ComparisonReport source;
    /** List of linked views for differences.*/
    private final List<Link> differences;
    /** List of linked views for failed queries.*/
    private final List<Link> failedQueries;
    /** List of linked views for only original responses.*/
    private final List<Link> onlyOriginalResponses;
    /** List of linked views for only rc responses.*/
    private final List<Link> onlyRCResponses;
    /** List of linked views for slow queries.*/
    private final List<Link> slowQueries;

    /**
     * Constructor.
     * @param source - source report.
     * @param differences - list of linked views of differences.
     * @param failedQueries - list of linked views of failed queries.
     * @param onlyOriginalResponses - list of linked views of only original responses.
     * @param onlyRCResponses - list of linked views of only rc responses.
     * @param slowQueries - list of linked views of slow queries.
     */
    public MainStatsViewModel(ComparisonReport source, List<Link> differences, List<Link> failedQueries,
        List<Link> onlyOriginalResponses, List<Link> onlyRCResponses, List<Link> slowQueries) {
        this.source = source;
        this.differences = differences;
        this.failedQueries = failedQueries;
        this.onlyOriginalResponses = onlyOriginalResponses;
        this.onlyRCResponses = onlyRCResponses;
        this.slowQueries = slowQueries;
    }

    @Override
    public String getMatchedTemplate() {
        return TEMPLATE_NAME;
    }

    @Override
    public String getHeader() {
        return TEMPLATE_NAME;
    }

    public ReportState getReportState() {
        return source.getReportState();
    }

    public PerformanceStatistic getOriginalPerformanceStats() {
        return source.getOriginalPerformanceStats();
    }

    public PerformanceStatistic getRcPerformanceStats() {
        return source.getRcPerformanceStats();
    }

    public Date getCreationDate() {
        return source.getCreationDate();
    }

    public ComparisonStats getComparisonStats() {
        return source.getComparisonStats();
    }

    public List<Link> getPerformanceQueries() {
        return Collections.unmodifiableList(this.slowQueries);
    }

    public List<Link> getOnlyOriginalResponses() {
        return Collections.unmodifiableList(onlyOriginalResponses);
    }

    public List<Link> getOnlyRCResponses() {
        return Collections.unmodifiableList(onlyRCResponses);
    }

    public List<Link> getFailedQueries() {
        return Collections.unmodifiableList(failedQueries);
    }

    public List<Link> getDifferences() {
        return Collections.unmodifiableList(differences);
    }

    public boolean isSuccess() {
        return source.isSuccess();
    }

    public String getRcVersion() {
        return source.getRcVersion();
    }

    public String getOriginalVersion() {
        return source.getOriginalVersion();
    }

}
