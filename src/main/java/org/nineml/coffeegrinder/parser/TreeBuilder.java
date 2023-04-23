package org.nineml.coffeegrinder.parser;

import org.nineml.coffeegrinder.tokens.Token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class TreeBuilder {
    protected final Split root = new Split();
    protected final HashMap<ForestNode,HashMap<Family,Integer>> nodeEdgeCounts = new HashMap<>();
    private Split current = null;
    protected boolean ambiguous = false;
    protected boolean infinitelyAmbiguous = false;

    protected int parseCount = 0;
    protected int revealedParses = 0;
    protected int currentParses = 0;

    public boolean isAmbiguous() {
        return ambiguous;
    }

    public boolean isInfinitelyAmbiguous() {
        return infinitelyAmbiguous;
    }

    public void loop(RuleChoice alternative) {
        ambiguous = true;
        infinitelyAmbiguous = true;
    }

    public boolean moreParses() {
        return parseCount == 0 || parseCount < revealedParses;
    }

    public int getRevealedParses() {
        return revealedParses;
    }

    public int getParseCount() {
        return parseCount;
    }

    public HashMap<Family,Integer> getEdgeCounts(ForestNode node) {
        final HashMap<Family,Integer> edgeCounts;
        if (nodeEdgeCounts.containsKey(node)) {
            edgeCounts = nodeEdgeCounts.get(node);
            if (edgeCounts.size() != node.families.size()) {
                throw new IllegalStateException("Edge counts have changed on " + node
                        + " (" + edgeCounts.size() + " != " + node.families.size() + ")");
            }
        } else {
            edgeCounts = new HashMap<>();
            for (Family family : node.families) {
                edgeCounts.put(family, 0);
            }
            nodeEdgeCounts.put(node, edgeCounts);
        }
        return edgeCounts;
    }

    public int chooseFromRemaining(ForestNode tree, List<RuleChoice> alternatives) {
        return 0;
    }

    public int chooseFromAll(ForestNode tree, List<RuleChoice> alternatives, List<Boolean> moreParses) {
        ArrayList<Integer> indexMap = new ArrayList<>();
        ArrayList<RuleChoice> remaining = new ArrayList<>();
        for (int index = 0; index < alternatives.size(); index++) {
            if (moreParses.get(index)) {
                indexMap.add(index);
                remaining.add(alternatives.get(index));
            }
        }

        int selected = chooseFromRemaining(tree, remaining);
        if (selected < 0 || selected > remaining.size()) {
            throw new IllegalStateException("Invalid alternative selected");
        }

        return indexMap.get(selected);
    }

    public int startAlternative(ForestNode tree, List<RuleChoice> alternatives) {
        currentParses += alternatives.size();

        ambiguous = true;
        int selected = current.choose(tree, alternatives);
        current = current.paths.get(selected);
        return selected;
    }

    public void endAlternative(RuleChoice alternative) {
        // nop
    }

    public void reset() {
        root.reset();
        parseCount = 0;
        revealedParses = 0;
        currentParses = 0;
        current = null;

    }

    public void startTree() {
        if (parseCount > 0 && parseCount >= revealedParses) {
            reset();
        }

        parseCount++;
        if (revealedParses == 0) {
            // There's *always* at least one.
            revealedParses = 1;
        }

        currentParses = 0;
        root.reset();
        current = root;
    }

    public void endTree() {
        if (currentParses > revealedParses) {
            revealedParses = currentParses;
        }
        Split parent = current.parent;
        while (parent != null) {
            parent.pathCount.put(current.selection, current.size);
            parent.size = 0;
            for (int pos = 0; pos < parent.pathCount.size(); pos++) {
                parent.size += parent.pathCount.get(pos);
            }
            current = parent;
            parent = current.parent;
        }
    }

    abstract public void startNonterminal(NonterminalSymbol symbol, Map<String,String> attributes, int leftExtent, int rightExtent);

    abstract public void endNonterminal(NonterminalSymbol symbol, Map<String,String> attributes, int leftExtent, int rightExtent);

    abstract public void token(Token token, Map<String,String> attributes);

    private class Split {
        private final Split parent;
        public final HashMap<Integer,Integer> pathCount;
        public final HashMap<Integer, Split> paths;
        public final int selection;
        public int size = 0;

        public Split() {
            this(null, -1);
        }

        public Split(Split parent, int selected) {
            this.parent = parent;
            this.selection = selected;
            pathCount = new HashMap<>();
            paths = new HashMap<>();
        }

        private void reset() {
            pathCount.clear();
            paths.clear();
        }

        public int choose(ForestNode tree, List<RuleChoice> alternatives) {
            if (paths.isEmpty()) {
                size = alternatives.size();
                for (int count = 0; count < alternatives.size(); count++) {
                    pathCount.put(count, 1);
                }
            }
            assert alternatives.size() == pathCount.size();
            ArrayList<Boolean> more = new ArrayList<>();
            for (int count = 0; count < alternatives.size(); count++) {
                more.add(pathCount.get(count) > 0);
            }
            int selected = chooseFromAll(tree, alternatives, more);
            if (selected < 0 || selected >= alternatives.size()) {
                throw new IllegalStateException("Invalid alternative selected");
            }
            if (!paths.containsKey(selected)) {
                paths.put(selected, new Split(this, selected));
            }
            return selected;
        }

    }
}
