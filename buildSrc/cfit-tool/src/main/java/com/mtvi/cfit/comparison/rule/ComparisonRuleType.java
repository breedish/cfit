package com.mtvi.cfit.comparison.rule;

/**
* Types of {@link com.mtvi.cfit.comparison.rule.ComparisonRule}.
*
* @author zenind
*/
public enum ComparisonRuleType {

    /** JSON based rule type.*/
    JSON_NODE_EXCLUSION("json"),
    /** REGEXP base rule type.*/
    REGEXP_EXCLUSION("regexp");
    /** type name.*/
    private String typeName;

    /**
     * Constructor.
     * @param typeName - type name.
     */
    ComparisonRuleType(String typeName) {
        this.typeName = typeName;
    }

    /** Gets a type name.*/
    public String typeName() {
        return this.typeName;
    }

}
