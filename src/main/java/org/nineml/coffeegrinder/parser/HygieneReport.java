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
    private final HashSet<NonterminalSymbol> unproductiveNonterminals;
    private final HashSet<NonterminalSymbol> unreachableSymbols;

    protected HygieneReport(Grammar grammar) {
        this.grammar = grammar;
        unproductiveRules = new HashSet<>();
        unproductiveNonterminals = new HashSet<>();
        unreachableSymbols = new HashSet<>();
    }

    /**
     * Is this grammar "clean"?
     * @return true iff the grammar has no unhygienic features
     */
    public boolean isClean() {
        return unproductiveRules.isEmpty()
                && unproductiveNonterminals.isEmpty()
                && unreachableSymbols.isEmpty();
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
        return unproductiveNonterminals;
    }

    /**
     * Get the unreachable symbols.
     * @return the unreachable symbols.
     */
    public Set<NonterminalSymbol> getUnreachableSymbols() {
        return unreachableSymbols;
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

    protected void addUnproductive(NonterminalSymbol symbol) {
        if (unproductiveNonterminals.contains(symbol)) {
            return;
        }

        unproductiveNonterminals.add(symbol);
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
