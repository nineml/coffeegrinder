package org.nineml.coffeegrinder.trees;

import org.nineml.coffeegrinder.parser.Family;
import org.nineml.coffeegrinder.parser.NonterminalSymbol;
import org.nineml.coffeegrinder.util.ParserAttribute;

import java.util.List;
import java.util.Map;

public class SequentialTreeSelector implements TreeSelector {
    private boolean madeAmbiguousChoice = false;

    public boolean getMadeAmbiguousChoice() {
        return madeAmbiguousChoice;
    }

    @Override
    public Family select(List<Family> choices, List<Family> otherChoices) {
        madeAmbiguousChoice = true;
        return choices.get(0);
    }

    @Override
    public void reset() {
        madeAmbiguousChoice = false;
    }

    @Override
    public void startNonterminal(NonterminalSymbol symbol, Map<String,String> attributes, int leftExtent, int rightExtent) {
        // nop
    }

    @Override
    public void endNonterminal(NonterminalSymbol symbol, Map<String,String> attributes, int leftExtent, int rightExtent) {
        // nop
    }
}
