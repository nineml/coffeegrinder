package org.nineml.coffeegrinder.util;

import org.nineml.coffeegrinder.parser.ForestNode;
import org.nineml.coffeegrinder.parser.TreeBuilder;

/**
 * The default tree builder.
 * <p>The default tree builder simply prints a representation of the tree's structure to
 * {@link System#err System.err}.</p>
 */
public class DefaultTreeBuilder implements TreeBuilder {
    private String indent = "";

    @Override
    public void reset() {
        // nop
    }

    @Override
    public void startTree() {
        System.err.println("========================================");
    }

    @Override
    public void endTree() {
        System.err.println("========================================");
    }

    @Override
    public void startNode(ForestNode node) {
        if (node.getSymbol() != null) {
            System.err.print(indent);
            System.err.println("> " + node.getSymbol());
            indent = indent + "   ";
        }
    }

    @Override
    public void endNode(ForestNode node) {
        if (node.getSymbol() != null) {
            indent = indent.substring(3);
            System.err.print(indent);
            System.err.println("< " + node.getSymbol());
        }
    }
}
