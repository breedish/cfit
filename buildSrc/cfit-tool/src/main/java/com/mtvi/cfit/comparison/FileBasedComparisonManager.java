package com.mtvi.cfit.comparison;

import com.google.common.base.Equivalence;
import com.google.common.base.Function;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.mtvi.cfit.CfitConfiguration;
import com.mtvi.cfit.CfitException;
import com.mtvi.cfit.ConversionException;
import com.mtvi.cfit.comparison.report.QueryDifference;
import com.mtvi.cfit.query.common.LineBasedIterable;
import com.mtvi.cfit.comparison.comparator.FileComparator;
import com.mtvi.cfit.comparison.comparator.TextComparator;
import com.mtvi.cfit.comparison.report.ComparisonReport;
import com.mtvi.cfit.comparison.report.PerformanceStatistic;
import com.mtvi.cfit.comparison.report.ReportBuilder;
import com.mtvi.cfit.exec.ExecutionResultManager;
import com.mtvi.cfit.query.response.QueryResponse;
import com.mtvi.cfit.query.response.QueryResponseManager;
import com.mtvi.cfit.util.IOUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple implementation of file based comparison manager.
 *
 * @author zenind
 */
public final class FileBasedComparisonManager implements ComparisonManager {

    /** Logger. */
    private static final Logger LOG  = LoggerFactory.getLogger(FileBasedComparisonManager.class);
    /** Failed query pattern message.*/
    private static final Pattern FAILED_QUERY_PATTERN = Pattern.compile("^\\[[0-9]{3}\\](.+)");

    /** Query Response file filter.*/
    private final QueryResponseFileFilter responseFileFilter = new QueryResponseFileFilter();
    /** Query Response Manager.*/
    private final QueryResponseManager<File> responseManager;
    /** Performance helper.*/
    private final PerformanceComparisonManager performanceComparisonManager;
    /** Execution Results manager.*/
    private final ExecutionResultManager resultsManager;
    /** Report manager.*/
    private final ReportBuilder reportBuilder;
    /** Cfit configuration.*/
    private final CfitConfiguration configuration;
    /** Text comparator.*/
    private final FileComparator contentComparator;

    /**
     * Constructor.
     *
     * @param configuration - configuration.
     * @param responseManager - response manager.
     * @throws ConversionException - conversion exception
     */
    public FileBasedComparisonManager(CfitConfiguration configuration, QueryResponseManager<File> responseManager)
        throws ConversionException {
        this.configuration = configuration;

        this.responseManager = responseManager;
        this.resultsManager = new ExecutionResultManager(configuration);
        this.performanceComparisonManager = new PerformanceComparisonManager(configuration);
        this.reportBuilder = new ReportBuilder(configuration);
        this.contentComparator = new TextComparator(configuration.layout().comparisonConfig());

        initReportDir();
    }

    private void initReportDir() {
        try {
            IOUtils.createDir(configuration.layout().reportOriginalContentDir());
            IOUtils.createDir(configuration.layout().reportRcContentDir());
        } catch (IOException e) {
            LOG.error("Unable to initialize report directories.", e);
        }
    }

    @Override
    public ComparisonReport compareResponses() throws CfitException {
        try {
            PerformanceStatistic originalStats = getPerformanceStats(configuration.layout().originalExecutionStats());
            PerformanceStatistic rcStats = getPerformanceStats(configuration.layout().rcExecutionStats());
            IOUtils.copy(configuration.layout().reportOriginalContentDir(), configuration.layout().originalExecutionStats());
            IOUtils.copy(configuration.layout().reportRcContentDir(), configuration.layout().rcExecutionStats());

            ComparisonReport report = new ComparisonReport(originalStats, rcStats, new Date());
            report.setRcVersion(configuration.getCfitProperties().getRcArtifactVersion());
            report.setOriginalVersion(configuration.getCfitProperties().getOriginalArtifactVersion());

            doComparison(report);

            detectFailedQueries(report);

            return buildReport(report);
        } catch (Exception e) {
            throw new ComparisonException(
                String.format("Error occurred while comparing Responses: %s and %s",
                    configuration.getOriginalResultsDir(), configuration.getRcResultsDir()),
                e
            );
        }
    }

