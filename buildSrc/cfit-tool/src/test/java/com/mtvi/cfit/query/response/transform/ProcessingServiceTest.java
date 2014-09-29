package com.mtvi.cfit.query.response.transform;

import com.mtvi.cfit.CfitConfiguration;
import com.mtvi.cfit.query.response.ToFileQueryResponseManager;
import com.mtvi.cfit.query.response.QueryResponse;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Pattern;

/**
 * Tests for {@link BasicTransformationManager}.
 *
 * @author Kiryl_Dubarenka
 */
public class ProcessingServiceTest {

    private static final String INITIAL_RESPONSE_REGEXP = "^(.*)_initial\\.txt$";
    private static final Pattern INITIAL_RESPONSE = Pattern.compile(INITIAL_RESPONSE_REGEXP);

    private BasicTransformationManager service;

    private ToFileQueryResponseManager queryResponseManager;

    @Before
    public void setUp() {
        queryResponseManager = new ToFileQueryResponseManager();
        service = new BasicTransformationManager(new CfitConfiguration(), queryResponseManager);
    }

    @After
    public void tearDown() {
        service = null;
        queryResponseManager = null;
    }

    @Test
    public void process() throws Exception {
        File responsesDir = new File(getClass().getResource("/jsonpath-responses").toURI());
        File[] responses = responsesDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return INITIAL_RESPONSE.matcher(name).matches();
            }
        });
        for (File response : responses) {
            QueryResponse actual = service.transform(queryResponseManager.load(response));
            QueryResponse expected = queryResponseManager.load(getExpectedResponseFile(response));
            Assert.assertEquals("Response URL is not correct.", expected.getUrl(), actual.getUrl());
            Assert.assertEquals("Response body is not correct.", expected.getContent(), actual.getContent());
        }
    }

    private File getExpectedResponseFile(File initialResponse) {
        return new File(
            initialResponse.getParent(),
            initialResponse.getName().replaceAll(INITIAL_RESPONSE_REGEXP, "$1_expected.txt")
        );
    }

}
