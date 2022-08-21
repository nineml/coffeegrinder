package org.nineml.coffeegrinder.parser;

import org.nineml.coffeegrinder.util.ParserAttribute;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/**
 * A node in the SPPF.
 *
 * <p>When walking the graph, for example to extract parses, these nodes represent what's
 * available in the graph.</p>
 */
public class ForestNode implements RuleChoice {
    public static final String logcategory = "ForestNode";

    private static int nextNodeId = 0;

    protected final ParseForest graph;
    public final Symbol symbol;
    public final State state;
    /**
     * The last position in the input covered by this node.
     */
    public final int rightExtent;
    /**
     * The first position in the input covered by this node.
     */
    public final int leftExtent;
    /**
     * This node's unique identifier.
     */
    public final int id;
    protected final ArrayList<Family> families = new ArrayList<>();
    protected final ArrayList<Family> loops = new ArrayList<>();
    protected boolean reachable = false;
    protected Integer nodeHash = null;

    protected ForestNode(ParseForest graph, Symbol symbol, int leftExtent, int rightExtent) {
        this.graph = graph;
        this.symbol = symbol;
        this.state = null;
        this.rightExtent = rightExtent;
        this.leftExtent = leftExtent;
        id = nextNodeId++;
    }

    protected ForestNode(ParseForest graph, State state, int leftExtent, int rightExtent) {
        this.graph = graph;
        this.symbol = null;
        this.state = state;
        this.rightExtent = rightExtent;
        this.leftExtent = leftExtent;
        id = nextNodeId++;
    }

    // N.B. This is a *symbol* node, the state is just being carried along so that we can tell
    // what rule defined this symbol. That can be useful when analysing the parse tree.
    protected ForestNode(ParseForest graph, Symbol symbol, State state, int leftExtent, int rightExtent) {
        this.graph = graph;
        this.symbol = symbol;
        this.state = state;
        this.rightExtent = rightExtent;
        this.leftExtent = leftExtent;
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

    public void addFamily(ForestNode v) {
        for (Family family : families) {
            if (family.w == null) {
                if ((v == null && family.v == null) || (v != null && v.equals(family.v))) {
                    return;
                }
            }
        }

        families.add(new Family(v));
    }

    public void addFamily(ForestNode w, ForestNode v) {
        for (Family family : families) {
            if (((v == null && family.v == null) || (v != null && v.equals(family.v)))
                    && ((w == null && family.w == null) || (w != null && w.equals(family.w)))) {
                return;
            }
        }

        families.add(new Family(w, v));
    }

    protected void reach() {
        // Don't try to use the stack for this...
        ArrayList<ForestNode> pending = new ArrayList<>();
        HashSet<ForestNode> seen = new HashSet<>();
        pending.add(this);
        while (!pending.isEmpty()) {
            ForestNode check = pending.remove(0);
            check.reach(pending, seen);
        }
    }

    protected void reach(ArrayList<ForestNode> pending, HashSet<ForestNode> seen) {
        if (seen.contains(this)) {
            return;
        }
        seen.add(this);

        if (!reachable) {
            reachable = true;

            for (Family family : families) {
                if (family.v != null && family.w != null) {
                    // If the left and right sides cover overlapping regions, then there must be
                    // ambiguity in the parse. (I wish I'd kept track of the test case that
                    // persuaded me this was possible.)
                    if ((family.v.leftExtent == family.v.rightExtent) || (family.w.leftExtent == family.w.rightExtent)) {
                        // If one side is an epsilon transition, that doesn't count as overlap
                    } else {
                        if ((family.v.leftExtent == family.w.leftExtent || family.v.rightExtent == family.w.rightExtent)) {
                            graph.options.getLogger().debug(logcategory, "Ambiguity detected; overlap: %d,%d :: %d,%d", family.v.leftExtent, family.v.rightExtent, family.w.leftExtent, family.w.rightExtent);
                        }
                    }
                }
                if (family.v != null) {
                    pending.add(family.v);
                    //family.v.reach(seen);
                }
                if (family.w != null) {
                    pending.add(family.w);
                    //family.w.reach(seen);
                }
            }
        }
    }

    // Remove discardable nodes that lead only to ε
    protected boolean trimEpsilon() {
        //if (true) { return false; }
        if (nodeHash != null) {
            return false;
        }

        if (families.isEmpty()) {
            nodeHash = getSymbol().hashCode();
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

        if (getSymbol() == null) {
            return false;
        }

        ParserAttribute pclass = getSymbol().getAttribute(ParserAttribute.PRUNING_NAME);
        if (pclass == null || pclass.getValue().equals(ParserAttribute.PRUNING_FORBIDDEN.getValue())) {
            return false;
        }

        if (families.size() == 1) {
            Family family = families.get(0);
            return family.w == null && family.v == null;
        }

        return false;
    }

    @Override
    public ForestNode getLeftNode() {
        return null;
    }

    @Override
    public ForestNode getRightNode() {
        return this;
    }

    public Symbol[] getRightHandSide() {
        if (state != null) {
            return state.rhs.symbols;
        } else {
            return null;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ForestNode) {
            ForestNode other = (ForestNode) obj;
            if (state == null) {
                assert symbol != null;
                return symbol.equals(other.symbol) && rightExtent == other.rightExtent && leftExtent == other.leftExtent;
            }
            return Objects.equals(symbol, other.symbol) && state.equals(other.state) && rightExtent == other.rightExtent && leftExtent == other.leftExtent;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int code = (17 * rightExtent) + (31 * leftExtent);
        if (symbol != null) {
            code += 11 * symbol.hashCode();
        } else {
            assert state != null;
            code += 13 * state.hashCode();
        }
        return code;
    }

    @Override
    public String toString() {
        if (symbol == null) {
            return state + ", " + leftExtent + ", " + rightExtent;
        } else {
            return symbol + ", " + leftExtent + ", " + rightExtent;
        }
    }
}
