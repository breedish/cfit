package com.mtvi.cfit.comparison.comparator;

import com.mtvi.cfit.comparison.ComparisonException;
import com.mtvi.cfit.comparison.ResponseComparisonResult;
import com.mtvi.cfit.comparison.rule.ComparisonRule;
import com.mtvi.cfit.comparison.rule.ComparisonRuleProvider;
import com.mtvi.cfit.comparison.rule.ComparisonRuleProviderFactory;
import com.mtvi.cfit.util.IOUtils;

import java.io.File;
import java.io.IOException;

/**
 * Text content comparator.
 *
 * @author zenind.
 */
public class TextComparator implements FileComparator {

    /** Exclusion rule provider.*/
    private final ComparisonRuleProvider ruleProvider;

    /**
     * Constructor.
     * @param config - config.
     */
    public TextComparator(File config) {
        ruleProvider = ComparisonRuleProviderFactory.getRulesProvider(config);
    }

    @Override
    public ResponseComparisonResult compare(File left, File right) throws ComparisonException {
        try {
            String leftContent = IOUtils.getContent(left, 1, "".intern());
            String rightContent = IOUtils.getContent(right, 1, "".intern());

            if (leftContent.equals(rightContent)) {
                return ResponseComparisonResult.IDENTICAL;
            }

            for (ComparisonRule rule : ruleProvider.getRules()) {
                leftContent = rule.apply(leftContent);
                rightContent = rule.apply(rightContent);
                if (leftContent.equals(rightContent)) {
                    return ResponseComparisonResult.SIMILAR;
                }
            }

            return ResponseComparisonResult.DIFFERENT;
        } catch (IOException e) {
            throw new ComparisonException(e);
        }
    }

}
