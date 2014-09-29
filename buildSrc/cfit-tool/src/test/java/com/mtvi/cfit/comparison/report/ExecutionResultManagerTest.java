package com.mtvi.cfit.comparison.report;

import com.mtvi.cfit.CfitConfiguration;
import com.mtvi.cfit.exec.ExecutionPhaseResult;
import com.mtvi.cfit.exec.ExecutionResultManager;
import com.mtvi.cfit.query.definition.QueryDefinition;
import com.mtvi.cfit.query.response.QueryResponse;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;

/**
 * Tests for {@link com.mtvi.cfit.exec.ExecutionResultManager}.
 *
 * @author zenind
 */
public class ExecutionResultManagerTest {

    private ExecutionResultManager manager;

    private CfitConfiguration configuration;

    private ExecutionPhaseResult result;

    private File sampleResult;

    @Before
    public void init() throws Exception {
        configuration = new CfitConfiguration();

        sampleResult = new File(ExecutionResultManagerTest.class.getResource("/result/sample_execution_result.xml").toURI());
        configuration.setCfitHome(sampleResult.getParentFile().getParentFile().getAbsolutePath());
        configuration.setQueriesDirName("queries");
        configuration.setRcResultsDir(sampleResult.getParent());
        configuration.setExecutionStatsFileName("sample_execution_result.xml");
        manager = new ExecutionResultManager(configuration);

        result = getResult();
    }

    @Test
    public void testSaveOperation() throws Exception {
        ExecutionPhaseResult actual = manager.save(result, new File(configuration.getRcResultsDir()));
        Assert.assertNotNull(actual);

        ExecutionPhaseResult expected = manager.load(sampleResult);
        Assert.assertNotNull(expected);
        Assert.assertEquals(expected, actual);

        Assert.assertEquals(3, actual.getSuccess());
        Assert.assertEquals(2, actual.getExceptions());
        Assert.assertEquals(2, actual.getFailures());
        Assert.assertEquals(11, actual.getTotal());
        Assert.assertEquals(7, actual.getCount());
    }

    private ExecutionPhaseResult getResult() {
        ExecutionPhaseResult result = new ExecutionPhaseResult(11);

        result.registerError();
        result.registerError();

        result.registerResponse(new QueryResponse(Mockito.mock(QueryDefinition.class), "localhost:12042/jp", "", 200, 20));
        result.registerResponse(new QueryResponse(Mockito.mock(QueryDefinition.class), "localhost:12042/jp", "", 200, 15));
        result.registerResponse(new QueryResponse(Mockito.mock(QueryDefinition.class), "localhost:12042/jp", "", 200, 10));
        result.registerResponse(new QueryResponse(Mockito.mock(QueryDefinition.class), "localhost:12042/jp", "", 503, 5));
        result.registerResponse(new QueryResponse(Mockito.mock(QueryDefinition.class), "localhost:12042/jp", "", 404, 5));

        return result;
    }

}