    /**
     * Does actual comparison of responses produced by queries.
     *
     * @param report - comparison report.
     * @throws IOException
     * @throws CfitException
     */
    @SuppressWarnings("unchecked")
    private void doComparison(ComparisonReport report) throws Exception {
        Map<String, File> originalResponses  = mapByName(
            FileUtils.listFiles(configuration.layout().originalResultsDir(), responseFileFilter, null)
        );
        Map<String, File> rcResponses  = mapByName(
            FileUtils.listFiles(configuration.layout().rcResultsDir(), responseFileFilter, null)
        );

        MapDifference<String, File> responsesDiff = getDiff(originalResponses, rcResponses);

        Map<String, File> inCommon = responsesDiff.entriesInCommon();
        ComparisonProgressListener listener = new ComparisonProgressListener(report, inCommon.size());
        for (File originalResponseFile : inCommon.values()) {
            try {
                File rcResponseFile = rcResponses.get(originalResponseFile.getName());
                QueryResponse originalResponse = responseManager.load(originalResponseFile);
                QueryResponse rcResponse = responseManager.load(rcResponseFile);
                ResponseComparisonResult result = contentComparator.compare(originalResponseFile, rcResponseFile);

                switch (result) {
                    case IDENTICAL:
                        listener.onIdentical(originalResponseFile);
                        break;
                    case SIMILAR:
                        listener.onSimilar(originalResponseFile);
                        break;
                    case DIFFERENT:
                        listener.onDifferent(originalResponseFile, originalResponse, rcResponse, rcResponseFile);
                        break;
                    default:
                        break;
                }

                performanceComparisonManager.checkPerformance(originalResponse, rcResponse, report);
                listener.onItemProcessed();
            } catch (Exception e) {
                LOG.error("\nUnable to load query response {}", e);
            }
        }

        for (File response : responsesDiff.entriesOnlyOnLeft().values()) {
            listener.onOnlyOriginal(response);
        }

        for (File response : responsesDiff.entriesOnlyOnRight().values()) {
            listener.onOnlyRc(response);
        }

        listener.onFinish();
    }

    private MapDifference<String, File> getDiff(Map<String, File> originalResponses, Map<String, File> rcResponses) {
        return Maps.difference(
                originalResponses,
                rcResponses,
                new Equivalence<File>() {
                    @Override
                    protected boolean doEquivalent(File a, File b) {
                        return a.getName().equals(b.getName());
                    }

                    @Override
                    protected int doHash(File file) {
                        return file.getName().hashCode();
                    }
                }
        );
    }

    private Map<String, File> mapByName(Collection<File> files) {
        return Maps.uniqueIndex(files, new Function<File, String>() {
            @Override
            public String apply(File input) {
                return input.getName();
            }
        });
    }

    private void detectFailedQueries(ComparisonReport report) {
        try {
            File rcFailedQueries = new File(configuration.getRcResultsDir(), configuration.getFailedQueriesFileName());
            Iterable<String> queries = new LineBasedIterable.SimpleLineBasedIterable(rcFailedQueries);

            for (String query : queries) {
                Matcher matcher = FAILED_QUERY_PATTERN.matcher(query);
                if (matcher.find()) {
                    String failedQuery = matcher.group(1);
                    report.addFailedQuery(new QueryResponse(null, failedQuery, null, 0, 0));
                }
            }

            File originalFailedQueries = new File(configuration.getOriginalResultsDir(), configuration.getFailedQueriesFileName());
            IOUtils.copy(configuration.layout().reportRcContentDir(), rcFailedQueries);
            IOUtils.copy(configuration.layout().reportOriginalContentDir(), originalFailedQueries);
        } catch (IOException e) {
            LOG.error("Error during processing of failed queries", e);
        }
    }

    private ComparisonReport buildReport(ComparisonReport report) throws ConversionException {
        reportBuilder.buildReport(report, configuration.layout().xmlReport(), ReportBuilder.Format.XML);
        reportBuilder.buildReport(report, configuration.layout().htmlReport(), ReportBuilder.Format.HTML);

        LOG.info("HTML Comparison Report could be found by the following path : {}", configuration.layout().htmlReport());
        return report;
    }

