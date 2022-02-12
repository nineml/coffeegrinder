package org.nineml.coffeegrinder.parser;

import org.nineml.coffeegrinder.util.NodeChoices;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Ambiguity {
    private final List<ForestNode> roots;
    private final Map<ForestNode, List<Family>> choices;
    private final boolean ambiguous;
    private final boolean infinitelyAmbiguous;

    protected Ambiguity(ForestNode root) {
        roots = new ArrayList<>();
        roots.add(root);
        ambiguous = false;
        infinitelyAmbiguous = false;
        choices = new HashMap<>();
    }

    protected Ambiguity(List<ForestNode> roots, boolean ambiguous, boolean infinitely, Map<ForestNode, NodeChoices> nodechoices) {
        this.roots = new ArrayList<>(roots);
        this.ambiguous = ambiguous;
        infinitelyAmbiguous = infinitely;
        choices = new HashMap<>();

        for (ForestNode node: nodechoices.keySet()) {
            ArrayList<Family> families = new ArrayList<>(nodechoices.get(node).getFamilies());
            choices.put(node, families);
        }
    }

    public List<ForestNode> getRoots() {
        return roots;
    }

    public boolean getAmbiguous() {
        return ambiguous;
    }

    public boolean getInfinitelyAmbiguous() {
        return infinitelyAmbiguous;
    }

    public Map<ForestNode, List<Family>> getChoices() {
        return choices;
    }
}
