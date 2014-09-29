package com.mtvi.cfit.comparison.report;

import com.mtvi.cfit.CfitConfiguration;
import com.mtvi.cfit.comparison.report.converter.HtmlReportConverter;
import com.mtvi.cfit.comparison.report.converter.XmlReportConverter;
import com.mtvi.cfit.query.definition.QueryDefinition;
import com.mtvi.cfit.query.response.QueryResponse;
import com.mtvi.cfit.util.IOUtils;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Tests for {@link ReportBuilder}.
 *
 * @author Dzmitry_Zenin
 */
public class ComparisonReportConversionTest {

    private CfitConfiguration configuration;

    @Before
    public void init() throws Exception {
        configuration = new CfitConfiguration();

        File sampleResult = new File(ExecutionResultManagerTest.class.getResource("/result/sample_execution_result.xml").getPath());
        configuration.setCfitHome(sampleResult.getParentFile().getParentFile().getAbsolutePath());
        configuration.setQueriesDirName("queries");
        configuration.setRcResultsDir(sampleResult.getParent());
        configuration.getCfitProperties().setRcArtifactVersion("1.20.3-RC1");
        configuration.setExecutionStatsFileName("sample_execution_result.xml");
        configuration.setConfigDir(new File(getClass().getResource("/sample/").getPath()).getAbsolutePath());
    }

    @Test
    public void testHtmlReport() throws Exception {
        ComparisonReport originalReport = buildReport();

        HtmlReportConverter converter = new HtmlReportConverter(configuration);

        File report = new File(getClass().getResource("/").getPath(), "report_test.html");
        System.out.println("Saving report to " + report.getAbsolutePath());
        converter.to(report, originalReport);
    }

    @Test
    public void testXmlReport() throws Exception {
        ComparisonReport originalReport = buildReport();

        XmlReportConverter converter = new XmlReportConverter();

        File report = new File(getClass().getResource("/").getPath() +"/report_test.xml");
        System.out.println("Saving report to " + report.getAbsolutePath());
        converter.to(report, originalReport);

        ComparisonReport comparisonReport = new ReportBuilder(configuration).load(report, ReportBuilder.Format.XML);
        Assert.assertNotNull(comparisonReport);
        Assert.assertEquals(ReportState.HAVE_DIFFERENCES, comparisonReport.getReportState());
        Assert.assertNotNull(comparisonReport.getRcPerformanceStats());
        Assert.assertEquals("1.14", comparisonReport.getOriginalVersion());
        Assert.assertNotNull(comparisonReport.getOriginalPerformanceStats());
        Assert.assertEquals(originalReport.getOriginalPerformanceStats(), comparisonReport.getOriginalPerformanceStats());
    }

