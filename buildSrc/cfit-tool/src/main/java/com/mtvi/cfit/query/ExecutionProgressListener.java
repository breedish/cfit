package com.mtvi.cfit.query;

import com.google.common.io.Files;
import com.mtvi.cfit.CfitConfiguration;
import com.mtvi.cfit.exec.ByStatusResponseGroup;
import com.mtvi.cfit.exec.ExecutionPhaseResult;
import com.mtvi.cfit.query.response.QueryResponse;
import com.mtvi.cfit.util.CfitUtils;
import com.mtvi.cfit.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;

/**
 * Query processing listener.
 *
 * @author zenind
 */
public class ExecutionProgressListener {

    /** Logger.*/
    private static final Logger LOG = LoggerFactory.getLogger(ExecutionProgressListener.class);
    /**
     *  Log message format for unrecognizable query.
     *  <p>
     *      Format:<br/>
     *      [${QUERY_STATUS}] ${QUERY_URL} \n ${EXCEPTION}
     *  </p>
     */
    private static final String FAILED_QUERY_LOG_FORMAT = "[%s] %s\n %s";
    /**
     *  Log message format for execution progress.
     *  <p>
     *  Format represents the following structure:<br/>
     *  ${CURRENT} / ${TOTAL}   ${NUM_SUCCESS} | ${NUM_FAILED} | ${NUM_NOT_RUN}  ${QUERY_ID}  [${STATUS_CODE}]    ${TIME} ms    ${RESPONSE_SIZE} Kb
     *  <br/>
     *  Where:
     *  <ul>
     *      <li>CURRENT - current index of executable query</li>
     *      <li>TOTAL - total number of queries to execute</li>
     *      <li>NUM_SUCCESS - total number of queries with successful response code</li>
     *      <li>NUM_FAILED - total number of queries with no successful response code</li>
     *      <li>NUM_NOT_RUN - total number of queries that were not executed due to some exception</li>
     *      <li>QUERY_ID - query id</li>
     *      <li>STATUS_CODE - response status code</li>
     *      <li>TIME - execution time</li>
     *      <li>RESPONSE_SIZE - response size in kilobytes</li>
     *  </ul>
     *  </p>
     */
    private static final String EXECUTION_PROGRESS_LOG_ENTRY_FORMAT = "{} / {}\t\t{}|{}|{}\t\t{}\t[{}]\t{} ms\t{}Kb";
    /** Error log entry format.*/
    private static final String ERROR_LOG_ENTRY_FORMAT = "%s - %s";
    /** N/A alias.*/
    private static final String NOT_AVAILABLE_ALIAS = "-";
    /** Result report log header.*/
    private static final String RESULT_LOG_HEADER = "\n---------------- QUERY EXECUTION REPORT ----------------\n";
    /** Response group stats log entry.*/
    private static final String RESULT_LOG_GROUP_LOG_ENTRY_FORMAT = "| %8s | %8s | %8s | %8s | %8s |\n";
    /** Result log table footer.*/
    private static final String RESULT_LOG_FOOTER = "--------------------------------------------------------\n";
    /** Error writer.*/
    private final PrintWriter errorsWriter;

    /**
     * Constructor.
     *
     * @param configuration - configuration.
     * @param responseStorage - response storage.
     * @throws java.io.IOException - in case of error during creation of error writer.
     */
    public ExecutionProgressListener(CfitConfiguration configuration, File responseStorage) throws IOException {
        if (configuration == null || responseStorage == null) {
            throw new IllegalArgumentException(
                String.format("Null value has been passed in as required argument ['configuration'=%s, 'responseStorage'=%s]",
                    configuration, responseStorage));
        }
        this.errorsWriter = new PrintWriter(
            Files.newWriter(new File(responseStorage, configuration.getFailedQueriesFileName()), Charset.defaultCharset()),
            false
        );
    }

    /**
     * On query execution finish custom actions.
     *
     * @param response - query response.
     * @param result - execution phase results.
     */
    public void onQueryProcessed(QueryResponse response, ExecutionPhaseResult result) {
        LOG.info(EXECUTION_PROGRESS_LOG_ENTRY_FORMAT, result.getCount(), result.getTotal(), result.getSuccess(),
            result.getFailures(), result.getExceptions(), response.getDefinition().getId(), response.getStatus(), response.getTime(),
            CfitUtils.inKB(response.getSize()));
    }

    /**
     * Registers failure message during query execution.
     *
     * @param result - failure message.
     * @param uri - origin query uri.
     * @param responseStatusCode - query response status code.
     * @param e - exception type.
     */
    public void onExecutionFailure(String uri, Integer responseStatusCode, ExecutionPhaseResult result, Exception e) {
        String status = responseStatusCode == null ? NOT_AVAILABLE_ALIAS : String.valueOf(responseStatusCode).intern();
        String message = e != null ? e.getMessage() : "";
        errorsWriter.println(String.format(FAILED_QUERY_LOG_FORMAT, status, uri, message));
        if (e != null && e.getCause() != null) {
            errorsWriter.println(e);
        }

        LOG.info(EXECUTION_PROGRESS_LOG_ENTRY_FORMAT, result.getCount(), result.getTotal(), result.getSuccess(),
            result.getFailures(), result.getExceptions(), NOT_AVAILABLE_ALIAS, NOT_AVAILABLE_ALIAS, NOT_AVAILABLE_ALIAS);
    }

    /**
     * Does specific actions of error issue.
     *
     * @param errorMessage - error message.
     * @param error - error.
     */
    public void onError(String errorMessage, Throwable error) {
        errorsWriter.println(String.format(ERROR_LOG_ENTRY_FORMAT, errorMessage, error.toString()));
    }

    /**
     * Does onFinish custom actions.
     */
    public void onFinish() {
        IOUtils.closeWriter(errorsWriter);
    }

    /**
     * Does on result report save custom actions.
     *
     * @param result - result.
     */
    public void onResultSave(ExecutionPhaseResult result) {
        printResultStatistics(result);
    }

    private void printResultStatistics(ExecutionPhaseResult result) {
        StringBuilder logEntry = new StringBuilder(RESULT_LOG_HEADER);
        String header = RESULT_LOG_GROUP_LOG_ENTRY_FORMAT;
        logEntry.append(String.format(header, "status", "count", "avg, ms", "max, ms", "min, ms"));
        logEntry.append(RESULT_LOG_FOOTER);
        for (ByStatusResponseGroup byStatusResponseGroup : result.getGroups().values()) {
            logEntry.append(String.format(header, byStatusResponseGroup.getStatus(), byStatusResponseGroup.getSize(),
                byStatusResponseGroup.getAvg(), byStatusResponseGroup.getMax(), byStatusResponseGroup.getMin()));
        }

        logEntry.append(String.format(header, NOT_AVAILABLE_ALIAS, result.getExceptions(),
            NOT_AVAILABLE_ALIAS, NOT_AVAILABLE_ALIAS, NOT_AVAILABLE_ALIAS));


        logEntry.append(RESULT_LOG_FOOTER);
        LOG.info(logEntry.toString());
    }

}
