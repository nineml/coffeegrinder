package org.nineml.coffeegrinder.parser;

import org.nineml.coffeegrinder.exceptions.GrammarException;
import org.nineml.coffeegrinder.exceptions.ParseException;
import org.nineml.coffeegrinder.tokens.Token;
import org.nineml.coffeegrinder.util.Iterators;
import org.nineml.coffeegrinder.util.StopWatch;

import java.util.*;

/** The Earley parser.
 *
 * <p>The Earley parser compares an input sequence against a grammar and determines if the input
 * is a sentence in the grammar.</p>
 * <p>This is a fairly literal implementation of the parser in §5 of
 * <a href="https://www.sciencedirect.com/science/article/pii/S1571066108001497?via%3Dihub">SPPF-Style
 * Parsing From Earley Recognisers</a>.</p>
 */
public class EarleyParser {
    public static final String logcategory = "Parser";

    private final EarleyChart chart = new EarleyChart();
    private final ForestNodeSet V;
    private final Grammar grammar;
    private final ParseForest graph;
    private final NonterminalSymbol S;
    private final HashMap<NonterminalSymbol, List<Rule>> Rho;
    private final ArrayList<Token> tokenBuffer = new ArrayList<>();
    private boolean success = false;
    protected final ParserOptions options;
    protected Iterator<Token> iterator;
    protected ProgressMonitor monitor = null;
    protected int progressSize = 0;
    protected int progressCount = 0;

    protected EarleyParser(Grammar grammar, NonterminalSymbol seed) {
        if (grammar.isOpen()) {
            throw new IllegalArgumentException("Cannot create a parser for an open grammar");
        }
        this.grammar = grammar;

        // I actually only care about the rules, so copy them.
        HashSet<NonterminalSymbol> nulled = new HashSet<>();
        Rho = new HashMap<>();
        for (Rule rule : grammar.getRules()) {
            if (!Rho.containsKey(rule.getSymbol())) {
                Rho.put(rule.getSymbol(), new ArrayList<>());
            }
            Rho.get(rule.getSymbol()).add(rule);
            if (rule.getRhs().isEmpty()) {
                nulled.add(rule.getSymbol());
            }
        }

        // If there are any nulled symbols that don't have an epsilon rule, add it
        for (NonterminalSymbol symbol : Rho.keySet()) {
            if (grammar.isNullable(symbol) && !nulled.contains(symbol)) {
                Rho.get(symbol).add(new Rule(symbol));
            }
        }

        if (!Rho.containsKey(seed)) {
            throw ParseException.seedNotInGrammar(seed.toString());
        }

        S = seed;
        options = grammar.getParserOptions();
        graph = new ParseForest(options);
        V = new ForestNodeSet(graph);

        // Add special rules for undefined symbols. If any attempt is made to access
        // the RHS of an UndefinedSymbolRule, it will throw the appropriate grammar
        // exception. This approach means we don't have to test if the symbol is
        // in Rho every time we access a symbol.
        for (NonterminalSymbol nt : grammar.undefinedSymbols()) {
            Rho.put(nt, Collections.singletonList(new UndefinedSymbolRule(nt)));
        }
    }

    /**
     * Get the grammar used by this parser.
     * @return the grammar
     */
    public Grammar getGrammar() {
        return grammar;
    }

    /**
     * Get the {@link NonterminalSymbol} seed value used by this parser.
     * @return the seed
     */
    public NonterminalSymbol getSeed() {
        return S;
    }

    /**
     * Parse an input string against the grammar.
     *
     * <p>This is a shortcut for parsing a sequence of characters.</p>
     *
     * @param input the input string
     * @return a parse result
     */
    public EarleyResult parse(String input) {
        return parse(Iterators.characterIterator(input));
    }

