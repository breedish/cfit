package com.mtvi.cfit.comparison.report;

/**
 * @author Dzmitry_Zenin
 */
public enum ReportState {

    /**
     * State that states that report has no differences.
     */
    NO_DIFFERENCES("NO DIFFERENCES"),

    /**
     * State that states that report has differences.
     */
    HAVE_DIFFERENCES("HAS DIFFERENCES");

    /** Actual state name. */
    private final String name;

    private ReportState(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
