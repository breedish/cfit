package com.mtvi.cfit.comparison.report.converter.vm;

import com.mtvi.cfit.query.response.QueryResponse;

import java.util.List;

/**
 * View Model for QueryResponse List views.
 *
 * @author zenind
 */
public class QueriesListViewModel implements ReportViewModel {
    /** Template name.*/
    private static final String TEMPLATE_NAME = "report-queries-list.html";
    /** Query response list.*/
    private final List<QueryResponse> queryResponseList;
    /** View name.*/
    private final String name;
    /** Header name.*/
    private final String header;

    /**
     * Constructor.
     * @param header - header name.
     * @param name - view name.
     * @param queryResponseList - queries list.
     */
    public QueriesListViewModel(String header, String name, List<QueryResponse> queryResponseList) {
        this.header = header;
        this.name = name;
        this.queryResponseList = queryResponseList;
    }

    @Override
    public String getMatchedTemplate() {
        return TEMPLATE_NAME;
    }

    @Override
    public String getHeader() {
        return header;
    }

    public String getName() {
        return name;
    }

    public List<QueryResponse> getQueryResponseList() {
        return queryResponseList;
    }
}
