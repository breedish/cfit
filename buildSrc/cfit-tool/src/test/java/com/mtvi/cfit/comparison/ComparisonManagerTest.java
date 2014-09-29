package com.mtvi.cfit.comparison;

import com.mtvi.cfit.CfitConfiguration;
import com.mtvi.cfit.comparison.report.ComparisonReport;
import com.mtvi.cfit.comparison.report.ReportState;
import com.mtvi.cfit.query.response.ToFileQueryResponseManager;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for {@link com.mtvi.cfit.comparison.FileBasedComparisonManager}.
 *
 * @author zenind
 */
public class ComparisonManagerTest extends Assert {

    /** Comparison manager.*/
    private static FileBasedComparisonManager MANAGER;

    @BeforeClass
    public static void init() throws Exception {
        CfitConfiguration configuration = new CfitConfiguration();
        configuration.setCfitHome(FileBasedComparisonManager.class.getResource("/sample/").getPath());
        configuration.setConfigDir(ComparisonManagerTest.class.getResource("/sample").getFile());
        configuration.setOriginalResultsDir(ComparisonManagerTest.class.getResource("/sample/original-results").getPath());
        configuration.setRcResultsDir(ComparisonManagerTest.class.getResource("/sample/rc-results").getPath());
        configuration.setReportDirName("reports");
        configuration.setHtmlReportName("test-report.html");
        configuration.setXmlReportName("test-report.xml");
        configuration.setComparisonLogName("comparison.log");
        configuration.getCfitProperties().setRcArtifactVersion("1.20.x");

        MANAGER = new FileBasedComparisonManager(configuration, new ToFileQueryResponseManager());
    }

    @Test
    public void testComparisonOfFolders() throws Exception {
        ComparisonReport report = MANAGER.compareResponses();

        assertNotNull(report);
        assertEquals(report.getReportState(), ReportState.HAVE_DIFFERENCES);
        assertEquals(1, report.getFailedQueries().size());
    }

}
