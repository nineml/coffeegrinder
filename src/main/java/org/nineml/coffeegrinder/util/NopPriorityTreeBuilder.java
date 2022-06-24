package org.nineml.coffeegrinder.util;

import org.nineml.coffeegrinder.parser.NonterminalSymbol;
import org.nineml.coffeegrinder.parser.RuleChoice;
import org.nineml.coffeegrinder.parser.TreeBuilder;
import org.nineml.coffeegrinder.tokens.Token;

import java.util.Collection;
import java.util.List;

public class NopPriorityTreeBuilder extends PriorityTreeBuilder {
    @Override
    public void startNonterminal(NonterminalSymbol symbol, Collection<ParserAttribute> attributes, int leftExtent, int rightExtent) {
        // nop
    }

    @Override
    public void endNonterminal(NonterminalSymbol symbol, Collection<ParserAttribute> attributes, int leftExtent, int rightExtent) {
        // nop
    }

    public void token(Token token, Collection<ParserAttribute> attributes) {
        // nop
    }
}
