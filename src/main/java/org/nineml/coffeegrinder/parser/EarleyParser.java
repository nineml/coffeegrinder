package org.nineml.coffeegrinder.parser;

import org.nineml.coffeegrinder.exceptions.ParseException;
import org.nineml.coffeegrinder.tokens.Token;
import org.nineml.coffeegrinder.tokens.TokenCharacter;
import org.nineml.coffeegrinder.tokens.TokenEOF;
import org.nineml.coffeegrinder.tokens.TokenString;
import org.nineml.coffeegrinder.util.Iterators;
import org.nineml.coffeegrinder.util.StopWatch;

import java.util.*;
import java.util.regex.Pattern;

/** The Earley parser.
 *
 * <p>The Earley parser compares an input sequence against a grammar and determines if the input
 * is a sentence in the grammar.</p>
 * <p>This is a fairly literal implementation of the parser in §5 of
 * <a href="https://www.sciencedirect.com/science/article/pii/S1571066108001497?via%3Dihub">SPPF-Style
 * Parsing From Earley Recognisers</a>.</p>
 */
public class EarleyParser implements GearleyParser {
    public static final String logcategory = "Parser";

    private final EarleyChart chart = new EarleyChart();
    private final ForestNodeSet V;
    private final CompiledGrammar grammar;
    private final ParseForest graph;
    private final NonterminalSymbol S;
    private final HashMap<NonterminalSymbol, List<Rule>> Rho;
    protected Token[] input = null;
    protected int startPos = 0;
    protected int inputPos = 0;
    protected int lineNumber = 1;
    protected int columnNumber = 1;

    protected boolean restartable = false;
    protected int restartPos = 0;
    private boolean success = false;
    private boolean moreInput = false;
    protected final ParserOptions options;
    protected ProgressMonitor monitor = null;
    protected int progressSize = 0;
    protected int progressCount = 0;

    protected EarleyParser(CompiledGrammar grammar, ParserOptions options) {
        this.grammar = grammar;
        this.options = options;

        List<Rule> usefulRules = usefulSubset(grammar.getRules());

        // I actually only care about the rules, so copy them.
        HashSet<NonterminalSymbol> nulled = new HashSet<>();
        Rho = new HashMap<>();
        for (Rule rule : usefulRules) {
            if (!Rho.containsKey(rule.getSymbol())) {
                Rho.put(rule.getSymbol(), new ArrayList<>());
            }
            Rho.get(rule.getSymbol()).add(rule);
            if (rule.getRhs().isEmpty()) {
                nulled.add(rule.getSymbol());
            }
        }

        // If there are any nulled symbols that don't have an epsilon rule, add one
        // (Since I rewrote Grammar.expandOptionalSymbols in May, 2022, I don't
        // think this condition ever applies anymore.
        for (NonterminalSymbol symbol : Rho.keySet()) {
            if (grammar.isNullable(symbol) && !nulled.contains(symbol)) {
                Rho.get(symbol).add(new Rule(symbol));
            }
        }

        S = grammar.getSeed();

        if (!Rho.containsKey(S)) {
            throw ParseException.seedNotInGrammar(S.toString());
        }

        graph = new ParseForest(options);
        V = new ForestNodeSet(graph);
    }

    /**
     * Return the parser type.
     * @return {@link ParserType#Earley}
     */
    public ParserType getParserType() {
        return ParserType.Earley;
    }

    private List<Rule> usefulSubset(List<Rule> initiallist) {
        ArrayList<Rule> currentList = new ArrayList<>();
        ArrayList<Rule> rules = new ArrayList<>(initiallist);
        boolean done = false;
        while (!done) {
            done = true;
            currentList.clear();
            currentList.addAll(rules);
            rules.clear();

            HashSet<NonterminalSymbol> defined = new HashSet<>();
            for (Rule rule : currentList) {
                defined.add(rule.getSymbol());
            }
            for (Rule rule : currentList) {
                boolean exclude = false;
                for (Symbol symbol : rule.getRhs().symbols) {
                    if (symbol instanceof NonterminalSymbol && !defined.contains(symbol)) {
                        options.getLogger().debug(logcategory, "Ignoring rule with undefined symbol: %s", rule);
                        exclude = true;
                        done = false;
                        break;
                    }
                }
                if (!exclude) {
                    rules.add(rule);
                }
            }
        }

        return rules;
    }

