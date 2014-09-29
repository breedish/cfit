package com.mtvi.cfit.query;

import com.mtvi.cfit.CfitException;
import com.mtvi.cfit.query.definition.QueryDefinition;
import com.mtvi.cfit.query.response.QueryResponse;
import org.apache.http.Header;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

/**
 * Executable query contract.
 *
 * @author zenind
 */
public interface Query {

    /**
     * Does query execution.
     *
     * @return -  execution result.
     * @throws CfitException - in case of:
     * <ul>
     *     <li>Connection issue</li>
     *     <li>Parse issue</li>
     * </ul>
     */
    QueryResponse execute() throws CfitException;

    /**
     * Gets query definition.
     * @return - query definition.
     */
    QueryDefinition getDefinition();

    /**
     * Gets query id.
     * @return - id
     */
    String getId();

    /**
     * Gets query URL to be executed.
     *
     * @return - query URL.
     * @throws java.net.URISyntaxException - in case of malformed query.
     * @throws java.io.UnsupportedEncodingException - in case of issue with bad encoding of characters of raw query.
     */
    URI getURI() throws URISyntaxException, UnsupportedEncodingException, MalformedURLException;

    /**
     * Set of additional headers query should be aware of.
     * @return - set of headers.
     */
    Set<Header> getHeaders();

}
