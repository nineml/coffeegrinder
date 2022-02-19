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
    public GrammarException(String code, String message) {
        super(code, message);
    }

    /**
     * Grammar exception with an underlying cause.
     * @param message the message
     * @param cause the cause
     */
    public GrammarException(String code, String message, Throwable cause) {
        super(code, message, cause);
    }

    private static GrammarException getException(String code) {
        return getException(code, new String[] {});
    }

    private static GrammarException getException(String code, String param) {
        return getException(code, new String[] {param});
    }

    private static GrammarException getException(String code, String[] params) {
        return new GrammarException(code, MessageGenerator.getMessage(code, params));
    }

    public static GrammarException grammarIsClosed() { return getException("G001"); }
    public static GrammarException noRuleForSymbol(String name) { return getException("E001", name); }
    public static GrammarException invalidCharacterClass(String name) { return getException("E002", name); }
}
