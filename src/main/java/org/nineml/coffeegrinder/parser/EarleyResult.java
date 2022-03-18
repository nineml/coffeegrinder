package org.nineml.coffeegrinder.parser;

import org.nineml.coffeegrinder.exceptions.ParseException;
import org.nineml.coffeegrinder.tokens.Token;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * The results of an Earley parse.
 */
public class EarleyResult {
    private final EarleyParser parser;
    private final EarleyChart chart;
    private final ParseForest graph;
    private final boolean success;
    private final int tokenCount;
    private final Token lastToken;
    private final ParserOptions options;
    private final HashSet<TerminalSymbol> predicted = new HashSet<>();
    private boolean continuingIteratorReturned = false;
    private long parseTime = -1;

    protected EarleyResult(EarleyParser parser, EarleyChart chart, ParseForest graph, boolean success, int tokenCount, Token lastToken) {
        this.parser = parser;
        this.chart = chart;
        this.graph = graph;
        this.success = success;
        this.tokenCount = tokenCount;
        this.lastToken = lastToken;
        this.options = parser.options;
    }

    protected EarleyResult(EarleyParser parser, ParseForest graph, boolean success, int tokenCount, Token lastToken) {
        this.parser = parser;
        this.chart = null;
        this.graph = graph;
        this.success = success;
        this.tokenCount = tokenCount;
        this.lastToken = lastToken;
        this.options = parser.options;
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
     * {@link ParserOptions#returnChart returnChart} option is enabled.</p>
     *
     * @return the Earley chart if it's available, or null otherwise.
     */
    public EarleyChart getChart() {
        return chart;
    }

    /**
     * The shared packed parse forest.
     * <p>For an unsuccessful parse, the forest will be incomplete unless a successful prefix parse
     * was performed. If prefix parsing is enabled and a prefix is found, the chart returned represents
     * the parsed prefix..</p>
     * @return the SPPF.
     */
    public ParseForest getForest() {
        return graph;
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
     * <p>Prefix parsing is only performed if the  {@link ParserOptions#prefixParsing prefixParsing} option is
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

        if (continuingIteratorReturned) {
            options.getLogger().debug(EarleyParser.logcategory, "Attempting to continue parsing after the continuing iterator was exposed");
        }

        EarleyParser newParser = parser.getGrammar().getParser(parser.getSeed());
        return newParser.parse(new PrefixIterator<>(parser.getBufferedTokens(), parser.iterator));
    }

    /**
     * Get an iterator for the rest of the tokens after a matched prefix.
     * <p>If prefix parsing is enabled, and a prefix was identified, this method will return an
     * iterator that begins with the first token after the prefix finished.</p>
     * <p>Note: if you read from this iterator, any future attempt to {@link #continueParsing}
     * will deliver unpredictable results.</p>
     * @return the iterator
     */
    public Iterator<Token> getContinuingIterator() {
        continuingIteratorReturned = true;
        return new PrefixIterator<>(parser.getBufferedTokens(), parser.iterator);
    }

    /**
     * How many tokens were parsed?
     * <p>In the case of an unsuccessful parse, the input iterator <em>will</em> have
     * been advanced beyond this token. You can get a new iterator that will begin in the
     * right place from {@link #getContinuingIterator}. Note, however, that if you read
     * from that iterator and then attempt to continue parsing, it will give unpredicatable results.</p>
     * @return the number of the last (successfully) parsed token.
     */
    public int getTokenCount() {
        return tokenCount;
    }

    /**
     * What was the last token parsed?
     * <p>In the case of an unsuccessful parse, the input iterator <em>will</em> have
     * been advanced beyond this token. You can get a new iterator that will begin in the
     * right place from {@link #getContinuingIterator}. Note, however, that if you read
     * from that iterator and then attempt to continue parsing, it will give unpredicatable results.</p>
     * @return the last (successfully) parsed token.
     */
    public Token getLastToken() {
        return lastToken;
    }

    protected void addPredicted(Set<TerminalSymbol> symbols) {
        predicted.addAll(symbols);
    }

    public Set<TerminalSymbol> predictedTerminals() {
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
