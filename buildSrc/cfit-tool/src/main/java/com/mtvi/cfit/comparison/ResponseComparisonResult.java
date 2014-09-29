package com.mtvi.cfit.comparison;

/**
* Response comparison status.
*
* @author zenind
*/
public enum ResponseComparisonResult {

    /**
     * Responses are identical.
     */
    IDENTICAL,

    /**
     * Responses are similar(with applied comparison rules to responses).
     * @see com.mtvi.cfit.comparison.rule.ComparisonRule
     */
    SIMILAR,

    /**
     * Responses are different.
     */
    DIFFERENT
}
