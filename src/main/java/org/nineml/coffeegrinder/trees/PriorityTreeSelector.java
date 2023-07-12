package org.nineml.coffeegrinder.trees;

import org.nineml.coffeegrinder.parser.Family;
import org.nineml.coffeegrinder.parser.NonterminalSymbol;
import org.nineml.coffeegrinder.util.ParserAttribute;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A {@link TreeSelector} that uses the {@link org.nineml.coffeegrinder.parser.ForestNode#PRIORITY_ATTRIBUTE}
 * to manage priorities.
 */
public class PriorityTreeSelector implements TreeSelector {
    protected boolean madeAmbiguousChoice = false;

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
        if (choices.size() == 1) {
            PriorityChoice choice = new PriorityChoice(choices, otherChoices);
            // If there were other choices, the might have been loops, but they
            // still make the choices we're making here ambigious
            if (choice.chosen != choices.get(0) || choice.ambiguous) {
                madeAmbiguousChoice = true;
            }
            return choices.get(0);
        }

        PriorityChoice choice = new PriorityChoice(choices);
        madeAmbiguousChoice = madeAmbiguousChoice || choice.ambiguous;

        assert choice.chosen != null;
        return choice.chosen;
    }

    @Override
    public void reset() {
        madeAmbiguousChoice = false;
    }

    private static class PriorityChoice {
        public final boolean ambiguous;
        public final Family chosen;

        public PriorityChoice(List<Family> choices) {
            this(choices, null);
        }

        public PriorityChoice(List<Family> primaryChoices, List<Family> otherChoices) {
            final List<Family> choices;
            if (otherChoices == null || otherChoices.isEmpty()) {
                choices = primaryChoices;
            } else {
                choices = new ArrayList<>(primaryChoices);
                choices.addAll(otherChoices);
            }

            boolean ambiguous = false;
            Family chosen = null;

            int maxPriority = -1;
            int curPriority = 0;
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

            this.chosen = chosen;
            this.ambiguous = ambiguous;
        }
    }

}