    private ComparisonReport buildReport() throws Exception {
        PerformanceStatistic oldStats = new PerformanceStatistic(1345, 100, 20, 35, 15);
        PerformanceStatistic rcStats = new PerformanceStatistic(1345, 109, 21, 36, 18);

        List<QueryDifference> differencesList = new LinkedList<QueryDifference>();

        String query = "http://shared-crx-001.1515.mtvi.com:12042/jp/gametrailers.com?q=%7B%22select%22%3A%7B%22mtvi%3Aid%22%3A1%2C%22mtvi%3AcontentType%22%3A1%2C%22OriginalPublishDate%22%3A1%7D%2C%22vars%22%3A%7B%7D%2C%22where%22%3A%7B%22byTypeAnd1LinkParamAndOriginalPublishDateRangeAndExcludeLinks%22%3A%5B%5B%22Standard%3AVideo%22%2C+%22Standard%3AShowVideo%22%2C+%22Standard%3AEpisode%22%5D%2C+%5B%2297b46494-442e-41ea-bcfd-3323cce6e099%22%2C+%2267bc2e80-69f9-102e-ae68-0026b9419ed3%22%2C+%22a9786e09-8aeb-4f3f-acda-568271d476ea%22%2C+%22c069590c-e6a7-11e0-9921-a4badb23230a%22%2C+%22d4986c9c-8bb4-4e60-9a86-372e14ad6086%22%5D%2C+%222013-07-25T04%3A00%3A00.000Z%22%2C+%222013-07-26T04%3A00%3A00.000Z%22%2C+%5B%5D%5D%7D%2C%22order%22%3A%22OriginalPublishDateDesc%22%2C%22start%22%3A0%2C%22rows%22%3A1000%2C%22omitNumFound%22%3Atrue%2C%22debug%22%3A%7B++%7D%7D&stage=live&filterSchedules=true&dateFormat=UTC&indent=true&plugin.timeTravel=2013-07-30T04%3A00%3A00.000Z&timeTravel=2013-07-30T04%3A00%3A00.000Z";
        String requestId = "access.log-20111214_024592";
        QueryDefinition definition = new QueryDefinition(requestId, query);
        differencesList.add(new QueryDifference(definition,
                new QueryResponse(definition, query, getResourceAsString("/sample/original-results/req1_001"), 200, 0),
                new QueryResponse(definition, query, getResourceAsString("/sample/rc-results/req1_001"), 200, 0)
            )
        );

        differencesList.add(new QueryDifference(new QueryDefinition(requestId + "3", query),
                new QueryResponse(definition, query, "<html><head>\n2</head></html>", 200, 0),
                new QueryResponse(definition, query, "<html><head>2</head></html>", 200, 0)));

        ComparisonStats stats = new ComparisonStats(45, 4, 5, 0, 0);
        ComparisonReport report = new ComparisonReport(differencesList, oldStats, rcStats, new Date(), stats);
        report.setRcVersion("1.15.3");
        report.setOriginalVersion("1.14");

        String requestIdOnlyOriginal = "access.log-20111214_024593";
        String onlyOriginalUrl = "http://udat.mtvnservices-d.mtvi.com/service1/dispatch.htm?tid=1672&feed.ttl=1&plugin.removeBrokenOrExpiredLinks=true&plugin.stage=authoring&mgid=mgid:arc:content:spike.com:a781b2e7-a823-446d-b846-5e2e68d706ed&depth=2&types=Standard:Image,Standard:ImageAssetRef&plugin.env=CFIT2&indent=true&onlyOriginal=true";
        QueryDefinition onlyOriginalDef = new QueryDefinition(requestIdOnlyOriginal, onlyOriginalUrl);
        report.addOnlyOriginalResponse(new QueryResponse(onlyOriginalDef, onlyOriginalUrl, "<html><head></head></html>", 200, 0));

        String requestIdOnlyRC = "access.log-20111214_024594";
        String onlyRCUrl = "http://udat.mtvnservices-d.mtvi.com/service1/dispatch.htm?tid=1672&feed.ttl=1&plugin.removeBrokenOrExpiredLinks=true&plugin.stage=authoring&mgid=mgid:arc:content:spike.com:a781b2e7-a823-446d-b846-5e2e68d706ed&depth=2&types=Standard:Image,Standard:ImageAssetRef&plugin.env=CFIT2&indent=true&onlyRC=true";
        QueryDefinition onlyRCDef = new QueryDefinition(requestIdOnlyRC, onlyRCUrl);
        report.addOnlyRCResponse(new QueryResponse(onlyRCDef, onlyRCUrl, "<html><head></head></html>", 200, 0));


        report.addFailedQuery(new QueryResponse(null, query, null, 500, 0));

        report.addSlowQuery(new ComparisonReport.SlowQuery("_id1", query, 20, 31));
        report.addSlowQuery(new ComparisonReport.SlowQuery("_id1", "http://udat.mtvnservices-d.mtvi.com/service1/dispatch.htm?", 21, 33));
        report.addSlowQuery(new ComparisonReport.SlowQuery("_id1", "http://udat.mtvnservices-d.mtvi.com/service1/dispatch.htm?", 19, 25));

        return report;
    }

    private String getResourceAsString(String path) throws IOException {
        URL resourceURL = ComparisonReportConversionTest.class.getResource(path);
        return IOUtils.getContent(new File(resourceURL.getFile()), 1);
    }

}
