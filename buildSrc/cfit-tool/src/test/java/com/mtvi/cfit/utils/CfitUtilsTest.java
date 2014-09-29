package com.mtvi.cfit.utils;

import com.mtvi.cfit.util.CfitUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.Semaphore;

/**
 * Tests for {@link com.mtvi.cfit.util.CfitUtils}.
 *
 * @author zenind.
 */
public class CfitUtilsTest {
    
    @Test
    public void testReplaceVariables() {
        System.setProperty("a", "A");
        System.setProperty("b", "B");
        System.setProperty("c", "C");
        Assert.assertEquals("text", CfitUtils.substituteParameter("text"));
        Assert.assertEquals("A B", CfitUtils.substituteParameter("${a} ${b}"));
        Assert.assertEquals("sAs C ${d} sBs", CfitUtils.substituteParameter("s${a}s ${c} ${d} s${b}s"));
    }

    @Test
    public void testGrowthRateCalculation() {
        Assert.assertEquals(10, CfitUtils.growthRate(100, 110));
        Assert.assertEquals(65, CfitUtils.growthRate(100, 165));
        Assert.assertEquals(56, CfitUtils.growthRate(50, 78));
        Assert.assertEquals(41, CfitUtils.growthRate(12, 17));
    }

    @Test
    public void testKBConversion() {
        Assert.assertEquals(2, CfitUtils.inKB(2048));
    }

}
