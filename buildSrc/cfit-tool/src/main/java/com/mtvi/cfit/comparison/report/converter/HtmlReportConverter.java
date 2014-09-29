package com.mtvi.cfit.comparison.report.converter;

import com.mtvi.cfit.CfitConfiguration;
import com.mtvi.cfit.CfitException;
import com.mtvi.cfit.ConversionException;
import com.mtvi.cfit.comparison.report.ComparisonReport;
import com.mtvi.cfit.comparison.report.QueryDifference;
import com.mtvi.cfit.comparison.report.converter.vm.DifferencesViewModel;
import com.mtvi.cfit.comparison.report.converter.vm.Link;
import com.mtvi.cfit.comparison.report.converter.vm.MainStatsViewModel;
import com.mtvi.cfit.comparison.report.converter.vm.QueriesListViewModel;
import com.mtvi.cfit.comparison.report.converter.vm.ReportViewModel;
import com.mtvi.cfit.query.common.SizeAwareIterable;
import com.mtvi.cfit.query.response.QueryResponse;
import com.mtvi.cfit.util.CfitUtils;
import com.mtvi.cfit.util.IOUtils;
import com.mtvi.cfit.util.exec.WorkProcessor;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Freemarker based to HTML converter for {@link com.mtvi.cfit.comparison.report.ComparisonReport}.
 *
 * @author Dzmitry_Zenin
 */
public class HtmlReportConverter implements ReportConverter {
    /** Model parameter name inside view.*/
    private static final String TEMPLATE_MODEL_PARAM_NAME = "model";
    /** String pattern for differences page name.*/
    private static final String DIFFERENCE_PAGE_NAME_PATTERN = "report-diff-%s.html";
    /** String pattern for failed queries page name.*/
    private static final String FAILED_Q_PAGE_NAME_PATTERN = "report-failed-%s.html";
    /** String pattern for only original queries page name.*/
    private static final String ONLY_ORIGINAL_Q_PAGE_NAME_PATTERN = "report-onlyoriginal-%s.html";
    /** String pattern for only rc queries page name.*/
    private static final String ONLY_RC_Q_PAGE_NAME_PATTERN = "report-onlyrc-%s.html";
    /** Freemarker config.*/
    private final Configuration configuration;
    /** Cfit Configuration.*/
    private final CfitConfiguration cfitConfiguration;

    /**
     * Constructor.
     * @param cfitConfiguration - cfit configuration.
     * @throws ConversionException - in case of issue with source repository of templates.
     */
    public HtmlReportConverter(CfitConfiguration cfitConfiguration) throws ConversionException {
        if (cfitConfiguration == null) {
            throw new IllegalArgumentException(
                "Null value has been passed in as required argument ['configuration' = null]");
        }

        this.cfitConfiguration = cfitConfiguration;
        this.configuration = new Configuration();
        try {
            this.configuration.setDirectoryForTemplateLoading(
                new File(cfitConfiguration.getConfigDir(), cfitConfiguration.getTemplateDirName())
            );
            this.configuration.setObjectWrapper(new DefaultObjectWrapper());
        } catch (IOException e) {
            throw new ConversionException("Error during initialization of template engine", e);
        }
    }

    @Override
    public void to(File mainViewStorage, ComparisonReport report) throws ConversionException {
        WorkProcessor<ViewHandle> workProcessor = new WorkProcessor<ViewHandle>(
            prepareViews(mainViewStorage, report),
            4,
            new SaveViewAction()
        );
        workProcessor.process();
    }

    @Override
    public ComparisonReport from(File reportFile) throws ConversionException {
        throw new UnsupportedOperationException();
    }

