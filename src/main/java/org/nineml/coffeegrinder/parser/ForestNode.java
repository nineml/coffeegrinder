package org.nineml.coffeegrinder.parser;

import org.nineml.coffeegrinder.util.ParserAttribute;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * A node in the SPPF.
 *
 * <p>When walking the graph, for example to extract parses, these nodes represent what's
 * avaialable in the graph.</p>
 */
public class ForestNode {
    public static final int UNAMBIGUOUS = 0;
    public static final int AMBIGUOUS = 1;
    public static final int INFINITELY_AMBIGUOUS = 2;

    public static final BigInteger MAX_LONG = new BigInteger("" + Long.MAX_VALUE);

    private static int nextNodeId = 0;

    protected final ParseForest graph;
    protected final Symbol symbol;
    protected final State state;
    protected final int i;
    protected final int j;
    protected final int id;
    protected final ArrayList<Family> families = new ArrayList<>();
    protected final ArrayList<Family> loops = new ArrayList<>();
    protected boolean reachable = false;
    protected boolean edgesChecked = false;
    protected Integer nodeHash = null;
    protected long parsesBelow = 0;
    protected BigInteger exactParsesBelow = BigInteger.ZERO;

    protected ForestNode(ParseForest graph, Symbol symbol, int j, int i) {
        this.graph = graph;
        this.symbol = symbol;
        this.state = null;
        this.i = i;
        this.j = j;
        id = nextNodeId++;
    }

    protected ForestNode(ParseForest graph, State state, int j, int i) {
        this.graph = graph;
        this.symbol = null;
        this.state = state;
        this.i = i;
        this.j = j;
        id = nextNodeId++;
    }

    protected ForestNode(ParseForest graph, Symbol symbol, State state, int j, int i) {
        this.graph = graph;
        this.symbol = symbol;
        this.state = state;
        this.i = i;
        this.j = j;
        id = nextNodeId++;
    }

    /**
     * What symbol does this node represent?
     * <p>There are nodes for terminal and nonterminal symbols as well as intermediate nodes
     * representing partial parses of a rule.</p>
     * @return The symbol, or null if this is a state node
     */
    public Symbol getSymbol() {
        return symbol;
    }

    /**
     * What state does this node represent?
     * <p>There are nodes for terminal and nonterminal symbols as well as intermediate nodes
     * representing partial parses of a rule. In order to expose as much information as possible,
     * the states associated with nonterminal symbols are included in the graph.</p>
     * @return The state associated with this node, or null if this is a terminal symbol node
     */
    public State getState() {
        return state;
    }

    public List<Family> getFamilies() {
        return families;
    }

    public List<Family> getLoops() {
        return loops;
    }

    public long getParsesBelow() {
        if (exactParsesBelow.compareTo(MAX_LONG) < 0) {
            return Long.parseLong(exactParsesBelow.toString());
        } else {
            return Long.MAX_VALUE;
        }
    }

    public BigInteger getExactParsesBelow() {
        return exactParsesBelow;
    }

    protected void addFamily(ForestNode v) {
        for (Family family : families) {
            if (family.w == null) {
                if ((v == null && family.v == null) || (v != null && v.equals(family.v))) {
                    return;
                }
            }
        }

        families.add(new Family(v));
    }

    protected void addFamily(ForestNode w, ForestNode v) {
        for (Family family : families) {
            if (((v == null && family.v == null) || (v != null && v.equals(family.v)))
                    && ((w == null && family.w == null) || (w != null && w.equals(family.w)))) {
                return;
            }
        }

        families.add(new Family(w, v));
    }

    protected int reach(ForestNode root) {
        NodeVisitor visitor = new NodeVisitor();
        reach(root, new Stack<>(), visitor);
        if (visitor.infinitelyAmbiguous) {
            return INFINITELY_AMBIGUOUS;
        }
        if (visitor.ambiguous) {
            return AMBIGUOUS;
        }
        return UNAMBIGUOUS;
    }

