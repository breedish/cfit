package com.mtvi.cfit.query;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.mtvi.cfit.CfitConfiguration;
import com.mtvi.cfit.CfitException;
import com.mtvi.cfit.query.definition.QueryDefinition;
import com.mtvi.cfit.query.response.QueryResponse;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Http executable query implementation.
 *
 * <p>Does execution via HTTP <code>GET</code> method.</p>
 * <p>Is not thread safe.</p>
 *
 *
 * @author zenind.
 */
public class HttpQuery implements Query {

    /** Logger.*/
    private static final Logger LOG = LoggerFactory.getLogger(HttpQuery.class);
    /** Query Definition. */
    private final QueryDefinition definition;
    /** Method to run query for.*/
    private final HttpRequestBase method;
    /** Http Client.*/
    private final HttpClient httpClient;
    /** Cfit configuration.*/
    private final CfitConfiguration configuration;
    /** Headers. */
    private final Set<Header> headers;
    /** Query URI.*/
    private URI queryURI;

    /**
     * Constructor.
     *
     * @param definition - query definition.
     * @param httpClient - http client.
     * @param configuration - cfit configuration.
     * @param httpRequestHeaders - request headers.
     */
    public HttpQuery(QueryDefinition definition, HttpClient httpClient, CfitConfiguration configuration, Set<Header> httpRequestHeaders) {
        this(definition, httpClient, configuration, new HttpGet(), httpRequestHeaders);
    }

    protected HttpQuery(QueryDefinition definition, HttpClient httpClient, CfitConfiguration configuration,
        HttpRequestBase httpRequestBase, Set<Header> httpRequestHeaders) {
        if (definition == null || httpClient == null || configuration == null) {
            throw new IllegalArgumentException(
                String.format("Null value has been passed in as required argument, [definition=%s, httpClient=%s, configuration=%s]",
                    definition, httpClient, configuration));
        }

        this.definition = definition;
        this.httpClient = httpClient;
        this.configuration = configuration;
        this.method = httpRequestBase;
        this.headers = httpRequestHeaders == null ? new HashSet<Header>() : httpRequestHeaders;
    }

    @Override
    public QueryResponse execute() throws CfitException {
        try {
            LOG.debug("Execute {}", getURI());

            configureExecutionMethod();

            long startTime = System.currentTimeMillis();
            HttpResponse response = httpClient.execute(method);
            long executionTime = System.currentTimeMillis() - startTime;

            return new QueryResponse(definition, getURI().toString(), EntityUtils.toString(response.getEntity()),
                response.getStatusLine().getStatusCode(), executionTime);
        } catch (Exception e) {
            throw new CfitException(String.format("Error during execution of' %s' query.", this.definition.getId()), e);
        }
    }

    @Override
    public QueryDefinition getDefinition() {
        return this.definition;
    }

    @Override
    public URI getURI() throws URISyntaxException, UnsupportedEncodingException, MalformedURLException {
        if (queryURI == null) {
            queryURI = compileQuery();
        }
        return queryURI;
    }

    @Override
    public Set<Header> getHeaders() {
        return Collections.unmodifiableSet(this.headers);
    }

    @Override
    public String getId() {
        return this.definition.getId();
    }

    protected void configureExecutionMethod() throws URISyntaxException, UnsupportedEncodingException, MalformedURLException {
        method.setURI(getURI());

        for (Header header : getHeaders()) {
            this.method.addHeader(header);
        }
    }

    // Deprecated due to performance issue of URLEncoder/URLDecoder.
    @Deprecated
    protected URI compQuery() throws URISyntaxException, UnsupportedEncodingException, MalformedURLException {
        String query = definition.getQuery();

        URL url = new URL(query);

        return new URIBuilder()
            .setScheme(url.getProtocol())
            .setHost(url.getHost())
            .setPort(url.getPort())
            .setPath(url.getPath())
            .setParameters(buildRequestParams(url))
            .build();
    }

    // Deprecated due to performance issue of URLEncoder/URLDecoder.
    @Deprecated
    protected List<NameValuePair> buildRequestParams(URL url) throws UnsupportedEncodingException {
        String queryString = URLEncoder.encode(
            URLDecoder.decode(url.getQuery(), Charsets.UTF_8.name()),
            Charsets.UTF_8.name()
        );

        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.addAll(URLEncodedUtils.parse(queryString, Charsets.UTF_8));

        String extraParameters = configuration.getCfitProperties().getExtraReqParams();
        if (!Strings.isNullOrEmpty(extraParameters)) {
            params.addAll(URLEncodedUtils.parse(extraParameters, Charsets.UTF_8));
        }

        return params;
    }


    protected URI compileQuery() throws URISyntaxException, UnsupportedEncodingException {
        String query = appendSystemParameters(definition.getQuery());

        String[] parts = query.split("\\?");

        List<NameValuePair> params = parts.length == 1 ? Collections.<NameValuePair>emptyList() : URLEncodedUtils.parse(
            URLDecoder.decode(parts[1], Charsets.UTF_8.name()), Charsets.UTF_8);
        URI decodedURI = new URI(parts[0]);

        return new URIBuilder().setScheme(decodedURI.getScheme())
                .setHost(decodedURI.getHost())
                .setPort(decodedURI.getPort())
                .setPath(decodedURI.getPath())
                .setFragment(decodedURI.getFragment())
                .setParameters(params)
                .build();
    }

    private String appendSystemParameters(String url) throws URISyntaxException {
        String systemParams = configuration.getCfitProperties().getExtraReqParams();

        if (!Strings.isNullOrEmpty(systemParams)) {
            String appender = url.contains("?") ? "&" : "?";
            url = url + appender + systemParams;
        }

        return url;
    }

}
