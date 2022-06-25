package org.nineml.coffeegrinder.util;

import org.nineml.coffeegrinder.parser.NonterminalSymbol;
import org.nineml.coffeegrinder.parser.RuleChoice;
import org.nineml.coffeegrinder.parser.TreeBuilder;
import org.nineml.coffeegrinder.tokens.Token;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class NopPriorityTreeBuilder extends PriorityTreeBuilder {
    @Override
    public void startNonterminal(NonterminalSymbol symbol, Map<String,String> attributes, int leftExtent, int rightExtent) {
        // nop
    }

    @Override
    public void endNonterminal(NonterminalSymbol symbol, Map<String,String> attributes, int leftExtent, int rightExtent) {
        // nop
    }

    public void token(Token token, Map<String,String> attributes) {
        // nop
    }
}
