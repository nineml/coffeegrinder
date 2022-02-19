package org.nineml.coffeegrinder.parser;

import org.nineml.coffeegrinder.exceptions.ParseException;

/**
 * An internal class representing a family of nodes in the SPPF.
 * <p>This has no public use, but it's shared between two classes so it can't be private to either of them.</p>
 */
public class Family {
    // if v is null, this family represents epsilon
    public ForestNode v;
    public ForestNode w;

    protected Family(ForestNode v) {
        this.v = v;
        this.w = null;
    }

    protected Family(ForestNode w, ForestNode v) {
        if (w == null) {
            throw ParseException.internalError("Attempt to create family with null 'w'");
        }
        this.w = w;
        this.v = v;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Family) {
            Family other = (Family) obj;
            if (v == null) {
                return other.v == null;
            }
            if (w == null) {
                return other.w == null && v.equals(other.v);
            }
            return w.equals(other.w) && v.equals(other.v);
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (v == null) {
            return 19;
        }
        if (w == null) {
            return v.hashCode();
        }
        return (31 * w.hashCode()) + v.hashCode();
    }

    @Override
    public String toString() {
        if (v == null) {
            return "ε";
        }
        if (w == null) {
            return v.toString();
        }
        return w + " / " + v;
    }
}
