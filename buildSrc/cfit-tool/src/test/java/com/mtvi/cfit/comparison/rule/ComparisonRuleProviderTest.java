package com.mtvi.cfit.comparison.rule;

import junit.framework.Assert;
import org.junit.Test;

import java.io.InputStream;
import java.util.List;

/**
 * ConfigurationFileComparisonRuleProviderTest class.
 */
public class ComparisonRuleProviderTest {

    @Test
    public void testLoadingOfRegexpExclusion() {
        InputStream config = ComparisonRuleProviderTest.class.getResourceAsStream("/comparison/comparison.yml");

        ComparisonRuleProvider provider = ComparisonRuleProviderFactory.getRulesProvider(config);
        List<ComparisonRule> rules = provider.getRules();

        Assert.assertNotNull(rules);
        Assert.assertEquals(9, rules.size());
        Assert.assertEquals("Remove xml related declarations", rules.get(0).getAlias());
    }
}
