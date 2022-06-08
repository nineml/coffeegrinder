package org.nineml.coffeegrinder.util;

import org.nineml.coffeegrinder.exceptions.TreeWalkerException;
import org.nineml.coffeegrinder.parser.*;

import java.math.BigInteger;
import java.util.*;

import static org.nineml.coffeegrinder.parser.ForestNode.MAX_LONG;

/**
 * The default tree walker.
 * <p>The default tree walker will return every tree in the forest. It does (roughly) a pre-order traversal
 * of the forest and returns each tree that it finds.</p>
 */
public class DefaultTreeWalker implements TreeWalker {
    private final ParseForest forest;
    private final TreeBuilder builder;
    private final ArrayList<ForestNode> roots = new ArrayList<>();
    private final HashMap<ForestNode, NodeChoices> choiceMap = new HashMap<>();
    private int rootIndex = 0;
    private BigInteger totalParses, remainingParses, remainingTreeParses;
    private boolean selectedFirst = false;

    /**
     * Construct the default tree walker for the given forest.
     * @param forest The forest
     * @param builder The builder to use
     */
    public DefaultTreeWalker(ParseForest forest, TreeBuilder builder) {
        this.builder = builder;
        this.forest = forest;
        reset();
    }

    @Override
    public void reset() {
        selectedFirst = false;
        rootIndex = 0;
        roots.clear();
        roots.addAll(forest.getRoots());
        choiceMap.clear();

        totalParses = BigInteger.ZERO;
        for (ForestNode node : roots) {
            totalParses = totalParses.add(node.getExactParsesBelow());
        }

        remainingParses = totalParses;
        remainingTreeParses = roots.get(rootIndex).getExactParsesBelow();
    }

    @Override
    public long getTotalParses() {
        if (totalParses.compareTo(MAX_LONG) < 0) {
            return Long.parseLong(totalParses.toString());
        } else {
            return Long.MAX_VALUE;
        }
    }

    @Override
    public BigInteger getExactTotalParses() {
        return totalParses;
    }

    @Override
    public long getRemainingParses() {
        if (remainingParses.compareTo(MAX_LONG) < 0) {
            return Long.parseLong(remainingParses.toString());
        } else {
            return Long.MAX_VALUE;
        }
    }

    @Override
    public BigInteger getExactRemainingParses() {
        return remainingParses;
    }

    @Override
    public void walk() {
        if (!selectedFirst) {
            throw TreeWalkerException.noTreesSelected();
        }
        if (rootIndex >= roots.size()) {
            throw TreeWalkerException.noMoreTrees();
        }

        builder.reset();
        builder.startTree();
        ForestNode root = roots.get(rootIndex);
        Stack<ForestNode> seen = new Stack<>();
        walk(root, seen);
        builder.endTree();
    }

    public Map<ForestNode, NodeChoices> getAmbiguityMap() {
        return choiceMap;
    }

    @Override
    public boolean hasNext() {
        return remainingParses.compareTo(BigInteger.ZERO) > 0;
    }

    @Override
    public void next() {
        builder.reset();
        if (BigInteger.ZERO.equals(remainingTreeParses)) {
            rootIndex++;
            if (rootIndex >= roots.size()) {
                throw new NoSuchElementException("No more trees");
            }
            remainingTreeParses = roots.get(rootIndex).getExactParsesBelow().subtract(BigInteger.ONE);
        } else {
            remainingTreeParses = remainingTreeParses.subtract(BigInteger.ONE);
            ForestNode root = roots.get(rootIndex);
            advance(root);
        }
        remainingParses = remainingParses.subtract(BigInteger.ONE);
        selectedFirst = true;
        walk();
    }

    @Override
    public TreeBuilder getTreeBuilder() {
        return builder;
    }

    private void walk(ForestNode node, Stack<ForestNode> seen) {
        builder.startNode(node);

        if (!node.getFamilies().isEmpty()) {
            Family family = getCurrentFamily(node);
            if (family != null) {
                if (family.w != null && !seen.contains(family.w)) {
                    seen.push(family.w);
                    walk(family.w, seen);
                    seen.pop();
                }
                if (family.v != null && !seen.contains(family.v)) {
                    seen.push(family.v);
                    walk(family.v, seen);
                    seen.pop();
                }
            }
        }

        builder.endNode(node);
    }

    private HashSet<ForestNode> seen = new HashSet<>();
    public void advance(ForestNode node) {
        if (node.getFamilies().isEmpty()) {
            return;
        }

        Family family;
        NodeChoices choices = getChoices(node);
        if (choices != null) {
            family = choices.advance();
        } else {
            family = node.getFamilies().get(0);
        }

        if (family.w != null) {
            if (!seen.contains(family.w)) {
                seen.add(family.w);
                advance(family.w);
            }
        }
        if (family.v != null) {
            if (!seen.contains(family.v)) {
                seen.add(family.v);
                advance(family.v);
            }
        }
    }

    private NodeChoices getChoices(ForestNode node) {
        if (node.getFamilies().size() > 1) {
            NodeChoices remaining;
            if (!choiceMap.containsKey(node)) {
                remaining = new NodeChoices(node, forest.getOptions());
                if (remaining.families.isEmpty()) {
                    // they were all loops
                    return null;
                }
                choiceMap.put(node, remaining);
            }
            remaining = choiceMap.get(node);
            return remaining;
        }
        return null;
    }

    private Family getCurrentFamily(ForestNode node) {
        NodeChoices remaining = getChoices(node);
        if (remaining == null) {
            return node.getFamilies().get(0);
        }
        return remaining.currentChoice();
    }
}
