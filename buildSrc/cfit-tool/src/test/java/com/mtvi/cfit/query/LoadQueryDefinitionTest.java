package com.mtvi.cfit.query;

import com.mtvi.cfit.CfitConfiguration;
import com.mtvi.cfit.query.definition.CompositeQueryDefinitionList;
import com.mtvi.cfit.query.definition.FileBasedQueryDefinitionProvider;
import com.mtvi.cfit.query.definition.QueryDefinition;
import com.mtvi.cfit.query.definition.QueryDefinitionProvider;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Test location and loading of queries to be run during CFIT.
 *
 * @author zenind
 */
public class LoadQueryDefinitionTest {

    private static QueryDefinitionProvider PROVIDER;

    @BeforeClass
    public static void setup() throws Exception{
        CfitConfiguration config = new CfitConfiguration();

        File sampleQuery = new File(LoadQueryDefinitionTest.class.getResource("/queries/queries-source.q").toURI());
        config.setCfitHome(sampleQuery.getParentFile().getParentFile().getAbsolutePath());
        config.setQueriesDirName("queries");

        PROVIDER = new FileBasedQueryDefinitionProvider(config);
    }

    @Test
    public void testSourcesAreLocated() throws Exception {
        CompositeQueryDefinitionList list = getQueries();
        Assert.assertNotNull(list);
        Assert.assertEquals(3, list.getLists().size());
    }

    @Test
    public void testCompositeListIteration() throws Exception {
        CompositeQueryDefinitionList list = getQueries();
        Assert.assertNotNull(list);

        int quantity = 0;
        for (QueryDefinition query : list.list()) {
            Assert.assertNotNull(query);
            Assert.assertNotNull(query.getId());
            Assert.assertNotNull(query.getQuery());
            quantity++;
        }

        Assert.assertEquals(17, quantity);
    }

    @Test
    public void testCompositeListIterator() throws Exception {
        CompositeQueryDefinitionList list = getQueries();

        QueryDefinition definition = list.list().iterator().next();
        Assert.assertNotNull(definition);
    }

    private CompositeQueryDefinitionList getQueries() throws IOException {
        return (CompositeQueryDefinitionList) PROVIDER.load();
    }

}
