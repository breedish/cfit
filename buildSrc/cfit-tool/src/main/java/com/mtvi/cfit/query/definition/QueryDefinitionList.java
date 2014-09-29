package com.mtvi.cfit.query.definition;

import com.mtvi.cfit.CfitException;
import com.mtvi.cfit.query.common.SizeAwareIterable;

/**
 * Class QueryDefinitionList.
 *
 * @author zenind
 */
public interface QueryDefinitionList {

    /**
     * Builds collection of queries from given source.
     *
     * @return list of query definitions for iteration.
     * @throws CfitException - in case of issue building list of queries.
     */
    SizeAwareIterable<QueryDefinition> list() throws CfitException;

}
