package org.nineml.coffeegrinder.trees;

import org.nineml.coffeegrinder.parser.Family;
import org.nineml.coffeegrinder.parser.NonterminalSymbol;
import org.nineml.coffeegrinder.util.ParserAttribute;

import java.util.List;
import java.util.Map;

public interface TreeSelector {
    boolean getMadeAmbiguousChoice();
    void startNonterminal(NonterminalSymbol symbol, Map<String,String> attributes, int leftExtent, int rightExtent);
    void endNonterminal(NonterminalSymbol symbol, Map<String,String> attributes, int leftExtent, int rightExtent);
    Family select(List<Family> choices, List<Family> otherChoices);
    void reset();
}
