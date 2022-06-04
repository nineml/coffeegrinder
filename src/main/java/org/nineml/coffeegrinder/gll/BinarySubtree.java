package org.nineml.coffeegrinder.gll;

import org.nineml.coffeegrinder.parser.*;
import org.nineml.coffeegrinder.tokens.Token;

import java.util.*;

public class BinarySubtree {
    private final HashSet<BinarySubtreePrefix> bsrPrefixes;
    private final HashSet<BinarySubtreeSlot> bsrSlots;
    private ArrayList<BinarySubtreeSlot> roots = null;
    private HashSet<BinarySubtreeNode> bsr = null;
    private final NonterminalSymbol seed;
    private int rightExtent = 0;

    protected BinarySubtree(NonterminalSymbol seed) {
        bsrPrefixes = new HashSet<>();
        bsrSlots = new HashSet<>();
        this.seed = seed;
        rightExtent = 0;
    }

    protected void addEpsilon(State slot, int k) {
        BinarySubtreeSlot bsrnode = new BinarySubtreeSlot(slot, k, k, k);
        bsrSlots.add(bsrnode);
    }

    protected void add(State L, int left, int pivot, int right) {
        if (right > rightExtent) {
            rightExtent = right;
        }

        if (L.nextSymbol() == null) {
            BinarySubtreeSlot bsrentry = new BinarySubtreeSlot(L, left, pivot, right);
            bsrSlots.add(bsrentry);
        } else if (L.position > 1) {
            BinarySubtreePrefix bsrentry = new BinarySubtreePrefix(L, left, pivot, right);
            bsrPrefixes.add(bsrentry);
        }
    }

    protected int getRightExtent() {
        return rightExtent;
    }

    protected boolean succeeded() {
        return !getRoots().isEmpty();
    }

    private List<BinarySubtreeSlot> getRoots() {
        if (roots != null) {
            return roots;
        }

        roots = new ArrayList<>();
        for (BinarySubtreeSlot node : bsrSlots) {
            assert node.slot.symbol != null;
            if (node.slot.symbol.equals(seed) && node.leftExtent == 0 && node.rightExtent == rightExtent) {
                roots.add(node);
            }
        }
        return roots;
    }

    protected ParseForest extractSPPF(Grammar grammar, Token[] inputTokens) {
        ParseForestGLL G = new ParseForestGLL(grammar.getParserOptions(), grammar, rightExtent, inputTokens);
        int n = rightExtent;

        if (getRoots().isEmpty()) {
            return G;
        }

        BinarySubtreeNode success = getRoots().get(0);

        if (bsr == null) {
            bsr = new HashSet<>(bsrPrefixes);
            bsr.addAll(bsrSlots);
        }

        G.findOrCreate(success.slot, seed, 0, n);
        ForestNodeGLL w = G.extendableLeaf();
        while (w != null) {
            if (w.symbol != null) {
                for (BinarySubtreeNode node : bsrSlots) {
                    if (node.leftExtent == w.leftExtent
                            && node.rightExtent == w.rightExtent
                            && w.symbol.equals(node.slot.symbol)) {
                        w.addEdge(G.mkPN(node.slot, node.leftExtent, node.pivot, node.rightExtent));
                    }
                }
            } else {
                State u = w.state;
                assert u != null;
                if (u.position == 1) {
                    w.addEdge(G.mkPN(u, w.leftExtent, w.leftExtent, w.rightExtent));
                } else {
                    for (BinarySubtreeNode node : bsr) {
                        if (node instanceof BinarySubtreePrefix) {
                            BinarySubtreePrefix pnode = (BinarySubtreePrefix) node;
                            if (pnode.leftExtent == w.leftExtent
                                    && pnode.rightExtent == w.rightExtent
                                    && pnode.matches(w)) {
                                w.addEdge(G.mkPN(u, node.leftExtent, node.pivot, node.rightExtent));
                            }
                        }
                    }
                }
            }

            w = G.extendableLeaf();
        }

        G.prune();
        return G;
    }
}
