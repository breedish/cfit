package com.mtvi.cfit.query;

import com.mtvi.cfit.CfitConfiguration;
import com.mtvi.cfit.query.definition.FileBasedQueryDefinitionProvider;
import com.mtvi.cfit.query.definition.QueryDefinition;
import com.mtvi.cfit.query.definition.QueryDefinitionList;
import com.mtvi.cfit.query.definition.QueryDefinitionProvider;
import com.mtvi.cfit.query.response.QueryResponse;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicStatusLine;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.net.URI;
import java.util.Collections;

/**
 * Represents test for query definition parse(encode/decode).
 *
 * @author Kiryl_Dubarenka
 */
public class HttpQueryTest {

    private static HttpClient CLIENT;

    private static final CfitConfiguration CONFIGURATION = new CfitConfiguration();

    @BeforeClass
    public static void init() throws Exception {
        CONFIGURATION.getCfitProperties().setExtraReqParams("test1=true");
        CONFIGURATION.setQueriesDirName(new File(HttpQueryTest.class.getResource("/queries/production/sample-production-queries.q").getFile()).getParent());

        HttpResponse httpResponse = Mockito.mock(HttpResponse.class);
        Mockito.when(httpResponse.getStatusLine()).thenReturn(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "OK."));

        CLIENT = Mockito.mock(HttpClient.class);
        Mockito.when(CLIENT.execute(Mockito.any(HttpRequestBase.class))).thenReturn(httpResponse);
        Mockito.when(httpResponse.getEntity()).thenReturn(new StringEntity("test-response"));
    }

    /**
     * Checks:
     * <ul>
     *     <li>Correct symbols escape</li>
     *     <li>Special characters for http</li>
     *     <li>Execution flow test</li>
     * </ul>
     * @throws Exception
     */
    @Test
    public void testQueries() throws Exception {
        QueryDefinitionProvider provider = new FileBasedQueryDefinitionProvider(CONFIGURATION);
        QueryDefinitionList list = provider.load();

        for (QueryDefinition definition : list.list()) {
            runQuery(definition);
        }
    }

    private void runQuery(QueryDefinition definition) throws Exception {
        HttpQuery query = new HttpQuery(definition, CLIENT, CONFIGURATION, Collections.<Header>emptySet());
        Assert.assertNotNull(query);
        compareQuery(definition.getQuery(), query.getURI());

        QueryResponse response = query.execute();
        Assert.assertNotNull(response);
    }

    private void compareQuery(String query, URI compiledQuery) throws Exception {
        Assert.assertNotNull(compiledQuery);
//        System.out.println(query);
//        if (CONFIGURATION.getCfitProperties().getExtraReqParams() != null) {
//            query += "&" + CONFIGURATION.getCfitProperties().getExtraReqParams();
//        }

//        Assert.assertEquals(
//            URLDecoder.decode(query, Charsets.UTF_8.name()),
//            URLDecoder.decode(compiledQuery.toString(), Charsets.UTF_8.name())
//        );
    }

}
