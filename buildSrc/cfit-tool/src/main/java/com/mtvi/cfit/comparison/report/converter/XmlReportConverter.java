package com.mtvi.cfit.comparison.report.converter;

import com.mtvi.cfit.ConversionException;
import com.mtvi.cfit.comparison.report.ComparisonReport;
import com.mtvi.cfit.util.XMLUtils;

import java.io.File;

/**
 * To XML conversion converter.
 *
 * @author Dzmitry_Zenin
 */
public class XmlReportConverter implements ReportConverter {

    @Override
    public void to(File reportFile, ComparisonReport report) throws ConversionException {
        XMLUtils.convertToXML(report, reportFile);
    }

    @Override
    public ComparisonReport from(File reportFile) throws ConversionException {
        return XMLUtils.convertFromXML(reportFile, ComparisonReport.class);
    }

}
