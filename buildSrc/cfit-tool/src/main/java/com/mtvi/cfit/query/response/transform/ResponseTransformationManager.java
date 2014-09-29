package com.mtvi.cfit.query.response.transform;

import com.mtvi.cfit.query.common.SizeAwareIterable;

import java.io.File;

/**
 * Does post processing/transformation of raw responses.
 *
 * @author Kiryl_Dubarenka
 */
public interface ResponseTransformationManager {

    /**
     * Does transformation of responses in given source directory.
     *
     * @param responseDir - source directory with query response.
     */
    void transformAll(File responseDir);

    /**
     * Locates all responses that should be transformed.
     *
     * @param responseDir - responses source directory.
     * @return iterable of response that should be processed.
     */
    SizeAwareIterable<File> getResponses(File responseDir);

    /**
     * Does transformation of response.
     * @param response - response storage.
     */
    void transform(File response);


}
