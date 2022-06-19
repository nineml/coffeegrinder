package org.nineml.coffeegrinder.parser;

import org.nineml.coffeegrinder.tokens.Token;
import org.nineml.coffeegrinder.util.ParserAttribute;

import java.util.*;

public abstract class TreeBuilder {
    protected final Split root = new Split();
    private Split current = null;
    protected boolean ambiguous = false;
    protected boolean infinitelyAmbiguous = false;

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

    public boolean moreTrees() {
        return current == null || root.size > 0;
    }

    public int chooseFromRemaining(List<RuleChoice> alternatives) {
        return 0;
    }

    public int chooseFromAll(List<RuleChoice> alternatives, List<Boolean> moreParses) {
        ArrayList<Integer> indexMap = new ArrayList<>();
        ArrayList<RuleChoice> remaining = new ArrayList<>();
        for (int index = 0; index < alternatives.size(); index++) {
            if (moreParses.get(index)) {
                indexMap.add(index);
                remaining.add(alternatives.get(index));
            }
        }

        int selected = chooseFromRemaining(remaining);
        if (selected < 0 || selected > remaining.size()) {
            throw new IllegalStateException("Invalid alternative selected");
        }

        return indexMap.get(selected);
    }

    public int startAlternative(List<RuleChoice> alternatives) {
        ambiguous = true;
        int selected = current.choose(alternatives);
        current = current.paths.get(selected);
        return selected;
    }

    public void endAlternative(RuleChoice alternative) {
        // nop
    }

    public void reset() {
        root.reset();
        current = null;
    }

    public void startTree() {
        if (!moreTrees()) {
            root.reset();
        }
        current = root;
    }

    public void endTree() {
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

    abstract public void startNonterminal(NonterminalSymbol symbol, Collection<ParserAttribute> attributes, int leftExtent, int rightExtent);

    abstract public void endNonterminal(NonterminalSymbol symbol, Collection<ParserAttribute> attributes, int leftExtent, int rightExtent);

    abstract public void token(Token token, Collection<ParserAttribute> attributes);

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

        public int choose(List<RuleChoice> alternatives) {
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
            int selected = chooseFromAll(alternatives, more);
            if (selected < 0 || selected >= alternatives.size()) {
                throw new IllegalStateException("Invalid alternative selected");
            }
            if (!paths.containsKey(selected)) {
                paths.put(selected, new Split(this, selected));
            }
            return selected;
        }

    }

    private static class StackFrame {
        public final Symbol symbol;
        public final Collection<ParserAttribute> attributes;
        public final int leftExtent;
        public final int rightExtent;
        public StackFrame(Symbol symbol, Collection<ParserAttribute> attributes, int left, int right) {
            this.symbol = symbol;
            this.attributes = attributes;
            this.leftExtent = left;
            this.rightExtent = right;
        }
    }
}
