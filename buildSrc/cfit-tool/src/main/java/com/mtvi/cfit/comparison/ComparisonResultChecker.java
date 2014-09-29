package com.mtvi.cfit.comparison;

import com.google.common.collect.ImmutableList;
import com.mtvi.cfit.CfitConfiguration;
import com.mtvi.cfit.CfitException;
import com.mtvi.cfit.ConversionException;
import com.mtvi.cfit.comparison.report.ComparisonReport;
import com.mtvi.cfit.comparison.report.PerformanceStatistic;
import com.mtvi.cfit.comparison.report.ReportBuilder;
import com.mtvi.cfit.exec.ExecutionResultManager;
import com.mtvi.cfit.query.common.LineBasedIterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Default state checker.
 *
 * @author Dzmitry_Zenin
 */
public final class ComparisonResultChecker {

    /** Logger.*/
    private static final Logger LOG = LoggerFactory.getLogger(ComparisonResultChecker.class);
    /** List of checkers of cfit state after cfit run.*/
    private List<Check> checks;

    /**
     * Constructor.
     * @param configuration - configuration.
     * @throws ConversionException - conversion exception.
     */
    public ComparisonResultChecker(CfitConfiguration configuration) throws ConversionException {
        if (configuration == null) {
            throw new IllegalArgumentException(
                "Null value has been passed in as required argument ['configuration'= null]");
        }
        this.checks = ImmutableList.of(
            new DifferenceCheck(configuration),
            new FailedQueriesCheck(configuration),
            new PerformanceDifferenceCheck(configuration)
        );
    }

    /**
     * Does resolution build state.
     * @throws CfitException - if build if failed.
     */
    public void checkState() throws CfitException {
        List<String> errorMessages = new ArrayList<String>();
        for (Check check : checks) {
            try {
                check.check();
            } catch (CfitException e) {
                errorMessages.add(e.getMessage());
            }
        }
        
        if (!errorMessages.isEmpty()) {
            String errorMessage = buildResolution(errorMessages);
            LOG.error(errorMessage);
            throw new CfitException(errorMessage);
        }
    }
    
    private String buildResolution(List<String> errors) {
        StringBuilder builder = new StringBuilder("BUILD FAILED. Reasons:\n");
        for (String error : errors) {
            builder.append("#. ").append(error).append("\n");
        }
        return builder.toString();
    }

    /**
     * State Checker.
     * Represents interface for checks for perform on build results.
     *
     * @author  zenind.
     */
    public interface Check {

        /**
         * Perform check on certain condition.
         *
         * @throws CfitException if check is failed.
         */
        void check() throws CfitException;
    }

    /**
     * Checks for differences in original and rc responses.
     *
     * @author zenind.
     */
    static class DifferenceCheck implements Check {

        /** Configuration.*/
        private CfitConfiguration configuration;

        /**
         * Constructor.
         * @param configuration - configuration.
         */
        public DifferenceCheck(CfitConfiguration configuration) {
            this.configuration = configuration;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void check() throws CfitException {
            try {
                Iterable<String> lines = new LineBasedIterable.SimpleLineBasedIterable(configuration.layout().comparisonLog());
                int differenceQuantity = 0;
                for (String line : lines) {
                    if (line.contains("are different")) {
                        differenceQuantity++;
                    }
                }

                if (differenceQuantity > 0) {
                    throw new CfitException(
                        String.format("Differences are found. Quantity of differences: %s . Report: %s",
                            differenceQuantity, configuration.layout().reportsDir()));
                }

            } catch (IOException e) {
                throw new CfitException(String.format("Unable to read report comparison log"), e);
            }
        }
    }

    /**
     * Checks for failed queries in rc responses.
     *
     * @author zenind.
     */
    static final class FailedQueriesCheck implements Check {
        /** Cfit configuration.*/
        private CfitConfiguration configuration;
        /** Execution results manager.*/
        private ExecutionResultManager resultsManager;

        /***
         * Constructor.
         *
         * @param configuration - cfit configuration.
         */
        public FailedQueriesCheck(CfitConfiguration configuration) {
            this.configuration = configuration;
            this.resultsManager = new ExecutionResultManager(configuration);
        }

        @Override
        public void check() throws CfitException {
            File rcStatsFile = configuration.layout().rcExecutionStats();
            PerformanceStatistic rcStatistic = resultsManager.load(rcStatsFile).getPerformanceStats();

            File originalStatsFile = configuration.layout().originalExecutionStats();
            PerformanceStatistic originalStatistic = resultsManager.load(originalStatsFile).getPerformanceStats();

            if (originalStatistic == null || rcStatistic == null) {
                return;
            }

            if (rcStatistic.getFailed() > originalStatistic.getFailed()) {
                throw new CfitException(
                    String.format("Failed queries are found for RC phase. Quantity of failed queries: "
                        + "in original responses %s, in rc responses: %s",
                        originalStatistic.getFailed(), rcStatistic.getFailed())
                );
            }
        }
    }

    /**
     * Checks that performance metrics of RC phase doesn't exceed original phase results by some value.
     *
     * @author zenind.
     */
    static final class PerformanceDifferenceCheck implements Check {
        /** Logger.*/
        private static final Logger LOG = LoggerFactory.getLogger(PerformanceDifferenceCheck.class);
        /** Performance metrics aware manager.*/
        private final PerformanceComparisonManager performanceComparisonManager;
        /** Cfit configuration.*/
        private final CfitConfiguration configuration;
        /** Report manager.*/
        private final ReportBuilder reportManager;

        /**
         * Constructor.
         *
         * @param configuration - configuration.
         * @throws ConversionException - in case of issue during initialization on html converter.
         */
        public PerformanceDifferenceCheck(CfitConfiguration configuration) throws ConversionException {
            this.configuration = configuration;
            this.performanceComparisonManager = new PerformanceComparisonManager(configuration);
            this.reportManager = new ReportBuilder(configuration);
        }

        @Override
        public void check() throws CfitException {
            File report = detectReportFile();
            try {
                ComparisonReport comparisonReport = reportManager.load(report, ReportBuilder.Format.XML);
                PerformanceStatistic originalStats = comparisonReport.getOriginalPerformanceStats();
                PerformanceStatistic rcStats = comparisonReport.getRcPerformanceStats();

                checkMetric(
                    originalStats.getAvg(),
                    rcStats.getAvg(),
                    String.format(
                        "Warning: difference of average query execution in RC phase exceed performance threshold. "
                        + "Original Value: %s, RC Value: %s", originalStats.getAvg(), rcStats.getAvg())
                );

                checkMetric(
                    originalStats.getMax(),
                    rcStats.getMax(),
                    String.format("Warning: difference of max query execution in RC phase exceed performance threshold. "
                        + "Original Value: %s, RC Value: %s",
                        originalStats.getMax(), rcStats.getMax())
                );
            } catch (ConversionException e) {
                LOG.info(String.format("Unable to load comparison report. Reason: %s", e.getMessage()));
            }
        }

        private File detectReportFile() {
            File lastReport = configuration.layout().reportsDir();
            if (lastReport != null && lastReport.exists()) {
                return new File(lastReport, configuration.getXmlReportName());
            }

            throw new IllegalStateException("Unable for find last generated report.");
        }

        private void checkMetric(int originalValue, int rcValue, String errorMessage) throws CfitException {
            if (performanceComparisonManager.isMetricExceedsThreshold(originalValue, rcValue)) {
                throw new CfitException(errorMessage);
            }
        }

    }
}