    /**
     * Loads performance stats for given phase.
     *
     * @param resultStorage - phase's performance stats.
     * @return - loaded stats.
     */
    private PerformanceStatistic getPerformanceStats(File resultStorage) throws CfitException {
        return resultsManager.load(resultStorage).getPerformanceStats();
    }

    /**
     * Responses comparison listener.
     */
    private class ComparisonProgressListener {
        /** Multiplicity factor.*/
        private static final int MULTIPLICITY_FACTOR = 1000;
        /** Comparison report. */
        private final ComparisonReport report;
        /** Comparison comparisonLog.*/
        private final PrintWriter comparisonLog;
        /** Current index of items.*/
        private int current;
        /** Total number of items to compare.*/
        private final int total;


        /**
         * Constructor.
         * @param report - comparison report.
         * @param total - total number of responses to compare.
         */
        ComparisonProgressListener(ComparisonReport report, int total) throws IOException {
            this.report = report;
            this.total = total;
            comparisonLog = new PrintWriter(
                Files.newWriter(
                    configuration.layout().comparisonLog(),
                    Charset.defaultCharset()
                )
            );

            LOG.info("Save compare stats to  " + configuration.layout().comparisonLog().getAbsolutePath());
            LOG.info("Comparing {} files", total);
            comparisonLog.write(String.format("Comparing %s files", total));
        }

        void onItemProcessed()  {
            ++current;
            if ((current - 1) % MULTIPLICITY_FACTOR == 0) {
                LOG.info("\r\t {} / {} Processed", current, total);
            }
        }

        void onIdentical(File response) throws IOException {
            comparisonLog.println(String.format("'%s' are identical", response.getName()));
            report.getComparisonStats().registerIdentical();
        }

        void onSimilar(File response) throws IOException {
            comparisonLog.println(String.format("'%s' are similar", response.getName()));
            report.getComparisonStats().registerSimilar();
        }

        void onOnlyOriginal(File response) throws CfitException, IOException {
            comparisonLog.println(String.format("'%s' has only original version\n", response.getName()));
            report.addOnlyOriginalResponse(responseManager.load(response));
        }

        void onOnlyRc(File response) throws CfitException, IOException {
            comparisonLog.println(String.format("'%s' has only rc version\n", response.getName()));
            report.addOnlyRCResponse(responseManager.load(response));
        }

        void onDifferent(File originalResponseFile, QueryResponse originalResponse,
             QueryResponse rcResponse, File rcResponseFile) {
            comparisonLog.println(String.format("'%s' are different", originalResponseFile.getName()));
            LOG.info("'{}' are different", originalResponseFile.getName());
            report.addDifference(new QueryDifference(originalResponse.getDefinition(), originalResponse, rcResponse));

            IOUtils.copy(configuration.layout().reportRcContentDir(), rcResponseFile);
            IOUtils.copy(configuration.layout().reportOriginalContentDir(), originalResponseFile);
        }

        void onFinish() throws IOException {
            if (CollectionUtils.isNotEmpty(report.getPerformanceQueries())) {
                LOG.info("\nPerformance issues( {} )", report.getPerformanceQueries().size());
                for (ComparisonReport.SlowQuery query : report.getPerformanceQueries()) {
                    LOG.info("\t {} - {} : {} : {}", query.getId(), query.getOriginalValue(), query.getRcValue(), query.getPercent());
                }
            }

            if (report.getDifferences().size() == 0) {
                LOG.info("CFIT successfully passed!");
                comparisonLog.write("CFIT successfully passed!");
            } else {
                LOG.info("CFIT not passed!");
                comparisonLog.write("CFIT not passed!");
            }
        }
    }

    /**
     * Filter for query responses in responses directories.
     */
    private final class QueryResponseFileFilter implements IOFileFilter {

        @Override
        public boolean accept(File file) {
            return isQueryResponseFile(file);
        }

        @Override
        public boolean accept(File dir, String name) {
            return false;
        }

        private boolean isQueryResponseFile(File file) {
            return !configuration.getExecutionStatsFileName().equals(file.getName())
                && !configuration.getFailedQueriesFileName().equals(file.getName());
        }
    }
}
