package com.mtvi.cfit.comparison.rule;

/**
 * Rule that should be applied for content before during/before comparison to make needed changes to content.
 *
 * @author zenind.
 */
public interface ComparisonRule {

    /**
     * Changes given content in some way.
     * @param content - initial content.
     * @return - transformed content.
     */
    String apply(String content);

    /**
     * @return alias for rule.
     */
    String getAlias();
}
