package org.nineml.coffeegrinder.parser;

import org.nineml.coffeegrinder.trees.SequentialTreeSelector;
import org.nineml.coffeegrinder.trees.TreeSelector;
import org.nineml.coffeegrinder.trees.TreeBuilder;
import org.nineml.coffeegrinder.tokens.Token;
import org.nineml.coffeegrinder.util.ParserAttribute;

import java.util.*;

public class ForestWalker {
    private final ParseForest graph;
    private final Stack<PathNode> prevPath;
    private final Stack<PathNode> curPath;
    private final HashSet<Family> productiveEdges;
    private final HashSet<Family> selectedEdges;
    private final TreeSelector treeSelector;
    private final HashSet<Integer> selectedNodes;
    private TreeBuilder builder = null;
    private boolean outOfChoices = false;
    int pos;

    protected ForestWalker(ParseForest forest) {
        this(forest, new SequentialTreeSelector());
    }

    protected ForestWalker(ParseForest forest, TreeSelector treeSelector) {
        graph = forest;
        this.treeSelector = treeSelector;

        outOfChoices = false;
        prevPath = new Stack<>();
        curPath = new Stack<>();
        productiveEdges = new HashSet<>();
        selectedEdges = new HashSet<>();
        selectedNodes = new HashSet<>();
    }

    public void reset() {
        outOfChoices = false;
        prevPath.clear();
        curPath.clear();
        productiveEdges.clear();
        selectedEdges.clear();
        selectedNodes.clear();
        treeSelector.reset();
    }

    public boolean hasMoreTrees() {
        return !outOfChoices;
    }

    public Set<Integer> selectedNodes() {
        return new HashSet<>(selectedNodes);
    }

    public void getNextTree(TreeBuilder builder) {
        selectedNodes.clear();
        if (outOfChoices) {
            return;
        }

        this.builder = builder;
        pos = 0;
        curPath.clear();

        builder.startTree(graph.ambiguous, graph.infinitelyAmbiguous);
        assert graph.getRoot().symbol != null;
        traverse(graph.getRoot(), graph.getRoot().symbol.getAttributes());
        builder.endTree(treeSelector.getMadeAmbiguousChoice());

        HashSet<ForestNode> seen = new HashSet<>();
        for (int pos = curPath.size() - 1; pos >= 0; pos--) {
            PathNode path = curPath.get(pos);
            if (!seen.contains(path.node)) {
                seen.add(path.node);
                productiveEdges.add(path.chosen);
            }
        }

        prevPath.clear();
        prevPath.addAll(curPath);
        boolean done = prevPath.isEmpty();
        while (!done) {
            done = true;
            PathNode pathlast = prevPath.peek();
            if (pathlast.choices.isEmpty()) {
                pathlast.reset();
                prevPath.pop();
                done = prevPath.isEmpty();
            }
        }
        outOfChoices = prevPath.isEmpty();
    }

