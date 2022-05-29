package org.nineml.coffeegrinder.gll;

import java.util.ArrayList;

public class GllForestBranch {
    private static int nextId = 0;

    public final BinarySubtreeSlot slot;
    public final int id;
    public final ArrayList<GllForestNode> trees;
    public boolean loop = false;

    public GllForestBranch(BinarySubtreeSlot slot) {
        this.id = ++nextId;
        this.slot = slot;
        this.trees = new ArrayList<>();
    }

    public GllForestBranch(GllForestBranch copy) {
        id = ++nextId;
        slot = copy.slot;
        loop = copy.loop;
        trees = new ArrayList<>(copy.trees);
    }

    @Override
    public String toString() {
        return slot.toString();
    }
}
