package org.nineml.coffeegrinder.parser;

import org.nineml.coffeegrinder.gll.GllParser;
import org.nineml.coffeegrinder.util.RegexCompiler;

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
public class ParserGrammar extends Grammar {
    private final ParserType parserType;
    private final NonterminalSymbol seed;
    private final HashSet<Symbol> nullable;
    private final ParserOptions options;
    private final HashMap<NonterminalSymbol, HashSet<Symbol>> firstSets = new HashMap<>();
    private final HashMap<NonterminalSymbol, HashSet<Symbol>> followSets = new HashMap<>();
    private boolean computedSets = false;

    protected ParserGrammar(SourceGrammar grammar, ParserType parserType, NonterminalSymbol seed) {
        this.parserType = parserType;
        this.seed = seed;

        this.rules.addAll(grammar.getRules());
        /*
        for (Rule rule : grammar.getRules()) {
            if (rule.getSymbol().hasAttribute(ParserAttribute.TOKEN_NAME)) {
                String regex = compileRegex(rule.getSymbol(), grammar.getRules());
                //this.rules.add(new Rule(rule.symbol, new TerminalSymbol(TokenRegex.get(regex))));
                this.rules.add(rule);
            } else {
                this.rules.add(rule);
            }
        }
         */

        this.metadata.putAll(grammar.getMetadataProperies());
        for (Rule rule : rules) {
            if (!rulesBySymbol.containsKey(rule.symbol)) {
                rulesBySymbol.put(rule.symbol, new ArrayList<>());
            }
            rulesBySymbol.get(rule.symbol).add(rule);
        }

        this.options = grammar.getParserOptions();
        nullable = new HashSet<>();
        if (parserType == ParserType.Earley) {
            computeEarleyNullable();
        } else {
            computeGllNullable();
        }
    }

    public NonterminalSymbol getSeed() {
        return seed;
    }

    public ParserOptions getParserOptions() {
        return options;
    }

    /**
     * Get a parser for this grammar.
     *
     * <p>Returns a parser that will parse an input against the rules that define this grammar.</p>
     * @param options the parser options.
     * @return the parser
     */
    public GearleyParser getParser(ParserOptions options) {
        if (parserType == ParserType.Earley) {
            return new EarleyParser(this, options);
        } else {
            return new GllParser(this, options);
        }
    }

    /**
     * Is a symbol nullable?
     * @param symbol the symbol
     * @return true if the symbol is nullable
     * @throws UnsupportedOperationException if the grammar is open
     */
    @Override
    public boolean isNullable(Symbol symbol) {
        if (symbol instanceof NonterminalSymbol) {
            return nullable.contains(symbol);
        }
        return false;
    }

    /**
     * Get a hygiene report for this compiled grammar.
     * @return the report.
     */
    public HygieneReport getHygieneReport() {
        HygieneReport report = new HygieneReport(this);
        report.checkGrammar();
        return report;
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

        // In an unhygienic grammar, the symbol might be unknown.
        if (!firstSets.containsKey((NonterminalSymbol) current)) {
            return;
        }

        Set<Symbol> first = firstSets.get((NonterminalSymbol) current);
        if (!first.contains(TerminalSymbol.EPSILON)) {
            // In an unhygienic grammar, the symbol might be unknown.
            if (followSets.containsKey(symbol)) {
                followSets.get(symbol).addAll(first);
            }
            return;
        }

        for (Symbol fs : first) {
            if (fs != TerminalSymbol.EPSILON) {
                followSets.get(symbol).add(fs);
            }
        }

        if (pos+1 == rule.rhs.length) {
            // In an unhygienic grammar, the symbol might be unknown.
            if (followSets.containsKey(symbol)) {
                followSets.get(symbol).add(rule.symbol);
            }
        } else {
            computeFollow(rule, pos+1, symbol);
        }
    }

    private HashSet<Symbol> expandFirstSet(NonterminalSymbol symbol, HashSet<NonterminalSymbol> seen) {
        HashSet<Symbol> combined = new HashSet<>();
        if (!seen.contains(symbol)) {
            seen.add(symbol);
            // In an unhygienic grammar, the symbol might be unknown.
            if (firstSets.containsKey(symbol)) {
                for (Symbol first : firstSets.get(symbol)) {
                    if (first instanceof TerminalSymbol) {
                        combined.add(first);
                    } else {
                        combined.addAll(expandFirstSet((NonterminalSymbol) first, seen));
                    }
                }
            }
        }
        return combined;
    }

    private HashSet<Symbol> expandFollowSet(NonterminalSymbol symbol, HashSet<NonterminalSymbol> seen) {
        HashSet<Symbol> combined = new HashSet<>();
        if (!seen.contains(symbol)) {
            seen.add(symbol);
            // In an unhygienic grammar, the symbol might be unknown.
            if (followSets.containsKey(symbol)) {
                for (Symbol follow : followSets.get(symbol)) {
                    if (follow instanceof TerminalSymbol) {
                        combined.add(follow);
                    } else {
                        combined.addAll(expandFollowSet((NonterminalSymbol) follow, seen));
                    }
                }
            }
        }
        return combined;
    }

    private void computeEarleyNullable() {
        // For the Earley parser, the only symbols that are nullable are the ones
        // that have an epsilon production.
        nullable.clear();
        for (Rule rule : rules) {
            if (rule.rhs.isEmpty()) {
                nullable.add(rule.symbol);
            }
        }
    }

    private void computeGllNullable() {
        // For the GLL parser, any symbol that can lead to an epsilon production
        // is nullable.
        computeEarleyNullable();
        HashSet<Symbol> notNullable = new HashSet<>();

        boolean changed = true;
        while (changed) {
            changed = false;
            for (Rule rule : rules) {
                if (!nullable.contains(rule.symbol) && !notNullable.contains(rule.symbol)) {
                    if (rule.rhs.isEmpty()) {
                        nullable.add(rule.symbol);
                        changed = true;
                    } else {
                        boolean canBeNull = true;
                        for (Symbol symbol : rule.rhs.symbols) {
                            if (symbol instanceof TerminalSymbol || notNullable.contains(symbol)) {
                                notNullable.add(rule.symbol);
                                changed = true;
                                canBeNull = false;
                            } else if (!nullable.contains(symbol)) {
                                canBeNull = false;
                            }
                        }
                        if (canBeNull) {
                            nullable.add(rule.symbol);
                            changed = true;
                        }
                    }
                }
            }
        }
    }

    private String compileRegex(NonterminalSymbol start, List<Rule> sourceRules) {
        RegexCompiler compiler = new RegexCompiler(sourceRules);
        return compiler.compile(start);
    }

}