    // Remove nodes that may be pruned
    protected void pruneNodes(Stack<ForestNode> seen) {
        if (seen.contains(this)) {
            return;
        }
        seen.push(this);

        if (!families.isEmpty() && !edgesChecked) {
            for (Family family : families) {
                if (family.v != null) {
                    family.v.pruneNodes(seen);
                }
                if (family.w != null) {
                    family.w.pruneNodes(seen);
                }
            }

            ArrayList<Family> newFamiles = new ArrayList<>();
            for (Family family : families) {
                // To avoid unbalancing the binary tree, only remove nodes which have one outbound edge.
                if (family.w == null) {
                    if (family.v.getSymbol() != null
                            && family.v.getSymbol().hasAttribute(ParserAttribute.PRUNING_ALLOWED.getName())) {
                        newFamiles.addAll(family.v.families);
                    } else {
                        newFamiles.add(family);
                    }
                } else {
                    newFamiles.add(family);
                }
            }

            families.clear();
            for (Family newfamily : newFamiles) {
                boolean haveV = newfamily.v == null;
                boolean haveW = newfamily.w == null;
                for (Family already : families) {
                    if (newfamily.w != null && (newfamily.w.equals(already.w) || newfamily.w.equals(already.v))) {
                        haveW = true;
                        newfamily.w = null;
                    }
                    if (newfamily.v != null && (newfamily.v.equals(already.v) || (newfamily.v.equals(already.w)))) {
                        haveV = true;
                        if (!haveW) {
                            newfamily.v = newfamily.w;
                            newfamily.w = null;
                        }
                    }
                }

                if (newfamily.v != null && (!haveW || !haveV)) {
                    families.add(newfamily);
                }
            }

            // Combine families
            newFamiles.clear();
            newFamiles.addAll(families);
            families.clear();;
            for (Family newfamily : newFamiles) {
                boolean add = true;
                for (Family already : families) {
                    if (newfamily.w == null && already.w == null) {
                        if (already.v.j == newfamily.v.i) {
                            already.w = newfamily.v;
                            add = false;
                        } else if (newfamily.v.j == already.v.i) {
                            already.w = already.v;
                            already.v = newfamily.v;
                            add = false;
                        }
                    }
                }
                if (add) {
                    families.add(newfamily);
                }
            }
        }

        edgesChecked = true;
        seen.pop();
    }

    protected void reach(ForestNode root, Stack<ForestNode> seen, NodeVisitor visitor) {
        if (seen.contains(this)) {
            visitor.ambiguous = true;
            visitor.infinitelyAmbiguous = true;
            return;
        }
        seen.push(this);

        if (!reachable) {
            visitor.ambiguous = visitor.ambiguous || families.size() > 1;

            for (Family family : families) {
                if (family.v != null && family.w != null) {
                    // If the left and right sides cover overlapping regions, then there must be
                    // ambiguity in the parse. (I wish I'd kept track of the test case that
                    // persuaded me this was possible.)
                    if ((family.v.j == family.v.i) || (family.w.j == family.w.i)) {
                        // If one side is an epsilon transition, that doesn't count as overlap
                    } else {
                        if ((family.v.j == family.w.j || family.v.i == family.w.i)) {
                            visitor.ambiguous = true;
                            graph.messages.debug("Ambiguity detected; overlap: %d,%d :: %d,%d", family.v.j, family.v.i, family.w.j, family.w.i);
                        }
                    }
                }
                if (family.v != null) {
                    family.v.reach(root, seen, visitor);
                }
                if (family.w != null) {
                    family.w.reach(root, seen, visitor);
                }
            }
        }

        reachable = true;
        seen.pop();
    }

