package com.mtvi.cfit.comparison.rule;

import java.util.List;

/**
 * REGEXP based exclusion rule.
 * <p>
 *     Does removal of content by regexp.
 * </p>
 *
 * @author zenind.
 */
public final class RegExpExclusionRule extends BaseComparisonRule {

    /** Replacement content.*/
    private static final String REPLACEMENT_CONTENT = "";

    /**
     * Constructor.
     * @param alias - rule name.
     * @param regexps - reg exp values.
     */
    public RegExpExclusionRule(String alias, List<String> regexps) {
        super(alias, regexps);
    }

    @Override
    public String apply(String content) {
        for (String regexp : getValues()) {
            content = content.replaceAll(regexp, REPLACEMENT_CONTENT);
        }
        return content;
    }
    
}
