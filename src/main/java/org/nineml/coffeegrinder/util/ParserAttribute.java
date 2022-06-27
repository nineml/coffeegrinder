package org.nineml.coffeegrinder.util;

/**
 * Attributes that can be associated with a symbol or token.
 * <p>ParserAttributes are a way to associate arbitrary additional information with the symbols
 * in the grammar or the tokens in the input. For example, to associate additional metadata with
 * nonterminals or to associate line and column numbers with tokens.</p>
 * <p>It's worth observing that the terminals created from the input stream don't get metadata.</p>
 */
public class ParserAttribute {
    public static final String LINE_NUMBER_NAME = "https://nineml.org/attr/line";
    public static final String COLUMN_NUMBER_NAME = "https://nineml.org/attr/column";
    public static final String OFFSET_NAME = "https://nineml.org/attr/offset";

    public static final String TOKEN_NAME = "https://nineml.org/attr/token";

    public static final String PRUNING_NAME = "https://nineml.org/attr/prune";
    public static final String ALLOWED_TO_PRUNE = "allowed";
    public static final String NOT_ALLOWED_TO_PRUNE = "forbidden";
    public static final ParserAttribute PRUNING_ALLOWED = new ParserAttribute(PRUNING_NAME, ALLOWED_TO_PRUNE);
    public static final ParserAttribute PRUNING_FORBIDDEN = new ParserAttribute(PRUNING_NAME, NOT_ALLOWED_TO_PRUNE);
    private final String name;
    private final String value;

    /**
     * Create a parser attribute.
     * @param name the attribute name
     * @param value the attribute value
     * @throws NullPointerException if the name is null
     */
    public ParserAttribute(String name, String value) {
        if (name == null) {
            throw new NullPointerException("Attribute name must not be null");
        }
        this.name = name;
        this.value = value;
    }

    /**
     * Get the name of an attribute.
     * @return the attribute name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the value of an attribute.
     * @return the attribute value
     */
    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ParserAttribute) {
            ParserAttribute other = (ParserAttribute) obj;
            return name.equals(other.name) && value.equals(other.value);
        }
        return false;
    }

    @Override
    public String toString() {
        return name + ": " + value;
    }
}