    // Remove discardable nodes that lead only to ε
    protected boolean trimEpsilon() {
        //if (true) { return false; }
        if (nodeHash != null) {
            return false;
        }

        if (families.isEmpty()) {
            nodeHash = getSymbol().hashCode();
            parsesBelow = 1;
            exactParsesBelow = BigInteger.ONE;
            return false;
        }

        nodeHash = 0; // mark that we've seen this one, even though we'll recompute this later

        for (Family family : families) {
            if ((family.w != null && family.w.nodeHash != null)
                || (family.v != null && family.v.nodeHash != null)) {
                loops.add(family);
            }
        }

        for (Family family : families) {
            if (family.w != null) {
                // Never trim w
                family.w.trimEpsilon();
            }

            if (family.v != null) {
                if (family.v.trimEpsilon()) {
                    if (family.w == null) {
                        graph.graph.remove(family.v);
                        family.v = null;
                    }
                }
            }
        }

        if (getSymbol() == null) {
            nodeHash = getState().hashCode();
        } else {
            nodeHash = getSymbol().hashCode();
        }

        for (Family family : families) {
            if (family.w != null) {
                nodeHash += 7 * family.w.nodeHash;
            }
            if (family.v != null) {
                nodeHash += 3 * family.v.nodeHash;
            }
        }

        ArrayList<Family> newFamilies = new ArrayList<>();
        for (Family family : families) {
            boolean found = false;
            for (Family newfam : newFamilies) {
                found = true;

                if (newfam.w != family.w) {
                    if ((newfam.w == null || family.w == null)) {
                        found = false;
                    } else {
                        found = newfam.w.nodeHash.equals(family.w.nodeHash);
                    }
                }

                if (newfam.v != family.v) {
                    if ((newfam.v == null || family.v == null)) {
                        found = false;
                    } else {
                        found = found && newfam.v.nodeHash.equals(family.v.nodeHash);
                    }
                }
            }
            if (!found) {
                newFamilies.add(family);
            }
        }

        families.clear();
        families.addAll(newFamilies);

        exactParsesBelow = BigInteger.ZERO;
        for (Family family : families) {
            BigInteger left = BigInteger.ONE;
            BigInteger right = BigInteger.ONE;
            // Avoid loops; we have no useful way to represent infinite ambiguity
            if (!loops.contains(family)) {
                if (family.w != null) {
                    left = family.w.exactParsesBelow;
                }
                if (family.v != null) {
                    right = family.v.exactParsesBelow;
                }
                exactParsesBelow = exactParsesBelow.add(left.multiply(right));
            }
        }
        if (BigInteger.ZERO.equals(exactParsesBelow)) {
            exactParsesBelow = BigInteger.ONE;
        }

        if (exactParsesBelow.compareTo(MAX_LONG) < 0) {
            parsesBelow = Long.parseLong(exactParsesBelow.toString());
        } else {
            parsesBelow = Long.MAX_VALUE;
        }

        if (getSymbol() == null) {
            return false;
        }

        ParserAttribute pclass = getSymbol().getAttribute(ParserAttribute.PRUNING);
        if (pclass == null || pclass.getValue().equals(ParserAttribute.PRUNING_FORBIDDEN.getValue())) {
            return false;
        }

        if (families.size() == 1) {
            Family family = families.get(0);
            return family.w == null && family.v == null;
        }

        return false;
    }

    // Remove discardable nodes that lead only to ε
    protected ForestNode x_trimEpsilon(Stack<ForestNode> seen) {
        if (seen.contains(this) || families.isEmpty()) {
            return this;
        }
        seen.push(this);

        ArrayList<Family> newFamilies = new ArrayList<>();
        for (Family family : families) {
            ForestNode w = null;
            ForestNode v = null;

            if (family.w != null) {
                w = family.w.x_trimEpsilon(seen);
            }

            if (family.v != null) {
                v = family.v.x_trimEpsilon(seen);
            }

            if (v != null || w != null) {
                Family newFamily;
                if (v == null) {
                    newFamily = new Family(w);
                } else {
                    if (w == null) {
                        newFamily = new Family(v);
                    } else {
                        newFamily = new Family(w, v);
                    }
                }

                boolean found = false;
                for (Family find : newFamilies) {
                    found = found || find.w == family.w && find.v == family.v;
                }

                if (!found) {
                    newFamilies.add(newFamily);
                }
            }
        }

        if (newFamilies.isEmpty()) {
            boolean remove = true;
            if (getSymbol() != null) {
                ParserAttribute pclass = getSymbol().getAttribute(ParserAttribute.PRUNING);
                if (pclass == null || pclass.getValue().equals(ParserAttribute.PRUNING_FORBIDDEN.getValue())) {
                    remove = false;
                }
            }

            if (remove) {
                graph.graph.remove(this);
                return null;
            }
        }

        families.clear();
        families.addAll(newFamilies);
        seen.pop();
        return this;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ForestNode) {
            ForestNode other = (ForestNode) obj;
            if (state == null) {
                assert symbol != null;
                return symbol.equals(other.symbol) && i == other.i && j == other.j;
            }
            return state.equals(other.state) && i == other.i && j == other.j;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int code = 31 * id;
        if (symbol != null) {
            code += 3 * symbol.hashCode();
        }
        if (state != null) {
            code += 17 * state.hashCode();
        }
        return code;
    }

    @Override
    public String toString() {
        if (symbol == null) {
            return state + ", " + j + ", " + i;
        } else {
            return symbol + ", " + j + ", " + i;
        }
    }

    private static final class NodeVisitor {
        public boolean ambiguous = false;
        public boolean infinitelyAmbiguous = false;
    }
}