    private void saveView(File storage, ReportViewModel viewModel) throws ConversionException {
        Writer reportWriter = null;
        try {
            Map<String, Object> root = new HashMap<String, Object>();
            root.put(TEMPLATE_MODEL_PARAM_NAME, viewModel);

            Template template = configuration.getTemplate(resolveTemplate(viewModel));
            reportWriter = new BufferedWriter(new FileWriter(storage));
            template.process(root, reportWriter);
        } catch (Exception e) {
            throw new ConversionException(String.format("Unable to save report \"%s\"", storage), e);
        } finally {
            IOUtils.closeWriter(reportWriter);
        }
    }

    private String resolveTemplate(ReportViewModel viewModel) {
        return viewModel.getMatchedTemplate();
    }

    private SizeAwareIterable<ViewHandle> prepareViews(File mainViewStorage, ComparisonReport report) {
        final List<ViewHandle> parts = new LinkedList<ViewHandle>();
        final List<ViewHandle> diffs = getDifferenceViews(mainViewStorage, report);
        final List<ViewHandle> failed = getQueriesViewsForQueriesList("Failed Queries", mainViewStorage,
            report.getFailedQueries(), FAILED_Q_PAGE_NAME_PATTERN);
        final List<ViewHandle> onlyOriginal = getQueriesViewsForQueriesList("Only Original Queries", mainViewStorage,
            report.getOnlyOriginalResponses(), ONLY_ORIGINAL_Q_PAGE_NAME_PATTERN);
        final List<ViewHandle> onlyRC = getQueriesViewsForQueriesList("Only RC Queries", mainViewStorage,
            report.getOnlyRCResponses(), ONLY_RC_Q_PAGE_NAME_PATTERN);

        parts.addAll(diffs);
        parts.addAll(failed);
        parts.addAll(onlyOriginal);
        parts.addAll(onlyRC);

        parts.add(
            new ViewHandle(
                new MainStatsViewModel(
                    report,
                    collectLinks(diffs),
                    collectLinks(failed),
                    collectLinks(onlyOriginal),
                    collectLinks(onlyRC),
                    Collections.<Link>emptyList()
                ),
                mainViewStorage
            )
        );

        return new SizeAwareIterable<ViewHandle>() {
            @Override
            public int size() {
                return parts.size();
            }

            @Override
            public Iterator<ViewHandle> iterator() {
                return parts.iterator();
            }
        };
    }

    private List<Link> collectLinks(List<ViewHandle> handles) {
        List<Link> links = new ArrayList<Link>(handles.size());

        for (ViewHandle handle : handles) {
            links.add(new Link(handle.getViewModel().getHeader(), handle.getTargetFile()));
        }

        return links;
    }

    private List<ViewHandle> getQueriesViewsForQueriesList(final String viewName, final File mainReportFile,
        final List<QueryResponse> queryResponseList, final String viewFileNamePattern) {
        return CfitUtils.slice(
            queryResponseList,
            new QuerySliceConditions<QueryResponse>(),
            new CfitUtils.GroupSlicer<ViewHandle, QueryResponse, QueriesAccumulator<QueryResponse>>() {
                @Override
                public ViewHandle create(QueriesAccumulator<QueryResponse> accumulator) {
                    String headerName = String.format("%s-%s", accumulator.getStartNumber(),
                        accumulator.getStartNumber() + accumulator.getItemsNumber() - 1);
                    return new ViewHandle(
                        new QueriesListViewModel(headerName, viewName, accumulator.items()),
                        new File(mainReportFile.getParent(), String.format(viewFileNamePattern, headerName))
                    );
                }
            },
            new CfitUtils.AccumulatorInitializer<QueryResponse, QueriesAccumulator<QueryResponse>>() {
                @Override
                public QueriesAccumulator<QueryResponse> init(QueriesAccumulator<QueryResponse> currentAccumulator) {
                    return new QueriesAccumulator<QueryResponse>(currentAccumulator) {
                        @Override
                        protected long calculateSize(QueryResponse query) {
                            return query.getSize();
                        }
                    };
                }
            }
        );
    }

