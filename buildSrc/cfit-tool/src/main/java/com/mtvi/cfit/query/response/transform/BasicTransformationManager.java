package com.mtvi.cfit.query.response.transform;

import com.google.common.collect.ImmutableSet;
import com.mtvi.cfit.CfitConfiguration;
import com.mtvi.cfit.CfitException;
import com.mtvi.cfit.query.common.SizeAwareIterable;
import com.mtvi.cfit.query.response.QueryResponse;
import com.mtvi.cfit.query.response.QueryResponseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Basic implementation of {@link ResponseTransformationManager}.
 *
 * @author Kiryl_Dubarenka
 */
public class BasicTransformationManager implements ResponseTransformationManager {

    /** Logger.*/
    private static final Logger LOG = LoggerFactory.getLogger(BasicTransformationManager.class);

    /** Set of tech file names that should be skipped during transformation.*/
    private Set<String> systemFiles;

    /** Query response manager.*/
    private final QueryResponseManager<File> queryResponseManager;

    /** List of transformers to apply for given response.*/
    private final List<ResponseTransformer> responseBodyTransformers = new ArrayList<ResponseTransformer>() {
        {
            add(new JsonFieldsSorter());
        }
    };

    /**
     * Constructor.
     *
     * @param configuration - cfit configuration.
     * @param queryResponseManager - query response manager.
     */
    public BasicTransformationManager(CfitConfiguration configuration, QueryResponseManager<File> queryResponseManager) {
        if (queryResponseManager == null) {
            throw new IllegalArgumentException("Null value has been passed in as required argument ['queryResponseManager'= null]");
        }

        this.queryResponseManager = queryResponseManager;
        this.systemFiles = ImmutableSet.of(configuration.getFailedQueriesFileName(), configuration.getExecutionStatsFileName());
    }

    @Override
    public void transformAll(File responseDir) {
        if (responseDir == null) {
            throw new IllegalArgumentException("Null value has been passed in as required argument ['responseDir' = null]");
        }

        for (File file : getResponses(responseDir)) {
            this.transform(file);
        }
    }

    @Override
    public SizeAwareIterable<File> getResponses(File responseDir) {
        final File[] responses = responseDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return !systemFiles.contains(name);
            }
        });

        return new SizeAwareIterable<File>() {

            @Override
            public int size() {
                return responses.length;
            }

            @Override
            public Iterator<File> iterator() {
                return new Iterator<File>() {

                    private int current = 0;

                    @Override
                    public boolean hasNext() {
                        return current < responses.length;
                    }

                    @Override
                    public File next() {
                        return responses[current++];
                    }

                    @Override
                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    @Override
    public void transform(File storage) {
        try {
            QueryResponse response = queryResponseManager.load(storage);
            QueryResponse transformation = transform(response);
            queryResponseManager.save(transformation, storage);
        } catch (Exception e) {
            LOG.error("Issue during transformation of response of query " + storage.getAbsolutePath(), e);
        }
    }

    protected QueryResponse transform(QueryResponse response) throws CfitException {
        StringWriter buffer = new StringWriter();
        for (ResponseTransformer responseBodyTransformer : responseBodyTransformers) {
            responseBodyTransformer.transform(new StringReader(response.getContent()), buffer);
        }
        return new QueryResponse(response.getDefinition(), response.getUrl(), buffer.toString(), response.getStatus(), response.getTime());
    }

}