    /**
     * Get the grammar used by this parser.
     * @return the grammar
     */
    public CompiledGrammar getGrammar() {
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
        int[] codepoints = input.codePoints().toArray();
        this.input = new Token[codepoints.length];
        for (int pos = 0; pos < codepoints.length; pos++) {
            this.input[pos] = TokenCharacter.get(codepoints[pos]);
        }
        return parseInput();
    }

    /**
     * Parse an array of tokens against the grammar.
     *
     * @param input the input array
     * @return a parse result
     */
    public EarleyResult parse(Token[] input) {
        this.input = new Token[input.length];
        System.arraycopy(input, 0, this.input,  0, input.length);
        return parseInput();
    }

    /**
     * Parse a sequence of tokens against the grammar.
     *
     * @param input the input sequence
     * @return a parse result
     */
    public EarleyResult parse(Iterator<Token> input) {
        ArrayList<Token> list = new ArrayList<>();
        while (input.hasNext()) {
            list.add(input.next());
        }
        this.input = new Token[list.size()];
        for (int pos = 0; pos < list.size(); pos++) {
            this.input[pos] = list.get(pos);
        }
        return parseInput();

    }

    private EarleyResult parseInput() {
        return parseInput(0);
    }

    private EarleyResult parseInput(int startPos) {
        inputPos = startPos;
        restartable = false;

        monitor = options.getProgressMonitor();
        if (monitor != null) {
            progressSize = monitor.starting(this, input.length - inputPos);
            progressCount = progressSize;
        }

        ArrayList<EarleyItem> Q = new ArrayList<>();
        ArrayList<EarleyItem> Qprime = new ArrayList<>();
        int tokenCount = 0;
        Token lastInputToken = null;

        boolean emptyInput = true;
        Token currentToken = null;
        if (inputPos < input.length) {
            emptyInput = false;
            currentToken = input[inputPos];
        }
        for (Rule rule : Rho.get(S)) {
            final Symbol alpha = rule.getRhs().getFirst();

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
        boolean consumedInput = true;
        boolean done = false;
        boolean lastToken = false;
        int checkpoint = -1;
        int i = 0;

        options.getLogger().info(logcategory, "Parsing %,d tokens with Earley parser.", input.length - startPos);

        StopWatch timer = new StopWatch();

        ArrayList<Hitem> H = new ArrayList<>();
        ArrayList<ForestNode> localRoots = new ArrayList<>();

        String greedy = null;
        while (!done) {
            currentToken = nextToken;

            // Whether we consumed the input or not matters during the process
            // and also at the end. If there are no more tokens, make sure that
            // consumedInput is true so that we don't think we missed one at the end.
            // (Conversely, if we did just get a token, then we haven't consumed it yet
            // and we want to keep track of that fact so that if we exit the loop, we
            // know there was a token left over.)
            consumedInput = currentToken == null;

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
                //options.getLogger().trace(logcategory, "Parsing token %d: %s", tokenCount, currentToken);
            }

            H.clear();
            ArrayList<EarleyItem> R = new ArrayList<>(chart.get(i));

            Q.clear();
            Q.addAll(Qprime);

            Qprime.clear();

            while (!R.isEmpty()) {
                //options.getLogger().trace(logcategory, "Processing R: %d", R.size());
                EarleyItem Lambda = R.remove(0);
                if (Lambda.state != null && Lambda.state.nextSymbol() instanceof NonterminalSymbol) {
                    NonterminalSymbol C = (NonterminalSymbol) Lambda.state.nextSymbol();
                    for (Rule rule : Rho.get(C)) {
                        final Symbol delta = rule.getRhs().getFirst();
                        if (delta == null || delta instanceof NonterminalSymbol) {
                            EarleyItem item = new EarleyItem(new State(rule), i, null);
                            if (!chart.contains(i, item)) {
                                chart.add(i, item);
                                R.add(item);
                            }
                        }
                        if (delta instanceof TerminalSymbol && delta.matches(currentToken)) {
                            EarleyItem item = new EarleyItem(new State(rule), i, null);
                            if (!Q.contains(item)) {
                                Q.add(item);
                                consumedInput = true;

                                Symbol symbol = item.state.nextSymbol();
                                greedy = symbol.getAttributeValue("regex", null);
                            }
                        }
                    }

                    //options.getLogger().trace(logcategory, "Processing H: %d", H.size());
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
                        //options.getLogger().trace(logcategory, "Processing chart: %d: %d of %d", h, hpos, chart.get(h).size());
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

            if (options.getPrefixParsing() && chart.size() > 0) {
                //options.getLogger().trace(logcategory, "Processing chart: %d: %d", chart.size()-1, chart.get(chart.size()-1).size());
                localRoots.clear();
                for (EarleyItem item : chart.get(chart.size()-1)) {
                    if (item.state.completed() && item.j == 0 && item.state.getSymbol().equals(S)) {
                        if (item.w != null) {
                            localRoots.add(item.w);
                        }
                        checkpoint = graph.size();
                        if (consumedInput) {
                            restartPos = inputPos+1;
                            restartable = true;
                        }
                    }
                }
                if (!localRoots.isEmpty()) {
                    //options.getLogger().debug(logcategory, "Resetting graph roots, %d new roots", localRoots.size());
                    graph.clearRoots();
                    for (ForestNode node : localRoots) {
                        graph.root(node);
                    }
                }
            }

            Token peek = null;
            V.clear();
            ForestNode v = null;
            if (currentToken != null) {
                if (greedy != null) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(currentToken.getValue());
                    Pattern patn = Pattern.compile(greedy);
                    while (inputPos < input.length) {
                        nextToken = input[inputPos++];
                        String s = nextToken.getValue();
                        if (patn.matcher(s).matches()) {
                            sb.append(s);
                            if (nextToken instanceof TokenCharacter) {
                                columnNumber++;
                                if (((TokenCharacter) nextToken).getCodepoint() == '\n') {
                                    columnNumber = 1;
                                    lineNumber++;
                                }
                            }
                        } else {
                            peek = nextToken;
                            break;
                        }
                    }
                    currentToken = TokenString.get(sb.toString());
                    options.getLogger().trace(logcategory, "Regex matched: " + sb.toString());
                }

                v = graph.createNode(new TerminalSymbol(currentToken), i, i+1);
            }

            done = lastToken;
            if (peek != null || inputPos+1 < input.length) {
                nextToken = peek == null ? input[++inputPos] : peek;
                if (nextToken instanceof TokenCharacter) {
                    columnNumber++;
                    if (((TokenCharacter) nextToken).getCodepoint() == '\n') {
                        columnNumber = 1;
                        lineNumber++;
                    }
                }
            } else {
                nextToken = null;
                lastToken = true;
            }

            while (!Q.isEmpty()) {
                //options.getLogger().trace(logcategory, "Processing Q: %d", Q.size());
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
        success = inputPos == input.length || (inputPos+1 == input.length && consumedInput && lastToken);
        moreInput = !success;

        if (success) {
            success = false;
            localRoots.clear();
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
                options.getLogger().debug(logcategory, "Resetting graph roots, %d new roots", localRoots.size());
                graph.clearRoots();
                for (ForestNode node : localRoots) {
                    graph.root(node);
                }
            }
        }

        EarleyResult result;
        if (success) {
            if (tokenCount == 0 || timer.duration() == 0) {
                options.getLogger().info(logcategory, "Parse succeeded");
            } else {
                options.getLogger().info(logcategory, "Parse succeeded, %,d tokens in %s (%s tokens/sec)",
                        tokenCount, timer.elapsed(), timer.perSecond(tokenCount));
            }

            graph.prune();

            if (options.getReturnChart()) {
                result = new EarleyResult(this, chart, graph, success, tokenCount, lastInputToken);
            } else {
                chart.clear();
                result = new EarleyResult(this, graph, success, tokenCount, lastInputToken);
            }
        } else {
            if (timer.duration() == 0) {
                options.getLogger().info(logcategory, "Parse failed after %,d tokens", tokenCount);
            } else {
                options.getLogger().info(logcategory, "Parse failed after %,d tokens in %s (%s tokens/sec)",
                        tokenCount, timer.elapsed(), timer.perSecond(tokenCount));
            }
            if (options.getPrefixParsing() && checkpoint >= 0) {
                graph.rollback(checkpoint);
                graph.prune();
            }
            result = new EarleyResult(this, chart, graph, success, tokenCount, lastInputToken);
            result.addPredicted(V.openPredictions());
        }

        result.setParseTime(timer.duration());

        return result;
    }

    protected EarleyResult continueParsing(EarleyParser parser) {
        parser.input = input;
        return parser.parseInput(restartPos);
    }

    /**
     * Is there more input?
     * <p>If the parse succeeded, the answer will always be false. But a failed parse
     * can fail because it was unable to process a token or because it ran out of tokens.
     * This method checks if there was any more input after the parse completed.</p>
     * @return true if parsing failed before the entire input was consumed
     */
    public boolean hasMoreInput() {
        return moreInput;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public int getColumnNumber() {
        return columnNumber;
    }

    public int getOffset() {
        return startPos + inputPos;
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
}
