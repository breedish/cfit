package com.mtvi.cfit.comparison.report.converter.vm;

import java.io.File;

/**
 * Represents a link to sub part(detailed part) of report.
 *
 * @author zenind
 */
public class Link {

    /** Name of a linked report sub part.*/
    private final String name;
    /** Path to a linked report sub part.*/
    private final File path;

    /**
     * Constructor.
     * @param name - name.
     * @param path - path.
     */
    public Link(String name, File path) {
        this.name = name;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public File getPath() {
        return path;
    }
}
