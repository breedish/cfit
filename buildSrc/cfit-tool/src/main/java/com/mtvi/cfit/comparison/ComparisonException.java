package com.mtvi.cfit.comparison;

import com.mtvi.cfit.CfitException;

/**
 * Comparison exception type.
 *
 * @author zenind.
 */
public final class ComparisonException extends CfitException {

    /** version id.*/
    private static final long serialVersionUID = 4060554196580730534L;

    /**
     * Constructor.
     *
     * @param message - error message.
     * @param cause - cause.
     */
    public ComparisonException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor.
     * @param cause - root cause.
     */
    public ComparisonException(Throwable cause) {
        super(cause);
    }
}
