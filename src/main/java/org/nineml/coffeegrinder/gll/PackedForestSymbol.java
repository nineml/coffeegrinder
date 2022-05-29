package org.nineml.coffeegrinder.gll;

import org.nineml.coffeegrinder.parser.State;
import org.nineml.coffeegrinder.parser.Symbol;

public class PackedForestSymbol extends PackedForestNode {
    public final State slot;
    public final Symbol symbol;
    public final int leftExtent;
    public final int rightExtent;
    public PackedForestSymbol(State slot, Symbol symbol, int left, int right) {
        this.slot = slot;
        this.symbol = symbol;
        this.leftExtent = left;
        this.rightExtent = right;
    }

    public PackedForestSymbol(State slot,int left, int right) {
        this.slot = slot;
        this.symbol = null;
        this.leftExtent = left;
        this.rightExtent = right;
    }

    @Override
    public int getLeftExtent() {
        return leftExtent;
    }

    @Override
    public int getRightExtent() {
        return rightExtent;
    }

    /*
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PackedForestSymbol) {
            PackedForestSymbol other = (PackedForestSymbol) obj;
            return symbol.equals(other.symbol) && leftExtent == other.leftExtent && rightExtent == other.rightExtent;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return symbol.hashCode() + (31 * leftExtent) + (17 * rightExtent);
    }
    */

    @Override
    public String toString() {
        if (symbol == null) {
            StringBuilder sb = new StringBuilder();
            for (int pos = 0; pos < slot.position; pos++) {
                if (pos > 0) {
                    sb.append(" ");
                }
                sb.append(slot.rhs.get(pos));
            }
            sb.append(", ").append(leftExtent).append(", ").append(rightExtent);
            return sb.toString();
        }
        return symbol.toString() + ", " + leftExtent + ", " + rightExtent;
    }
}
