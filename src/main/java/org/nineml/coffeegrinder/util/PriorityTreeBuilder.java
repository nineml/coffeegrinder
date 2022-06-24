package org.nineml.coffeegrinder.util;

import org.nineml.coffeegrinder.parser.RuleChoice;
import org.nineml.coffeegrinder.parser.Symbol;
import org.nineml.coffeegrinder.parser.TreeBuilder;

import java.util.List;

public abstract class PriorityTreeBuilder extends TreeBuilder {
    private static final double defaultPriority = 1.0;

    @Override
    public int chooseFromRemaining(List<RuleChoice> alternatives) {
        ambiguous = true;

        int choice = 0;
        double priority = 0;
        for (int idx = 0; idx < alternatives.size(); idx++) {
            double test = 0;
            // The alternative is null if it matches epsilon
            if (alternatives.get(idx) != null) {
                Symbol nt = alternatives.get(idx).getSymbol();
                Symbol[] rhs = alternatives.get(idx).getRightHandSide();

                if (nt != null && nt.hasAttribute("priority")) {
                    test = Double.parseDouble(alternatives.get(idx).getSymbol().getAttribute("priority").getValue());
                } else {
                    // The rhs is null if this is a non-terminal symbol (as opposed to an intermediate state)
                    if (rhs == null) {
                        test = defaultPriority;
                    } else {
                        for (Symbol symbol : alternatives.get(idx).getRightHandSide()) {
                            if (symbol.hasAttribute("priority")) {
                                test += Double.parseDouble(symbol.getAttribute("priority").getValue());
                            } else {
                                test += defaultPriority;
                            }
                        }
                    }
                }
            }

            if (test > priority) {
                choice = idx;
                priority = test;
            }
        }
        return choice;
    }
}
