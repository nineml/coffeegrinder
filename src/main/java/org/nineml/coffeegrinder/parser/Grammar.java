package org.nineml.coffeegrinder.parser;

import org.nineml.coffeegrinder.exceptions.GrammarException;
import org.nineml.coffeegrinder.util.Messages;
import org.nineml.coffeegrinder.util.ParserAttribute;

import java.util.*;

/**
 * A grammar for the parser.
 *
 * <p>A grammar is a list of rules. Each rule defines a non-terminal symbol as a sequence of zero or
 * more symbols (terminal or nonterminal).</p>
 *
 * <p>A grammar can be used to create a parser for that grammar. After doing so, you cannot make any
 * further changes to the grammar.</p>
 */
public class Grammar {
    public static final String logcategory = "Grammar";

    private static int nextGrammarId = 0;
    private final ArrayList<Rule> rules;
    private final HashSet<NonterminalSymbol> nonterminals;
    private final HashSet<NonterminalSymbol> nullable;
    protected final int id;
    private ParserOptions options;
    private boolean open = true;

    /**
     * Create a new grammar.
     */
    public Grammar() {
        this(new ParserOptions());
    }

    /**
     * Create a new grammar with a specific set of options.
     * @param options The options.
     */
    public Grammar(ParserOptions options) {
        id = nextGrammarId++;
        rules = new ArrayList<>();
        this.options = options;
        nullable = new HashSet<>();
        nonterminals = new HashSet<>();
        options.logger.debug(logcategory, "Created grammar %d", id);
    }

    /**
     * Create a new grammar from an existing grammar.
     * <p>This is a convenient way to extend a previously closed grammar.</p>
     * @param current the grammar to copy
     */
    public Grammar(Grammar current) {
        id = nextGrammarId++;
        rules = new ArrayList<>(current.getRules());
        options = current.options;
        nullable = new HashSet<>(current.nullable);
        nonterminals = new HashSet<>();
        open = true;
        options.logger.debug(logcategory, "Created grammar %d", id);
    }

    /**
     * Return the nonterminal symbol identified by name for this grammar.
     * <p>Nonterminal symbols are uniquely identified by their name.</p>
     * <p>Any string can be used as a name. Symbols are required (i.e., not optional) by default.</p>
     * @param name The name of this symbol.
     * @return The nonterminal for the name specified.
     * @throws NullPointerException if the name is null.
     */
    public NonterminalSymbol getNonterminal(String name) {
        return getNonterminal(name, false);
    }

    /**
     * Return the nonterminal symbol identified by name.
     * <p>Nonterminal symbols are uniquely identified by their name.</p>
     * <p>Any string can be used as a name.</p>
     * @param name The name of this symbol.
     * @param optional true if the symbol is optional.
     * @return The nonterminal for the name specified.
     * @throws NullPointerException if the name is null.
     */
    public NonterminalSymbol getNonterminal(String name, boolean optional) {
        ArrayList<ParserAttribute> attr = new ArrayList<>();
        if (optional) {
            attr.add(Symbol.OPTIONAL);
        }
        return getNonterminal(name, attr);
    }

    /**
     * Return the nonterminal symbol identified by name.
     * <p>Nonterminal symbols are uniquely identified by their name.</p>
     * <p>Any string can be used as a name.</p>
     * @param name The name of this symbol.
     * @param attribute an attribute
     * @return The nonterminal for the name specified.
     * @throws NullPointerException if the name is null or the attribute is null
     */
    public NonterminalSymbol getNonterminal(String name, ParserAttribute attribute) {
        if (attribute == null) {
            throw new NullPointerException("Nonterminal symbol attribute must not be null");
        }
        return getNonterminal(name, Collections.singletonList(attribute));
    }