    private List<ViewHandle> getDifferenceViews(final File mainReportFile, final ComparisonReport report) {
        return CfitUtils.slice(
            report.getDifferences(),
            new QuerySliceConditions<QueryDifference>(),
            new CfitUtils.GroupSlicer<ViewHandle, QueryDifference, QueriesAccumulator<QueryDifference>>() {
                @Override
                public ViewHandle create(QueriesAccumulator<QueryDifference> accumulator) {
                    String headerName = String.format("%s-%s", accumulator.getStartNumber(),
                        accumulator.getStartNumber() + accumulator.getItemsNumber() - 1);
                    return new ViewHandle(
                        new DifferencesViewModel(headerName, accumulator.items()),
                        new File(mainReportFile.getParent(), String.format(DIFFERENCE_PAGE_NAME_PATTERN, headerName))
                    );
                }
            },
            new CfitUtils.AccumulatorInitializer<QueryDifference, QueriesAccumulator<QueryDifference>>() {
                @Override
                public QueriesAccumulator<QueryDifference> init(QueriesAccumulator<QueryDifference> currentAccumulator) {
                    return new QueriesAccumulator<QueryDifference>(currentAccumulator) {
                        @Override
                        protected long calculateSize(QueryDifference queryDifference) {
                            return queryDifference.getRcResponse().getSize() + queryDifference.getOriginalResponse().getSize();
                        }
                    };
                }
            }
        );
    }

    /** QueriesAccumulator to check for splitting view.*/
    private abstract class QueriesAccumulator<Q> implements CfitUtils.Accumulator<Q> {
        /** Start number of item.*/
        private long startNumber;
        /** Current view size of items.*/
        private long memorySize;
        /** Queries.*/
        private final List<Q> queries;

        private QueriesAccumulator(QueriesAccumulator<Q> initialQueriesAccumulator) {
            this.startNumber = initialQueriesAccumulator != null
                ? (initialQueriesAccumulator.getItemsNumber() + initialQueriesAccumulator.getStartNumber()) : 1L;
            this.queries = new ArrayList<Q>();
        }

        @Override
        public void add(Q q) {
            this.memorySize += calculateSize(q);
            this.queries.add(q);
        }

        protected abstract long calculateSize(Q q);

        @Override
        public List<Q> items() {
            return Collections.unmodifiableList(queries);
        }

        public long getStartNumber() {
            return startNumber;
        }

        public long getItemsNumber() {
            return queries.size();
        }

        public long getMemorySize() {
            return memorySize;
        }
    }

    /** QueriesAccumulator Checker to split into view.*/
    private class QuerySliceConditions<Q> implements CfitUtils.SliceConditions<Q, QueriesAccumulator<Q>> {

        @Override
        public boolean slice(QueriesAccumulator<Q> accumulator) {
            return accumulator.getMemorySize() >= cfitConfiguration.getCfitProperties().getSubReportItemsSize()
                || accumulator.getItemsNumber() >= cfitConfiguration.getCfitProperties().getSubReportItemNumber();
        }

    }

    /**
     * Save view action for {@link com.mtvi.cfit.util.exec.WorkProcessor}.
     */
    private class SaveViewAction implements WorkProcessor.Action<ViewHandle> {
        @Override
        public void doAction(ViewHandle subject) throws CfitException {
            saveView(subject.getTargetFile(), subject.getViewModel());
        }
    }

    /**
     * Internal helper class to store view with it's target storage file.
     */
    private static final class ViewHandle {
        /** View Model.*/
        private final ReportViewModel viewModel;
        /** Storage file.*/
        private final File targetFile;
        /** Constructor.*/
        private ViewHandle(ReportViewModel viewModel, File targetFile) {
            this.viewModel = viewModel;
            this.targetFile = targetFile;
        }

        public ReportViewModel getViewModel() {
            return viewModel;
        }

        public File getTargetFile() {
            return targetFile;
        }
    }

}