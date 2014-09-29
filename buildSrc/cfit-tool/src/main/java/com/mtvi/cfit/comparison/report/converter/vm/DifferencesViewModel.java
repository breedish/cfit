package com.mtvi.cfit.comparison.report.converter.vm;

import com.mtvi.cfit.comparison.report.QueryDifference;

import java.util.List;

/**
 * Class DifferencesViewModel.
 *
 * @author zenind
 */
public class DifferencesViewModel implements ReportViewModel {
    /** Template name.*/
    private static final String TEMPLATE_NAME = "report-diff.html";
    /** View header name|description.*/
    private final String header;
    /** Differences.*/
    private final List<QueryDifference> differences;

    /**
     * Constructor.
     * @param header - header name.
     * @param differences - list of differences.
     */
    public DifferencesViewModel(String header, List<QueryDifference> differences) {
        if (header == null || differences == null) {
            throw new IllegalArgumentException(
                String.format("Null value has been passed in as required argument ['header'=%s, 'differences'=%s]", header, differences));
        }

        this.header = header;
        this.differences = differences;
    }

    @Override
    public String getMatchedTemplate() {
        return TEMPLATE_NAME;
    }

    public String getHeader() {
        return header;
    }

    public List<QueryDifference> getDifferences() {
        return differences;
    }
}
