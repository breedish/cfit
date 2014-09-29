package com.mtvi.cfit.exec;

import com.mtvi.cfit.CfitConfiguration;
import com.mtvi.cfit.CfitException;
import com.mtvi.cfit.query.ExecutionProgressListener;
import com.mtvi.cfit.query.Query;
import com.mtvi.cfit.query.common.SizeAwareIterable;
import com.mtvi.cfit.query.response.QueryResponse;
import com.mtvi.cfit.query.response.QueryResponseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * Class ComplexDistributedPhaseExecutor.
 *
 * @author zenind
 */
public class ComplexDistributedPhaseExecutor extends BasicPhaseExecutor {

    /** Logger.*/
    private static final Logger LOG = LoggerFactory.getLogger(ComplexDistributedPhaseExecutor.class);
    /** Default executor service termination timeout(seconds).*/
    private static final int DEFAULT_SERVICE_TERMINATION_TIMEOUT = 20;
    /** Executor for jobs to execute queries.*/
    private final ExecutorService executionService;
    /** Executor for workers to save responses.*/
    private final ExecutorService storageService;
    /** Execution service with completion queue.*/
    private final CompletionService<QueryResponse> completionService;

    /**
     * Constructor.
     *
     * @param configuration - cfit configuration.
     * @param responseManager - response manager.
     * @param responsesDir - responses storage dir name.
     */
    public ComplexDistributedPhaseExecutor(CfitConfiguration configuration, QueryResponseManager<File> responseManager, File responsesDir) {
        super(configuration, responseManager, responsesDir);
        int workersNumber = configuration.getCfitProperties().getWorkersNumber();
        this.executionService = Executors.newFixedThreadPool(workersNumber);
        this.storageService = Executors.newFixedThreadPool(workersNumber / 4);
        this.completionService = new ExecutorCompletionService<QueryResponse>(
            executionService,
            new LinkedBlockingDeque<Future<QueryResponse>>(workersNumber)
        );
    }

    @Override
    protected ExecutionPhaseResult doExecution(SizeAwareIterable<Query> queries, final ExecutionProgressListener listener) {
        int queriesCount = queries.size();
        final ExecutionPhaseResult result = new ExecutionPhaseResult(queriesCount);
        CountDownLatch checkPoint = new CountDownLatch(queriesCount);
        initResponseWorkers(result, listener, checkPoint);

        for (final Query query : queries) {
            completionService.submit(new Callable<QueryResponse>() {
                @Override
                public QueryResponse call() throws Exception {
                    try {
                        return executeQuery(query, result);
                    } catch (CfitException e) {
                        handleManagedQueryException(query.getDefinition().getQuery(), null, e, result, listener);
                        throw e;
                    }
                }
            });
        }

        try {
            checkPoint.await();
        } catch (InterruptedException e) {
            Thread.interrupted();
        }

        return result;
    }

    @Override
    public void shutdown() {
        shutdownExecutorService(executionService);
        shutdownExecutorService(storageService);
    }

    private void initResponseWorkers(ExecutionPhaseResult result, ExecutionProgressListener listener, CountDownLatch checkPoint) {
        int workersNumber = getConfiguration().getCfitProperties().getWorkersNumber();
        while (workersNumber-- > 0) {
            storageService.submit(new ResponseProcessingWorker(result, listener, checkPoint));
        }
    }

    private void shutdownExecutorService(ExecutorService executorService) {
        executorService.shutdownNow();

        try {
            executorService.awaitTermination(DEFAULT_SERVICE_TERMINATION_TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.error("Error while trying to shutdown executor service", e);
        }
    }

    /**
     * Worker for storing query responses.
     */
    private final class ResponseProcessingWorker implements Runnable {
        /** Execution phase result.*/
        private final ExecutionPhaseResult result;
        /** Execution progress listener.*/
        private final ExecutionProgressListener listener;
        /** CheckPoint Charlie.*/
        private final CountDownLatch checkPoint;

        private ResponseProcessingWorker(ExecutionPhaseResult result, ExecutionProgressListener listener, CountDownLatch checkPoint) {
            if (result == null || listener == null || checkPoint == null) {
                throw new IllegalArgumentException(
                    String.format("Null value has been passed in as required argument ['result'=%s, 'listener'=%s, 'checkPoint'=%s]",
                        result, listener, checkPoint));
            }
            this.result = result;
            this.listener = listener;
            this.checkPoint = checkPoint;
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Future<QueryResponse> futureResponse = completionService.take();
                    QueryResponse response = null;
                    try {
                        response = futureResponse.get();
                    } catch (ExecutionException e) {
                        listener.onError("Issue during saving response", e.getCause());
                    } catch (CancellationException e) {
                        listener.onError("Query execution was cancelled", e);

                    }
                    if (response != null) {
                        try {
                            saveResponse(response);
                            listener.onQueryProcessed(response, result);
                        } catch (CfitException e) {
                            handleManagedQueryException(response.getUrl(), response.getStatus(), e, result, listener);
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.interrupted();
                } finally {
                    checkPoint.countDown();
                }
            }
        }
    }
}
