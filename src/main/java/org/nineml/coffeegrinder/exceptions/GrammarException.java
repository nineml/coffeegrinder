package org.nineml.coffeegrinder.exceptions;

/**
 * Grammar exceptions.
 * <p>Grammar exceptions are generally errors in the grammar, use of a nonterminal
 * that has no rule defining it, for example.</p>
 */
public class GrammarException extends CoffeeGrinderException {
    /**
     * Grammar exception with a message.
     * @param message the message
     */
    public GrammarException(String message) {
        super(message);
    }

    /**
     * Grammar exception with an underlying cause.
     * @param message the message
     * @param cause the cause
     */
    public GrammarException(String message, Throwable cause) {
        super(message, cause);
    }
}
