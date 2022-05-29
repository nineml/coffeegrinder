package org.nineml.coffeegrinder.gll;

import org.nineml.coffeegrinder.parser.NonterminalSymbol;
import org.nineml.coffeegrinder.parser.State;
import org.nineml.coffeegrinder.parser.Symbol;
import org.nineml.coffeegrinder.parser.TerminalSymbol;
import org.nineml.coffeegrinder.tokens.Token;
import org.nineml.coffeegrinder.tokens.TokenCharacter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class PackedForest {
    private final ArrayList<PackedForestNode> allNodes;
    private final HashSet<PackedForestNode> extendedLeaves;
    private final HashMap<Symbol, HashMap<Integer, HashMap<Integer, ArrayList<PackedForestSymbol>>>> nodes;
    private final HashMap<Symbol, PrefixTrie> slotPrefixes;
    private final HashMap<PrefixTrie, HashMap<Integer, HashMap<Integer, ArrayList<PackedForestSymbol>>>> slots;
    private final HashMap<Symbol, PrefixTrie> intermediate;

    protected PackedForest() {
        allNodes = new ArrayList<>();
        extendedLeaves = new HashSet<>();
        nodes = new HashMap<>();
        slotPrefixes = new HashMap<>();
        slots = new HashMap<>();
        intermediate = new HashMap<>();
    }

    public List<PackedForestNode> getNodes() {
        return allNodes;
    }

    int f = 0;
    int nf = 0;
    protected PackedForestSymbol findOrCreate(State state, Symbol symbol, int leftExtent, int rightExtent) {
        boolean found = true;
        if (!nodes.containsKey(symbol)) {
            nodes.put(symbol, new HashMap<>());
            found = false;
        }
        if (!nodes.get(symbol).containsKey(leftExtent)) {
            nodes.get(symbol).put(leftExtent, new HashMap<>());
            found = false;
        }
        if (!nodes.get(symbol).get(leftExtent).containsKey(rightExtent)) {
            found = false;
            PackedForestSymbol node = new PackedForestSymbol(state, symbol, leftExtent, rightExtent);
            allNodes.add(node);
            ArrayList<PackedForestSymbol> list = new ArrayList<>();
            list.add(node);
            nodes.get(symbol).get(leftExtent).put(rightExtent, list);

            if (found) {
                f++;
                System.err.printf("found symbol: %d / %d%n", f, nf);
            } else {
                nf++;
            }
            return node;
        }
        if (found) {
            f++;
            System.err.printf("found symbol: %d / %d%n", f, nf);
        } else {
            nf++;
        }

        return nodes.get(symbol).get(leftExtent).get(rightExtent).get(0);
    }

    int f2 = 0;
    int nf2 = 0;
    protected PackedForestSymbol findOrCreate(State slot, int leftExtent, int rightExtent) {
        boolean found = true;
        PrefixTrie trie = getPrefix(slot, slotPrefixes);

        if (!slots.containsKey(trie)) {
            slots.put(trie, new HashMap<>());
            found = false;
        }
        if (!slots.get(trie).containsKey(leftExtent)) {
            slots.get(trie).put(leftExtent, new HashMap<>());
            found = false;
        }
        if (!slots.get(trie).get(leftExtent).containsKey(rightExtent)) {
            found = false;
            PackedForestSymbol node = new PackedForestSymbol(slot, leftExtent, rightExtent);
            allNodes.add(node);
            ArrayList<PackedForestSymbol> list = new ArrayList<>();
            list.add(node);
            slots.get(trie).get(leftExtent).put(rightExtent, list);

            if (found) {
                f2++;
                System.err.printf("found2 slot: %d / %d%n", f2, nf2);
            } else {
                nf2++;
            }

            return node;
        }

        if (found) {
            f2++;
            System.err.printf("found2 slot: %d / %d%n", f2, nf2);
        } else {
            nf2++;
        }

        return slots.get(trie).get(leftExtent).get(rightExtent).get(0);
    }

    protected PackedForestIntermediate create(State slot, int pivot) {
        PrefixTrie trie = getPrefix(slot, intermediate);
        if (!trie.nodes.containsKey(pivot)) {
            trie.nodes.put(pivot, new ArrayList<>());
        }

        PackedForestIntermediate node = new PackedForestIntermediate(slot, pivot);
        allNodes.add(node);
        trie.nodes.get(pivot).add(node);
        return node;
    }

    private PrefixTrie getPrefix(State slot, HashMap<Symbol,PrefixTrie> root) {
        final Symbol start;
        if (slot.position == 0) {
            start = TerminalSymbol.EPSILON;
        } else {
            start = slot.rhs.get(0);
        }
        if (!root.containsKey(start)) {
            root.put(start, new PrefixTrie(start));
        }
        PrefixTrie trie = root.get(start);
        for (int pos = 1; pos < slot.position; pos++) {
            trie = trie.child(slot.rhs.get(pos));
        }
        return trie;
    }

    protected PackedForestSymbol extendableLeaf() {
        for (PackedForestNode node : allNodes) {
            if (!(node instanceof PackedForestIntermediate) && !extendedLeaves.contains(node)) {
                if (node instanceof PackedForestSymbol) {
                    PackedForestSymbol symnode = (PackedForestSymbol) node;
                    if (symnode.symbol == null) {
                        extendedLeaves.add(node);
                        return symnode;
                    }
                    if (symnode.symbol instanceof NonterminalSymbol) {
                        extendedLeaves.add(symnode);
                        return symnode;
                    }
                }
            }
        }
        return null;
    }

    protected PackedForestNode mkPN(State slot, int leftExtent, int pivot, int rightExtent) {
        //System.err.printf("\tmkPN(%s, %d, %d, %d)%n", slot, leftExtent, pivot, rightExtent);
        PackedForestIntermediate y = create(slot, pivot);
        //System.err.printf("\ty = %s%n", y);
        if (slot.rhs.length == 1 && slot.rhs.get(0).equals(TerminalSymbol.EPSILON)) {
            mkN(slot, TerminalSymbol.EPSILON, leftExtent, leftExtent, y);
            return y;
        }

        if (slot.position > 0) {
            Symbol x = slot.prevSymbol();
            mkN(slot, x, pivot, rightExtent, y);
            if (slot.position == 2) {
                mkN(slot, slot.rhs.get(0), leftExtent, pivot, y);
            } else if (slot.position > 2) {
                assert slot.rule != null;
                State newSlot = new State(slot.rule, slot.position-1);
                mkN(newSlot, leftExtent, pivot, y);
            }
        }

        return y;
    }

    protected void mkN(State state, Symbol symbol, int leftExtent, int rightExtent, PackedForestNode parent) {
        PackedForestSymbol node = findOrCreate(state, symbol, leftExtent, rightExtent);
        //System.err.printf("\t\tmkN %s => %s%n", parent, node);
        parent.addEdge(node);
        if (parent.getEdges().size() > 2) {
            //throw new RuntimeException("Packed forest is not binary");
        }
    }

    protected void mkN(State slot, int leftExtent, int rightExtent, PackedForestNode parent) {
        PackedForestSymbol node = findOrCreate(slot, leftExtent, rightExtent);
        //System.err.printf("\t\tmkN %s => %s%n", parent, node);
        parent.addEdge(node);
        if (parent.getEdges().size() > 2) {
            //throw new RuntimeException("Packed forest is not binary");
        }
    }

    /*
    protected void patch() {
        // After all of the nodes have been created, stitch the nodes and slots into the graph
        for (PackedForestNode node : allNodes) {
            if (!(node instanceof PackedForestIntermediate) && node.getEdges().isEmpty()) {
                final Symbol symbol;
                if (node instanceof PackedForestSymbol) {
                    symbol = ((PackedForestSymbol) node).symbol;
                } else {
                    symbol = ((PackedForestSlot) node).slot.symbol;
                }

                for (PackedForestNode child : allNodes) {
                    if (child instanceof PackedForestIntermediate && child.getParent() == null && node.getLeftExtent() == child.getLeftExtent() && node.getRightExtent() == child.getRightExtent()) {
                        if (symbol.equals(((PackedForestIntermediate) child).slot.symbol)) {
                            //System.err.printf("EDGE: %s => %s (%s -> %s)%n", node, child, node.getClass().getName(), child.getClass().getName());
                            node.addXEdge(child);
                        }
                    }
                }
            }
        }
    }
     */

    protected void tidy() {
        for (PackedForestNode node : allNodes) {
            if (node.getParent() == null) {
                tidy(node, new HashSet<>());
            }
        }
        ArrayList<PackedForestNode> remaining = new ArrayList<>();
        for (PackedForestNode node : allNodes) {
            if (!node.getPruned()) {
                remaining.add(node);
            }
        }
        allNodes.clear();
        allNodes.addAll(remaining);
    }

    private void tidy(PackedForestNode node, HashSet<PackedForestNode> seen) {
        node.tidyEdges();
        seen.add(node);
        for (PackedForestNode child : node.getEdges()) {
            if (!seen.contains(child)) {
                tidy(child, seen);
            }
        }
        seen.remove(node);
    }

    public void dumpdot(Token[] tokens) {
        System.err.println("digraph sppf {");
        for (PackedForestNode node : allNodes) {
            if (node instanceof PackedForestSymbol && ((PackedForestSymbol) node).symbol instanceof TerminalSymbol) {
                TerminalSymbol symbol = (TerminalSymbol) ((PackedForestSymbol) node).symbol;
                if (symbol.getToken() instanceof TokenCharacter) {
                    System.err.printf("N%d [label=\"%s\", shape=house]%n", node.nodeId, symbol);
                } else {
                    Token t = tokens[((PackedForestSymbol) node).leftExtent];
                    System.err.printf("N%d [label=\"%s (%s)\", shape=house]%n", node.nodeId, symbol, t);
                }
            } else {
                if (node instanceof PackedForestIntermediate) {
                    //System.err.printf("N%d [label=\"\" shape=circle width=0.25 height=0.25 fixedsize=true]%n", node.nodeId);
                    System.err.printf("N%d [label=\"%s\" shape=rectangle]%n", node.nodeId, node);
                } else {
                    System.err.printf("N%d [label=\"%s\"]%n", node.nodeId, node);
                }
            }

            // There can only ever be two edges...and make sure they're in the right order
            ArrayList<PackedForestNode> edgeList = new ArrayList<>();
            for (PackedForestNode child : node.getEdges()) {
                if (edgeList.isEmpty()) {
                    edgeList.add(child);
                } else {
                    if (child.getLeftExtent() < edgeList.get(0).getLeftExtent()) {
                        edgeList.add(0, child);
                    } else {
                        edgeList.add(child);
                    }
                }
            }

            boolean rank = true;
            for (PackedForestNode child : edgeList) {
                rank = rank && !(child instanceof PackedForestIntermediate);
                System.err.printf("N%d -> N%d\n", node.nodeId, child.nodeId);
            }

            if (rank && edgeList.size() > 1) {
                System.err.printf("{ rank=same N%d -> N%d [style=invis] }%n", edgeList.get(0).nodeId, edgeList.get(1).nodeId);
            }
        }
        System.err.println("}");
    }

    public void dump(Token[] tokens) {
        System.err.println("<sppf>");
        int uid = 0;
        for (PackedForestNode node : allNodes) {
            if (!(node instanceof PackedForestIntermediate)) {
                if (node instanceof PackedForestSymbol && ((PackedForestSymbol) node).symbol instanceof TerminalSymbol) {
                    TerminalSymbol symbol = (TerminalSymbol) ((PackedForestSymbol) node).symbol;
                    if (symbol.getToken() instanceof TokenCharacter) {
                        System.err.printf("<u%d id='id%d' hash='%d' label='%s' type='terminal'>%n",
                                uid, node.nodeId, node.hashCode(), symbol);
                    } else {
                        Token t = tokens[((PackedForestSymbol) node).leftExtent];
                        System.err.printf("N%d [label=\"%s (%s)\", shape=house]%n", node.nodeId, symbol, t);
                        System.err.printf("<u%d id='id%d' hash='%d' label='%s / %s' type='terminal'>%n",
                                uid, node.nodeId, node.hashCode(), symbol, t);
                    }
                } else {
                    System.err.printf("<u%d id='id%d' hash='%d' label='%s' type='nonterminal'>%n",
                            uid, node.nodeId, node.hashCode(), node);
                }

                // There can only ever be two edges...and make sure they're in the right order
                ArrayList<PackedForestNode> edgeList = new ArrayList<>();
                for (PackedForestNode child : node.getEdges()) {
                    if (edgeList.isEmpty()) {
                        edgeList.add(child);
                    } else {
                        if (child.getLeftExtent() < edgeList.get(0).getLeftExtent()) {
                            edgeList.add(0, child);
                        } else {
                            edgeList.add(child);
                        }
                    }
                }

                for (PackedForestNode child : edgeList) {
                    if (child instanceof PackedForestIntermediate) {
                        System.err.println("  <pair>");
                        for (PackedForestNode link : child.getEdges()) {
                            System.err.printf("    <link target='id%d'/>%n", link.nodeId);
                        }
                        System.err.println("  </pair>");
                    } else {
                        System.err.printf("  <link target='id%d'/>%n", child.nodeId);
                    }
                }

                System.err.printf("</u%d>%n", uid++);
            }
            }
        System.err.println("</sppf>");
    }

    private static class PrefixTrie {
        public final Symbol symbol;
        public final HashMap<Symbol, PrefixTrie> children;
        public final HashMap<Integer, ArrayList<PackedForestIntermediate>> nodes;
        public PrefixTrie(Symbol symbol) {
            this.symbol = symbol;
            children = new HashMap<>();
            nodes = new HashMap<>();
        }
        public PrefixTrie child(Symbol symbol) {
            if (children.containsKey(symbol)) {
                return children.get(symbol);
            }
            PrefixTrie newchild = new PrefixTrie(symbol);
            children.put(symbol, newchild);
            return newchild;
        }
    }
}
