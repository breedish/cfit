package com.mtvi.cfit.comparison;

import com.mtvi.cfit.comparison.comparator.TextComparator;
import junit.framework.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

public class TextComparatorTest {

    static TextComparator COMPARATOR;

    @BeforeClass
    public static void init() {
        COMPARATOR = new TextComparator(getResource("comparison.yml"));
    }

    @Test
    public void testJsonPathResponses() throws Exception {
        checkComparison("or1", "rc1", ResponseComparisonResult.SIMILAR);
        checkComparison("or2", "rc2", ResponseComparisonResult.SIMILAR);
        checkComparison("or3", "rc3", ResponseComparisonResult.SIMILAR);
        checkComparison("or4", "rc4", ResponseComparisonResult.SIMILAR);
        checkComparison("original_jp1", "rc_jp1", ResponseComparisonResult.DIFFERENT);
    }

    @Test
    public void testXmlResponses() throws Exception {
        checkComparison("simple_original", "simple_original", ResponseComparisonResult.IDENTICAL);
        checkComparison("simple_original", "simple_staging_1", ResponseComparisonResult.DIFFERENT);
        checkComparison("simple_original", "simple_staging_2", ResponseComparisonResult.SIMILAR);
        checkComparison("simple_original", "simple_staging_3", ResponseComparisonResult.DIFFERENT);
        checkComparison("complex_original", "complex_original", ResponseComparisonResult.IDENTICAL);
        checkComparison("complex_original", "complex_staging_1", ResponseComparisonResult.DIFFERENT);
        checkComparison("src_element_original", "src_element_original", ResponseComparisonResult.IDENTICAL);
        checkComparison("src_element_original", "src_element_staging", ResponseComparisonResult.SIMILAR);
        checkComparison("status_field_original_1", "status_field_staging_1", ResponseComparisonResult.SIMILAR);
        checkComparison("status_field_original_2", "status_field_staging_2", ResponseComparisonResult.SIMILAR);
    }

    private void checkComparison(String original, String rc, ResponseComparisonResult expectedResult) throws ComparisonException {
        Assert.assertEquals(expectedResult, COMPARATOR.compare(getResource(original), getResource(rc)));
    }

    private static File getResource(String resourceName) {
        return new File(TextComparator.class.getResource("/comparison/" + resourceName).getPath());
    }
}
