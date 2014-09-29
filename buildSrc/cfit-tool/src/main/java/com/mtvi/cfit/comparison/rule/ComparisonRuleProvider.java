package com.mtvi.cfit.comparison.rule;

import java.util.List;

/**
 * ComparisonRuleProvider class.
 *
 * @author zenind.
 */
public interface ComparisonRuleProvider {

    /**
     * @return list of rules to apply during comparison phase.
     */
    List<ComparisonRule> getRules();

}
