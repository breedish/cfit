package com.mtvi.cfit.query.definition;

/**
 * Query Definition implementation.
 *
 * @author zenind
 */
public class QueryDefinition {

    /** Raw query to execute.*/
    private final String query;
    /** Query id.*/
    private final String id;

    /**
     * Constructor.
     *
     * @param id - query id.
     * @param query - raw query string.
     */
    public QueryDefinition(String id, String query) {
        if (id == null || query == null) {
            throw new IllegalArgumentException(
                String.format("Null value has been passed in as required argument, [id=%s, query=%s]",
                    id, query));
        }

        this.id = id;
        this.query = query;
    }


    public String getQuery() {
        return query;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        QueryDefinition that = (QueryDefinition) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
