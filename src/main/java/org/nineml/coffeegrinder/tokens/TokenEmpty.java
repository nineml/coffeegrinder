package org.nineml.coffeegrinder.tokens;

import org.nineml.coffeegrinder.util.ParserAttribute;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

/**
 * A single character {@link Token}.
 */
public class TokenEmpty extends Token {
    public static final TokenEmpty EMPTY = new TokenEmpty(null);

    private TokenEmpty(Collection<ParserAttribute> attributes) {
        super(attributes);
    }

    /**
     * Create a token for the specified character.
     * @return a token
     */
    public static TokenEmpty get() {
        return new TokenEmpty(null);
    }

    /**
     * Create a token for the specified character.
     * @param attribute the attribute
     * @return a token
     */
    public static TokenEmpty get(ParserAttribute attribute) {
        if (attribute == null) {
            throw new NullPointerException("Token attribute must not be null");
        }
        return new TokenEmpty(Collections.singletonList(attribute));
    }

    /**
     * Create a token for the specified character.
     * @param attributes the attributes
     * @return a token
     */
    public static TokenEmpty get(Collection<ParserAttribute> attributes) {
        return new TokenEmpty(attributes);
    }

    /**
     * Return the value of this token (its character).
     * @return The character value of the token.
     */
    public String getValue() {
        return "";
    }

    /**
     * Does this token match the input?
     * <p>This token matches other {@link TokenEmpty token characters} that have the same
     * character as well as {@link TokenString TokenStrings} that are one character long and
     * contain the same character.</p>
     * @param input The input.
     * @return true if they match.
     */
    @Override
    public boolean matches(Token input) {
        return input instanceof TokenEmpty;
    }

    /**
     * Test tokens for equality.
     *
     * <p>Two tokens are equal if they represent the same character.</p>
     *
     * @param obj An object.
     * @return true if <code>obj</code> is equal to this terminal character.
     */
    @Override
    public boolean equals(Object obj) {
        return obj instanceof TokenEmpty;
    }

    /**
     * Assure that equal tokens return the same hash code.
     * @return the hash code.
     */
    @Override
    public int hashCode() {
        return 65581;
    }

    /**
     * Pretty print a token.
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        return "∅";
    }
}
