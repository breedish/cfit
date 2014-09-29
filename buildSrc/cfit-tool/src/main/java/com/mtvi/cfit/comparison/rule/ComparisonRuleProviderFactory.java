package com.mtvi.cfit.comparison.rule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Factory for {@link com.mtvi.cfit.comparison.rule.ComparisonRuleProvider}.
 *
 * @author zenind.
 */
public final class ComparisonRuleProviderFactory {

    /** Log.*/
    private static final Logger LOG = LoggerFactory.getLogger(ComparisonRuleProviderFactory.class);

    /** Hidden constructor.*/
    private ComparisonRuleProviderFactory() { }

    /**
     * Finds a rule provider for given config.
     * @param config - rule config file.
     * @return - comparison rule provider.
     */
    public static ComparisonRuleProvider getRulesProvider(File config) {
        try {
            return getRulesProvider(new FileInputStream(config));
        } catch (Exception e) {
            return new EmptyComparisonRuleProvider();
        }
    }

    /**
     * Finds a rule provider for given config.
     * @param inputStream - rule config input stream.
     * @return - comparison rule provider.
     */
    public static ComparisonRuleProvider getRulesProvider(InputStream inputStream) {
        try {
            Yaml yaml = new Yaml();
            YamlComparisonRuleConfig config = yaml.loadAs(inputStream, YamlComparisonRuleConfig.class);

            List<ComparisonRule> comparisonRules = new ArrayList<ComparisonRule>();
            for (YamlComparisonRuleDefinition rule : config.getExclusions()) {
                if (ComparisonRuleType.JSON_NODE_EXCLUSION.typeName().equalsIgnoreCase(rule.getType())) {
                    comparisonRules.add(new JsonNodeExclusionRule(rule.getAlias(), rule.getValues()));
                } else if (ComparisonRuleType.REGEXP_EXCLUSION.typeName().equalsIgnoreCase(rule.getType())) {
                    comparisonRules.add(new RegExpExclusionRule(rule.getAlias(), rule.getValues()));
                }
            }

            return new DefaultComparisonRuleProvider(comparisonRules);
        } catch (Exception e) {
            LOG.error("Error during initialization/parsing of comparison rule config. No exclusion rules will be applied", e);
        }

        return new EmptyComparisonRuleProvider();
    }

    /**
     * Empty rules provider.
     */
    private static final class EmptyComparisonRuleProvider implements ComparisonRuleProvider {
        @Override
        public List<ComparisonRule> getRules() {
            return Collections.emptyList();
        }
    }

}



