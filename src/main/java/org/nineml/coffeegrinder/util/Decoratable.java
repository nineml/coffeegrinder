package org.nineml.coffeegrinder.util;

import org.nineml.coffeegrinder.exceptions.AttributeException;
import org.nineml.coffeegrinder.exceptions.GrammarException;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class Decoratable {
    private final HashMap<String, ParserAttribute> attributes;

    /**
     * A decoratable item with no attributes.
     */
    public Decoratable() {
        this.attributes = new HashMap<>();
    }

    /**
     * A decoratable item with attributes.
     *
     * @param attributes the attributes
     * @throws GrammarException if the attribute names are not unique
     * @throws AttributeException if an attribute has an invalid value
     */
    public Decoratable(Collection<ParserAttribute> attributes) {
        this.attributes = new HashMap<>();
        addAttributes(attributes);
    }

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
        return attributes.getOrDefault(name, null);
    }

    /**
     * Get a specific token attribute value.
     * @param name the name of the attribute.
     * @param defaultValue the default value.
     * @return the associated attribute value, or the default if there is no attribute with that name.
     */
    public final String getAttributeValue(String name, String defaultValue) {
        if (attributes.containsKey(name)) {
            return attributes.get(name).getValue();
        }
        return defaultValue;
    }

    /**
     * Get all the token's attributes.
     *
     * @return the attributes.
     */
    public final Collection<ParserAttribute> getAttributes() {
        return attributes.values();
    }

    /**
     * Get all the token's attributes as a map.
     *
     * @return the associated attribute, or null if there is no attribute with that name.
     */
    public final Map<String,String> getAttributesMap() {
        if (attributes.isEmpty()) {
            return Collections.emptyMap();
        }
        HashMap<String,String> amap = new HashMap<>();
        for (ParserAttribute attr : attributes.values()) {
            amap.put(attr.getName(), attr.getValue());
        }
        return amap;
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
            if (attr.getName().equals(ParserAttribute.PRUNING_NAME)
                    && !ParserAttribute.ALLOWED_TO_PRUNE.equals(attr.getValue())
                    && !ParserAttribute.NOT_ALLOWED_TO_PRUNE.equals(attr.getValue())) {
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
