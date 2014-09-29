package com.mtvi.cfit.query.response;

import com.mtvi.cfit.query.definition.QueryDefinition;
import com.mtvi.cfit.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * File based query response representation.
 * <p>
 *     Is used to minimize memory usage. Loads content on demand.
 * </p>
 * @author zenind
 */
public final class FileQueryResponseProxy extends QueryResponse {

    /** Logger.*/
    private static final Logger LOG = LoggerFactory.getLogger(FileQueryResponseProxy.class);

    /** Source file.*/
    private final File source;

    /**
     * Constructor.
     * @param source - source file.
     * @param definition - definition.
     * @param url - url.
     * @param status - status.
     * @param time - time.
     */
    public FileQueryResponseProxy(File source, QueryDefinition definition, String url, int status, long time) {
        super(definition, url, "".intern(), status, time);
        this.source = source;
    }

    @Override
    public String getContent() {
        try {
            return IOUtils.getContent(this.source, 1);
        } catch (IOException e) {
            LOG.error("Unable to load content for response {} from {}", getDefinition().getId(), this.source.getAbsolutePath());
            return super.getContent();
        }
    }

    @Override
    public long getSize() {
        return source.length();
    }
}
