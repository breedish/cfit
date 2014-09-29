package com.mtvi.cfit.comparison.report;

import com.mtvi.cfit.CfitConfiguration;
import com.mtvi.cfit.ConversionException;
import com.mtvi.cfit.comparison.report.converter.HtmlReportConverter;
import com.mtvi.cfit.comparison.report.converter.ReportConverter;
import com.mtvi.cfit.comparison.report.converter.XmlReportConverter;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Dzmitry_Zenin
 */
public class ReportBuilder {
    /** Report type.*/
    public static enum Format {
        /** XML report type.*/
        XML,
        /** HTML report type.*/
        HTML
    }

    /** Registered converters.*/
    private final Map<Format, ReportConverter> converterMap;

    /**
     * Constructor.
     * @param configuration - configuration.
     * @throws ConversionException - in case of issue during initialization of converters.
     */
    public ReportBuilder(CfitConfiguration configuration) throws ConversionException {
        final ReportConverter htmlConverter = new HtmlReportConverter(configuration);
        this.converterMap = new HashMap<Format, ReportConverter>() {
            {
                put(Format.XML, new XmlReportConverter());
                put(Format.HTML, htmlConverter);
            }
        };
    }

    /**
     * Builds report.
     *
     * @param storageFile - target report storage file.
     * @param report - report instance.
     * @param reportFormat - target report format type.
     * @throws ConversionException - in case of issue during report generation.
     */
    public void buildReport(ComparisonReport report, File storageFile, Format reportFormat) throws ConversionException {
        ReportConverter converter = converterMap.get(reportFormat);

        if (converter == null) {
            throw new ConversionException(String.format("%s report format is not supported", reportFormat.name()));
        }

        converter.to(storageFile, report);
    }

    /**
     * Does conversion of saved report version to domain object.
     * <p>Conversion is done only partially. It will not include diffs.
     *
     * @param storageFile - report source file.
     * @param reportFormat - report format type.
     * @return - comparison report.
     * @throws ConversionException
     */
    public ComparisonReport load(File storageFile, Format reportFormat) throws ConversionException {
        ReportConverter converter = converterMap.get(reportFormat);

        if (converter == null) {
            throw new ConversionException(String.format("%s report format is not supported", reportFormat.name()));
        }

        return converter.from(storageFile);
    }

}
