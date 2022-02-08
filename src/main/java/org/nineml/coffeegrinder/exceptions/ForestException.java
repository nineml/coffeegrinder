package org.nineml.coffeegrinder.exceptions;

/**
 * Exceptions that arise processing the shared packed parse forest (SPPF).
 * <p>These are generally caused by errors in how the API is used.</p>
 */
public class ForestException extends CoffeeGrinderException {
    /**
     * An SPPF exception with a message.
     * @param message the message
     */
    public ForestException(String message) {
        super(message);
    }

    /**
     * An SPPF exception with an underlying cause.
     * @param message the message
     * @param cause the cause
     */
    public ForestException(String message, Throwable cause) {
        super(message, cause);
    }
}
