package com.mtvi.cfit.exec;

import com.mtvi.cfit.CfitConfiguration;
import com.mtvi.cfit.CfitException;
import com.mtvi.cfit.query.ExecutionProgressListener;
import com.mtvi.cfit.query.Query;
import com.mtvi.cfit.query.common.SizeAwareIterable;
import com.mtvi.cfit.query.response.QueryResponseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Query Executor, that queries distributes execution between several workers.
 *
 * @author zenind
 */
public class DistributedPhaseExecutor extends BasicPhaseExecutor {

    /** Logger.*/
    private static final Logger LOG = LoggerFactory.getLogger(DistributedPhaseExecutor.class);
    /** Default executor service termination timeout(seconds).*/
    private static final int DEFAULT_SERVICE_TERMINATION_TIMEOUT = 20;

    /** Queries queue.*/
    private final BlockingQueue<QueueEntry> queriesQueue;
    /** Executor.*/
    private final ExecutorService executorService;
    /** Query Execution workers.*/
    private final List<QueryExecutionWorker> workers;
    /** Number of total active workers.*/
    private final AtomicInteger activeThreads;

    /**
     * Constructor.
     *
     * @param configuration - cfit configuration.
     * @param responseManager - response manager.
     * @param responsesDir - responses dir.
     */
    public DistributedPhaseExecutor(CfitConfiguration configuration, QueryResponseManager<File> responseManager, File responsesDir)  {
        super(configuration, responseManager, responsesDir);
        int workersNumber = configuration.getCfitProperties().getWorkersNumber();
        this.queriesQueue = new LinkedBlockingDeque<QueueEntry>(workersNumber);
        this.executorService = Executors.newFixedThreadPool(workersNumber);
        this.workers = new ArrayList<QueryExecutionWorker>(workersNumber);
        this.activeThreads = new AtomicInteger();
        init();
    }

    private void init() {
        int numWorkers = getConfiguration().getCfitProperties().getWorkersNumber();
        while (numWorkers-- > 0) {
            QueryExecutionWorker worker = new QueryExecutionWorker();
            executorService.submit(worker);
            workers.add(worker);
        }
    }

    @Override
    protected ExecutionPhaseResult doExecution(SizeAwareIterable<Query> queries, ExecutionProgressListener executionListener) {
        ExecutionPhaseResult result = new ExecutionPhaseResult(queries.size());

        for (Query query : queries) {
            try {
                queriesQueue.put(new QueueEntry(query, result, executionListener));
                activeThreads.incrementAndGet();
            } catch (InterruptedException e) {
                LOG.error(String.format("Error while adding query '%s' to queue", query.getDefinition().getId()), e);
            }
        }

        try {
            while (activeThreads.get() > 0) {
                TimeUnit.SECONDS.sleep(1);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return result;
    }

    @Override
    public void shutdown() {
        for (QueryExecutionWorker worker : workers) {
            worker.stop();
        }

        executorService.shutdownNow();

        try {
            executorService.awaitTermination(DEFAULT_SERVICE_TERMINATION_TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOG.error("Error while trying to shutdown executor service", e);
        }
    }

    /**
     * Queue entry.
     */
    private final class QueueEntry {
        /** Executable query.*/
        private final Query query;
        /** Execution result.*/
        private final ExecutionPhaseResult result;
        /** Execution progress listener.*/
        private final ExecutionProgressListener listener;

        private QueueEntry(Query query, ExecutionPhaseResult result, ExecutionProgressListener listener) {
            this.query = query;
            this.result = result;
            this.listener = listener;
        }

        public Query getQuery() {
            return query;
        }

        public ExecutionPhaseResult getResult() {
            return result;
        }

        public ExecutionProgressListener getListener() {
            return listener;
        }
    }

    /**
     * Query execution runnable implementation.
     */
    private final class QueryExecutionWorker implements Runnable {
        /** Flag to control execution state of worker.*/
        private volatile boolean stopped;

        @Override
        public void run() {
            boolean interrupted = false;

            while (!stopped) {
                try {
                    execute(queriesQueue.take());
                    activeThreads.decrementAndGet();
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }

            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }

        public void stop() {
            stopped = true;
        }

        private void execute(QueueEntry entry) {
            try {
                processQuery(entry.getQuery(), entry.getResult(), entry.getListener());
            } catch (CfitException e) {
                handleManagedQueryException(entry.getQuery().getDefinition().getQuery(), null, e,
                    entry.getResult(), entry.getListener());
            }
        }
    }

}
