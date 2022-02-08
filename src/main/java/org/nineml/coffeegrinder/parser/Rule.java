package org.nineml.coffeegrinder.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A grammar rule.
 *
 * <p>A grammar rule maps a single {@link NonterminalSymbol} to zero or more {@link Symbol symbols} (either {@link TerminalSymbol Terminals}
 * or further {@link NonterminalSymbol Nonterminals}).</p>
 */
public class Rule {
    private final NonterminalSymbol nonterminal;
    private final List<Symbol> rhs;

    /**
     * Construct a new rule mapping the nonterminal to a sequence of symbols.
     * <p>If the sequence of symbols is empty or null, then the nonterminal maps to "ε", that is to say,
     * it's allowed to be absent.</p>
     * @param nonterminal The nonterminal.
     * @param rhs The sequence of symbols.
     */
    public Rule(NonterminalSymbol nonterminal, Symbol... rhs) {
        if (nonterminal == null) {
            throw new NullPointerException("Rule name cannot be null");
        }

        this.nonterminal = nonterminal;
        this.rhs = new ArrayList<>();
        if (rhs != null) {
            this.rhs.addAll(Arrays.asList(rhs));
        }
    }

    /**
     * Construct a new rule mapping the nonterminal to a sequence of symbols.
     * <p>If the sequence of symbols is empty or null, then the nonterminal maps to "ε", that is to say,
     * it's allowed to be absent.</p>
     * @param nonterminal The nonterminal.
     * @param rhs The list of symbols.
     */
    public Rule(NonterminalSymbol nonterminal, List<Symbol> rhs) {
        if (nonterminal == null) {
            throw new NullPointerException("Rule name cannot be null");
        }

        this.nonterminal = nonterminal;
        this.rhs = new ArrayList<>();
        if (rhs != null) {
            this.rhs.addAll(rhs);
        }
    }

    /**
     * The nonterminal symbol defined by this rule.
     * @return The nonterminal symbol.
     */
    public NonterminalSymbol getSymbol() {
        return nonterminal;
    }

    /**
     * The sequence of symbols that comprise the definition of the rule's nonterminal.
     * <p>Note: although a rule may be defined with a null "right hand side", this method
     * always returns an empty list in such cases.</p>
     * @return The sequence of symbols.
     */
    public List<Symbol> getRhs() {
        return rhs;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Rule) {
            Rule other = (Rule) obj;
            if (nonterminal != other.nonterminal || rhs.size() != other.rhs.size()) {
                return false;
            }
            for (int pos = 0; pos < rhs.size(); pos++) {
                if (!rhs.get(pos).equals(other.rhs.get(pos))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = nonterminal.hashCode();
        for (Symbol s: rhs) {
            hash += (3 * s.hashCode());
        }
        return hash;
    }

    /**
     * Pretty print a node.
     * @return a string representation of the node.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(nonterminal);
        sb.append(" ⇒ ");
        int count = 0;
        for (Symbol symbol : rhs) {
            if (count > 0) {
                sb.append(", ");
            }
            sb.append(symbol.toString());
            count += 1;
        }
        return sb.toString();
    }
}
