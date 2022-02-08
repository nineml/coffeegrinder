package org.nineml.coffeegrinder.tokens;

import org.nineml.coffeegrinder.exceptions.AttributeException;
import org.nineml.coffeegrinder.exceptions.GrammarException;
import org.nineml.coffeegrinder.parser.Symbol;
import org.nineml.coffeegrinder.util.ParserAttribute;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

/**
 * An input token.
 *
 * <p>This is an abstraction for input tokens. It allows the parser to be used, for example, for both
 * sequences of characters and sequences of strings. (Sequences of anything you like, provided you define
 * the tokens.)</p>
 * <p>The only thing that's important about tokens is that we can tell when they match each other.
 * This is not the same as equality becuase, for example, the same regular expression token might
 * match many different input strings.</p>
 */
public abstract class Token {
    private final HashMap<String, ParserAttribute> attributes = new HashMap<>();

    /**
     * A token with attributes.
     *
     * @param attributes the attributes
     * @throws GrammarException if the attribute names are not unique
     */
    public Token(Collection<ParserAttribute> attributes) {
        addAttributes(attributes);
    }

    /**
     * Does this token match the input?
     *
     * @param input The input.
     * @return true if this token matches that input.
     */
    public abstract boolean matches(Token input);


    /**
     * Check if a specific attribute is specified.
     * @param name the name of the attribute.
     * @return true if the attribute is associated with this token.
     */
    public final boolean hasAttribute(String name) {
        return attributes.containsKey(name);
    }

    /**
     * Get a specific token attribute.
     * @param name the name of the attribute.
     * @return the associated attribute, or null if there is no attribute with that name.
     */
    public final ParserAttribute getAttribute(String name) {
        if (attributes.containsKey(name)) {
            return attributes.get(name);
        }
        return null;
    }

    /**
     * Get all the token's attributes.
     *
     * @return the associated attribute, or null if there is no attribute with that name.
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
            if (attr.getName().equals(Symbol.OPTIONAL.getName())
                    && !attr.getValue().equals(Symbol.OPTIONAL.getValue())) {
                throw new AttributeException("The OPTIONAL attribute has an invalid value: " + attr.getValue());
            }
            if (attr.getName().equals(ParserAttribute.PRUNING)
                    && (!attr.getValue().equals(ParserAttribute.PRUNING_ALLOWED.getValue())
                    && !attr.getValue().equals(ParserAttribute.PRUNING_FORBIDDEN.getValue()))) {
                throw new AttributeException("The PRUNING attribute has an invalid value: " + attr.getValue());
            }
            if (this.attributes.containsKey(attr.getName())) {
                if (!this.attributes.get(attr.getName()).getValue().equals(attr.getValue())) {
                    throw new AttributeException("Attribute values cannot be changed: " + attr.getName());
                }
            } else {
                this.attributes.put(attr.getName(), attr);
            }
        }
    }
}
