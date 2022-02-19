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
    public TreeWalkerException(String code, String message) {
        super(code, message);
    }

    /**
     * Grammar exception with an underlying cause.
     * @param message the message
     * @param cause the cause
     */
    public TreeWalkerException(String code, String message, Throwable cause) {
        super(code, message, cause);
    }

    private static TreeWalkerException getException(String code) {
        return new TreeWalkerException(code, MessageGenerator.getMessage(code));
    }

    public static TreeWalkerException noTreesSelected() { return getException("T001"); }
    public static TreeWalkerException noMoreTrees() { return getException("T002"); }
    public static TreeWalkerException internalError() { return getException("T003"); }
}
