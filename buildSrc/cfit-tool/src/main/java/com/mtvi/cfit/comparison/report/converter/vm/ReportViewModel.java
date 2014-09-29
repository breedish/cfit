package com.mtvi.cfit.comparison.report.converter.vm;

/**
 * Common interface for all view models of {@link com.mtvi.cfit.comparison.report.ComparisonReport}.
 *
 * @author zenind
 */
public interface ReportViewModel {

    /**
     * @return name of a matched template for rendering.
     */
    String getMatchedTemplate();

    /**
     * @return header name of a view model.
     */
    String getHeader();

}
