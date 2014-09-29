package com.mtvi.cfit.query.response;

import com.google.common.base.Charsets;
import com.mtvi.cfit.query.definition.QueryDefinition;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.http.HttpStatus;

/**
 * Query response.
 *
 * @author zenind.
 */
public class QueryResponse {

    /** Response status.*/
    private final int status;
    /** Content.*/
    private final String content;
    /** Exact URL that was executed. */
    private final String url;
    /** Query Definition.*/
    private final QueryDefinition definition;
    /** Execution time.*/
    private final long time;

    /**
     * Constructor.
     *
     * @param definition - query definition.
     * @param url - url.
     * @param content - raw response content.
     * @param status - status.
     * @param time - execution time.
     */
    public QueryResponse(QueryDefinition definition, String url, String content, int status, long time) {
        this.definition = definition;
        this.url = url;
        this.content = content;
        this.status = status;
        this.time = time;
    }

    public int getStatus() {
        return status;
    }

    public String getContent() {
        return content;
    }

    /**
     * Calculates approximate size of response in bytes.
     *
     * @return size in bytes.
     */
    public long getSize() {
        if (content == null) {
            return 0;
        }
        return content.getBytes(Charsets.UTF_8).length;
    }

    public QueryDefinition getDefinition() {
        return definition;
    }

    public String getUrl() {
        return url;
    }

    public long getTime() {
        return time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        QueryResponse that = (QueryResponse) o;

        return definition.equals(that.definition) && url.equals(that.url);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(url).append(definition).toHashCode();
    }
}
