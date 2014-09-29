package com.mtvi.cfit.exec;

import com.mtvi.cfit.CfitConfiguration;
import com.mtvi.cfit.CfitException;
import com.mtvi.cfit.query.ExecutionProgressListener;
import com.mtvi.cfit.query.Query;
import com.mtvi.cfit.query.QueryProvider;
import com.mtvi.cfit.query.common.SizeAwareIterable;
import com.mtvi.cfit.query.response.QueryResponse;
import com.mtvi.cfit.query.response.QueryResponseManager;
import com.mtvi.cfit.util.CfitUtils;

import java.io.File;

/**
 * Basic implementation of query executor.
 * <p>Synchronously(one by one) does execution of queries and saving of responses.
 *
 * @author zenind
 */
public class BasicPhaseExecutor implements PhaseExecutor {

    /** Cfit configuration.*/
    private final CfitConfiguration configuration;
    /** Execution results manager.*/
    private final ExecutionResultManager resultsManager;
    /** Query response manager.*/
    private final QueryResponseManager<File> responseManager;
    /** Response dir name.*/
    private final File responsesDir;

    /**
     * Constructor.
     *
     * @param configuration - cfit configuration.
     * @param responseManager - responses manager.
     * @param responseDirName - response dir name.
     */
    public BasicPhaseExecutor(CfitConfiguration configuration, QueryResponseManager<File> responseManager, File responseDirName) {
        this.configuration = configuration;
        this.resultsManager = new ExecutionResultManager(configuration);
        this.responseManager = responseManager;
        this.responsesDir = responseDirName;
    }

    @Override
    public ExecutionPhaseResult executeQueries(QueryProvider queryProvider) throws CfitException {
        ExecutionProgressListener executionListener = null;
        try {
            executionListener = new ExecutionProgressListener(configuration, responsesDir);

            ExecutionPhaseResult result = doExecution(
                queryProvider.getQueries(executionListener),
                executionListener
            );

            return saveResult(result, executionListener);
        } catch (Exception e) {
            throw new CfitException(
                String.format("Error occurred while running queries from %s", responsesDir), e
            );
        } finally {
            if (executionListener != null) {
                executionListener.onFinish();
            }
        }
    }

    @Override
    public void shutdown() { }

    protected ExecutionPhaseResult doExecution(SizeAwareIterable<Query> queries, ExecutionProgressListener listener) {
        ExecutionPhaseResult result = new ExecutionPhaseResult(queries.size());

        for (Query query : queries) {
            try {
                processQuery(query, result, listener);
            } catch (CfitException e) {
                handleManagedQueryException(
                    query.getDefinition().getQuery(),
                    null,
                    e,
                    result,
                    listener
                );
            }
        }
        return result;
    }

    protected void processQuery(Query query, ExecutionPhaseResult result, ExecutionProgressListener listener) throws CfitException {
        QueryResponse response = executeQuery(query, result);
        saveResponse(response);

        if (CfitUtils.isSuccessStatus(response.getStatus())) {
            listener.onQueryProcessed(response, result);
        } else {
            listener.onExecutionFailure(response.getUrl(), response.getStatus(), result, null);
        }
    }

    protected QueryResponse executeQuery(Query query, ExecutionPhaseResult result) throws CfitException {
        QueryResponse response = query.execute();
        result.registerResponse(response);
        return response;
    }

    protected void saveResponse(QueryResponse response) throws CfitException {
        this.responseManager.save(
            response,
            new File(responsesDir, response.getDefinition().getId())
        );
    }

    protected void handleManagedQueryException(String uri, Integer statusCode, Exception e, ExecutionPhaseResult result,
        ExecutionProgressListener listener) {
        result.registerError();

        listener.onExecutionFailure(uri, statusCode, result, e);
    }

    protected ExecutionPhaseResult saveResult(ExecutionPhaseResult result, ExecutionProgressListener listener)
        throws CfitException {
        listener.onResultSave(result);
        return resultsManager.save(result, responsesDir);
    }

    protected CfitConfiguration getConfiguration() {
        return configuration;
    }
}
