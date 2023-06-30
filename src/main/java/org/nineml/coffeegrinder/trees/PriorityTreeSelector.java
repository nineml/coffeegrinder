package org.nineml.coffeegrinder.trees;

import org.nineml.coffeegrinder.parser.Family;
import org.nineml.coffeegrinder.parser.NonterminalSymbol;
import org.nineml.coffeegrinder.util.ParserAttribute;

import java.util.List;
import java.util.Map;

public class PriorityTreeSelector implements TreeSelector {
    protected boolean madeAmbiguousChoice = false;

    public boolean getMadeAmbiguousChoice() {
        return madeAmbiguousChoice;
    }
    @Override
    public Family select(List<Family> choices, List<Family> otherChoices) {
        if (choices.size() == 1) {
            return choices.get(0);
        }

        boolean ambiguous = false;
        int maxPriority = -1;
        int curPriority = 0;
        Family chosen = null;
        for (Family family : choices) {
            curPriority = family.getPriority();
            if (curPriority > maxPriority) {
                maxPriority = curPriority;
                chosen = family;
                ambiguous = false;
            } else if (curPriority == maxPriority) {
                ambiguous = true;
            }
        }

        madeAmbiguousChoice = madeAmbiguousChoice || ambiguous;

        assert chosen != null;
        return chosen;
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
