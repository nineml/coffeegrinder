package org.nineml.coffeegrinder.parser;

import org.nineml.coffeegrinder.tokens.Token;
import org.nineml.coffeegrinder.util.ParserAttribute;

import java.util.Collection;
import java.util.List;

public interface TreeBuilder {
    boolean isAmbiguous();
    boolean isInfinitelyAmbiguous();
    int chooseAlternative(List<RuleChoice> alternatives);
    void loop(RuleChoice alternative);
    void startTree();
    void endTree();
    void startNonterminal(NonterminalSymbol symbol, Collection<ParserAttribute> attributes, int leftExtent, int rightExtent);
    void endNonterminal(NonterminalSymbol symbol, Collection<ParserAttribute> attributes, int leftExtent, int rightExtent);
    void token(Token token, Collection<ParserAttribute> attributes);
}
