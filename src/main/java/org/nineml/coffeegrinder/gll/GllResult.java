package org.nineml.coffeegrinder.gll;

import org.nineml.coffeegrinder.parser.*;
import org.nineml.coffeegrinder.tokens.Token;

import java.util.Collections;
import java.util.Set;

public class GllResult implements GearleyResult {
    private final GllParser parser;
    private final ParseForest forest;
    public final boolean success;
    private final int tokenCount;
    private final Token lastToken;

    public GllResult(GllParser parser, ParseForest forest) {
        this.parser = parser;
        this.forest = forest;
        tokenCount = parser.tokenCount;
        lastToken = parser.lastToken;
        success = parser.succeeded();
    }

    @Override
    public GearleyResult continueParsing() {
        throw new UnsupportedOperationException("Not implemented.");
    }

    public ParseForest getForest() {
        return forest;
    }

    @Override
    public int getTokenCount() {
        return tokenCount;
    }

    @Override
    public Token getLastToken() {
        return lastToken;
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
        return -1;
    }

    @Override
    public boolean succeeded() {
        return success;
    }
}
