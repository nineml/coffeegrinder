package org.nineml.coffeegrinder.parser;

import java.math.BigInteger;

/**
 * The TreeWalker works with a {@link TreeBuilder} to construct parse trees from the graph.
 * <p>How the class that implements this interface gets the builder (or even, technically), if it does,
 * is implementation defined.</p>
 * <p>Users can treat this as a kind of iterator, usign {@link #hasNext}, {@link #next}, and {@link #walk}
 * to construct trees from the graph.</p>
 */

public interface TreeWalker {
    /**
     * Returns the total number of parses available.
     * <p>If the total number is larger than {@link Long#MAX_VALUE}, <code>MAX_VALUE</code>
     * should be returned. The {@link #getExactTotalParses} method will return the correct
     * value.</p>
     * @return the total number of parses
     */
    long getTotalParses();

    /**
     * Returns the exact total number of parses avialable.
     * <p>In an ambigous parse, there may be many (many!) possible trees. This method
     * returns the exact number, even if it's larger than will fit in a long.</p>
     * @return the exact total number of parses
     */
    BigInteger getExactTotalParses();

    /**
     * Returns the number of parses remaining.
     * <p>If an ambiguous parse has multiple trees, the walker may allow you to iterate through them.
     * If so, this method should return the number remaining. If the number is larger than {@link Long#MAX_VALUE},
     * <code>MAX_VALUE</code> should be returned. The {@link #getExactRemainingParses} method will return the
     * correct value.</p>
     * @return the remaining number of parses
     */
    long getRemainingParses();

    /**
     * Returns the exact number of parses remaining.
     * <p>If an ambiguous parse has multiple trees, the walker may allow you to iterate through them.
     * If so, this method should return the number remaining.</p>
     * @return the remaining number of parses
     */
    BigInteger getExactRemainingParses();

    /**
     * Walk the graph, producing the current tree.
     * <p>The walker will make calls into the {@link TreeBuilder} to construct the tree.</p>
     * <p>If <code>walk</code> is called more than once, it should return the same tree each time.
     * To advance to the next tree, call {@link #next}.</p>
     */
    void walk();

    /**
     * Are there any more parses?
     * @return true if there are more parses available
     */
    boolean hasNext();

    /**
     * Advance to the next parse.
     */
    void next();

    /**
     * Reset parsing.
     * <p>If this method is called, the walker should reset to its initial state and begin returning
     * the same sequence of trees that it did when it was first created.</p>
     */
    void reset();

    /**
     * Return the tree builder used by this walker.
     * @return the tree builder
     */
    TreeBuilder getTreeBuilder();
}
