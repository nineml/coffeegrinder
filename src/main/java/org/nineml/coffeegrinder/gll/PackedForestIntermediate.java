package org.nineml.coffeegrinder.gll;

import org.nineml.coffeegrinder.parser.State;
import org.nineml.coffeegrinder.parser.Symbol;

import java.util.Arrays;

public class PackedForestIntermediate extends PackedForestNode {
    public final State slot;
    public final int pivot;

    private int leftExtent = -1;
    private int rightExtent = -1;

    public PackedForestIntermediate(State slot, int pivot) {
        this.slot = slot;
        this.pivot = pivot;
    }

    @Override
    public int getLeftExtent() {
        if (leftExtent < 0) {
            leftExtent = Integer.MAX_VALUE;
            for (PackedForestNode node : getEdges()) {
                if (node.getLeftExtent() < leftExtent) {
                    leftExtent = node.getLeftExtent();
                }
            }
        }

        return leftExtent;
    }

    @Override
    public int getRightExtent() {
        if (rightExtent < 0) {
            rightExtent = 0;
            for (PackedForestNode node : getEdges()) {
                if (node.getRightExtent() > rightExtent) {
                    rightExtent = node.getRightExtent();
                }
            }
        }

        return rightExtent;
    }


    /*
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PackedForestIntermediate) {
            PackedForestIntermediate other = (PackedForestIntermediate) obj;
            if (pivot != other.pivot) {
                return false;
            }
            return slot.rhs.equals(other.slot.rhs);
        }
        return false;
    }

    @Override
    public int hashCode() {
        Symbol[] prefix = new Symbol[slot.position];
        System.arraycopy(slot.rhs.symbols, 0, prefix, 0, slot.position);
        int code = Arrays.hashCode(prefix);
        return code + (17 * pivot);
    }
    */

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(slot.symbol).append(" ::= ");
        for (int pos = 0; pos < slot.position; pos++) {
            if (pos > 0) {
                sb.append(" ");
            }
            sb.append(slot.rhs.get(pos));
        }
        sb.append(", ").append(pivot);
        return sb.toString();
    }
}
