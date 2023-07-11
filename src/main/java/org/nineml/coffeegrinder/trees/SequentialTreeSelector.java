package org.nineml.coffeegrinder.trees;

import org.nineml.coffeegrinder.parser.Family;
import org.nineml.coffeegrinder.parser.NonterminalSymbol;
import org.nineml.coffeegrinder.util.ParserAttribute;

import java.util.List;
import java.util.Map;

/**
 * A {@link TreeSelector} that returns each tree in the forest successively.
 * <p>Note that in the case of infinitely ambiguous forests, it skips loops.</p>
 */
public class SequentialTreeSelector implements TreeSelector {
    private boolean madeAmbiguousChoice = false;

    public boolean getMadeAmbiguousChoice() {
        return madeAmbiguousChoice;
    }

    @Override
    public void startNonterminal(NonterminalSymbol symbol, Map<String, String> attributes, int leftExtent, int rightExtent) {
        // nop
    }

    @Override
    public void endNonterminal(NonterminalSymbol symbol, Map<String, String> attributes, int leftExtent, int rightExtent) {
        // nop
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
}
