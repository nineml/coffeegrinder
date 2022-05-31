package org.nineml.coffeegrinder.parser;

import org.nineml.coffeegrinder.exceptions.GrammarException;
import org.nineml.coffeegrinder.gll.GllParser;
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
    private final HashMap<NonterminalSymbol,ArrayList<Rule>> rulesBySymbol;
    private final HashSet<Symbol> nullable;
    private final HashSet<Symbol> alwaysOptional;
    private NonterminalSymbol seed = null;
    private final HashMap<String,String> metadata = new HashMap<>();
    private ParserOptions options;
    private final HashMap<NonterminalSymbol, HashSet<Symbol>> firstSets = new HashMap<>();
    private final HashMap<NonterminalSymbol, HashSet<Symbol>> followSets = new HashMap<>();
    private boolean computedSets = false;
    protected final int id;
    protected final ParserType defaultParserType;

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
        rulesBySymbol = new HashMap<>();
        this.options = options;
        nullable = new HashSet<>();
        alwaysOptional = new HashSet<>();
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
    public Grammar(Grammar current) {
        id = nextGrammarId++;
        rules = new ArrayList<>(current.rules);
        rulesBySymbol = new HashMap<>(current.rulesBySymbol);
        options = current.options;
        nullable = new HashSet<>(current.nullable);
        alwaysOptional = new HashSet<>(current.alwaysOptional);
        defaultParserType = current.defaultParserType;
        seed = null;
        computedSets = false;
        options.getLogger().debug(logcategory, "Created grammar %d", id);
    }

    public NonterminalSymbol getSeed() {
        return seed;
    }

    public List<Rule> getRulesForSymbol(NonterminalSymbol symbol) {
        if (!rulesBySymbol.containsKey(symbol)) {
            return null;
        }
        return new ArrayList<>(rulesBySymbol.get(symbol));
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
        options.getLogger().debug(logcategory, "Creating nonterminal %s for grammar %d", name, id);
        return new NonterminalSymbol(this, name, attributes);
    }

    /**
     * Get the currently defined nonterminals in the grammar.
     * <p>After a grammar is closed, this is the final set.</p>
     * @return the set of nonterminals.
     */
    public Set<NonterminalSymbol> getSymbols() {
        return rulesBySymbol.keySet();
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
     * @throws UnsupportedOperationException if the grammar is open
     */
    public boolean isNullable(Symbol symbol) {
        if (isOpen()) {
            throw new UnsupportedOperationException("Cannot ask about nullability on an open grammar");
        }
        if (symbol instanceof NonterminalSymbol) {
            return nullable.contains(symbol);
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
        if (this.seed == null) {
            close(seed);
        } else {
            if (!this.seed.equals(seed)) {
                throw new RuntimeException("Cannot change seed");
            }
        }
        if (defaultParserType == ParserType.Earley) {
            return new EarleyParser(this);
        } else {
            return new GllParser(this);
        }
    }

    public GearleyParser getParser(ParserType parserType, String seed) {
        return getParser(parserType, getNonterminal(seed));
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
        if (this.seed == null) {
            close(seed);
        } else {
            if (!this.seed.equals(seed)) {
                throw new RuntimeException("Cannot change seed");
            }
        }
        if (parserType == ParserType.Earley) {
            return new EarleyParser(this);
        } else {
            return new GllParser(this);
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
        return seed == null;
    }

    /**
     * Close the grammar.
     * @param seed The start symbol
     */
    public void close(NonterminalSymbol seed) {
        if (!rulesBySymbol.containsKey(seed)) {
            throw new IllegalArgumentException("Grammar does not contain " + seed);
        }
        expandOptionalSymbols();
        this.seed = seed;
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

    /**
     * Gets a metadata property.
     * <p>Metadata properties exist solely for annotations by an application. They have
     * no bearing on the function of the grammar. The {@link org.nineml.coffeegrinder.util.GrammarCompiler GrammarCompiler}
     * stores metadata properties in the compiled grammar and they are restored when a parsed
     * grammar is loaded.</p>
     * @param name the name of the property
     * @return the value of the property, or null if no such property exists
     * @throws NullPointerException if the name is null
     */
    public String getMetadataProperty(String name) {
        if (name == null) {
            throw new NullPointerException("Name must not be null");
        }
        return metadata.getOrDefault(name, null);
    }

    /**
     * Gets the metadata properties.
     * <p>Metadata properties exist solely for annotations by an application. They have
     * no bearing on the function of the grammar. The {@link org.nineml.coffeegrinder.util.GrammarCompiler GrammarCompiler}
     * stores metadata properties in the compiled grammar and they are restored when a parsed
     * grammar is loaded.</p>
     * @return the metadata properties
     */
    public Map<String,String> getMetadataProperies() {
        return metadata;
    }

    protected void expandOptionalSymbols() {
        // This whole business of allowing symbols to be marked as optional is a bit of a problem.
        // We now have the challenge of working out which ones should be allowed to go to epsilon.
        // I suspect this feature should be pulled out and CoffeeFilter should be rewritten to
        // deal with it through rewrite rules.
        //
        // But in the meantime, the following appears to work:
        // 1. If all of the symbols on the RHS of a rule are optional (or nullable)
        // 2. And if the symbol defined by the rule is marked as optional
        // then add an epsilon rule for it.
        //
        // That seems to work, at least for the grammars produced by CoffeeFilter

        HashSet<NonterminalSymbol> finished = new HashSet<>();
        HashSet<NonterminalSymbol> optional = new HashSet<>();
        HashSet<Symbol> sometimesRequired = new HashSet<>();

        for (Rule rule : rules) {
            if (rule.getRhs().isEmpty()) {
                nullable.add(rule.getSymbol());
                finished.add(rule.getSymbol());
            } else {
                for (Symbol symbol : rule.getRhs().symbols) {
                    if (symbol.isOptional()) {
                        alwaysOptional.add(symbol);
                    } else {
                        sometimesRequired.add(symbol);
                    }
                }
            }
        }

        for (Symbol symbol : sometimesRequired) {
            alwaysOptional.remove(symbol);
        }

        boolean changed = true;
        while (changed) {
            changed = false;

            for (Rule rule : rules) {
                NonterminalSymbol curSymbol = rule.getSymbol();

                if (!finished.contains(curSymbol)) {
                    boolean canBeNull = true;
                    for (Symbol symbol : rule.getRhs().symbols) {
                        if (symbol instanceof NonterminalSymbol) {
                            canBeNull = canBeNull
                                    && (optional.contains((NonterminalSymbol) symbol)
                                        || nullable.contains(symbol)
                                        || symbol.isOptional());
                        } else {
                            canBeNull = false;
                            finished.add(curSymbol);
                        }
                    }
                    if (canBeNull) {
                        changed = true;
                        optional.add(curSymbol);
                        finished.add(curSymbol);
                    }
                }
            }
        }

        for (Symbol symbol : optional) {
            if (symbol.isOptional()) {
                nullable.add(symbol);
            }
        }

        ArrayList<Rule> copyRules = new ArrayList<>(rules);
        for (Rule rule : copyRules) {
            expandOptionalSymbols(rule, 0);
        }
    }

    private void expandOptionalSymbols(Rule rule, int pos) {
        if (pos >= rule.getRhs().length) {
            return;
        }
        Symbol symbol = rule.getRhs().get(pos);
        expandOptionalSymbols(rule, pos+1);
        if (symbol.isOptional()) {
            // If the symbol is a nullable nonterminal, we don't need to instantiate a
            // rule where it's absent. It'll be made absent by the parser.
            if (symbol instanceof TerminalSymbol || !nullable.contains((NonterminalSymbol) symbol)) {
                ArrayList<Symbol> newRhs = new ArrayList<>();
                for (int rhspos = 0; rhspos < rule.getRhs().length; rhspos++) {
                    if (rhspos != pos) {
                        newRhs.add(rule.getRhs().get(rhspos));
                    }
                }
                Rule newRule = new Rule(rule.getSymbol(), newRhs);

                // Don't make epsilon productions for symbols that are always optional, it isn't
                // necessary and it can introduce ambiguity.
                if (!newRhs.isEmpty() || !alwaysOptional.contains(rule.getSymbol())) {
                    // The contains() test is in some sense unnecessary here because addRule() tests
                    // for this. But addRule() generates a trace message if the rules are the same
                    // and that's potentially misleading here, so let's avoid it.
                    if (!contains(newRule)) {
                        addRule(newRule);
                    }

                    // If the newRhs is empty, then this is a nullable rule.
                    if (newRhs.isEmpty()) {
                        nullable.add(rule.getSymbol());
                    }
                }

                expandOptionalSymbols(newRule, pos);
            }
        }
    }
    protected List<NonterminalSymbol> undefinedSymbols() {
        HashSet<NonterminalSymbol> definedNames = new HashSet<>();
        HashSet<NonterminalSymbol> usedNames = new HashSet<>();

        for (Rule rule : rules) {
            definedNames.add(rule.getSymbol());
            for (Symbol s : rule.getRhs().symbols) {
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

            for (NonterminalSymbol nt : rulesBySymbol.keySet()) {
                boolean isProductiveSymbol = false;
                for (Rule rule : rules) {
                    if (nt.equals(rule.getSymbol())) {
                        boolean isProductiveRule = productiveRule.contains(rule);
                        if (!isProductiveRule) {
                            isProductiveRule = true;
                            for (Symbol s : rule.getRhs().symbols) {
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

        for (NonterminalSymbol s : rulesBySymbol.keySet()) {
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
                for (Symbol s : rule.getRhs().symbols) {
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

    public Set<Symbol> getFirst(Symbol symbol) {
        computeFirstAndFollowSets();

        final HashSet<Symbol> firstSet;
        if (symbol instanceof NonterminalSymbol && firstSets.containsKey(symbol)) {
            firstSet = firstSets.get(symbol);
        } else {
            firstSet = new HashSet<>();
        }

        if (!(symbol instanceof NonterminalSymbol)) {
            firstSet.add(symbol);
        }

        return firstSet;
    }

    public Set<Symbol> getFollow(Symbol symbol) {
        computeFirstAndFollowSets();

        final HashSet<Symbol> followSet;
        if (symbol instanceof NonterminalSymbol && followSets.containsKey(symbol)) {
            followSet = followSets.get(symbol);
        } else {
            followSet = new HashSet<>();
        }

        return followSet;
    }

    private void computeFirstAndFollowSets() {
        if (computedSets) {
            return; // we don't need to do this twice
        }
        if (seed == null) {
            throw new NullPointerException("Start symbol is null");
        }
        computedSets = true;

        firstSets.clear();
        followSets.clear();

        for (Rule rule : rules) {
            if (!firstSets.containsKey(rule.symbol)) {
                firstSets.put(rule.symbol, new HashSet<>());
                followSets.put(rule.symbol, new HashSet<>());
            }

            if (rule.epsilonRule()) {
                firstSets.get(rule.symbol).add(TerminalSymbol.EPSILON);
            }

            HashSet<Symbol> first = firstSets.get(rule.symbol);
            for (Symbol symbol : rule.rhs.symbols) {
                first.add(symbol);
                if (!nullable.contains(symbol)) {
                    break;
                }
            }
        }

        for (NonterminalSymbol symbol : firstSets.keySet()) {
            HashSet<Symbol> first = expandFirstSet(symbol, new HashSet<>());
            firstSets.get(symbol).clear();
            firstSets.get(symbol).addAll(first);
        }

        followSets.get(seed).add(TerminalSymbol.EOF);
        for (Rule rule : rules) {
            for (int pos = 0; pos < rule.rhs.length; pos++) {
                NonterminalSymbol prev = pos > 0 && rule.rhs.get(pos-1) instanceof NonterminalSymbol
                        ? (NonterminalSymbol) rule.rhs.get(pos-1)
                        : null;
                Symbol symbol = rule.rhs.get(pos);

                if (prev != null) {
                    computeFollow(rule, pos, prev);
                }

                if (pos+1 == rule.rhs.length && symbol instanceof NonterminalSymbol) {
                    NonterminalSymbol nt = (NonterminalSymbol) symbol;
                    // N.B. In an unhygienic grammar where there are undefined symbols,
                    // the followSet can be null.
                    HashSet<Symbol> followSet = followSets.get(nt);
                    if (followSet == null) {
                        followSet = new HashSet<>();
                        followSet.add(rule.symbol);
                        followSets.put(nt, followSet);
                    } else {
                        followSet.add(rule.symbol);
                    }
                }
            }
        }

        for (NonterminalSymbol symbol : followSets.keySet()) {
            HashSet<Symbol> follow = expandFollowSet(symbol, new HashSet<>());
            followSets.get(symbol).clear();
            followSets.get(symbol).addAll(follow);
        }
    }

    private void computeFollow(Rule rule, int pos, NonterminalSymbol symbol) {
        Symbol current = rule.rhs.get(pos);
        if (current instanceof TerminalSymbol) {
            followSets.get(symbol).add(current);
            return;
        }

        Set<Symbol> first = firstSets.get((NonterminalSymbol) current);
        if (!first.contains(TerminalSymbol.EPSILON)) {
            followSets.get(symbol).addAll(first);
            return;
        }

        for (Symbol fs : first) {
            if (fs != TerminalSymbol.EPSILON) {
                followSets.get(symbol).add(fs);
            }
        }

        if (pos+1 == rule.rhs.length) {
            followSets.get(symbol).add(rule.symbol);
        } else {
            computeFollow(rule, pos+1, symbol);
        }
    }

    private HashSet<Symbol> expandFirstSet(NonterminalSymbol symbol, HashSet<NonterminalSymbol> seen) {
        HashSet<Symbol> combined = new HashSet<>();
        if (!seen.contains(symbol)) {
            seen.add(symbol);
            for (Symbol first : firstSets.get(symbol)) {
                if (first instanceof TerminalSymbol) {
                    combined.add(first);
                } else {
                    combined.addAll(expandFirstSet((NonterminalSymbol) first, seen));
                }
            }
        }
        return combined;
    }

    private HashSet<Symbol> expandFollowSet(NonterminalSymbol symbol, HashSet<NonterminalSymbol> seen) {
        HashSet<Symbol> combined = new HashSet<>();
        if (!seen.contains(symbol)) {
            seen.add(symbol);
            for (Symbol follow : followSets.get(symbol)) {
                if (follow instanceof TerminalSymbol) {
                    combined.add(follow);
                } else {
                    combined.addAll(expandFollowSet((NonterminalSymbol) follow, seen));
                }
            }
        }
        return combined;
    }
}
