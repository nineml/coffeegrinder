package org.nineml.coffeegrinder.trees;

import org.nineml.coffeegrinder.parser.NonterminalSymbol;
import org.nineml.coffeegrinder.tokens.Token;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class NopTreeBuilder implements TreeBuilder {
    @Override
    public void startTree(boolean ambiguous, boolean infinitelyAmbiguous) {
        // nop
    }

    @Override
    public void endTree(boolean madeAmbiguousChoice) {
        // nop
    }

    @Override
    public void startNonterminal(NonterminalSymbol symbol, Map<String,String> attributes, int leftExtent, int rightExtent) {
        // nop
    }

    @Override
    public void endNonterminal(NonterminalSymbol symbol, Map<String,String> attributes, int leftExtent, int rightExtent) {
        // nop
    }

    @Override
    public void token(Token token, Map<String,String> attributes, int leftExtent, int rightExtent) {
        // nop
    }
}
