package com.mtvi.cfit.query;

import com.mtvi.cfit.query.common.SizeAwareIterable;

/**
 * Provides set of queries to be run for CFIT phase.
 *
 * @author zenind.
 */
public interface QueryProvider {

    /**
     * Finds and filters set of queries to run.
     *
     * @param listener - query processing listener.
     * @return - list of queries to run.
     */
    SizeAwareIterable<Query> getQueries(ExecutionProgressListener listener);
    
}
