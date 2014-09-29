package com.mtvi.cfit.comparison;

import com.mtvi.cfit.comparison.comparator.FileComparator;
import com.mtvi.cfit.comparison.comparator.XmlComparator;
import junit.framework.Assert;
import org.junit.Test;

import java.io.File;

public class XmlComparatorTest {

    @Test
    public void testComparison() throws Exception {
        FileComparator comp = new XmlComparator();

        check(comp, ResponseComparisonResult.IDENTICAL, res("/comparison/simple_original"), res("/comparison/simple_original"));
        check(comp, ResponseComparisonResult.DIFFERENT, res("/comparison/simple_original"), res("/comparison/simple_staging_1"));
        check(comp, ResponseComparisonResult.IDENTICAL, res("/comparison/simple_original"), res("/comparison/simple_staging_2"));
        check(comp, ResponseComparisonResult.DIFFERENT, res("/comparison/simple_original"), res("/comparison/simple_staging_3"));
        check(comp, ResponseComparisonResult.IDENTICAL, res("/comparison/complex_original"), res("/comparison/complex_original"));
        check(comp, ResponseComparisonResult.DIFFERENT, res("/comparison/complex_original"), res("/comparison/complex_staging_1"));
    }

    private void check(FileComparator comparator, ResponseComparisonResult expected, File original, File rc) throws ComparisonException {
        ResponseComparisonResult result = comparator.compare(original, rc);
        Assert.assertEquals(String.format("Issue with %s and %s files", original, rc), expected, result);
    }

    private File res(String resourcePath) {
        return new File(getClass().getResource(resourcePath).getPath());
    }

}
