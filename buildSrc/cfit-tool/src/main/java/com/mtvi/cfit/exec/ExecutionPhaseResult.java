package com.mtvi.cfit.exec;

import com.google.common.base.Objects;
import com.mtvi.cfit.comparison.report.PerformanceStatistic;
import com.mtvi.cfit.query.response.QueryResponse;
import com.mtvi.cfit.util.CfitUtils;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Execution result storage/ implementation.
 *
 * @author zenind
 */
@XmlRootElement(name = "execution-result")
public class ExecutionPhaseResult {

    /** Map with stats for responses based on status code.*/
    @XmlElement
    @XmlJavaTypeAdapter(ExecutionResultManager.ByStatusResponseGroupAdapter.class)
    private Map<Integer, ByStatusResponseGroup> groups;
    /** Number of responses with response code treated as success.*/
    @XmlAttribute
    @XmlJavaTypeAdapter(ExecutionResultManager.AtomicIntegerAdapter.class)
    private AtomicInteger success;
    /** Number of responses with response code treated as failure.*/
    @XmlAttribute
    @XmlJavaTypeAdapter(ExecutionResultManager.AtomicIntegerAdapter.class)
    private AtomicInteger failures;
    /** Number of queries that did not finish due to an exception.*/
    @XmlAttribute
    @XmlJavaTypeAdapter(ExecutionResultManager.AtomicIntegerAdapter.class)
    private AtomicInteger exceptions;
    /** Total number of executed of queries.*/
    @XmlAttribute
    @XmlJavaTypeAdapter(ExecutionResultManager.AtomicIntegerAdapter.class)
    private AtomicInteger count;
    /** Total number of queries to run.*/
    @XmlAttribute
    private int total;

    /**
     * Default constructor.
     */
    @Deprecated
    public ExecutionPhaseResult() {
        this(0);
    }


    /**
     * Default constructor.
     *
     * @param total - Total number of queries to run.
     */
    public ExecutionPhaseResult(int total) {
        this.groups = new ConcurrentHashMap<Integer, ByStatusResponseGroup>();
        this.success = new AtomicInteger();
        this.failures = new AtomicInteger();
        this.exceptions = new AtomicInteger();
        this.count = new AtomicInteger();
        this.total = total;
    }

    /**
     * Registers query response.
     *
     * @param response - query response.
     */
    public void registerResponse(QueryResponse response) {
        if (response == null) {
            return;
        }

        //Mark response based on it's response status.
        markResponse(response);
        // Update execution result with values/stats provided in response.
        processResponse(response);
    }

    /** Register error.*/
    public void registerError() {
        exceptions.incrementAndGet();
        markRegistered();
    }

    private void markResponse(QueryResponse response) {
        if (isSuccessStatus(response.getStatus())) {
            markSuccess();
        } else {
            markFailed();
        }
    }

    private boolean isSuccessStatus(int status) {
        return CfitUtils.isSuccessStatus(status);
    }

    private void processResponse(QueryResponse response) {
        Integer status = response.getStatus();
        if (!groups.containsKey(response.getStatus())) {
            groups.put(status, new ByStatusResponseGroup(status));
        }
        ByStatusResponseGroup byStatusResponseGroup = groups.get(status);
        byStatusResponseGroup.addResponse(response);
    }

    private void markSuccess() {
        success.incrementAndGet();
        markRegistered();
    }

    private void markFailed() {
        failures.incrementAndGet();
        markRegistered();
    }

    private void markRegistered() {
        count.incrementAndGet();
    }

    public int getCount() {
        return count.get();
    }

    public int getSuccess() {
        return success.get();
    }

    public int getFailures() {
        return failures.get();
    }

    public int getTotal() {
        return total;
    }

    public Map<Integer, ByStatusResponseGroup> getGroups() {
        return groups;
    }

    public int getExceptions() {
        return exceptions.get();
    }

    /**
     * Builds performance stats aggregation for execution run.
     * <p>
     *     Collects information only from successfully executed response.
     * </p>
     * @return - performance statistics.
     */
    public PerformanceStatistic getPerformanceStats() {
        int min = 0;
        int max = 0;
        int avg = 0;

        for (ByStatusResponseGroup group : groups.values()) {
            if (isSuccessStatus(group.getStatus())) {
                min = group.getMin();
                max = group.getMax();
                avg = group.getAvg();
            }
        }

        return new PerformanceStatistic(getSuccess(), getFailures(), avg, max, min);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ExecutionPhaseResult result = (ExecutionPhaseResult) o;

        return Objects.equal(success.get(), result.success.get()) && Objects.equal(groups, result.groups)
            && Objects.equal(failures.get(), result.failures.get()) && Objects.equal(exceptions.get(), result.exceptions.get())
            && Objects.equal(count.get(), result.count.get()) && total == result.total;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(success, groups, failures, exceptions, count, total);
    }
}


