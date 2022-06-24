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
    ParseTree getTree();
    void getTree(TreeBuilder builder);
    boolean isAmbiguous();
    boolean isInfinitelyAmbiguous();
    int getTokenCount();
    Token getLastToken();
    int getOffset();
    int getLineNumber();
    int getColumnNumber();
    Set<TerminalSymbol> getPredictedTerminals();
}
