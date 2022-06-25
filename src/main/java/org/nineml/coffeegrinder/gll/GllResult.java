package org.nineml.coffeegrinder.gll;

import org.nineml.coffeegrinder.parser.*;
import org.nineml.coffeegrinder.tokens.Token;
import org.nineml.coffeegrinder.util.ParseTreeBuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class GllResult implements GearleyResult {
    private final GllParser parser;
    private final BinarySubtree bsr;
    private final ParseForest graph;
    public final boolean success;
    private final int tokenCount;
    private final Token lastToken;
    private final int offset;
    private final int lineNumber;
    private final int columnNumber;
    private long parseTime = -1;

    public GllResult(GllParser parser, BinarySubtree bsr) {
        this.parser = parser;
        this.bsr = bsr;
        this.graph = bsr.extractSPPF(parser.getGrammar(), parser.getTokens());
        tokenCount = parser.tokenCount;
        lastToken = parser.lastToken;
        offset = parser.getOffset();
        lineNumber = parser.getLineNumber();
        columnNumber = parser.getColumnNumber();
        success = parser.succeeded();
    }

    @Override
    public GearleyResult continueParsing() {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public ParseForest getForest() {
        return graph;
    }

    @Override
    public ParseTree getTree() {
        ParseTreeBuilder builder = new ParseTreeBuilder();
        getTree(builder);
        return builder.getParseTree();
    }

    @Override
    public void getTree(TreeBuilder builder) {
        graph.getTree(builder);
    }

    public BinarySubtree getBinarySubtree() {
        return bsr;
    }

    @Override
    public boolean isAmbiguous() {
        return graph != null && graph.isAmbiguous();
    }

    @Override
    public boolean isInfinitelyAmbiguous() {
        return graph != null && graph.isInfinitelyAmbiguous();
    }

    @Override
    public int getTokenCount() {
        return tokenCount;
    }

    @Override
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

    public Set<TerminalSymbol> getPredictedTerminals() {
        return Collections.emptySet();
    }

    @Override
    public GearleyParser getParser() {
        return parser;
    }

    @Override
    public boolean prefixSucceeded() {
        return false;
    }

    @Override
    public long getParseTime() {
        return parseTime;
    }

    protected void setParseTime(long time) {
        parseTime = time;
    }

    @Override
    public boolean succeeded() {
        return success;
    }
}