    /**
     * Return the nonterminal symbol identified by name.
     * <p>Nonterminal symbols are uniquely identified by their name.</p>
     * <p>Any string can be used as a name.</p>
     * @param name The name of this symbol.
     * @param attributes attributes to associate with this symbol, may be null
     * @return The nonterminal for the name specified.
     * @throws NullPointerException if the name is null.
     * @throws GrammarException if the symbol already exists and attributes are different
     */
    public NonterminalSymbol getNonterminal(String name, Collection<ParserAttribute> attributes) {
        options.logger.debug(logcategory, "Creating nonterminal %s for grammar %d", name, id);
        return new NonterminalSymbol(this, name, attributes);
    }

    /**
     * Get the currently defined nonterminals in the grammar.
     * <p>After a grammar is closed, this is the final set.</p>
     * @return the set of nonterminals.
     */
    public Set<NonterminalSymbol> getSymbols() {
        return nonterminals;
    }

    /**
     * Add a rule to the grammar.
     * <p>Multiple rules can exist for the same {@link NonterminalSymbol}. There must be at least
     * one rule for every nonterminal symbol that occurs on the "right hand side" of a rule.</p>
     * <p>Once added, a rule can never be removed.</p>
     * @param rule The rule to add
     * @throws GrammarException if any nonterminal in the rule is not from this grammar, or if the grammar is closed
     */
    public void addRule(Rule rule) {
        if (!open) {
            throw GrammarException.grammarIsClosed();
        }
        if (contains(rule)) {
            options.logger.trace(logcategory, "Ignoring duplicate rule: %s", rule);
        } else {
            nonterminals.add(rule.getSymbol());
            options.logger.trace(logcategory, "Adding rule: %s", rule);
            rules.add(rule);
            computeNullable(rule);
        }
    }

    /**
     * Add a rule to the grammar.
     * <p>This is a convenience method that will construct the {@link Rule} for you.</p>
     * <p>Multiple rules can exist for the same {@link NonterminalSymbol}. There must be at least
     * one rule for every nonterminal symbol that occurs on the "right hand side" of a rule.</p>
     * @param nonterminal The nonterminal symbol defined by this rule.
     * @param rhs The list of symbols that define it
     * @throws GrammarException if any nonterminal in the rule is not from this grammar or if the grammar is closed
     */
    public void addRule(NonterminalSymbol nonterminal, Symbol... rhs) {
        addRule(new Rule(nonterminal, rhs));
    }

    /**
     * Add a rule to the grammar.
     * <p>This is a convenience method that will construct the {@link Rule} for you.</p>
     * <p>Multiple rules can exist for the same {@link NonterminalSymbol}. There must be at least
     * one rule for every nonterminal symbol that occurs on the "right hand side" of a rule.</p>
     * @param nonterminal The nonterminal symbol defined by this rule.
     * @param rhs The list of symbols that define it.
     * @throws GrammarException if any nonterminal in the rule is not from this grammar or if the grammar is closed
     */
    public void addRule(NonterminalSymbol nonterminal, List<Symbol> rhs) {
        addRule(new Rule(nonterminal, rhs));
    }

    /**
     * What rules are in this grammar?
     * @return Returns the rules that define the grammar.
     */
    public List<Rule> getRules() {
        return rules;
    }

    /**
     * Is a symbol nullable?
     * <p>Note: in an open grammar, the result of calling this method can vary. Once a symbol
     * is nullable, it will forever be nullable.</p>
     * @param symbol the symbol
     * @return true if the symbol is nullable
     */
    public boolean isNullable(NonterminalSymbol symbol) {
        return nullable.contains(symbol);
    }

    /**
     * Get a parser for this grammar.
     *
     * <p>Returns a parser that will parse an input against the rules that define this grammar.</p>
     *
     * @param seed The {@link NonterminalSymbol} that your input is expected to match.
     * @return the parser
     * @throws GrammarException if there are any nonterminals mentioned in the grammar that are not defined by rules in the grammar.
     */
    public EarleyParser getParser(NonterminalSymbol seed) {
        if (open) {
            close();
        }
        return new EarleyParser(this, seed);
    }

