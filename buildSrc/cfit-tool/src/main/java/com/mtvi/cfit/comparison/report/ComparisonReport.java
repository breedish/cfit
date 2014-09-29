package com.mtvi.cfit.comparison.report;

import com.mtvi.cfit.util.CfitUtils;
import com.mtvi.cfit.query.response.QueryResponse;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * Cfit comparison report entity.
 *
 * @author Dzmitry_Zenin
 */
@SuppressWarnings("unused")
@XmlRootElement(name = "cfit-report")
@XmlAccessorType(XmlAccessType.NONE)
public class ComparisonReport {
    /** Report state.*/
    @XmlAttribute(name = "state")
    private ReportState reportState;
    /** Report creation date.*/
    @XmlAttribute(name = "date")
    private Date creationDate;
    /** Version of RC artifact.*/
    @XmlAttribute(name = "rc-version")
    private String rcVersion;
    /** Version of original artifact.*/
    @XmlAttribute(name = "original-version")
    private String originalVersion;
    /** Original stats.*/
    @XmlElement(name = "original-stats")
    private PerformanceStatistic originalPerformanceStats;
    /** RC stats.*/
    @XmlElement(name = "rc-stats")
    private PerformanceStatistic rcPerformanceStats;
    /** List with diffs.*/
    private List<QueryDifference> differences;
    /** List of responses of queries processed only on original phase.*/
    private List<QueryResponse> onlyOriginalResponses;
    /** List of responses of queries processed only on rc phase.*/
    private List<QueryResponse> onlyRCResponses;
    /** List of failed queries on RC phase.*/
    private List<QueryResponse> failedQueries;
    /** List of slow queries on rc phase compared to original. */
    private List<SlowQuery> performanceQueries;
    /** Comparison stats.*/
    @XmlElement(name = "comparison-stats")
    private ComparisonStats comparisonStats;

    /** Default constructor.*/
    @Deprecated
    private ComparisonReport() { }

    /**
     * Constructor.
     *
     * @param originalPerformanceStats - original performance stats.
     * @param rcPerformanceStats - rc performance stats.
     * @param creationDate - creation date.
     */
    public ComparisonReport(PerformanceStatistic originalPerformanceStats, PerformanceStatistic rcPerformanceStats,
        Date creationDate) {
        this(new LinkedList<QueryDifference>(), originalPerformanceStats, rcPerformanceStats, creationDate, new ComparisonStats());
    }

    /**
     * Constructor.
     * @param differences - list of diffs.
     * @param originalPerformanceStats - original performance stats.
     * @param rcPerformanceStats - rc performance stats.
     * @param creationDate - creation date.
     * @param stats - comparison stats.
     */
    ComparisonReport(List<QueryDifference> differences, PerformanceStatistic originalPerformanceStats,
        PerformanceStatistic rcPerformanceStats, Date creationDate, ComparisonStats stats) {
        this.differences = differences;
        this.originalPerformanceStats = originalPerformanceStats;
        this.rcPerformanceStats = rcPerformanceStats;
        this.reportState = differences.size() > 0 ? ReportState.HAVE_DIFFERENCES : ReportState.NO_DIFFERENCES;
        this.creationDate = creationDate;
        this.onlyOriginalResponses = new ArrayList<QueryResponse>();
        this.onlyRCResponses = new ArrayList<QueryResponse>();
        this.failedQueries = new ArrayList<QueryResponse>();
        this.comparisonStats = stats;
        this.performanceQueries = new ArrayList<SlowQuery>();
    }

    public ReportState getReportState() {
        return reportState;
    }

    /**
     * Adds difference.
     * @param difference - difference.
     */
    public void addDifference(QueryDifference difference) {
        differences.add(difference);
        reportState = ReportState.HAVE_DIFFERENCES;
        this.comparisonStats.registerDifferent();
    }

    /**
     * Adds only original response.
     * @param response - response.
     */
    public void addOnlyOriginalResponse(QueryResponse response) {
        if (response != null) {
            this.onlyOriginalResponses.add(response);
            this.comparisonStats.registerOnlyOriginal();
        }
    }

    /**
     * Adds only rc response.
     * @param response - response.
     */
    public void addOnlyRCResponse(QueryResponse response) {
        if (response != null) {
            this.onlyRCResponses.add(response);
            this.comparisonStats.registerOnlyRC();
        }
    }

    /**
     * Adds failed response.
     * @param response - response.
     */
    public void addFailedQuery(QueryResponse response) {
        if (response != null) {
            this.failedQueries.add(response);
        }
    }

    /**
     * Adds slow query.
     * @param query - slow query response.
     */
    public void addSlowQuery(SlowQuery query) {
        this.performanceQueries.add(query);
    }

    public PerformanceStatistic getOriginalPerformanceStats() {
        return originalPerformanceStats;
    }

    public PerformanceStatistic getRcPerformanceStats() {
        return rcPerformanceStats;
    }

    public List<QueryDifference> getDifferences() {
        return Collections.unmodifiableList(differences);
    }

    public List<QueryResponse> getOnlyOriginalResponses() {
        return Collections.unmodifiableList(onlyOriginalResponses);
    }

    public List<QueryResponse> getOnlyRCResponses() {
        return Collections.unmodifiableList(onlyRCResponses);
    }

    public List<QueryResponse> getFailedQueries() {
        return Collections.unmodifiableList(failedQueries);
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public ComparisonStats getComparisonStats() {
        return comparisonStats;
    }

    public List<SlowQuery> getPerformanceQueries() {
        return this.performanceQueries;
    }

    @SuppressWarnings("unused")
    public boolean isSuccess() {
        return this.reportState == ReportState.NO_DIFFERENCES;
    }

    public String getRcVersion() {
        return rcVersion;
    }

    public void setRcVersion(String rcVersion) {
        this.rcVersion = rcVersion;
    }

    public String getOriginalVersion() {
        return originalVersion;
    }

    public void setOriginalVersion(String originalVersion) {
        this.originalVersion = originalVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ComparisonReport that = (ComparisonReport) o;

        return EqualsBuilder.reflectionEquals(this, that);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    /**
     * Slow query info class.
     */
    public static final class SlowQuery {
        /** Query Id.*/
        private final String id;
        /** Query url.*/
        private final String query;
        /** Original execution time value.*/
        private final long originalValue;
        /** RC execution time value.*/
        private final long rcValue;
        /** Execution time growth rate (in percents).*/
        private final long percent;

        /**
         * Constructor.
         * @param id - query id.
         * @param query - query url.
         * @param originalValue - original value.
         * @param rcValue - rc value.
         */
        public SlowQuery(String id, String query, long originalValue, long rcValue) {
            this.id = id;
            this.query = query;
            this.originalValue = originalValue;
            this.rcValue = rcValue;
            this.percent = CfitUtils.growthRate(originalValue, rcValue);
        }

        public String getId() {
            return id;
        }

        public String getQuery() {
            return query;
        }

        public long getPercent() {
            return percent;
        }

        public long getOriginalValue() {
            return originalValue;
        }

        public long getRcValue() {
            return rcValue;
        }
    }
}
