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
    private static int nextGrammarId = 0;
    private final ArrayList<Rule> rules;
    private final HashSet<NonterminalSymbol> nullable;
    private final Messages messages;
    protected final int id;
    private ParserOptions options;
    private boolean open = true;

    /**
     * Create a new grammar.
     */
    public Grammar() {
        this(ParseListener.ERROR);
    }

    /**
     * Create a new grammar with a specific parse message verbosity.
     * @param level The parse listener level.
     */
    public Grammar(int level) {
        id = nextGrammarId++;
        rules = new ArrayList<>();
        nullable = new HashSet<>();
        options = new ParserOptions();
        options.listener.setMessageLevel(level);
        messages = new Messages(options.listener);
        messages.info("Created grammar %d", id);
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
        messages = new Messages(options.listener);
        messages.info("Created grammar %d", id);
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
        messages = current.messages;
        messages.info("Created grammar %d", id);
        nullable = new HashSet<>(current.nullable);
        open = true;
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
        messages.debug("Creating nonterminal %s for grammar %d", name, id);
        return new NonterminalSymbol(this, name, attributes);
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
            throw new GrammarException("Attempt to modify closed grammar");
        }
        if (contains(rule)) {
            messages.detail("Ignoring duplicate rule: %s", rule);
        } else {
            messages.detail("Adding rule: %s", rule);
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
        messages.setParseListener(options.listener);
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

    protected Messages getMessages() {
        return messages;
    }
}
