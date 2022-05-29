package org.nineml.coffeegrinder.gll;

import org.nineml.coffeegrinder.parser.*;
import org.nineml.coffeegrinder.tokens.Token;

import java.math.BigInteger;
import java.util.*;

public class BinarySubtree {
    private final HashSet<BinarySubtreePrefix> bsrPrefixes;
    private final HashSet<BinarySubtreeSlot> bsrSlots;
    private ArrayList<BinarySubtreeSlot> roots = null;
    private HashSet<BinarySubtreeNode> bsr = null;
    private final NonterminalSymbol seed;
    private final HashMap<BinarySubtreeNode,HashSet<BinarySubtreeNode>> edges;
    private int rightExtent = 0;
    private boolean ambiguous = false;
    private boolean infinitelyAmbiguous = false;

    public BinarySubtree(NonterminalSymbol seed) {
        bsrPrefixes = new HashSet<>();
        bsrSlots = new HashSet<>();
        this.seed = seed;
        edges = new HashMap<>();
        rightExtent = 0;
    }

    public void addEpsilon(State slot, int k) {
        BinarySubtreeSlot bsrnode = new BinarySubtreeSlot(slot, k, k, k);
        if (!bsrSlots.contains(bsrnode)) {
            bsrSlots.add(bsrnode);
        }
    }

    public void add(State L, int left, int pivot, int right) {
        if (right > rightExtent) {
            rightExtent = right;
        }

        if (L.nextSymbol() == null) {
            BinarySubtreeSlot bsrentry = new BinarySubtreeSlot(L, left, pivot, right);
            if (!bsrSlots.contains(bsrentry)) {
                //System.err.println("*** add slt: " + bsrentry);
                bsrSlots.add(bsrentry);
            }
        } else if (L.position > 1) {
            BinarySubtreePrefix bsrentry = new BinarySubtreePrefix(L, left, pivot, right);
            if (!bsrPrefixes.contains(bsrentry)) {
                //System.err.println("*** add pfx: " + bsrentry);
                bsrPrefixes.add(bsrentry);
            }
        }
    }

    public int getRightExtent() {
        return rightExtent;
    }

    protected boolean succeeded() {
        return !getRoots().isEmpty();
    }

    protected boolean isAmbiguous() {
        return ambiguous;
    }

    protected boolean isInfinitelyAmbiguous() {
        return infinitelyAmbiguous;
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

    protected PackedForest extractSPPF(Grammar grammar) {
        PackedForest G = new PackedForest();
        int n = rightExtent;

        if (getRoots().isEmpty()) {
            return G;
        }

        BinarySubtreeNode success = getRoots().get(0);

        if (bsr == null) {
            bsr = new HashSet<>(bsrPrefixes);
            bsr.addAll(bsrSlots);
        }

        if (success != null) {
            G.findOrCreate(success.slot, seed, 0, n);
            PackedForestSymbol w = G.extendableLeaf();
            while (w != null) {
                //System.err.println("w = " + w);

                if (w.symbol == null) {
                    State u = w.slot;
                    if (u.position == 1) {
                        w.addEdge(G.mkPN(u, w.leftExtent, w.leftExtent, w.rightExtent));
                    } else {
                        for (BinarySubtreeNode node : bsr) {
                            if (node instanceof BinarySubtreePrefix) {
                                BinarySubtreePrefix pnode = (BinarySubtreePrefix) node;
                                if (pnode.leftExtent == w.leftExtent && pnode.rightExtent == w.rightExtent
                                        && pnode.matches(w)) {
                                    w.addEdge(G.mkPN(u, node.leftExtent, node.pivot, node.rightExtent));
                                }
                            }
                        }
                    }
                } else {
                    for (BinarySubtreeNode node : bsrSlots) {
                        if (node.leftExtent == w.leftExtent && node.rightExtent == w.rightExtent
                                && w.symbol.equals(node.slot.symbol)) {
                            w.addEdge(G.mkPN(node.slot, node.leftExtent, node.pivot, node.rightExtent));
                        }
                    }
                }

                w = G.extendableLeaf();
            }
        }

        G.tidy();
        return G;
    }

    protected EarleyForest extractSPPF2(Grammar grammar, Token[] inputTokens) {
        EarleyForestGLL G = new EarleyForestGLL(grammar.getParserOptions(), grammar, rightExtent, inputTokens);
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
