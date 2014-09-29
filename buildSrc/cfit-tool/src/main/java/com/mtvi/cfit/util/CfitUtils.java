package com.mtvi.cfit.util;

import org.apache.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CfitUtils.
 *
 * @author zenind.
 */
public final class CfitUtils {

    /** Regexp pattern for locating parameters in string.*/
    private static final Pattern PARAMETER_SUBSTITUTION_PATTERN = Pattern.compile("(\\$\\{(.+?)\\})");
    /** Percentage fraction.*/
    private static final float PERCENTAGE_FRACTION = 100f;
    /** Kilobytes fraction.*/
    private static final int KILOBYTES_FRACTION = 1024;

    /** Hidden constructor.*/
    private CfitUtils() { }

    /**
     * Does substitution of parameters in given string from System properties.
     *
     * @param str - string.
     * @return - processed string.
     */
    public static String substituteParameter(String str) {
        Matcher matcher = PARAMETER_SUBSTITUTION_PATTERN.matcher(str);
        while (matcher.find()) {
            String value = System.getProperty(matcher.group(2));
            if (value != null) {
                str = str.replace(matcher.group(1), value);
            }
        }
        return str;
    }

    /**
     * Calculates size in Kilobytes.
     *
     * @param inBytes - initial size in bytes.
     * @return - size in kilobytes.
     */
    public static long inKB(long inBytes) {
        return inBytes / KILOBYTES_FRACTION;
    }

    /**
     * Checks whether status code is successful.
     * @param status - status code.
     * @return - <code>true</code> if status code is successful.
     */
    public static boolean isSuccessStatus(int status) {
        return HttpStatus.SC_OK == status;
    }

    /**
     * Calculate growth rate value diff of a <code>newValue</code> from <code>originalValue</code>.
     *
     * @param originalValue - original value.
     * @param newValue      - new value.
     * @return - percentage value.
     */
    public static int growthRate(long originalValue, long newValue) {
        return newValue > originalValue ? (int) ((newValue - originalValue) * PERCENTAGE_FRACTION / originalValue) : 0;
    }


    /**
     * Does slicing of list of items to groups of target type by provided slicing conditions.
     *
     * @param items - list of source items to
     * @param conditionsChecker - slicing conditions to check to slice accumulated during iteration items.
     * @param groupSlicer - create a new group of accumulated items(does slice op).
     * @param init - initializer of accumulator for items.
     * @param <GROUP> - group type.
     * @param <ITEM> - items type.
     * @param <ACC> - accumulator type.
     * @return - list of groups of items.
     */
    public static <GROUP, ITEM, ACC extends Accumulator<ITEM>> List<GROUP> slice(List<ITEM> items,
        SliceConditions<ITEM, ACC> conditionsChecker,  GroupSlicer<GROUP, ITEM, ACC> groupSlicer, AccumulatorInitializer<ITEM, ACC> init) {
        List<GROUP> groups = new ArrayList<GROUP>();
        if (items == null || items.size() == 0) {
            return groups;
        }

        ACC accumulator = init.init(null);

        for (ITEM item : items) {
            if (conditionsChecker.slice(accumulator)) {
                groups.add(groupSlicer.create(accumulator));
                accumulator = init.init(accumulator);
            }

            accumulator.add(item);
        }

        groups.add(groupSlicer.create(accumulator));

        return groups;
    }

    /**
     * Items accumulator interface.
     * @param <ITEM>
     */
    public interface Accumulator<ITEM> {
        /**
         * Registers item.
         * @param item - item to accumulate.
         */
        void add(ITEM item);

        /**
         * Returns list of accumulated items.
         * @return - list of items.
         */
        List<ITEM> items();
    }

    /**
     * Accumulator Initializer interface.
     * @param <ITEM> - item type.
     * @param <ACC> - accumulator type.
     */
    public interface AccumulatorInitializer<ITEM, ACC extends Accumulator<ITEM>> {
        /**
         * Initializes a new accumulator.
         * @param currentAccumulator - current accumulator.
         * @return - new accumulator.
         */
        ACC init(ACC currentAccumulator);
    }

    /**
     * Slicing conditions checker.
     * @param <ITEM> - item type.
     * @param <ACC> - accumulator type.
     */
    public interface SliceConditions<ITEM, ACC extends Accumulator<ITEM>> {
        /**
         * Checks whether slicing should be made.
         * @param accumulator - accumulator.
         * @return - <code>true</code> or <code>false</code>
         */
        boolean slice(ACC accumulator);
    }

    /**
     * Does actual slicing.
     * @param <GROUP> - slice group type.
     * @param <ITEM> - item type.
     * @param <ACC> - accumulator type.
     */
    public interface GroupSlicer<GROUP, ITEM, ACC extends Accumulator<ITEM>> {
        /**
         * Creates/Slices a new group.
         * @param accumulator - accumulator.
         * @return - group.
         */
        GROUP create(ACC accumulator);
    }
}