    /**
     * Get a parser for this grammar.
     *
     * <p>Returns a parser that will parse an input against the rules that define this grammar.</p>
     *
     * @param seed The name of the nonterminal that your input is expected to match.
     * @return the parser
     * @throws GrammarException if there are any nonterminals mentioned in the grammar that are not defined by rules in the grammar.
     */
    public EarleyParser getParser(String seed) {
        if (open) {
            close();
        }
        return new EarleyParser(this, getNonterminal(seed));
    }

    /**
     * Gets the parser options.
     * @return the current options.
     */
    public ParserOptions getParserOptions() {
        return options;
    }

    /**
     * Sets the parser options.
     * <p>The parser sends messages to the listener which may be helpful in understanding what the parser is doing.
     * It's mostly for debugging, but may be useful in other contexts.</p>
     * @param options The options.
     */
    public void setParserOptions(ParserOptions options) {
        this.options = options;
    }

    /**
     * Is this grammar still open, can you still add rules to it?
     * @return true if the grammar is open.
     */
    public boolean isOpen() {
        return open;
    }

    /**
     * Close the grammar.
     */
    public void close() {
        expandOptionalSymbols();
        open = false;
    }

    /**
     * Does this grammar contain an equivalent rule?
     * <p>Two rules are equivalent if they have the same symbol, the same list of right-hand-side
     * symbols, and if the optionality of every symbol on the right-hand-side is the same in both rules.</p>
     * @param candidate the candidat rule
     * @return true if the grammar contains an equivalent rule
     */
    public boolean contains(Rule candidate) {
        for (Rule rule : rules) {
            if (rule.getSymbol().equals(candidate.getSymbol())) {
                if (rule.getRhs().size() == candidate.getRhs().size()) {
                    boolean same = true;
                    for (int pos = 0; pos < rule.getRhs().size(); pos++) {
                        Symbol symbol = rule.getRhs().get(pos);
                        Symbol csym = candidate.getRhs().get(pos);
                        if (symbol instanceof NonterminalSymbol) {
                            if (csym instanceof NonterminalSymbol) {
                                if (!((NonterminalSymbol) symbol).getName().equals(((NonterminalSymbol) csym).getName())
                                    || symbol.isOptional() != csym.isOptional()) {
                                    same = false;
                                }
                            } else {
                                same = false;
                            }
                        } else {
                            same = same && symbol.equals(csym);
                        }
                    }
                    if (same) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    protected void computeNullable(Rule rule) {
        // A symbol is nullable if every symbol on the right-hand-side of any
        // rule that defines it is entirely optional.
        if (nullable.contains(rule.getSymbol())) {
            return;
        }
        boolean isNullable = true;
        for (Symbol symbol : rule.getRhs()) {
            isNullable = isNullable && symbol.isOptional();
        }
        if (isNullable) {
            nullable.add(rule.getSymbol());
        }
    }

    protected void expandOptionalSymbols() {
        ArrayList<Rule> copyRules = new ArrayList<>(rules);
        for (Rule rule : copyRules) {
            expandOptionalSymbols(rule, 0);
        }
    }

    private void expandOptionalSymbols(Rule rule, int pos) {
        if (pos >= rule.getRhs().size()) {
            return;
        }
        Symbol symbol = rule.getRhs().get(pos);
        expandOptionalSymbols(rule, pos+1);
        if (symbol.isOptional()) {
            // If the symbol is a nullable nonterminal, we don't need to instantiate a
            // rule where it's absent. It'll be made absent by the parser.
            if (symbol instanceof TerminalSymbol || !nullable.contains((NonterminalSymbol) symbol)) {
                ArrayList<Symbol> newRhs = new ArrayList<>();
                for (int rhspos = 0; rhspos < rule.getRhs().size(); rhspos++) {
                    if (rhspos != pos) {
                        newRhs.add(rule.getRhs().get(rhspos));
                    }
                }
                Rule newRule = new Rule(rule.getSymbol(), newRhs);
                addRule(newRule);
                expandOptionalSymbols(newRule, pos);
            }
        }
    }

    protected List<NonterminalSymbol> undefinedSymbols() {
        HashSet<NonterminalSymbol> definedNames = new HashSet<>();
        HashSet<NonterminalSymbol> usedNames = new HashSet<>();

        for (Rule rule : rules) {
            definedNames.add(rule.getSymbol());
            for (Symbol s : rule.getRhs()) {
                if (s instanceof NonterminalSymbol) {
                    usedNames.add((NonterminalSymbol) s);
                }
            }
        }

        ArrayList<NonterminalSymbol> unused = new ArrayList<>();
        for (NonterminalSymbol nt : usedNames) {
            if (!definedNames.contains(nt)) {
                unused.add(nt);
            }
        }

        return unused;
    }

    /**
     * Get a hygiene report for the specified start symbol.
     * <p>See {@link #checkHygiene(NonterminalSymbol)}.</p>
     * @param seed the start symbol.
     * @return the report.
     */
    public HygieneReport checkHygiene(String seed) {
        return checkHygiene(getNonterminal(seed));
    }

    /**
     * Get a hygiene report for the specified start symbol.
     * <p>If the grammar is closed, creating the report will also generate warning messages
     * to the grammar's logger.</p>
     * @param seed the start symbol.
     * @return the report.
     */
    public HygieneReport checkHygiene(NonterminalSymbol seed) {
        HygieneReport report = new HygieneReport(this);

        HashSet<NonterminalSymbol> reachable = new HashSet<>();
        walk(seed, reachable);
        for (Rule rule : rules) {
            if (!reachable.contains(rule.getSymbol())) {
                report.addUnreachable(rule.getSymbol());
            }
        }

        for (NonterminalSymbol nt : undefinedSymbols()) {
            report.addUndefined(nt);
        }

        // What about unproductive non-terminals?
        HashSet<NonterminalSymbol> productiveNT = new HashSet<>();
        HashSet<Rule> productiveRule = new HashSet<>();
        int psize = -1;
        int rsize = -1;
        while (psize != productiveNT.size() || rsize != productiveRule.size()) {
            psize = productiveNT.size();
            rsize = productiveRule.size();

            for (NonterminalSymbol nt : nonterminals) {
                boolean isProductiveSymbol = false;
                for (Rule rule : rules) {
                    if (nt.equals(rule.getSymbol())) {
                        boolean isProductiveRule = productiveRule.contains(rule);
                        if (!isProductiveRule) {
                            isProductiveRule = true;
                            for (Symbol s : rule.getRhs()) {
                                if (s instanceof NonterminalSymbol && !productiveNT.contains((NonterminalSymbol) s)) {
                                    isProductiveRule = false;
                                    break;
                                }
                            }
                            if (isProductiveRule) {
                                productiveRule.add(rule);
                                isProductiveSymbol = true;
                            }
                        }
                    }
                }
                if (isProductiveSymbol) {
                    productiveNT.add(nt);
                }
            }
        }

        for (NonterminalSymbol s : nonterminals) {
            if (!productiveNT.contains(s)) {
                report.addUnproductive(s);
            }
        }
        for (Rule rule : rules) {
            if (!productiveRule.contains(rule)) {
                report.addUnproductive(rule);
            }
        }

        return report;
    }

    private void walk(NonterminalSymbol symbol, HashSet<NonterminalSymbol> reachable) {
        reachable.add(symbol);
        for (Rule rule : rules) {
            if (rule.getSymbol().equals(symbol)) {
                for (Symbol s : rule.getRhs()) {
                    if (s instanceof NonterminalSymbol) {
                        NonterminalSymbol nt = (NonterminalSymbol) s;
                        if (!reachable.contains(nt)) {
                            walk(nt, reachable);
                        }
                    }
                }
            }
        }
    }

}
