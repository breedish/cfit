package com.mtvi.cfit.comparison.report.converter;

import com.mtvi.cfit.ConversionException;
import com.mtvi.cfit.comparison.report.ComparisonReport;

import java.io.File;

/**
 * Report conversion entity interface.
 *
 * @author Dzmitry_Zenin
 */
public interface ReportConverter {

    /**
     * Does conversion of given report to target storage file.
     * @param reportFile - cfit report.
     * @param report - storage file.
     * @throws ConversionException in case of conversion issues.
     */
    void to(File reportFile, ComparisonReport report) throws ConversionException;

    /**
     * Does conversion from given source storage.
     * @param reportFile - source storage.
     * @return - comparison report.
     * @throws ConversionException in case of conversion issues.
     */
    ComparisonReport from(File reportFile) throws ConversionException;

}
