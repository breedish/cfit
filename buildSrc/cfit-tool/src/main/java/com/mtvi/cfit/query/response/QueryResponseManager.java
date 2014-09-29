package com.mtvi.cfit.query.response;

import com.mtvi.cfit.CfitException;

/**
 * Class QueryResponseManager.
 *
 * @param <ST> - storage type.
 * @author zenind
 */
public interface QueryResponseManager<ST> {

    /**
     * Saves response in storage.
     *
     * @param response - response to save.
     * @param storage - target storage.
     * @throws CfitException - in case of i/o errors.
     */
    void save(QueryResponse response, ST storage) throws CfitException;

    /**
     * Load response from given response storage.
     *
     * @param source - source storage.
     * @return - query response.
     * @throws CfitException - in case of i/o errors.
     */
    QueryResponse load(ST source) throws CfitException;

}
