package com.mtvi.cfit.comparison.rule;

import java.util.List;

/**
 * Exclusion rule for definition that is used during response conversion.
 *
 * @author zenind.
 * @see com.mtvi.cfit.comparison.report.ComparisonReport
 * @see com.mtvi.cfit.comparison.ComparisonManager
 */
public final class YamlComparisonRuleDefinition {
    /** Rule name.*/
    private String alias;
    /** Rule type.*/
    private String type;
    /** Rule values.*/
    private List<String> values;
    /** Default constructor.*/
    public YamlComparisonRuleDefinition() { }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }
}
