package org.nineml.coffeegrinder.util;

import org.nineml.coffeegrinder.exceptions.TreeWalkerException;
import org.nineml.coffeegrinder.parser.Family;
import org.nineml.coffeegrinder.parser.ForestNode;
import org.nineml.coffeegrinder.parser.ParserOptions;
import org.nineml.logging.Logger;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class NodeChoices {
    public static final String logcategory = "NodeChoices";
    private final ForestNode node;
    protected final ParserOptions options;
    protected final ArrayList<Family> families = new ArrayList<>();
    private int index = 0;

    protected NodeChoices(ForestNode node, ParserOptions options) {
        this.node = node;
        this.options = options;
        reset();
    }

    public List<Family> getFamilies() {
        return families;
    }

    private void reset() {
        index = 0;
        families.clear();

        for (Family family : node.getFamilies()) {
            if (!node.getLoops().contains(family)) {
                families.add(family);
            }
        }
    }
}
