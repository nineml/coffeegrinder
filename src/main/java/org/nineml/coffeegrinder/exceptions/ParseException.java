package org.nineml.coffeegrinder.exceptions;

/**
 * Parse exceptions.
 * <p>Parse exceptions are generally errors in the API, or uses of the API.</p>
 */
public class ParseException extends CoffeeGrinderException {
    /**
     * An parse exception with a message.
     * @param message the message
     */
    public ParseException(String message) {
        super(message);
    }

    /**
     * An parse exception with an underlying cause.
     * @param message the message
     * @param cause the cause
     */
    public ParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
