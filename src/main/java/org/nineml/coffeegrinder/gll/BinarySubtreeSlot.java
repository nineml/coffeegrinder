package org.nineml.coffeegrinder.gll;

import org.nineml.coffeegrinder.parser.NonterminalSymbol;
import org.nineml.coffeegrinder.parser.State;
import org.nineml.coffeegrinder.parser.Symbol;
import org.nineml.coffeegrinder.parser.RuleChoice;

public class BinarySubtreeSlot extends BinarySubtreeNode implements RuleChoice {
    protected BinarySubtreeSlot(State slot, int left, int pivot, int right) {
        super(slot, left, pivot, right);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BinarySubtreeSlot) {
            BinarySubtreeSlot other = (BinarySubtreeSlot) obj;
            if (leftExtent != other.leftExtent || rightExtent != other.rightExtent || pivot != other.pivot) {
                return false;
            }
            return slot.equals(other.slot);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return slot.hashCode() + (11 * leftExtent) + (13 * pivot) + (31 * rightExtent);
    }

    @Override
    public NonterminalSymbol getSymbol() {
        return slot.symbol;
    }

    @Override
    public Symbol[] getRightHandSide() {
        return slot.rhs.symbols;
    }

    @Override
    public int getLeftExtent() {
        return leftExtent;
    }

    @Override
    public int getRightExtent() {
        return rightExtent;
    }
}
