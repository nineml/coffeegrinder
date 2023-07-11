package org.nineml.coffeegrinder.parser;

import org.nineml.coffeegrinder.tokens.Token;
import org.nineml.coffeegrinder.trees.ParseTree;
import org.nineml.coffeegrinder.trees.TreeBuilder;
import org.nineml.coffeegrinder.trees.TreeSelector;

import java.util.Set;

public interface GearleyResult {
    GearleyParser getParser();
    boolean succeeded();
    boolean prefixSucceeded();
    long getParseTime();
    GearleyResult continueParsing();
    ParseForest getForest();
    boolean hasMoreTrees();
    void resetTrees();
    void setTreeSelector(TreeSelector selector);
    ParseTree getTree();
    void getTree(TreeBuilder builder);
    Set<Integer> lastSelectedNodes();
    boolean isAmbiguous();
    boolean isInfinitelyAmbiguous();
    int getTokenCount();
    Token getLastToken();
    int getOffset();
    int getLineNumber();
    int getColumnNumber();
    Set<TerminalSymbol> getPredictedTerminals();
}
