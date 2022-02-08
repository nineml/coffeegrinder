package org.nineml.coffeegrinder.exceptions;

/**
 * Exceptions that arise processing the attributes of a symbol or token.
 * <p>These are generally caused by errors in how the API is used.</p>
 */
public class AttributeException extends CoffeeGrinderException {
    /**
     * An attribute exception with a message.
     * @param message the message
     */
    public AttributeException(String message) {
        super(message);
    }

    /**
     * An attribute exception with an underlying cause.
     * @param message the message
     * @param cause the cause
     */
    public AttributeException(String message, Throwable cause) {
        super(message, cause);
    }
}
