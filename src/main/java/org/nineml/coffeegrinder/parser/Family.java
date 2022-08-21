package org.nineml.coffeegrinder.parser;

import org.nineml.coffeegrinder.exceptions.ParseException;

/**
 * An internal class representing a family of nodes in the SPPF.
 * <p>This has no public use, but it's shared between two classes so it can't be private to either of them.</p>
 */
public class Family implements RuleChoice {
    // if v is null, this family represents epsilon
    public ForestNode v;
    public ForestNode w;
    private Symbol[] combinedRHS = null;

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
    public Symbol getSymbol() {
        if (v == null) {
            return TerminalSymbol.EPSILON;
        }
        if (w == null) {
            if (v.symbol == null) {
                return v.state.symbol;
            }
            return v.symbol;
        }
        if (w.symbol == null) {
            return w.state.symbol;
        }
        return w.symbol;
    }

    @Override
    public Symbol[] getRightHandSide() {
        // Work out what the combined right hand side for this family is. If there's only
        // one choice, it's that choice. If there are two choices, it's the concatenation
        // of the left and right.
        if (combinedRHS != null) {
            return combinedRHS;
        }

        if (v == null) {
            combinedRHS = new Symbol[0];
            return combinedRHS;
        }

        if (w == null) {
            if (v.state == null) {
                combinedRHS = new Symbol[0];
            } else {
                combinedRHS = v.state.getRhs().symbols;
            }
            return combinedRHS;
        }

        // The GLL parser sometimes constructs nodes that share the same RHS.
        // I'm not quite sure how or why or if this is a bug in the forest
        // builder. But for now...
        if (v.state != null && w.state != null && v.state.rhs == w.state.rhs) {
            combinedRHS = v.state.getRhs().symbols;
            return combinedRHS;
        }

        int slength = w.state == null ? 0 : w.state.getRhs().symbols.length;
        if (v.state != null) {
            slength += v.state.getRhs().symbols.length;
        }
        combinedRHS = new Symbol[slength];

        int pos = 0;
        if (w.state != null) {
            for (Symbol symbol : w.state.getRhs().symbols) {
                combinedRHS[pos] = symbol;
                pos++;
            }
        }
        if (v.state != null) {
            for (Symbol symbol : v.state.getRhs().symbols) {
                combinedRHS[pos] = symbol;
                pos++;
            }
        }
        return combinedRHS;
    }

    @Override
    public ForestNode getLeftNode() {
        return w;
    }

    @Override
    public ForestNode getRightNode() {
        return v;
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
