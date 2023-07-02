package org.nineml.coffeegrinder.parser;

import org.nineml.coffeegrinder.exceptions.ParseException;
import org.nineml.coffeegrinder.trees.ParseTree;
import org.nineml.coffeegrinder.trees.ParseTreeBuilder;
import org.nineml.coffeegrinder.trees.TreeBuilder;
import org.nineml.coffeegrinder.tokens.Token;
import org.nineml.coffeegrinder.trees.TreeSelector;

import java.util.*;

/**
 * The results of an Earley parse.
 */
public class EarleyResult implements GearleyResult {
    private final EarleyParser parser;
    private final EarleyChart chart;
    private final ParseForest graph;
    private final boolean success;
    private final int tokenCount;
    private final Token lastToken;
    private final int offset;
    private final int lineNumber;
    private final int columnNumber;
    private final ParserOptions options;
    private final HashSet<TerminalSymbol> predicted = new HashSet<>();
    private ForestWalker walker = null;
    private long parseTime = -1;

    protected EarleyResult(EarleyParser parser, EarleyChart chart, ParseForest graph, boolean success, int tokenCount, Token lastToken) {
        this.parser = parser;
        this.chart = chart;
        this.graph = graph;
        this.success = success;
        this.tokenCount = tokenCount;
        this.lastToken = lastToken;
        this.options = parser.options;
        this.offset = parser.getOffset();
        this.lineNumber = parser.getLineNumber();
        this.columnNumber = parser.getColumnNumber();
    }

    protected EarleyResult(EarleyParser parser, ParseForest graph, boolean success, int tokenCount, Token lastToken) {
        this.parser = parser;
        this.chart = null;
        this.graph = graph;
        this.success = success;
        this.tokenCount = tokenCount;
        this.lastToken = lastToken;
        this.options = parser.options;
        this.offset = parser.getOffset();
        this.lineNumber = parser.getLineNumber();
        this.columnNumber = parser.getColumnNumber();
    }

    /**
     * How long did the parse take?
     * <p>Returns the number of milliseconds required to parse the input. Returns -1 if
     * the timing is unavailable.</p>
     * @return the parse time
     */
    public long getParseTime() {
        return parseTime;
    }

    protected void setParseTime(long time) {
        parseTime = time;
    }

    /**
     * Get the parser for this result.
     * @return the parser that produced this result.
     */
    public EarleyParser getParser() {
        return parser;
    }

    /**
     * Get the Earley chart for the parse.
     * <p>After a parse, the Earley chart isn't usually very useful. It's discarded unless the
     * {@link ParserOptions#getReturnChart()} returnChart} option is enabled.</p>
     *
     * @return the Earley chart if it's available, or null otherwise.
     */
    public EarleyChart getChart() {
        return chart;
    }

    @Override
    public ParseForest getForest() {
        return graph;
    }

    @Override
    public boolean hasMoreTrees() {
        if (walker == null) {
            walker = graph.getWalker();
        }
        return walker.hasMoreTrees();
    }

    @Override
    public void resetTrees() {
        if (walker == null) {
            walker = graph.getWalker();
            return;
        }
        walker.reset();
    }

    @Override
    public void setTreeSelector(TreeSelector selector) {
        walker = graph.getWalker(selector);
    }

    @Override
    public ParseTree getTree() {
        if (hasMoreTrees()) {
            ParseTreeBuilder builder = new ParseTreeBuilder();
            walker.getNextTree(builder);
            return builder.getTree();
        }
        return null;
    }

    /**
     * Get a tree.
     */
    @Override
    public void getTree(TreeBuilder builder) {
        if (hasMoreTrees()) {
            walker.getNextTree(builder);
        }
    }

    @Override
    public Set<Integer> lastSelectedNodes() {
        if (walker == null) {
            return Collections.emptySet();
        }
        return walker.selectedNodes();
    }

    @Override
    public boolean isAmbiguous() {
        return graph != null && graph.isAmbiguous();
    }

    @Override
    public boolean isInfinitelyAmbiguous() {
        return graph != null && graph.isInfinitelyAmbiguous();
    }

    /**
     * Did the parse succeed?
     * @return true if the parse succeeded
     */
    public boolean succeeded() {
        return success;
    }

    /**
     * Did the parse match the beginning of the input?
     * <p>Suppose you're looking for "abb" and you give "abbc" as the input. That parse will fail,
     * but it did succeed on a prefix of the input. This method will return true if the input began
     * with a string that could be successfully parsed.</p>
     * <p>Prefix parsing is only performed if the  {@link ParserOptions#getPrefixParsing() prefixParsing} option is
     * enabled.</p>
     * <p>Note: if the whole parse succeeded, this method returns false.</p>
     * @return true if a prefix was successfully parsed, false otherwise.
     */
    public boolean prefixSucceeded() {
        if (success || !parser.getGrammar().getParserOptions().getPrefixParsing()) {
            return false;
        }

        if (chart != null && chart.size() > 1) {
            for (EarleyItem item : chart.get(chart.size()-2)) {
                if (item.state.completed() && item.state.getSymbol().equals(parser.getSeed()) && item.j == 0) {
                    return true;
                }
            }
        }

        return false;
    }

    public Token[] getSuffix() {
        if (!prefixSucceeded()) {
            return null;
        }

        Token[] tokens = new Token[parser.input.length - parser.restartPos];
        System.arraycopy(parser.input, parser.restartPos, tokens, 0, tokens.length);
        return tokens;
    }

    /**
     * Continue parsing from the last successfully matched prefix.
     * <p>If prefix parsing is enabled, and a prefix was identified, this method will attempt to continue
     * parsing from the next token after the previous prefix parse.</p>
     * @return a parse result
     * @throws ParseException if this result doesn't indicate that a prefix parse was successful
     */
    public EarleyResult continueParsing() {
        if (!prefixSucceeded()) {
            throw ParseException.attemptToContinueInvalidParse();
        }

        EarleyParser newParser = (EarleyParser) parser.getGrammar().getParser(options);
        return parser.continueParsing(newParser);
    }

    /**
     * How many tokens were parsed?
     * <p>In the case of an unsuccessful parse, restarting may occur before this position.</p>
     * @return the number of tokens parsed.
     */
    public int getTokenCount() {
        return tokenCount;
    }

    /**
     * What was the last token parsed?
     * <p>In the case of an unsuccessful parse, restarting may reparse this token.</p>
     * @return the last (successfully) parsed token.
     */
    public Token getLastToken() {
        return lastToken;
    }

    public int getOffset() {
        return offset;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public int getColumnNumber() {
        return columnNumber;
    }

    protected void addPredicted(Set<TerminalSymbol> symbols) {
        predicted.addAll(symbols);
    }

    public Set<TerminalSymbol> getPredictedTerminals() {
        return predicted;
    }

    private static final class PrefixIterator<Token> implements Iterator<Token> {
        private ArrayList<Token> buffer = null;
        private Iterator<Token> iterator = null;

        public PrefixIterator(List<Token> buffer, Iterator<Token> iterator) {
            this.buffer = new ArrayList<>(buffer);
            this.iterator = iterator;
        }

        @Override
        public boolean hasNext() {
            return !buffer.isEmpty() || iterator.hasNext();
        }

        @Override
        public Token next() {
            if (buffer.isEmpty()) {
                return iterator.next();
            }
            Token t = buffer.get(0);
            buffer.remove(0);
            return t;
        }
    }
}
