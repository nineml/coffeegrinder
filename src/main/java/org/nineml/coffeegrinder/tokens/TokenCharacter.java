package org.nineml.coffeegrinder.tokens;

import org.nineml.coffeegrinder.util.ParserAttribute;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

/**
 * A single character {@link Token}.
 */
public class TokenCharacter extends Token {
    private static HashMap<Character,String> charmap = null;
    private final char ch;
    private final String chstr; // So we only have to convert char to String once.

    private TokenCharacter(char ch,  Collection<ParserAttribute> attributes) {
        super(attributes);
        this.ch = ch;
        chstr = Character.toString(ch);
        if (charmap == null) {
            charmap = new HashMap<>();
            charmap.put('\n', "\\n");
            charmap.put('\r', "\\r");
            charmap.put('\t', "\\t");
        }
    }

    /**
     * Create a token for the specified character.
     * @param ch the character
     * @return a token
     */
    public static TokenCharacter get(char ch) {
        return new TokenCharacter(ch, null);
    }

    /**
     * Create a token for the specified character.
     * @param ch the character
     * @param attribute the attribute
     * @return a token
     */
    public static TokenCharacter get(char ch, ParserAttribute attribute) {
        if (attribute == null) {
            throw new NullPointerException("Token attribute must not be null");
        }
        return new TokenCharacter(ch, Collections.singletonList(attribute));
    }

    /**
     * Create a token for the specified character.
     * @param ch the character
     * @param attributes the attributes
     * @return a token
     */
    public static TokenCharacter get(char ch, Collection<ParserAttribute> attributes) {
        return new TokenCharacter(ch, attributes);
    }

    /**
     * Return the value of this token (its character).
     * @return The character value of the token.
     */
    public String getValue() {
        return chstr;
    }

    /**
     * Return the value of this token (its character).
     * @return The character value of the token.
     */
    public char getCharacter() {
        return ch;
    }

    /**
     * Does this token match the input?
     * <p>This token matches other {@link TokenCharacter token characters} that have the same
     * character as well as {@link TokenString TokenStrings} that are one character long and
     * contain the same character.</p>
     * @param input The input.
     * @return true if they match.
     */
    @Override
    public boolean matches(Token input) {
        if (input instanceof TokenCharacter) {
            return ((TokenCharacter) input).ch == ch;
        }
        if (input instanceof TokenString) {
            return matches(((TokenString) input).getValue());
        }

        return false;
    }

    /**
     * Does this token match this character?
     * @param input the input character.
     * @return true if it's the same character as this token.
     */
    public boolean matches(char input) {
        return this.ch == input;
    }

    /**
     * Does this token match this string?
     * @param input the input string.
     * @return true if it's a single-character long string containing the same character as this token.
     */
    public boolean matches(String input) {
        return input != null && input.length() == 1 && input.charAt(0) == ch;
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
        if (obj instanceof TokenCharacter) {
            return ((TokenCharacter) obj).ch == ch;
        }
        return false;
    }

    /**
     * Assure that equal tokens return the same hash code.
     * @return the hash code.
     */
    @Override
    public int hashCode() {
        return 11 * ch;
    }

    /**
     * Pretty print a token.
     * @return a string representation of the object.
     */
    @Override
    public String toString() {
        if (charmap.containsKey(ch)) {
            return "'" + charmap.get(ch) + "'";
        }
        return "'" + ch + "'";
    }
}
