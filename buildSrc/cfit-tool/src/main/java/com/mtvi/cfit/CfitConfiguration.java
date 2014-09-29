package com.mtvi.cfit;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.File;

/**
 * Cfit Layout configuration.
 *
 * @author Dzmitry_Zenin
 */
public final class CfitConfiguration {

    /** Cfit properties.*/
    private CfitProperties cfitProperties = new CfitProperties();

    /** RC Phase execution results directory.*/
    private String rcResultsDir = "rc-results";
    /** Original Phase execution result directory.*/
    private String originalResultsDir = "original-results";
    /** Cfit Home location.*/
    private String cfitHome = "build";
    /** Config directory.*/
    private String configDir = "config";

    /**
     * Layout for {@link #cfitHome}
     */
    /** Reports directory name.*/
    private String reportDirName = "reports";
    /** Queries storage directory name.*/
    private String queriesDirName = "queries";

    /**
     * Layout for Phase execution.
     *
     * @see #rcResultsDir
     * @see #originalResultsDir
     */
    /** Phase execution stats file name.*/
    private String executionStatsFileName = "execution_stats.xml";
    /** Phase execution failed queries file name.*/
    private String failedQueriesFileName = "failed_queries";

    /**
     * Comparison
     */
    /** Name of report file that will be generated after comparison. html version.*/
    private String htmlReportName = "report.html";
    /** Name of report file that will be generated after comparison. xml version.*/
    private String xmlReportName = "report.xml";
    /** Name of a comparison log file.*/
    private String comparisonLogName = "comparison.log";
    /** Original content in report dir.*/
    private String reportOriginalContentDirName = "original";
    /** RC content in report dir.*/
    private String reportRcContentDirName = "rc";

    /**
     * Config.
     */
    /** Comparison config file name.*/
    private String comparisonConfigFileName = "comparison.yml";
    /** Report templates directory name. */
    private String templateDirName = "templates";

    /**
     * Cfit layout helper.
     */
    private final Layout layout = new Layout();


    public void setCfitProperties(CfitProperties cfitProperties) {
        this.cfitProperties = cfitProperties;
    }

    public CfitProperties getCfitProperties() {
        return cfitProperties;
    }

    public String getCfitHome() {
        return cfitHome;
    }

    public void setCfitHome(String cfitHome) {
        this.cfitHome = cfitHome;
    }

    public String getConfigDir() {
        return configDir;
    }

    public void setConfigDir(String configDir) {
        this.configDir = configDir;
    }

    public String getReportDirName() {
        return reportDirName;
    }

    public void setReportDirName(String reportDirName) {
        this.reportDirName = reportDirName;
    }

    public String getRcResultsDir() {
        return rcResultsDir;
    }

    public void setRcResultsDir(String rcResultsDir) {
        this.rcResultsDir = rcResultsDir;
    }

    public String getComparisonConfigFileName() {
        return comparisonConfigFileName;
    }

    public void setComparisonConfigFileName(String comparisonConfigFileName) {
        this.comparisonConfigFileName = comparisonConfigFileName;
    }

    public String getTemplateDirName() {
        return templateDirName;
    }

    public void setTemplateDirName(String templateDirName) {
        this.templateDirName = templateDirName;
    }

    public String getQueriesDirName() {
        return queriesDirName;
    }

    public void setQueriesDirName(String queriesDirName) {
        this.queriesDirName = queriesDirName;
    }

    public String getComparisonLogName() {
        return comparisonLogName;
    }

    public void setComparisonLogName(String comparisonLogName) {
        this.comparisonLogName = comparisonLogName;
    }

    public String getXmlReportName() {
        return xmlReportName;
    }

    public void setXmlReportName(String xmlReportName) {
        this.xmlReportName = xmlReportName;
    }

    public String getHtmlReportName() {
        return htmlReportName;
    }

    public void setHtmlReportName(String htmlReportName) {
        this.htmlReportName = htmlReportName;
    }

    public String getFailedQueriesFileName() {
        return failedQueriesFileName;
    }

    public void setFailedQueriesFileName(String failedQueriesFileName) {
        this.failedQueriesFileName = failedQueriesFileName;
    }

    public String getExecutionStatsFileName() {
        return executionStatsFileName;
    }

    public void setExecutionStatsFileName(String executionStatsFileName) {
        this.executionStatsFileName = executionStatsFileName;
    }

    public String getOriginalResultsDir() {
        return originalResultsDir;
    }

    public void setOriginalResultsDir(String originalResultsDir) {
        this.originalResultsDir = originalResultsDir;
    }

    public String getReportRcContentDirName() {
        return reportRcContentDirName;
    }

    public void setReportRcContentDirName(String reportRcContentDirName) {
        this.reportRcContentDirName = reportRcContentDirName;
    }

    public String getReportOriginalContentDirName() {
        return reportOriginalContentDirName;
    }

    public void setReportOriginalContentDirName(String reportOriginalContentDirName) {
        this.reportOriginalContentDirName = reportOriginalContentDirName;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    /**
     * Gets a layout helper.
     * @return - layout helper.
     */
    public Layout layout() {
        return this.layout;
    }

    /**
     * Layout helper instance.
     */
    public final class Layout {

        /** Private constructor.*/
        private Layout() { }

        /**
         * Returns cfit config directory.
         * @return - cfit config directory.
         */
        public File configDir() {
            return new File(getConfigDir());
        }

        /**
         * Returns cfit comparison config.
         * @return - cfit comparison config.
         */
        public File comparisonConfig() {
            return relative(configDir(), getComparisonConfigFileName());
        }

        /**
         * Returns cfit original execution stats.
         * @return - cfit original execution stats.
         */
        public File originalExecutionStats() {
            return new File(originalResultsDir(), getExecutionStatsFileName());
        }

        /**
         * Returns cfit rc execution stats.
         * @return - cfit rc execution stats.
         */
        public File rcExecutionStats() {
            return new File(rcResultsDir(), getExecutionStatsFileName());
        }

        /**
         * Returns cfit original results dir.
         * @return - cfit original results dir.
         */
        public File originalResultsDir() {
            return new File(getOriginalResultsDir());
        }

        /**
         * Returns cfit rc results dir.
         * @return - cfit rc results dir.
         */
        public File rcResultsDir() {
            return new File(getRcResultsDir());
        }

        /**
         * Returns cfit reports dir.
         * @return - cfit reports dir.
         */
        public File reportsDir() {
            return rootResource(getReportDirName());
        }

        /**
         * Returns html comparison report.
         * @return - html comparison report.
         */
        public File htmlReport() {
            return relative(reportsDir(), getHtmlReportName());
        }

        /**
         * Returns xml comparison report.
         * @return - xml comparison report.
         */
        public File xmlReport() {
            return relative(reportsDir(), getXmlReportName());
        }

        /**
         * Returns Directory that contain content from original phase in report dir.
         * @return - original report contents dir.
         */
        public File reportOriginalContentDir() {
            return relative(reportsDir(), getReportOriginalContentDirName());
        }

        /**
         * Returns Directory that contain content from rc phase in report dir.
         * @return - rc report contents dir.
         */
        public File reportRcContentDir() {
            return relative(reportsDir(), getReportRcContentDirName());
        }

        /**
         * Returns comparison log.
         * @return - comparison log.
         */
        public File comparisonLog() {
            return relative(reportsDir(), getComparisonLogName());
        }

        private File rootResource(String resource) {
            return relative(new File(getCfitHome()), resource);
        }

        private File relative(File root, String resource) {
            return new File(root, resource);
        }

    }

}
