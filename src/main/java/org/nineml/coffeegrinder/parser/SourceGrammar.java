package org.nineml.coffeegrinder.parser;

import org.nineml.coffeegrinder.exceptions.GrammarException;
import org.nineml.coffeegrinder.gll.GllParser;
import org.nineml.coffeegrinder.util.ParserAttribute;

import java.util.*;

public class SourceGrammar extends Grammar {
    public static final String logcategory = "Grammar";

    private static int nextGrammarId = 0;
    private NonterminalSymbol seed = null;
    private ParserOptions options;
    protected final int id;
    protected final ParserType defaultParserType;

    /**
     * Create a new grammar.
     */
    public SourceGrammar() {
        this(new ParserOptions());
    }

    /**
     * Create a new grammar with a specific set of options.
     * @param options The options.
     */
    public SourceGrammar(ParserOptions options) {
        id = nextGrammarId++;
        this.options = options;
        if ("Earley".equals(options.getParserType())) {
            defaultParserType = ParserType.Earley;
        } else {
            defaultParserType = ParserType.GLL;
        }
        options.getLogger().debug(logcategory, "Created grammar %d", id);
    }

    /**
     * Create a new grammar from an existing grammar.
     * <p>This is a convenient way to extend a previously closed grammar.</p>
     * @param current the grammar to copy
     */
    public SourceGrammar(SourceGrammar current) {
        id = nextGrammarId++;
        options = current.options;
        defaultParserType = current.defaultParserType;
        seed = null;
        options.getLogger().debug(logcategory, "Created grammar %d", id);
    }

    /**
     * Return the nonterminal symbol identified by name.
     * <p>Nonterminal symbols are uniquely identified by their name.</p>
     * <p>Any string can be used as a name.</p>
     * @param name The name of this symbol.
     * @return The nonterminal for the name specified.
     * @throws NullPointerException if the name is null.
     */
    public NonterminalSymbol getNonterminal(String name) {
        ArrayList<ParserAttribute> attr = new ArrayList<>();
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
        options.getLogger().trace(logcategory, "Creating nonterminal %s for grammar %d", name, id);
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
        if (seed != null) {
            throw GrammarException.grammarIsClosed();
        }
        if (contains(rule)) {
            options.getLogger().trace(logcategory, "Ignoring duplicate rule: %s", rule);
        } else {
            options.getLogger().trace(logcategory, "Adding rule: %s", rule);
            rules.add(rule);
            if (!rulesBySymbol.containsKey(rule.symbol)) {
                rulesBySymbol.put(rule.symbol, new ArrayList<>());
            }
            rulesBySymbol.get(rule.symbol).add(rule);
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

    @Override
    public boolean isNullable(Symbol symbol) {
        // We don't have to be too fussy here. The answer isn't definitive for an InputGrammar.
        if (symbol instanceof NonterminalSymbol) {
            if (rulesBySymbol.containsKey(symbol)) {
                for (Rule rule : rulesBySymbol.get(symbol)) {
                    if (rule.rhs.isEmpty()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public GearleyParser getParser(String seed) {
        return getParser(getNonterminal(seed));
    }

    /**
     * Get a parser for this grammar.
     *
     * <p>Returns a parser that will parse an input against the rules that define this grammar.
     * The parser type returned is the grammar's default parser type.</p>
     *
     * @param seed The {@link NonterminalSymbol} that your input is expected to match.
     * @return the parser
     */
    public GearleyParser getParser(NonterminalSymbol seed) {
        return getParser(defaultParserType, seed);
    }

    public GearleyParser getParser(ParserType parserType, String seed) {
        return getParser(parserType, getNonterminal(seed));
    }

    public CompiledGrammar getCompiledGrammar(NonterminalSymbol seed) {
        return new CompiledGrammar(this, defaultParserType, seed);
    }

    public CompiledGrammar getCompiledGrammar(ParserType parserType, NonterminalSymbol seed) {
        return new CompiledGrammar(this, parserType, seed);
    }

    /**
     * Get a parser for this grammar.
     *
     * <p>Returns a parser that will parse an input against the rules that define this grammar.</p>
     *
     * @param parserType the type of parser
     * @param seed The {@link NonterminalSymbol} that your input is expected to match.
     * @return the parser
     */
    public GearleyParser getParser(ParserType parserType, NonterminalSymbol seed) {
        CompiledGrammar compiled = getCompiledGrammar(parserType, seed);
        if (parserType == ParserType.Earley) {
            return new EarleyParser(compiled);
        } else {
            return new GllParser(compiled);
        }
    }

    /**
     * Gets the parser options.
     * @return the current options.
     */
    public ParserOptions getParserOptions() {
        return options;
    }

    /**
     * Get a hygiene report for this grammar.
     * @param seed The seed rule for hygiene checking.
     * @return the report.
     */
    public HygieneReport getHygieneReport(NonterminalSymbol seed) {
        return new HygieneReport(this, seed);
    }

    /**
     * Does this grammar contain an equivalent rule?
     * <p>Two rules are equivalent if they have the same symbol, the same list of right-hand-side
     * symbols, and if the optionality of every symbol on the right-hand-side is the same in both rules.</p>
     * @param candidate the candidate rule
     * @return true if the grammar contains an equivalent rule
     */
    public boolean contains(Rule candidate) {
        for (Rule rule : rules) {
            if (rule.getSymbol().equals(candidate.getSymbol())) {
                if (rule.getRhs().length == candidate.getRhs().length) {
                    boolean same = true;
                    for (int pos = 0; pos < rule.getRhs().length; pos++) {
                        Symbol symbol = rule.getRhs().get(pos);
                        Symbol csym = candidate.getRhs().get(pos);
                        if (symbol instanceof NonterminalSymbol) {
                            if (csym instanceof NonterminalSymbol) {
                                same = same && ((NonterminalSymbol) symbol).getName().equals(((NonterminalSymbol) csym).getName());
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

    /**
     * Sets a metadata property.
     * <p>Metadata properties exist solely for annotations by an application. They have
     * no bearing on the function of the grammar. The {@link org.nineml.coffeegrinder.util.GrammarCompiler GrammarCompiler}
     * stores metadata properties in the compiled grammar and they are restored when a parsed
     * grammar is loaded.</p>
     * @param name the name of the property
     * @param value the value of the property, or null to remove a property
     * @throws NullPointerException if the name is null
     */
    public void setMetadataProperty(String name, String value) {
        if (name == null) {
            throw new NullPointerException("Name must not be null");
        }
        if (value == null) {
            metadata.remove(name);
        } else {
            metadata.put(name, value);
        }
    }
}
