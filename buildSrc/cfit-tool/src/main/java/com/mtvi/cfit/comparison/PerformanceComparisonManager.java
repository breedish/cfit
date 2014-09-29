package com.mtvi.cfit.comparison;

import com.mtvi.cfit.CfitConfiguration;
import com.mtvi.cfit.comparison.report.ComparisonReport;
import com.mtvi.cfit.query.response.QueryResponse;
import com.mtvi.cfit.util.CfitUtils;

/**
 * Performance manager.
 *
 * @author zenind
 */
public class PerformanceComparisonManager {

    /** Performance threshold.*/
    private final int performanceThreshold;

    /**
     * Constructor.
     * @param configuration - cfit configuration.
     */
    public PerformanceComparisonManager(CfitConfiguration configuration) {
        this.performanceThreshold = configuration.getCfitProperties().getPerformanceThreshold();
    }

    /**
     * Checks if rc query response time is not slower than original response time by given threshold.
     * @param original - original response.
     * @param rc - rc response.
     * @param report - cfit report.
     */
    public void checkPerformance(QueryResponse original, QueryResponse rc, ComparisonReport report) {
        if (isMetricExceedsThreshold(original.getTime(), rc.getTime())) {
            report.addSlowQuery(
                new ComparisonReport.SlowQuery(
                    original.getDefinition().getId(),
                    original.getUrl(),
                    original.getTime(),
                    rc.getTime()
                )
            );
        }

    }

    /**
     * Checks whether rcValue exceeds original value by given performance threshold.
     *
     * @param originalValue - original value.
     * @param rcValue       - rc value.
     * @return false if metric is ok, otherwise true
     */
    public boolean isMetricExceedsThreshold(long originalValue, long rcValue) {
        int difference = CfitUtils.growthRate(originalValue, rcValue);
        return difference > 0 && difference >= performanceThreshold;
    }
}
