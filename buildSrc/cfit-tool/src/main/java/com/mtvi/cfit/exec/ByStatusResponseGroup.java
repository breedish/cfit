package com.mtvi.cfit.exec;

import com.google.common.base.Objects;
import com.mtvi.cfit.query.response.QueryResponse;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Accumulator of responses stats grouped by response code.
 *
 * @author Dzmitry_Zenin
 */
public class ByStatusResponseGroup {

    /** Group alias, represented by status code value.*/
    @XmlAttribute
    private Integer status;
    /** Max response time in group.*/
    @XmlAttribute
    private int max;
    /** Min response time in group.*/
    @XmlAttribute
    private int min;
    /** Total execution time for all requests in group.*/
    @XmlAttribute
    private int totalTime;
    /** Total quantity of responses.*/
    @XmlAttribute
    private int size;

    /** Default constructor.*/
    @Deprecated
    public ByStatusResponseGroup() { }

    /**
     * Constructor.
     * @param status - status code alias.
     */
    public ByStatusResponseGroup(Integer status) {
        if (status == null) {
            throw new IllegalArgumentException(
                "Null value has been passed in as required argument ['status'= null]");
        }

        this.status = status;
        this.min = Integer.MAX_VALUE;
        this.max = 0;
    }

    /**
     * Add response.
     *
     * @param response - query response.
     */
    public synchronized void addResponse(QueryResponse response) {
        long time = response.getTime();
        min = (int) Math.min(min, time);
        max = (int) Math.max(max, time);
        totalTime += time;
        size++;
    }

    /**
     * @return the max
     */
    public int getMax() {
        return max;
    }

    /**
     * @return the min
     */
    public int getMin() {
        return min;
    }

    /**
     * @return the avg response time.
     */
    public int getAvg() {
        if (size == 0) {
            return 0;
        }
        return totalTime / size;
    }

    /**
     * @return the size
     */
    public int getSize() {
        return size;
    }

    /**
     * @return the alias
     */
    public Integer getStatus() {
        return status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ByStatusResponseGroup group = (ByStatusResponseGroup) o;

        return Objects.equal(status, group.status);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(status);
    }
}
