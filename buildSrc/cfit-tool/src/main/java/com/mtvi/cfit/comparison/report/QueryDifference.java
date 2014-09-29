package com.mtvi.cfit.comparison.report;

import com.google.common.base.Objects;
import com.mtvi.cfit.query.definition.QueryDefinition;
import com.mtvi.cfit.query.response.QueryResponse;

/**
 * Query Difference for executions on different artifact versions.
 *
 * @author Dzmitry_Zenin.
 */
@SuppressWarnings("unused")
public class QueryDifference {

    /** Original response.*/
    private final QueryResponse originalResponse;
    /** RC response.*/
    private final QueryResponse rcResponse;
    /** Query Definition.*/
    private final QueryDefinition definition;

    /**
     * Constructor.
     * @param definition - query definition.
     * @param originalResponse - original response.
     * @param rcResponse - rc response.
     */
    public QueryDifference(QueryDefinition definition, QueryResponse originalResponse, QueryResponse rcResponse) {
        this.definition = definition;
        this.originalResponse = originalResponse;
        this.rcResponse = rcResponse;
    }

    public String getRequestUrl() {
        return definition.getQuery();
    }

    public QueryResponse getOriginalResponse() {
        return originalResponse;
    }

    public QueryResponse getRcResponse() {
        return rcResponse;
    }

    public String getRequestId() {
        return definition.getId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        QueryDifference actual = (QueryDifference) o;

        return Objects.equal(definition, actual.definition)
            && Objects.equal(originalResponse, actual.originalResponse)
            && Objects.equal(rcResponse, actual.rcResponse);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(definition, originalResponse, rcResponse);
    }
}
