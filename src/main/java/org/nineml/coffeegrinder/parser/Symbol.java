package org.nineml.coffeegrinder.parser;

import org.nineml.coffeegrinder.exceptions.AttributeException;
import org.nineml.coffeegrinder.tokens.Token;
import org.nineml.coffeegrinder.util.ParserAttribute;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

/**
 * A grammar symbol.
 *
 * <p>Symbols match either tokens in the input (for {@link TerminalSymbol} symbols) or other symbols
 * (for {@link NonterminalSymbol} symbols). For convenience, this interface defines both match methods
 * for all Symbols.</p>
 */
public abstract class Symbol {
    public static final ParserAttribute OPTIONAL = new ParserAttribute("https://nineml.org/CoffeeGrinder/attributes/optionality", "optional");
    private final HashMap<String, ParserAttribute> attributes = new HashMap<>();

    /**
     * Is this symbol optional?
     * <p>Optionality of symbols is a convenience for grammar authors. The parser will
     * detect optional symbols and automatically generate appropriate "null" rules for them.</p>
     * @return true if this symbol is optional.
     */
    public final boolean isOptional() {
        return hasAttribute(Symbol.OPTIONAL.getName());
    }

    public final boolean isPruneable() {
        return attributes.containsKey(ParserAttribute.PRUNING)
                && attributes.get(ParserAttribute.PRUNING).getValue().equals(ParserAttribute.PRUNING_ALLOWED.getValue());
    }

    /**
     * Does this symbol match the specified token?
     * <p>This is very like equality, but consider that for some kinds of symbols (for example, tokens
     * that match regular expressions) it isn't really the same as equality.</p>
     * @param input The token.
     * @return true if the token matches.
     */
    public abstract boolean matches(Token input);

    /**
     * Does this symbol match the specified symbol?
     * @param input The symbol.
     * @return true if it is the same symbol as this symbol.
     */
    public abstract boolean matches(Symbol input);

    /**
     * Check if a specific attribute is specified.
     * @param name the name of the attribute.
     * @return true if the attribute is associated with this symbol.
     */
    public final boolean hasAttribute(String name) {
        return attributes.containsKey(name);
    }

    /**
     * Get a specific symbol attribute.
     * @param name the name of the attribute.
     * @return the associated symbol, or null if there is no symbol with that name.
     */
    public final ParserAttribute getAttribute(String name) {
        if (attributes.containsKey(name)) {
            return attributes.get(name);
        }
        return null;
    }

    /**
     * Get the value of a specific symbol attribute.
     * @param name the name of the attribute.
     * @param defaultValue the default value
     * @return the associated value, or the default value if there is no symbol with that name.
     */
    public final String getAttributeValue(String name, String defaultValue) {
        if (attributes.containsKey(name)) {
            return attributes.get(name).getValue();
        }
        return defaultValue;
    }

    /**
     * Get all of the attributes for this symbol.
     * @return the symbol attributes
     */
    public final Collection<ParserAttribute> getAttributes() {
        return attributes.values();
    }

    /**
     * Add the specified attribute to the attributes collection.
     * <p>Once added, an attribute cannot be removed, nor can its value be changed.</p>
     * @param attribute the attribute
     * @throws AttributeException if you attempt to change the value of an attribute
     * @throws AttributeException if you pass an illegal attribute
     * @throws NullPointerException if the attribute is null
     */
    public final void addAttribute(ParserAttribute attribute) {
        if (attribute == null) {
            throw new NullPointerException("Attribute must not be null");
        }
        addAttributes(Collections.singletonList(attribute));
    }

    /**
     * Add the specified attributes to the attributes collection.
     * <p>Once added, an attribute cannot be removed, nor can its value be changed.</p>
     * @param attributes the attributes
     * @throws AttributeException if you attempt to change the value of an attribute
     * @throws AttributeException if you pass an illegal attribute
     */
    public final void addAttributes(Collection<ParserAttribute> attributes) {
        if (attributes == null) {
            return;
        }
        for (ParserAttribute attr : attributes) {
            if (attr.getName().equals(OPTIONAL.getName())
                && !attr.getValue().equals(OPTIONAL.getValue())) {
                throw AttributeException.invalidOPTIONAL(attr.getValue());
            }
            if (attr.getName().equals(ParserAttribute.PRUNING)
                && (!attr.getValue().equals(ParserAttribute.PRUNING_ALLOWED.getValue())
                    && !attr.getValue().equals(ParserAttribute.PRUNING_FORBIDDEN.getValue()))) {
                throw AttributeException.invalidPRUNING(attr.getValue());
            }
            if (this.attributes.containsKey(attr.getName())) {
                if (!this.attributes.get(attr.getName()).getValue().equals(attr.getValue())) {
                    throw AttributeException.immutable(attr.getName(), attr.getValue());
                }
            } else {
                this.attributes.put(attr.getName(), attr);
            }
        }
    }
}
