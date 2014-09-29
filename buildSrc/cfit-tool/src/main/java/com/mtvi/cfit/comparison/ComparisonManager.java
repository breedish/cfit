package com.mtvi.cfit.comparison;

import com.mtvi.cfit.CfitException;
import com.mtvi.cfit.comparison.report.ComparisonReport;

/**
 * Comparison manager.
 *
 * @author Dzmitry_Zenin
 */
public interface ComparisonManager {

    /***
     * Does comparison with the help of given comparator.
     * @return - comparison report.
     * @throws CfitException - in case of conversion/comparison issues.
     */
    ComparisonReport compareResponses() throws CfitException;

}
