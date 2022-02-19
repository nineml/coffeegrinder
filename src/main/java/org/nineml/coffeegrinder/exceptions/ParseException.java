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
    public ParseException(String code, String message) {
        super(code, message);
    }

    /**
     * An parse exception with an underlying cause.
     * @param message the message
     * @param cause the cause
     */
    public ParseException(String code, String message, Throwable cause) {
        super(code, message, cause);
    }

    private static ParseException getException(String code) {
        return getException(code, new String[] {});
    }

    private static ParseException getException(String code, String param) {
        return getException(code, new String[] {param});
    }

    private static ParseException getException(String code, String param1, String param2) {
        return getException(code, new String[] {param1, param2});
    }

    private static ParseException getException(String code, String[] params) {
        return new ParseException(code, MessageGenerator.getMessage(code, params));
    }

    public static ParseException seedNotInGrammar(String seed) { return getException("P001", seed); }
    public static ParseException attemptToContinueInvalidParse() { return getException("P002"); }
    public static ParseException internalError(String reason) { return getException("P003", reason); }

}
