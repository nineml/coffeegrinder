package org.nineml.coffeegrinder.util;

import org.nineml.coffeegrinder.exceptions.ParseException;
import org.nineml.coffeegrinder.exceptions.TreeWalkerException;
import org.nineml.coffeegrinder.parser.*;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;

import static org.nineml.coffeegrinder.parser.ForestNode.MAX_LONG;

/**
 * The default tree walker.
 * <p>The default tree walker will return every tree in the forest. It does (roughly) a pre-order traversal
 * of the forest and returns each tree that it finds.</p>
 */
public class DefaultTreeWalker implements TreeWalker {
    private final ParseForest forest;
    private final TreeBuilder builder;
    private final Messages messages;
    private final ArrayList<ForestNode> roots = new ArrayList<>();
    private final HashMap<ForestNode, Choices> choiceMap = new HashMap<>();
    private int rootIndex = 0;
    private BigInteger totalParses, remainingParses, remainingTreeParses;
    private boolean selectedFirst = false;

    /**
     * Construct the default tree walker for the given forest.
     * @param forest The forest
     * @param builder The builder to use
     * @param messages The messages object will be used to print any progress or status or warning messages
     */
    public DefaultTreeWalker(ParseForest forest, TreeBuilder builder, Messages messages) {
        this.builder = builder;
        this.forest = forest;
        this.messages = messages;
        reset();
    }

    /**
     * Construct the default tree walker for the given forest.
     * @param forest The forest
     * @param builder The builder to use
     */
    public DefaultTreeWalker(ParseForest forest, TreeBuilder builder) {
        this.builder = builder;
        this.forest = forest;
        this.messages = null;
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
            throw new TreeWalkerException("No trees selected");
        }
        if (rootIndex >= roots.size()) {
            throw new TreeWalkerException("No more trees");
        }

        builder.reset();
        builder.startTree();
        ForestNode root = roots.get(rootIndex);
        walk(root);
        builder.endTree();
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

    private void walk(ForestNode node) {
        builder.startNode(node);

        if (!node.getFamilies().isEmpty()) {
            Family family = getCurrentFamily(node);
            if (family != null) {
                if (family.w != null) {
                    walk(family.w);
                }
                if (family.v != null) {
                    walk(family.v);
                }
            }
        }

        builder.endNode(node);
    }

        public void advance(ForestNode node) {
        if (node.getFamilies().isEmpty()) {
            return;
        }

        Family family;
        Choices choices = getChoices(node);
        if (choices != null) {
            family = choices.advance();
        } else {
            family = node.getFamilies().get(0);
        }

        if (family.w != null) {
            advance(family.w);
        }
        if (family.v != null) {
            advance(family.v);
        }
    }

    private Choices getChoices(ForestNode node) {
        if (node.getFamilies().size() > 1) {
            Choices remaining;
            if (!choiceMap.containsKey(node)) {
                remaining = new Choices(node);
                choiceMap.put(node, remaining);
            }
            remaining = choiceMap.get(node);
            return remaining;
        }
        return null;
    }

    private Family getCurrentFamily(ForestNode node) {
        Choices remaining = getChoices(node);
        if (remaining == null) {
            return node.getFamilies().get(0);
        }
        return remaining.currentChoice();
    }

    private final class Choices {
        private final ForestNode node;
        private final ArrayList<Family> families = new ArrayList<>();
        private final ArrayList<BigInteger> chooseW = new ArrayList<>();
        private final ArrayList<BigInteger> chooseV = new ArrayList<>();
        private int index = 0;

        public Choices(ForestNode node) {
            this.node = node;
            reset();
        }

        private void reset() {
            index = 0;
            families.clear();
            chooseW.clear();
            chooseV.clear();

            for (Family family : node.getFamilies()) {
                if (!node.getLoops().contains(family)) {
                    families.add(family);
                    if (family.w == null) {
                        chooseW.add(BigInteger.ZERO);
                    } else {
                        chooseW.add(family.w.getExactParsesBelow().subtract(BigInteger.ONE));
                    }
                    if (family.v == null) {
                        chooseV.add(BigInteger.ZERO);
                    } else {
                        chooseV.add(family.v.getExactParsesBelow().subtract(BigInteger.ONE));
                    }
                }
            }
        }

        public Family currentChoice() {
            if (families.isEmpty()) {
                return null;
            }
            //System.err.printf("CUR: %d of %d / %s\n", index+1, families.size(), node);
            return families.get(index);
        }

        public Family advance() {
            if (families.isEmpty()) {
                throw new ParseException("Error in tree walk");
            }

            Family family = families.get(index);
            BigInteger w = chooseW.get(index);
            BigInteger v = chooseV.get(index);

            if (!BigInteger.ZERO.equals(w)) {
                w = w.subtract(BigInteger.ONE);
                chooseW.set(index, w);
            } else if (!BigInteger.ZERO.equals(v)) {
                v = v.subtract(BigInteger.ONE);
                chooseV.set(index, v);
            } else {
                index++;
                if (messages != null) {
                    messages.debug("Changing %s :: %d", node, index);
                }
            }

            if (index == families.size()) {
                index = 0;
                if (messages != null) {
                    messages.debug("Resetting %s :: %d", node, index);
                }
                reset();
            }

            return family;
        }
    }
}
