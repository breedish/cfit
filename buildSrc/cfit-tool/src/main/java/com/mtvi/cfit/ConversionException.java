package com.mtvi.cfit;

/**
 * @author Dzmitry_Zenin
 */
public class ConversionException extends CfitException {

    /**
     * Constructor.
     * @param message - error message.
     * @param cause - root issue.
     */
    public ConversionException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor.
     * @param message - error message.
     */
    public ConversionException(String message) {
        super(message);
    }
}
