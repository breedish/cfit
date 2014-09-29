package com.mtvi.cfit.query.definition;

import com.mtvi.cfit.query.ExecutionProgressListener;

/**
 * QueryDefinition sources provider.
 *
 * @author zenind.
 */
public interface QueryDefinitionProvider {

    /**
     * Finds and loads query definitions.
     *
     * @return list of found queries.
     */
    QueryDefinitionList load();

}
