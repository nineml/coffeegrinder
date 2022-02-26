package org.nineml.coffeegrinder.parser;

import java.util.HashSet;
import java.util.Set;

/**
 * A report on the hygiene of a grammar.
 * <p>A grammar is considered "unhygienic" if it contains any of the following features:
 * unreachable (i.e., unused) nonterminals, unproductive nonterminals, or unproductive
 * rules.</p>
 */
public class HygieneReport {
    public static final String logcategory = "Hygiene";

    private final Grammar grammar;
    private final HashSet<Rule> unproductiveRules;
    private final HashSet<NonterminalSymbol> unproductiveSymbols;
    private final HashSet<NonterminalSymbol> unreachableSymbols;
    private final HashSet<NonterminalSymbol> undefinedSymbols;

    protected HygieneReport(Grammar grammar) {
        this.grammar = grammar;
        unproductiveRules = new HashSet<>();
        unproductiveSymbols = new HashSet<>();
        unreachableSymbols = new HashSet<>();
        undefinedSymbols = new HashSet<>();
    }

    /**
     * Is this grammar "clean"?
     * @return true iff the grammar has no unhygienic features
     */
    public boolean isClean() {
        return unproductiveRules.isEmpty()
                && unproductiveSymbols.isEmpty()
                && unreachableSymbols.isEmpty()
                && undefinedSymbols.isEmpty();
    }

    /**
     * Get the grammar associated with this report.
     * <p>Note that if the grammar was open when the report was created, it
     * may have changed since this report was created.</p>
     * @return the grammar.
     */
    public Grammar getGrammar() {
        return grammar;
    }

    /**
     * Get the unproductive rules.
     * @return the unproductive rules.
     */
    public Set<Rule> getUnproductiveRules() {
        return unproductiveRules;
    }

    /**
     * Get the unproductive symbols.
     * @return the unproductive symbols.
     */
    public Set<NonterminalSymbol> getUnproductiveSymbols() {
        return unproductiveSymbols;
    }

    /**
     * Get the unreachable symbols.
     * @return the unreachable symbols.
     */
    public Set<NonterminalSymbol> getUnreachableSymbols() {
        return unreachableSymbols;
    }

    /**
     * Get the unreachable symbols.
     * @return the unreachable symbols.
     */
    public Set<NonterminalSymbol> getUndefinedSymbols() {
        return undefinedSymbols;
    }

    protected void addUnreachable(NonterminalSymbol symbol) {
        if (unreachableSymbols.contains(symbol)) {
            return;
        }

        unreachableSymbols.add(symbol);
        if (!grammar.isOpen()) {
            grammar.getParserOptions().logger.warn(logcategory, "Unreachable symbol: %s", symbol);
        }
    }

    protected void addUndefined(NonterminalSymbol symbol) {
        if (undefinedSymbols.contains(symbol)) {
            return;
        }

        undefinedSymbols.add(symbol);
        if (!grammar.isOpen()) {
            grammar.getParserOptions().logger.warn(logcategory, "Undefined symbol: %s", symbol);
        }
    }

    protected void addUnproductive(NonterminalSymbol symbol) {
        if (unproductiveSymbols.contains(symbol)) {
            return;
        }

        unproductiveSymbols.add(symbol);
        if (!grammar.isOpen()) {
            grammar.getParserOptions().logger.warn(logcategory, "Unproductive symbol: %s", symbol);
        }
    }

    protected void addUnproductive(Rule rule) {
        if (unproductiveRules.contains(rule)) {
            return;
        }

        unproductiveRules.add(rule);
        if (!grammar.isOpen()) {
            grammar.getParserOptions().logger.warn(logcategory, "Unproductive rule: %s", rule);
        }
    }
}
