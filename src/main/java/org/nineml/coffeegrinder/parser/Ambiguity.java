package org.nineml.coffeegrinder.parser;

import org.nineml.coffeegrinder.util.NodeChoices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides access to successive trees from an ambiguous parse.
 * <p>This class ignores loops; infinitely ambiguous parses. The order of the trees returned
 * is roughly a depth-first tree walk of the graph. But they aren't explicitly ordered by
 * any property.</p>
 */
public class Ambiguity {
    private final List<ForestNode> roots;
    private final boolean ambiguous;
    private final boolean infinitelyAmbiguous;

    protected Ambiguity(ForestNode root) {
        roots = new ArrayList<>();
        roots.add(root);
        ambiguous = false;
        infinitelyAmbiguous = false;
    }

    protected Ambiguity(List<ForestNode> roots, boolean ambiguous, boolean infinitely) {
        this.roots = new ArrayList<>(roots);
        this.ambiguous = ambiguous;
        infinitelyAmbiguous = infinitely;
    }

    /**
     * Return the graph roots.
     * @return the roots
     */
    public List<ForestNode> getRoots() {
        return roots;
    }

    /**
     * Is this graph ambiguous?
     * @return true if the graph is ambiguous.
     */
    public boolean getAmbiguous() {
        return ambiguous;
    }

    /**
     * Is this graph infinitely ambiguous?
     * @return true if the graph is infinitely ambigous.
     */
    public boolean getInfinitelyAmbiguous() {
        return infinitelyAmbiguous;
    }
}
