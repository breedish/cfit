package com.mtvi.cfit.comparison.rule;

import java.util.List;

/**
 * Default implementation of {@link com.mtvi.cfit.comparison.rule.ComparisonRuleProvider}.
 * <p>
 *     Used as a container of rules defined in config.
 * </p>
 *
 * @author zenind.
 */
public final class DefaultComparisonRuleProvider implements ComparisonRuleProvider {
    /** Container.*/
    private final List<ComparisonRule> rules;

    /**
     * Constructor.
     * @param rules - container.
     */
    public DefaultComparisonRuleProvider(List<ComparisonRule> rules) {
        if (rules == null) {
            throw new IllegalArgumentException(String.format(
                    "Null or blank or wrong value has been passed in as required argument [rules=null]"));
        }
        this.rules = rules;
    }

    public List<ComparisonRule> getRules() {
        return rules;
    }

}