    /**
     * Parse a sequence of tokens against the grammar.
     *
     * @param input the input sequence
     * @return a parse result
     */
    public EarleyResult parse(Iterator<Token> input) {
        iterator = input;

        monitor = options.monitor;
        if (monitor != null) {
            progressSize = monitor.starting(this);
            progressCount = progressSize;
        }

        ArrayList<EarleyItem> Q = new ArrayList<>();
        ArrayList<EarleyItem> Qprime = new ArrayList<>();
        int tokenCount = 0;
        Token lastInputToken = null;

        boolean emptyInput = true;
        Token currentToken = null;
        if (input.hasNext()) {
            emptyInput = false;
            currentToken = input.next();
        }
        for (Rule rule : Rho.get(S)) {
            Symbol alpha = null;
            if (rule.getRhs().size() > 0) {
                alpha = rule.getRhs().get(0);
            }

            if (alpha == null || alpha instanceof NonterminalSymbol) {
                State state = new State(rule);
                chart.add(0, new EarleyItem(state, 0, null));
            }

            if (alpha instanceof TerminalSymbol && alpha.matches(currentToken)) {
                State state = new State(rule);
                Qprime.add(new EarleyItem(state, 0, null));
            }
        }

        Token nextToken = currentToken;
        boolean buffering = false;
        boolean consumedInput = true;
        boolean done = false;
        boolean lastToken = false;
        int checkpoint = -1;
        int i = 0;

        StopWatch timer = new StopWatch();

        ArrayList<Hitem> H = new ArrayList<>();

        while (!done) {
            currentToken = nextToken;

            if (progressSize > 0) {
                if (progressCount == 0) {
                    monitor.progress(this, tokenCount);
                    progressCount = progressSize - 1;
                } else {
                    progressCount--;
                }
            }

            if (currentToken != null) {
                lastInputToken = currentToken;
                tokenCount = i + 1;
                options.logger.trace(logcategory, "Parsing token %d: %s", tokenCount, currentToken);
            }

            H.clear();
            ArrayList<EarleyItem> R = new ArrayList<>(chart.get(i));

            Q.clear();
            Q.addAll(Qprime);

            Qprime.clear();

            while (!R.isEmpty()) {
                //options.logger.trace(logcategory, "Processing R: %d", R.size());
                EarleyItem Lambda = R.remove(0);
                if (Lambda.state != null && Lambda.state.nextSymbol() instanceof NonterminalSymbol) {
                    NonterminalSymbol C = (NonterminalSymbol) Lambda.state.nextSymbol();
                    for (Rule rule : Rho.get(C)) {
                        Symbol delta = null;
                        if (rule.getRhs().size() > 0) {
                            delta = rule.getRhs().get(0);
                        }
                        if (delta == null || delta instanceof NonterminalSymbol) {
                            EarleyItem item = new EarleyItem(new State(rule), i, null);
                            if (!chart.contains(i, item)) {
                                chart.add(i, item);
                                R.add(item);
                                consumedInput = consumedInput || delta != null;
                            }
                        }
                        if (delta instanceof TerminalSymbol && delta.matches(currentToken)) {
                            EarleyItem item = new EarleyItem(new State(rule), i, null);
                            if (!Q.contains(item)) {
                                Q.add(item);
                                consumedInput = true;
                            }
                        }
                    }

                    //options.logger.trace(logcategory, "Processing H: %d", H.size());
                    for (Hitem hitem : H) {
                        if (hitem.symbol.equals(C)) {
                            State newState = Lambda.state.advance();
                            ForestNode y = make_node(newState, Lambda.j, i, Lambda.w, hitem.w);
                            Symbol Beta = newState.nextSymbol();
                            EarleyItem item = new EarleyItem(newState, Lambda.j, y);
                            if (Beta == null || Beta instanceof NonterminalSymbol) {
                                if (!chart.contains(i, item)) {
                                    chart.add(i, item);
                                    R.add(item);
                                    consumedInput = consumedInput || Beta != null;
                                }
                            }
                            if (Beta instanceof TerminalSymbol && Beta.matches(currentToken)) {
                                if (!Q.contains(item)) {
                                    Q.add(item);
                                    consumedInput = true;
                                }
                            }
                        }
                    }
                }

                if (Lambda.state != null && Lambda.state.completed()) {
                    ForestNode w = Lambda.w;
                    int h = Lambda.j;
                    NonterminalSymbol D = Lambda.state.getSymbol();
                    if (w == null) {
                        w = V.conditionallyCreateNode(D, Lambda.state, i, i);
                        w.addFamily(null);
                    }
                    if (h == i) {
                        H.add(new Hitem(D, w));
                    }
                    int hpos = 0;
                    while (hpos < chart.get(h).size()) {
                        //options.logger.trace(logcategory, "Processing chart: %d: %d of %d", h, hpos, chart.get(h).size());
                        EarleyItem item = chart.get(h).get(hpos);
                        if (item.state != null && D.equals(item.state.nextSymbol())) {
                            State newState = item.state.advance();
                            ForestNode y = make_node(newState, item.j, i, item.w, w);
                            Symbol delta = newState.nextSymbol();
                            EarleyItem nextItem = new EarleyItem(newState, item.j, y);
                            if (delta == null || delta instanceof NonterminalSymbol) {
                                if (!chart.contains(i, nextItem)) {
                                    chart.add(i, nextItem);
                                    R.add(nextItem);
                                    consumedInput = consumedInput || delta != null;
                                }
                            }
                            if (delta instanceof TerminalSymbol && delta.matches(currentToken)) {
                                Q.add(nextItem);
                                consumedInput = true;
                            }
                        }
                        hpos++;
                    }
                }
            }

            if (chart.size() > 0) {
                //options.logger.trace(logcategory, "Processing chart: %d: %d", chart.size()-1, chart.get(chart.size()-1).size());
                for (EarleyItem item : chart.get(chart.size()-1)) {
                    ArrayList<ForestNode> localRoots = new ArrayList<>();
                    if (item.state.completed() && item.j == 0 && item.state.getSymbol().equals(S)) {
                        if (item.w != null) {
                            localRoots.add(item.w);
                        }
                        buffering = true;
                        checkpoint = graph.size();
                        if (consumedInput) {
                            tokenBuffer.clear();
                            consumedInput = false;
                        }
                    }
                    if (!localRoots.isEmpty()) {
                        graph.clearRoots();
                        for (ForestNode node : localRoots) {
                            graph.root(node);
                        }
                    }
                }
            }

            V.clear();
            ForestNode v = null;
            if (currentToken != null) {
                v = graph.createNode(new TerminalSymbol(currentToken), i, i+1);
            }

            done = lastToken;
            if (input.hasNext()) {
                nextToken = input.next();
                if (buffering) {
                    tokenBuffer.add(nextToken);
                }
            } else {
                nextToken = null;
                lastToken = true;
            }

            while (!Q.isEmpty()) {
                //options.logger.trace(logcategory, "Processing Q: %d", Q.size());
                EarleyItem Lambda = Q.remove(0);
                State nextState = Lambda.state.advance();
                ForestNode y = make_node(nextState, Lambda.j, i+1, Lambda.w, v);
                Symbol Beta = nextState.nextSymbol();
                if (Beta == null || Beta instanceof NonterminalSymbol) {
                    EarleyItem nextItem = new EarleyItem(nextState, Lambda.j, y);
                    if (!chart.contains(i+1, nextItem)) {
                        chart.add(i+1, nextItem);
                    }
                }
                if (Beta instanceof TerminalSymbol && Beta.matches(nextToken)) {
                    Qprime.add(new EarleyItem(nextState, Lambda.j, y));
                }
            }

            i++;

            done = done || (chart.get(i).isEmpty() && Qprime.isEmpty());
        }

        timer.stop();

        if (monitor != null) {
            if (progressSize > 0) {
                monitor.progress(this, tokenCount);
            }
            monitor.finished(this);
        }

        // If there are still tokens left, we bailed early. (No pun intended.)
        if (input.hasNext() || !tokenBuffer.isEmpty()) {
            success = false;
        } else {
            ArrayList<ForestNode> localRoots = new ArrayList<>();
            int index = chart.size() - 1;
            while (index > 0 && chart.get(index).isEmpty()) {
                index--;
            }
            for (EarleyItem item : chart.get(index)) {
                if (item.state.completed() && item.state.getSymbol().equals(S) && item.j == 0) {
                    success = true;
                    // Don't add null to the list of roots. item.w will be null if, for example,
                    // there was no input and the start symbol matched the empty sequence.
                    if (item.w != null) {
                        localRoots.add(item.w);
                    }
                }
            }

            // If we got here because there's no input, make the (only) node in the
            // graph a root, even though it doesn't have any children.
            if (emptyInput && localRoots.isEmpty() && !graph.graph.isEmpty()) {
                localRoots.add(graph.graph.get(0));
            }

            if (!localRoots.isEmpty()) {
                graph.clearRoots();
                for (ForestNode node : localRoots) {
                    graph.root(node);
                }
            }
        }

        EarleyResult result;
        if (success) {
            if (tokenCount == 0 || timer.duration() == 0) {
                options.logger.info(logcategory, "Parse succeeded");
            } else {
                options.logger.info(logcategory, "Parse succeeded, %d tokens in %s (%s tokens/sec)",
                        tokenCount, timer.elapsed(), timer.perSecond(tokenCount));
            }

            int count = graph.prune();
            options.logger.debug(logcategory, "Pruned %d nodes from graph", count);

            if (options.returnChart) {
                result = new EarleyResult(this, chart, graph, success, tokenCount, lastInputToken);
            } else {
                chart.clear();
                result = new EarleyResult(this, graph, success, tokenCount, lastInputToken);
            }
        } else {
            if (timer.duration() == 0) {
                options.logger.info(logcategory, "Parse failed after %d tokens", tokenCount);
            } else {
                options.logger.info(logcategory, "Parse failed after %d tokens in %s (%s tokens/sec)",
                        tokenCount, timer.elapsed(), timer.perSecond(tokenCount));
            }
            if (options.prefixParsing && checkpoint >= 0) {
                graph.rollback(checkpoint);
                int count = graph.prune();
                options.logger.debug(logcategory, "Pruned %d nodes from prefix graph", count);
            }
            result = new EarleyResult(this, chart, graph, success, tokenCount, lastInputToken);
        }

        result.setParseTime(timer.duration());

        return result;
    }

