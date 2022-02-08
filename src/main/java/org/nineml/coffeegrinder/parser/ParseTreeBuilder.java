package org.nineml.coffeegrinder.parser;

import java.util.Stack;

/**
 * The ParseTreeBuilder builds {@link ParseTree}s.
 * <p>ParseTree objects are returned by the {@link EarleyResult} object.</p>
 */
public class ParseTreeBuilder implements TreeBuilder {
    private final Stack<ParseTree> stack;
    private ParseTree finalTree = null;

    /**
     * Default constructor.
     */
    public ParseTreeBuilder() {
        stack = new Stack<>();
    }

    /**
     * Return the last tree built.
     * @return the last tree built.
     */
    public ParseTree getTree() {
        return finalTree;
    }

    @Override
    public void reset() {
        stack.clear();
        finalTree = null;
    }

    @Override
    public void startTree() {
        // nop
    }

    @Override
    public void endTree() {
        // nop
    }

    @Override
    public void startNode(ForestNode node) {
        ParseTree tree = new ParseTree(node);
        if (!stack.isEmpty()) {
            stack.peek().addChild(tree);
        }
        stack.push(tree);
    }

    @Override
    public void endNode(ForestNode node) {
        finalTree = stack.pop();
    }
}
