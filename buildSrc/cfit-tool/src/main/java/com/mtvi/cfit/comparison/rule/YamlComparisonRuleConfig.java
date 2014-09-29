package com.mtvi.cfit.comparison.rule;

import java.util.List;

/**
 * Comparison rule config view based on YAML.
 *
 * @author zenind.
 */
public final class YamlComparisonRuleConfig {

    /** List of exclusions provided in config.*/
    private List<YamlComparisonRuleDefinition> exclusions;

    public List<YamlComparisonRuleDefinition> getExclusions() {
        return exclusions;
    }

    @SuppressWarnings("unused")
    public void setExclusions(List<YamlComparisonRuleDefinition> exclusions) {
        this.exclusions = exclusions;
    }
}
