package org.nineml.coffeegrinder.exceptions;

/**
 * Tree walking exceptions.
 * <p>Tree walking exceptions are generally errors in the implementation of the TreeWalker.</p>
 */
public class TreeWalkerException extends CoffeeGrinderException {
    /**
     * Grammar exception with a message.
     * @param message the message
     */
    public TreeWalkerException(String message) {
        super(message);
    }

    /**
     * Grammar exception with an underlying cause.
     * @param message the message
     * @param cause the cause
     */
    public TreeWalkerException(String message, Throwable cause) {
        super(message, cause);
    }
}
