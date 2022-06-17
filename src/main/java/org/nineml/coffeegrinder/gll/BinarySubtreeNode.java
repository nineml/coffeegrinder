package org.nineml.coffeegrinder.gll;

import org.nineml.coffeegrinder.parser.State;

public abstract class BinarySubtreeNode {
    private static int nextNodeId = 0;

    public final int nodeId;
    public final State slot;
    public final int leftExtent;
    public final int rightExtent;
    public final int pivot;

    protected BinarySubtreeNode(State slot, int left, int pivot, int right) {
        this.nodeId = ++nextNodeId;
        this.slot = slot;
        this.leftExtent = left;
        this.rightExtent = right;
        this.pivot = pivot;
    }

    @Override
    public String toString() {
        return String.format("%s, %d, %d, %d", slot, leftExtent, pivot, rightExtent);
    }
}
