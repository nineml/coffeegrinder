package org.nineml.coffeegrinder.parser;

import org.nineml.coffeegrinder.tokens.Token;

import java.util.Set;

public interface GearleyResult {
    GearleyParser getParser();
    boolean succeeded();
    boolean prefixSucceeded();
    long getParseTime();
    GearleyResult continueParsing();
    ParseForest getForest();
    int getTokenCount();
    Token getLastToken();
    Set<TerminalSymbol> getPredictedTerminals();
}