    private ForestNode make_node(State B, int j, int i, ForestNode w, ForestNode v) {
        ForestNode y;

        if (B.completed()) {
            Symbol s = B.getSymbol();
            y = V.conditionallyCreateNode(s, B, j, i);
            if (w == null) {
                y.addFamily(v);
            } else {
                y.addFamily(w, v);
            }
        } else {
            State s = B;
            if (B.getPosition() == 1 && !B.completed()) {
                y = v;
            } else {
                y = V.conditionallyCreateNode(s, j, i);
                if (w == null) {
                    y.addFamily(v);
                } else {
                    y.addFamily(w, v);
                }
            }
        }

        return y;
    }

    protected List<Token> getBufferedTokens() {
        return tokenBuffer;
    }

    private static final class Hitem {
        public final NonterminalSymbol symbol;
        public final ForestNode w;
        public Hitem(NonterminalSymbol symbol, ForestNode w) {
            this.symbol = symbol;
            this.w = w;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Hitem) {
                Hitem other = (Hitem) obj;
                if (w == null) {
                    if (other.w != null) {
                        return false;
                    }
                    return symbol.equals(other.symbol);
                }
                return symbol.equals(other.symbol) && w.equals(other.w);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return symbol.hashCode() + (w == null ? 0 : 19 * w.hashCode());
        }

        @Override
        public String toString() {
            return symbol + ", " + w;
        }
    }

    private static class UndefinedSymbolRule extends Rule {
        private final NonterminalSymbol nt;

        public UndefinedSymbolRule(NonterminalSymbol symbol) {
            super(symbol);
            nt = symbol;
        }

        @Override
        public List<Symbol> getRhs() {
            throw GrammarException.noRuleForSymbol(nt.toString());
        }
    }
}
