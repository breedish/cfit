package com.mtvi.cfit.util.exec;

import com.mtvi.cfit.CfitException;
import com.mtvi.cfit.query.common.SizeAwareIterable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Represents a component to asynchronous processing of items.
 * <p>Does not provide any results of processing items.</p>
 *
 * @param <S> - represents a type of items that should be processed.
 * @author zenind
 */
public final class WorkProcessor<S> {

    /** Logger.*/
    private static final Logger LOG = LoggerFactory.getLogger(WorkProcessor.class);
    /** Time in {@link java.util.concurrent.TimeUnit#SECONDS} to wait for executor service to shutdown.*/
    private static final int SHUTDOWN_AWAIT_PERIOD = 5;
    /** Desired CPU utilization for work processor. This is not a accurate value */
    private static final float UTILIZATION = 0.2f;
    /** Items to process provider.*/
    private final SizeAwareIterable<S> workProvider;
    /** Work queue.*/
    private final BlockingQueue<S> workQueue;
    /** Stateless action to apply.*/
    private final Action<S> action;
    /** Number of workers to use during {@link #process()} processing.*/
    private final int workersNumber;

    /**
     * Constructor.
     * @param workProvider - items to process provider.
     * @param workersNumber - total number of workers to use to processed all items.
     * @param action - action to apply to each work item.
     */
    public WorkProcessor(SizeAwareIterable<S> workProvider, int workersNumber, Action<S> action) {
        this.workProvider = workProvider;
        this.action = action;
        this.workQueue = new LinkedBlockingQueue<S>(workersNumber);
        this.workersNumber = workersNumber;
    }

    /**
     * Does processing of items.
     */
    public void process() {
        int workAmount = workProvider.size();
        final CountDownLatch checkPoint = new CountDownLatch(workAmount);

        ExecutorService executorService = Executors.newFixedThreadPool(workersNumber);

        initWorkers(workersNumber, executorService, checkPoint);

        int processed = 1;
        for (S work : workProvider) {
            try {
                workQueue.put(work);
                LOG.info("\t {} / {} Processed", processed++, workAmount);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        try {
            checkPoint.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        shutdown(executorService);
    }

    private void initWorkers(int workersCount, ExecutorService executorService, final CountDownLatch checkPoint) {
        while (workersCount-- > 0) {
            executorService.submit(new Runnable() {
                @Override
                public void run() {
                    while (!Thread.currentThread().isInterrupted()) {
                        try {
                            S subject = workQueue.take();
                            action.doAction(subject);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        } catch (CfitException e) {
                            LOG.error("Issue during processing subject", e);
                        } finally {
                            checkPoint.countDown();
                        }
                    }
                }
            });
        }
    }

    private void shutdown(ExecutorService executorService) {
        executorService.shutdownNow();

        try {
            executorService.awaitTermination(SHUTDOWN_AWAIT_PERIOD, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Public interface for processing action, that should be applied to given subject.
     * @param <S> - type of subject.
     */
    public interface Action<S> {

        /**
         * Performs an action on given subject.
         * @param subject - subject.
         * @throws CfitException - in case of any issue.
         */
        void doAction(S subject) throws CfitException;

    }

    /**
     * See {@link #newWorkProcessor(com.mtvi.cfit.query.common.SizeAwareIterable, com.mtvi.cfit.util.exec.WorkProcessor.Action, float, float)}
     *
     * @param workProvider - work provider.
     * @param action - action.
     * @param waitRatio - wait ratio.
     * @param <S> - action to be done for each work item.
     * @return - configured work processor.
     */
    public static <S> WorkProcessor<S> newWorkProcessor(SizeAwareIterable<S> workProvider, Action<S> action, float waitRatio) {
        return newWorkProcessor(workProvider, action, UTILIZATION, waitRatio);
    }

    /**
     * Instantiates a new work processor for given work provider.
     * <p>Performs {@code action} for each work item. Action has a given wait to running time ratio.
     * Work is divided into number of workers that should produce load on processor no more than value provided by
     * {@code utilization}, that is in [0..1] interval. Well don't know if it will be helpful.
     *
     * @param workProvider - work provider.
     * @param action - action.
     * @param utilization - cpu utilization.
     * @param waitRatio - wait ratio.
     * @param <S> - action to be done for each work item.
     * @return - configured work processor.
     */
    public static <S> WorkProcessor<S> newWorkProcessor(SizeAwareIterable<S> workProvider, Action<S> action,
        float utilization, float waitRatio) {
        if (utilization == 0 || waitRatio < 0) {
            throw new IllegalArgumentException();
        }

        int cpuNumber = Runtime.getRuntime().availableProcessors();
        int workers = (int) (cpuNumber * utilization * (1 + waitRatio));

        return new WorkProcessor<S>(workProvider, workers, action);
    }
}
