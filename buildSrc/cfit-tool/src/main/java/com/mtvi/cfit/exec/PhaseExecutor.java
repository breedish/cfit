package com.mtvi.cfit.exec;

import com.mtvi.cfit.CfitException;
import com.mtvi.cfit.query.QueryProvider;

/**
 * This class represents common interface for query execution.
 *
 * @author zenind
 */
public interface PhaseExecutor {

    /**
     * Does execution of queries for CFIT.
     *
     * @param queryProvider - executable query provider.
     * @return - execution result with stats
     * @throws CfitException - in case of issue during execution.
     */
    ExecutionPhaseResult executeQueries(QueryProvider queryProvider) throws CfitException;

    /**
     * Does graceful shutdown of executor.
     */
    void shutdown();

}
