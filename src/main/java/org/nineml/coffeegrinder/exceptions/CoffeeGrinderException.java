package org.nineml.coffeegrinder.exceptions;

/**
 * Exceptions raised by this API.
 * <p>With a few exceptions (@{link NullPointerException} and {@link IllegalArgumentException}, for example),
 * subclasses of {@link CoffeeGrinderException} are used for all exceptions raised by this API.</p>
 */
public abstract class CoffeeGrinderException extends RuntimeException {
    /**
     * An exception with a message.
     * @param message the message
     */
    public CoffeeGrinderException(String message) {
        super(message);
    }

    /**
     * An exception with an underlying cause.
     * @param message the message
     * @param cause the cause
     */
    public CoffeeGrinderException(String message, Throwable cause) {
        super(message,cause);
    }
}
