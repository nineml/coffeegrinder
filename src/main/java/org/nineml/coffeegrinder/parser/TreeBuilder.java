package org.nineml.coffeegrinder.parser;

/**
 * A tree builder builds trees for a tree walker.
 */
public interface TreeBuilder {
    /**
     * Reset the builder.
     * <p>Calling this method should cause it to revert back to its original state, discarding any
     * tree that might already have been built or partially built. This method is called by the
     * {@link TreeWalker} between (or before) parses.</p>
     */
    void reset();

    /**
     * Start building a tree.
     * <p>This method is called once by the {@link TreeWalker} when construction begins.</p>
     */
    void startTree();

    /**
     * End building a tree.
     * <p>This method is called once by the {@link TreeWalker} when construction finishes.</p>
     */
    void endTree();

    /**
     * Insert the node into the tree.
     * <p>Start a new node to the tree. This node should become the "next child" of the currently
     * open node.</p>
     * @param node the forest node
     */
    void startNode(ForestNode node);

    /**
     * Complete the node in the tree.
     * <p>Indicates that the node is finished. Any subsequent start should become a sibling of this
     * node, rather than a child of it.</p>
     * @param node the forest node
     */
    void endNode(ForestNode node);
}
