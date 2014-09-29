package com.mtvi.cfit.comparison.report;

import com.google.common.base.Objects;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Performance statistics for execution of {@link com.mtvi.cfit.Cfit#run(java.io.File)} phase.
 *
 * @author Dzmitry_Zenin.
 */
public final class PerformanceStatistic {

    /** Number of total successful requests.*/
    @XmlAttribute
    private int success;
    /**
     * Quantity of failed queries. Failed query means that it was successfully sent to the server,
     * but response code was not successful.
     */
    @XmlAttribute
    private int failed;
    /** Average query execution time(ms).*/
    @XmlAttribute
    private int avg;
    /** Max query execution time(ms).*/
    @XmlAttribute
    private int max;
    /** Min query execution time(ms).*/
    @XmlAttribute
    private int min;

    /** Default constructor.*/
    @Deprecated
    private PerformanceStatistic() { }
    /**
     * Constructor.
     *
     * @param success - number of successfully executed queries.
     * @param failed - number of failed queries.
     * @param avg - average execution time for all queries in a set.
     * @param max - max value of execution time of a query in a set of queries.
     * @param min - max value of execution time of a query in a set of queries.
     */
    public PerformanceStatistic(int success, int failed, int avg, int max, int min) {
        this.success = success;
        this.avg = avg;
        this.max = max;
        this.min = min;
        this.failed = failed;
    }

    public int getSuccess() {
        return success;
    }

    public int getAvg() {
        return avg;
    }

    public int getMax() {
        return max;
    }

    public int getMin() {
        return min;
    }

    public int getFailed() {
        return failed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PerformanceStatistic that = (PerformanceStatistic) o;

        return this.success == that.success
            && this.failed == that.failed
            && this.avg == that.avg
            && this.max == that.max
            && this.min == that.min;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(success, failed, avg, max, min);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
            .add("success", success)
            .add("failed", failed)
            .add("avg", avg)
            .add("max", max)
            .add("min", min)
            .toString();
    }
}
