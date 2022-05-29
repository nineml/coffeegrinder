package org.nineml.coffeegrinder.gll;

import java.util.ArrayList;
import java.util.List;

public abstract class PackedForestNode {
    private static int nextNodeId = 0;

    public final int nodeId;
    private final ArrayList<PackedForestNode> edges;
    private final ArrayList<XFamily> families;
    private PackedForestNode parent = null;
    private boolean pruned = false;

    protected PackedForestNode() {
        nodeId = ++nextNodeId;
        edges = new ArrayList<>();
        families = new ArrayList<>();
    }

    public PackedForestNode getParent() {
        return parent;
    }

    protected boolean getPruned() {
        return pruned;
    }

    public abstract int getLeftExtent();

    public abstract int getRightExtent();

    protected void addEdge(PackedForestNode node) {
        //System.err.printf("\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t%s -> %s%n", this, node);
        System.err.printf("EDGE %s   ->   %s (%d / %d)%n", this, node, edges.size(), node.edges.size());
        edges.add(node);
        node.parent = this;

        if (this instanceof PackedForestIntermediate) {
            if (families.isEmpty()) {
                families.add(new XFamily(node));
            } else {
                assert families.size() == 1;
                assert families.get(0).w == null;
                families.get(0).w = node;
            }
            return;
        }

        final XFamily fam;
        if (node instanceof PackedForestIntermediate) {
            assert node.families.size() == 1;
            fam = node.families.get(0);
        } else {
            fam = new XFamily(node);
        }
        families.add(fam);
    }

    protected void tidyEdges() {
        ArrayList<PackedForestNode> newEdges = new ArrayList<>();

        for (PackedForestNode child : edges) {
            //System.err.printf("UNTIDY: %s -> %s%n", this, child);
            if (child instanceof PackedForestIntermediate && child.getEdges().size() == 1) {
                //System.err.printf("  TIDY: %s -> %s%n", this, child.getEdges().get(0));
                newEdges.add(child.getEdges().get(0));
                child.pruned = true;
            } else {
                newEdges.add(child);
            }
        }
        edges.clear();
        edges.addAll(newEdges);
    }

    protected List<PackedForestNode> getEdges() {
        return new ArrayList<>(edges);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PackedForestNode) {
            return nodeId == ((PackedForestNode) obj).nodeId;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return nodeId;
    }
}
