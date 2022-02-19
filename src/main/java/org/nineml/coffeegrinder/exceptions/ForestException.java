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
    public ForestException(String code, String message) {
        super(code, message);
    }

    /**
     * An SPPF exception with an underlying cause.
     * @param message the message
     * @param cause the cause
     */
    public ForestException(String code, String message, Throwable cause) {
        super(code, message, cause);
    }

    private static ForestException getException(String code, String param) {
        return getException(code, new String[] {param});
    }

    private static ForestException getException(String code, String[] params) {
        return new ForestException(code, MessageGenerator.getMessage(code, params));
    }

    public static ForestException ioError(String filename, Exception ex) {
        String code = "F001";
        return new ForestException(code, MessageGenerator.getMessage(code, new String[] {filename}), ex);
    }
    public static ForestException noSuchNode(String node) { return getException("F002", node); }
}