    private void traverse(ForestNode node, List<ParserAttribute> parserAttributes) {
        if (node == null) {
            return;
        }

        if (node.getFamilies().isEmpty()) {
            assert node.getSymbol() instanceof TerminalSymbol;
            selectedNodes.add(node.id);
            token(((TerminalSymbol) node.getSymbol()).getToken(), parserAttributes, node.leftExtent, node.rightExtent);
            return;
        }

        final NonterminalSymbol symbol = (NonterminalSymbol) node.getSymbol();

        if (node.getFamilies().size() == 1) {
            if (symbol != null) {
                selectedNodes.add(node.id);
                startNonterminal(symbol, parserAttributes, node.leftExtent, node.rightExtent);
            }

            ForestNode left = node.getFamilies().get(0).getLeftNode();
            ForestNode right = node.getFamilies().get(0).getRightNode();
            traverse(left, node.getFamilies().get(0).getLeftAttributes());
            traverse(right, node.getFamilies().get(0).getRightAttributes());

            if (symbol != null) {
                endNonterminal(symbol, parserAttributes, node.leftExtent, node.rightExtent);
            }

            return;
        }

        final PathNode pathNode;
        if (pos < prevPath.size()) {
            pathNode = prevPath.get(pos);
            pos++;
        } else {
            PathNode loop = null;
            for (PathNode pnode : curPath) {
                if (pnode.node == node) {
                    loop = pnode;
                }
            }

            if (loop == null) {
                // Never knowingly follow a looping path
                ArrayList<Family> noloops = new ArrayList<>();
                for (Family family : node.getFamilies()) {
                    if (!graph.loops.contains(family)) {
                        noloops.add(family);
                    }
                }

                pathNode = new PathNode(node, noloops);
            } else {
                // If we've wandered down a looping path, find a way out.
                Family select = null;

                // If we know how to get somewhere productive from here, go that way.
                for (Family family : node.getFamilies()) {
                    if (productiveEdges.contains(family)) {
                        select = family;
                        break;
                    }
                }

                if (select == null) {
                    // If there's some choice we haven't previously made, try that.
                    for (Family family : node.getFamilies()) {
                        if (!selectedEdges.contains(family)) {
                            select = family;
                            break;
                        }
                    }
                }

                if (select == null) {
                    // If there's an epsilon edge, try that.
                    for (Family family : node.getFamilies()) {
                        // Take the epsilon edge, if it's available
                        if (family.getLeftNode() == null && family.getRightNode() == null) {
                            select = family;
                            break;
                        }
                    }
                }

                // When we used to try to find loops, we could "get stuck", but
                // I don't think we can have failed to find an exit by now given
                // that we never willingly follow a path that loops.
                assert select != null;
                pathNode = new PathNode(node, node.getFamilies(), select);
            }
        }

        if (pathNode.chosen == null) {
            pathNode.getNextChoice();
        }

        if (!prevPath.isEmpty() && prevPath.peek() == pathNode) {
            pathNode.getNextChoice();
        }

        selectedNodes.add(pathNode.node.id);

        selectedEdges.add(pathNode.chosen);
        curPath.push(pathNode);

        if (symbol != null) {
            startNonterminal(symbol, parserAttributes, node.leftExtent, node.rightExtent);
        }

        ForestNode left = pathNode.chosen.getLeftNode();
        ForestNode right = pathNode.chosen.getRightNode();

        traverse(left, pathNode.chosen.getLeftAttributes());
        traverse(right, pathNode.chosen.getRightAttributes());

        if (symbol != null) {
            endNonterminal(symbol, parserAttributes, node.leftExtent, node.rightExtent);
        }
    }

    private void startNonterminal(NonterminalSymbol symbol, List<ParserAttribute> attributes, int leftExtent, int rightExtent) {
        treeSelector.startNonterminal(symbol, attributeMap(attributes), leftExtent, rightExtent);
        builder.startNonterminal(symbol, attributeMap(attributes), leftExtent, rightExtent);
    }

    private void endNonterminal(NonterminalSymbol symbol, List<ParserAttribute> attributes, int leftExtent, int rightExtent) {
        treeSelector.endNonterminal(symbol, attributeMap(attributes), leftExtent, rightExtent);
        builder.endNonterminal(symbol, attributeMap(attributes), leftExtent, rightExtent);
    }

    private void token(Token token, List<ParserAttribute> attributes, int leftExtent, int rightExtent) {
        //System.err.println(token.getValue());
        builder.token(token, attributeMap(attributes), leftExtent, rightExtent);
    }

    private Map<String,String> attributeMap(List<ParserAttribute> attributes) {
        if (attributes.isEmpty()) {
            return Collections.emptyMap();
        }

        HashMap<String,String> attmap = new HashMap<>();
        for (ParserAttribute attr : attributes) {
            if (!attmap.containsKey(attr.getName())) {
                attmap.put(attr.getName(), attr.getValue());
            }
        }
        return attmap;
    }

    private class PathNode {
        public final ForestNode node;
        public final ArrayList<Family> previousChoices;
        public final ArrayList<Family> choices;
        public Family chosen;

        public PathNode(ForestNode node, List<Family> choices) {
            this(node, choices, null);
        }

        public PathNode(ForestNode node, List<Family> choices, Family choose) {
            this.node = node;

            if (choose == null) {
                this.choices = new ArrayList<>(choices);
                this.previousChoices = new ArrayList<>();
            } else {
                this.choices = new ArrayList<>();
                this.previousChoices = new ArrayList<>(choices);
                this.previousChoices.remove(choose);
                this.choices.add(choose);
            }

            this.chosen = null;
        }

        public void reset() {
            choices.addAll(this.previousChoices);
            previousChoices.clear();
            chosen = null;
        }

        public void getNextChoice() {
            chosen = treeSelector.select(choices, previousChoices);

            if (chosen == null) {
                if (!choices.isEmpty()) {
                    chosen = choices.get(0);
                } else {
                    chosen = previousChoices.get(0);
                }
            }

            if (this.choices.remove(chosen)) {
                this.previousChoices.add(chosen);
            }
        }

        @Override
        public String toString() {
            return choices.size() + ": " + node.toString() + " :: " + chosen;
        }
    }
}
