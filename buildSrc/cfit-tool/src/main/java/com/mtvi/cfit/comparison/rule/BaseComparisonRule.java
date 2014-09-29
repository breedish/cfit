package com.mtvi.cfit.comparison.rule;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.util.List;

/**
 * BaseComparisonRule class.
 *
 * @author zenind.
 */
abstract class BaseComparisonRule implements ComparisonRule {
    /** Rule alias name.*/
    private String alias;
    /** Rule values.*/
    private List<String> values;

    protected BaseComparisonRule(String alias, List<String> values) {
        if (alias == null || values == null) {
            throw new IllegalArgumentException(String.format(
                    "Null or blank or wrong value has been passed in as required argument [alias=%s, values=%s]", alias, values));
        }
        this.alias = alias;
        this.values = values;
    }

    public String getAlias() {
        return alias;
    }

    public List<String> getValues() {
        return values;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof BaseComparisonRule)) {
            return false;
        }

        BaseComparisonRule that = (BaseComparisonRule) o;

        EqualsBuilder builder = new EqualsBuilder();
        return builder.append(this.getAlias(), that.getAlias()).isEquals();
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder();
        return builder.append(this.getAlias()).toHashCode();
    }

    @Override
    public String toString() {
        return getAlias();
    }
}
