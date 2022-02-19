package org.nineml.coffeegrinder.exceptions;

/**
 * Compiler exceptions.
 * <p>Compiler exceptions identify problems encountered attempting to compile
 * a grammar or parse a compiled grammar.</p>
 */
public class CompilerException extends GrammarException {
    /**
     * Grammar exception with a message.
     * @param message the message
     */
    public CompilerException(String code, String message) {
        super(code, message);
    }

    /**
     * Grammar exception with an underlying cause.
     * @param message the message
     * @param cause the cause
     */
    public CompilerException(String code, String message, Throwable cause) {
        super(code, message, cause);
    }

    private static CompilerException getException(String code) {
        return getException(code, new String[] {});
    }

    private static CompilerException getException(String code, String param) {
        return getException(code, new String[] {param});
    }

    private static CompilerException getException(String code, String param1, String param2) {
        return getException(code, new String[] {param1, param2});
    }

    private static CompilerException getException(String code, String[] params) {
        return new CompilerException(code, MessageGenerator.getMessage(code, params));
    }

    public static CompilerException messageDigestError(String hash, String reason) { return getException("C001", hash, reason); }
    public static CompilerException unexpectedCharacterSet(String charset) { return getException("C002", charset); }
    public static CompilerException unexpectedTerminalTokenClass(String tokenClass) { return getException("C003", tokenClass); }
    public static CompilerException invalidNameEscaping(String escape, String name) { return getException("C004", escape, name); }
    public static CompilerException errorReadingGrammar(String message) { return getException("C005", message); }
    public static CompilerException notAGrammar(String namespace) { return getException("C006", namespace); }
    public static CompilerException unexpectedElement(String name) { return getException("C007", name); }
    public static CompilerException noVersionProvided() { return getException("C008"); }
    public static CompilerException unsupportedVersion(String version) { return getException("C009", version); }
    public static CompilerException checkumFailed() { return getException("C010"); }
    public static CompilerException missingXmlId(String name) { return getException("C011", name); }
    public static CompilerException missingAttributeGroup(String id) { return getException("C012", id); }
    public static CompilerException unexpectedFlag(String flag) { return getException("C013", flag); }
    public static CompilerException invalidGramamr(String message) { return getException("C014", message); }
    public static CompilerException textNotAllowed(String text) { return getException("C015", text); }
}
