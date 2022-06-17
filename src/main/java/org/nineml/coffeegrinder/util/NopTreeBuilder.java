package org.nineml.coffeegrinder.util;

import org.nineml.coffeegrinder.parser.NonterminalSymbol;
import org.nineml.coffeegrinder.parser.TreeBuilder;
import org.nineml.coffeegrinder.parser.RuleChoice;
import org.nineml.coffeegrinder.tokens.Token;

import java.util.Collection;
import java.util.List;

public class NopTreeBuilder implements TreeBuilder {
    private boolean ambiguous = false;
    private boolean infinitelyAmbiguous = false;

    @Override
    public boolean isAmbiguous() {
        return ambiguous;
    }

    @Override
    public boolean isInfinitelyAmbiguous() {
        ambiguous = true;
        return infinitelyAmbiguous;
    }

    @Override
    public int chooseAlternative(List<RuleChoice> alternatives) {
        ambiguous = true;
        return 0;
    }

    @Override
    public void loop(RuleChoice alternative) {
        ambiguous = true;
        infinitelyAmbiguous = true;
    }

    @Override
    public void startTree() {
        // nop
    }

    @Override
    public void endTree() {
        // nop
    }

    @Override
    public void startNonterminal(NonterminalSymbol symbol, Collection<ParserAttribute> attributes, int leftExtent, int rightExtent) {
        // nop
    }

    @Override
    public void endNonterminal(NonterminalSymbol symbol, Collection<ParserAttribute> attributes, int leftExtent, int rightExtent) {
        // nop
    }

    @Override
    public void token(Token token, Collection<ParserAttribute> attributes) {
        // nop
    }
}
