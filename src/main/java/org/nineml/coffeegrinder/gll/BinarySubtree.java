package org.nineml.coffeegrinder.gll;

import org.nineml.coffeegrinder.parser.*;
import org.nineml.coffeegrinder.tokens.Token;

import java.util.*;

public class BinarySubtree {
    private final HashSet<BinarySubtreePrefix> bsrPrefixes;
    private final HashSet<BinarySubtreeSlot> bsrSlots;
    private ArrayList<BinarySubtreeSlot> roots = null;
    private HashSet<BinarySubtreeNode> bsr = null;
    private HashMap<Integer, ArrayList<BinarySubtreeNode>> bsrmap = null;
    private final NonterminalSymbol seed;
    private int rightExtent = 0;
    private boolean ambiguous = false;
    private boolean infinitelyAmbiguous = false;

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

    protected void dump() {
        if (getRoots().isEmpty()) {
            return;
        }

        bsrmap = new HashMap<>();
        for (BinarySubtreeNode node : bsrSlots) {
            if (!bsrmap.containsKey(node.leftExtent)) {
                bsrmap.put(node.leftExtent, new ArrayList<>());
            }
            bsrmap.get(node.leftExtent).add(node);
        }

        for (BinarySubtreeNode node : getRoots()) {
            zcover(node, new HashSet<>());
        }
    }

    private void zcover(BinarySubtreeNode node, HashSet<BinarySubtreeNode> seen) {
        XTree coverings = zcover(node.slot.rhs.symbols, node.leftExtent, node.rightExtent, seen);
        System.err.println("coverings");
    }

    private XTree zcover(Symbol[] symbols, int leftExtent, int rightExtent, HashSet<BinarySubtreeNode> seen) {
        XTree cover = new XTree(leftExtent, rightExtent);
        if (symbols.length == 0) {
            return cover;
        }

        ArrayList<XTree> prevSet = new ArrayList<>();
        if (symbols[0] instanceof TerminalSymbol) {
            XTree next = new XTree((TerminalSymbol) symbols[0], leftExtent);
            cover.addNext(next);
            prevSet.add(next);
        } else {
            for (BinarySubtreeNode node : bsrmap.get(leftExtent)) {
                if (symbols[0].equals(node.slot.symbol) && node.rightExtent <= rightExtent) {
                    XTree next = new XTree(node);
                    if (seen.contains(node)) {
                        ambiguous = true;
                        infinitelyAmbiguous = true;
                    } else {
                        seen.add(node);
                        next.cover = zcover(node.slot.getRhs().symbols, node.leftExtent, node.rightExtent, seen);
                        if (next.cover != null) {
                            if (cover.addNext(next)) {
                                ambiguous = true;
                            }
                            prevSet.add(next);
                        }
                    }
                }
            }
        }

        ArrayList<XTree> nextSet = new ArrayList<>();
        for (int pos = 1; pos < symbols.length; pos++) {
            if (symbols[pos] instanceof TerminalSymbol) {
                XTree next = new XTree((TerminalSymbol) symbols[pos], prevSet.get(0).rightExtent);
                nextSet.add(next);
                for (XTree cp : prevSet) {
                    if (cp.rightExtent == next.leftExtent) {
                        if (cover.addNext(next)) {
                            ambiguous = true;
                        }
                    }
                }
            } else {
                for (XTree pnode : prevSet) {
                    List<BinarySubtreeNode> nodes = bsrmap.getOrDefault(pnode.rightExtent, null);
                    if (nodes != null) {
                        for (BinarySubtreeNode node : nodes) {
                            if (symbols[pos].equals(node.slot.symbol) && node.rightExtent <= rightExtent) {
                                XTree next = new XTree(node);
                                if (seen.contains(node)) {
                                    ambiguous = true;
                                    infinitelyAmbiguous = true;
                                } else {
                                    seen.add(node);
                                    next.cover = zcover(node.slot.getRhs().symbols, node.leftExtent, node.rightExtent, seen);
                                    if (next.cover != null) {
                                        nextSet.add(next);
                                        for (XTree cp : prevSet) {
                                            if (cp.rightExtent == next.leftExtent) {
                                                if (cover.addNext(next)) {
                                                    ambiguous = true;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            prevSet.clear();
            prevSet.addAll(nextSet);
            nextSet.clear();
        }

        return cover;
    }

    private static class XTree {
        public final BinarySubtreeNode node;
        public final TerminalSymbol symbol;
        public final int leftExtent;
        public final int rightExtent;
        public ArrayList<XTree> next = null;
        public XTree cover = null;
        public XTree(int leftExtent, int rightExtent) {
            this.node = null;
            this.symbol = null;
            this.next = new ArrayList<>();
            this.leftExtent = leftExtent;
            this.rightExtent = rightExtent;
        }

        public XTree(BinarySubtreeNode node) {
            this.node = node;
            this.symbol = null;
            leftExtent = node.leftExtent;
            rightExtent = node.rightExtent;
        }

        public XTree(TerminalSymbol symbol, int pos) {
            this.node = null;
            this.symbol = symbol;
            leftExtent = pos;
            rightExtent = pos+1;
        }

        public boolean addNext(XTree tree) {
            if (next == null) {
                next = new ArrayList<>();
            }
            next.add(tree);
            return next.size() > 1;
        }

        @Override
        public String toString() {
            return String.format("%s [%d,%d]", symbol == null ? node : symbol, leftExtent, rightExtent);
        }
    }
}
