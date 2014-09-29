package com.mtvi.cfit.comparison.comparator;

import com.mtvi.cfit.comparison.ComparisonException;
import com.mtvi.cfit.comparison.ResponseComparisonResult;

import java.io.File;

/**
 * File content comparator.
 *
 * @author yushkour.
 */
public interface FileComparator {

    /**
     * Does content comparison for given files.
     * @param left - file
     * @param right - file
     * @return - comparison result.
     * @see com.mtvi.cfit.comparison.ResponseComparisonResult
     * @throws ComparisonException - in case issues during comparison.
     */
    ResponseComparisonResult compare(File left, File right) throws ComparisonException;
    
}
