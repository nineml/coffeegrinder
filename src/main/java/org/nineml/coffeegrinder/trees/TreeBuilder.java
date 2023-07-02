package org.nineml.coffeegrinder.trees;

import org.nineml.coffeegrinder.parser.NonterminalSymbol;
import org.nineml.coffeegrinder.tokens.Token;

import java.util.Map;
import java.util.Set;

public interface TreeBuilder {
    void startTree(boolean ambiguous, boolean infinitelyAmbiguous);
    void endTree(boolean madeAmbiguousChoice);
    void startNonterminal(NonterminalSymbol symbol, Map<String,String> attributes, int leftExtent, int rightExtent);
    void endNonterminal(NonterminalSymbol symbol, Map<String,String> attributes, int leftExtent, int rightExtent);
    void token(Token token, Map<String,String> attributes, int leftExtent, int rightExtent);
}
