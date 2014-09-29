package com.mtvi.cfit.query.response.transform;

import com.mtvi.cfit.CfitException;

import java.io.Reader;
import java.io.Writer;

/**
 * Does processing of a response from given input. Saves result in output.
 * @author Kiryl_Dubarenka
 */
public interface ResponseTransformer {

    /**
     * Does processing of response.
     *
     * @param input - response input.
     * @param output - processed response.
     * @throws CfitException - in case of transformation issue.
     */
    void transform(Reader input, Writer output) throws CfitException;

}
