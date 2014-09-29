package com.mtvi.cfit.comparison.report;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Comparison stats.
 *
 * @author Dzmitry_Zenin
 */
public final class ComparisonStats {
    /** Number of identical responses.*/
    @XmlAttribute
    private int identical;
    /** Number of similar responses.*/
    @XmlAttribute
    private int similar;
    /** Number of different responses.*/
    @XmlAttribute
    private int different;
    /** Number of only original responses.*/
    @XmlAttribute
    private int onlyOriginal;
    /** Number of only rc responses.*/
    @XmlAttribute
    private int onlyRC;

    /** Default constructor.*/
    public ComparisonStats() { }

    /**
     * Constructor.
     * @param identical - identical value.
     * @param similar - similar value.
     * @param different - diff value.
     * @param onlyOriginal - original value.
     * @param onlyRC - rc value.
     */
    public ComparisonStats(int identical, int similar, int different, int onlyOriginal, int onlyRC) {
        this.identical = identical;
        this.similar = similar;
        this.different = different;
        this.onlyOriginal = onlyOriginal;
        this.onlyRC = onlyRC;
    }

    /** Registers identical response.*/
    public void registerIdentical() {
        identical++;
    }

    /** Registers similar response.*/
    public void registerSimilar() {
        similar++;
    }

    /** Registers different response.*/
    public void registerDifferent() {
        this.different++;
    }

    /** Registers only original response.*/
    public void registerOnlyOriginal() {
        this.onlyOriginal++;
    }

    /** Registers only rc response.*/
    public void registerOnlyRC() {
        this.onlyRC++;
    }

    public int getIdentical() {
        return identical;
    }

    public int getSimilar() {
        return similar;
    }

    public int getDifferent() {
        return different;
    }

    public int getOnlyOriginal() {
        return onlyOriginal;
    }

    public int getOnlyRC() {
        return onlyRC;
    }

}
