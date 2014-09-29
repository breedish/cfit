package com.mtvi.cfit;

/**
 * Exception for issues during cfit execution.
 *
 * @author Dzmitry_Zenin
 */
public class CfitException extends Exception {

    /**
     * Constructor.
     *
     * @param message - error message.
     */
    public CfitException(String message) {
        super(message);
    }

    /**
     * Constructor.
     *
     * @param cause - - root cause of an error.
     */
    public CfitException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor.
     *
     * @param message - error message.
     * @param cause - root cause of an error.
     */
    public CfitException(String message, Throwable cause) {
        super(message, cause);
    }

}
