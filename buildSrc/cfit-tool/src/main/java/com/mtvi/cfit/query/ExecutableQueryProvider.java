package com.mtvi.cfit.query;

import com.google.common.collect.Sets;
import com.google.common.net.HttpHeaders;
import com.google.common.net.MediaType;
import com.mtvi.cfit.CfitConfiguration;
import com.mtvi.cfit.CfitException;
import com.mtvi.cfit.query.definition.FileBasedQueryDefinitionProvider;
import com.mtvi.cfit.query.definition.QueryDefinition;
import com.mtvi.cfit.query.definition.QueryDefinitionProvider;
import com.mtvi.cfit.query.common.SizeAwareIterable;
import org.apache.http.Header;
import org.apache.http.client.HttpClient;
import org.apache.http.message.BasicHeader;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Provides a list of {@link HttpQuery queries} to run.
 *
 * @author zenind.
 */
public final class ExecutableQueryProvider implements QueryProvider {

    /** Http Client. */
    private final HttpClient httpClient;
    /** Query Definition provider..*/
    private final QueryDefinitionProvider definitionSource;
    /** Configuration.*/
    private CfitConfiguration configuration;
    /** Set of required system headers.*/
    private Set<Header> systemHeaders;

    /**
     * Constructor.
     *
     * @param httpClient - http client
     * @param configuration config
     */
    public ExecutableQueryProvider(HttpClient httpClient, CfitConfiguration configuration) {
        if (httpClient == null) {
            throw new IllegalArgumentException(
                "Null value has been passed in as required argument ['httpClient'= null]");
        }
        this.httpClient = httpClient;
        this.definitionSource = new FileBasedQueryDefinitionProvider(configuration);
        this.configuration = configuration;
        this.systemHeaders = Sets.<Header>newHashSet(
            new BasicHeader(HttpHeaders.CONTENT_TYPE, MediaType.JSON_UTF_8.toString())
        );
    }

    /**
     * Builds a list of executable queries for execution.
     *
     * @param listener - query processing listener.
     * @return - list of queries.
     */
    public SizeAwareIterable<Query> getQueries(ExecutionProgressListener listener) {
        try {
            return new LazyExecutableIterable(definitionSource.load().list());
        } catch (CfitException e) {
            return new SizeAwareIterable<Query>() {
                @Override
                public int size() {
                    return 0;
                }

                @Override
                public Iterator<Query> iterator() {
                    return Collections.<Query>emptyList().iterator();
                }
            };
        }
    }

    /**
     * Lazy list for executable queries.
     */
    private final class LazyExecutableIterable implements SizeAwareIterable<Query> {
        /** Source Definition list.*/
        private final Iterator<QueryDefinition> definitionList;
        /** Size.*/
        private final int size;

        /**
         * Constructor.
         * @param definitionList - source.
         */
        private LazyExecutableIterable(SizeAwareIterable<QueryDefinition> definitionList) {
            if (definitionList == null) {
                throw new IllegalArgumentException(
                    "Null value has been passed in as required argument ['definitionList'= null]");
            }
            this.definitionList = definitionList.iterator();
            this.size = definitionList.size();

        }

        @Override
        public int size() {
            return this.size;
        }

        @Override
        public Iterator<Query> iterator() {
            return new Iterator<Query>() {
                @Override
                public boolean hasNext() {
                    return definitionList.hasNext();
                }

                @Override
                public Query next() {
                    QueryDefinition definition = definitionList.next();
                    if (definition == null) {
                        throw new NoSuchElementException();
                    }
                    return new HttpQuery(definition, httpClient, configuration, systemHeaders);
                }

                @Override
                public void remove() {
                    throw new UnsupportedOperationException("Unsupported operation");
                }
            };
        }
    }

}
