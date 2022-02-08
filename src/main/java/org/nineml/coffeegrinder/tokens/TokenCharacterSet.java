package org.nineml.coffeegrinder.tokens;

import org.nineml.coffeegrinder.util.ParserAttribute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A token character set.
 * <p>Token character sets allow you to specify that set(s) of characters should be included or excluded.
 * A set that includes "A-F0-9" matches hexidecimal numbers, for example. A character set that excludes "~" matches
 * all of Unicode except the "~" character.</p>
 */
public class TokenCharacterSet extends Token {
    private final List<CharacterSet> ranges;
    private final boolean inclusion;

    private TokenCharacterSet(List<CharacterSet> ranges, Collection<ParserAttribute> attributes, boolean inclusion) {
        super(attributes);
        this.ranges = ranges;
        this.inclusion = inclusion;
    }

    /**
     * Create a character set terminal that matches any character in the specified range.
     * @param range The character set.
     * @return A TerminalCharacterSet that matches any character in that range.
     */
    public static TokenCharacterSet inclusion(CharacterSet range) {
        return new TokenCharacterSet(Collections.singletonList(range), null,true);
    }

    /**
     * Create a character set terminal that matches any character in the specified range (with attributes)
     * @param range The character set.
     * @param attributes The attributes.
     * @return A TerminalCharacterSet that matches any character in that range.
     */
    public static TokenCharacterSet inclusion(CharacterSet range, Collection<ParserAttribute> attributes) {
        return new TokenCharacterSet(Collections.singletonList(range), attributes,true);
    }

    /**
     * Create a character set terminal that matches any character in the specified ranges.
     * @param range A list of character set ranges.
     * @return A TerminalCharacterSet that matches any character in any of those ranges.
     */
    public static TokenCharacterSet inclusion(List<CharacterSet> range) {
        return new TokenCharacterSet(range, null, true);
    }

    /**
     * Create a character set terminal that matches any character in the specified ranges (with attributes).
     * @param range A list of character set ranges.
     * @param attributes The attributes.
     * @return A TerminalCharacterSet that matches any character in any of those ranges.
     */
    public static TokenCharacterSet inclusion(List<CharacterSet> range, Collection<ParserAttribute> attributes) {
        return new TokenCharacterSet(range, attributes, true);
    }

    /**
     * Create a character set terminal that matches any character in the specified ranges.
     * @param ranges A list of character set ranges.
     * @return A TerminalCharacterSet that matches any character in any of those ranges.
     */
    public static TokenCharacterSet inclusion(CharacterSet... ranges) {
        ArrayList<CharacterSet> sets = new ArrayList<>();
        Collections.addAll(sets, ranges);
        return new TokenCharacterSet(sets, null, true);
    }

    /**
     * Create a character set terminal that matches any character <em>not</em> in the specified range.
     * @param range The character set.
     * @return A TerminalCharacterSet that matches any character not in that range.
     */
    public static TokenCharacterSet exclusion(CharacterSet range) {
        return new TokenCharacterSet(Collections.singletonList(range), null,false);
    }

    /**
     * Create a character set terminal that matches any character <em>not</em> in the specified range (with attributes).
     * @param range The character set.
     * @param attributes The attributes.
     * @return A TerminalCharacterSet that matches any character not in that range.
     */
    public static TokenCharacterSet exclusion(CharacterSet range, Collection<ParserAttribute> attributes) {
        return new TokenCharacterSet(Collections.singletonList(range), attributes, false);
    }

    /**
     * Create a character set terminal that matches any character <em>not</em> in any of the specified ranges.
     * @param range A list of character set ranges.
     * @return A TerminalCharacterSet that matches any character not in any of those ranges.
     */
    public static TokenCharacterSet exclusion(List<CharacterSet> range) {
        return new TokenCharacterSet(range, null, false);
    }

    /**
     * Create a character set terminal that matches any character <em>not</em> in any of the specified ranges (with attributes).
     * @param range A list of character set ranges.
     * @param attributes The attributes.
     * @return A TerminalCharacterSet that matches any character not in any of those ranges.
     */
    public static TokenCharacterSet exclusion(List<CharacterSet> range, Collection<ParserAttribute> attributes) {
        return new TokenCharacterSet(range, attributes, false);
    }

    /**
     * Create a character set terminal that matches any character <em>not</em> in the specified ranges.
     * @param ranges A list of character set ranges.
     * @return A TerminalCharacterSet that matches any character in any of those ranges.
     */
    public static TokenCharacterSet exclusion(CharacterSet... ranges) {
        ArrayList<CharacterSet> sets = new ArrayList<>();
        Collections.addAll(sets, ranges);
        return new TokenCharacterSet(sets, null, false);
    }

    /**
     * Is this an inclusion characater set?
     * <p>If not, it's an exclusion.</p>
     * @return true if this is an inclusion character set
     */
    public boolean isInclusion() {
        return inclusion;
    }

    /**
     * What are the character sets?
     * @return the character sets
     */
    public List<CharacterSet> getCharacterSets() {
        return ranges;
    }

    /**
     * Returns true if and only if the specified token matches.
     *
     * <p>Tests that the given character token matches the character set terminal. For inclusions,
     * that it is a character in one of the specified ranges. For exclusions, that it is a character
     * <em>not</em> present in any range.</p>
     *
     * <p>This method always returns <code>false</code> if the token specified is not a single character.</p>
     *
     * @param token The token character.
     * @return True iff the token matches.
     */
    @Override
    public boolean matches(Token token) {
        if (token instanceof TokenCharacter) {
            int cp = ((TokenCharacter) token).getValue();
            boolean found = false;
            for (CharacterSet range : ranges) {
                found = found || range.matches(cp);
            }

            if (inclusion) {
                return found;
            } else {
                return !found;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (!inclusion) {
            sb.append("~");
        }
        sb.append("[");
        boolean first = true;
        for (CharacterSet range : ranges) {
            if (!first) {
                sb.append(";");
            }
            first = false;
            sb.append(range);
        }
        sb.append("]");
        return sb.toString();
    }
}
