package org.nineml.coffeegrinder.parser;

import org.nineml.coffeegrinder.tokens.Token;
import org.nineml.coffeegrinder.trees.ParseTree;
import org.nineml.coffeegrinder.trees.TreeBuilder;
import org.nineml.coffeegrinder.trees.TreeSelector;

import java.util.Set;

/**
 * The result of a parse with an Earley or GLL parser.
 */
public interface GearleyResult {
    /** The parser used.
     *
     * @return The parser.
     */
    GearleyParser getParser();

    /** Returns true of the parse was successful.
     *
     * @return true if the parse was successful.
     */
    boolean succeeded();

    /** Returns true if the parser successfully processed a prefix of the input.
     * <p>Note that the GLL parser does not support prefix parsing.</p>
     *
     * @return true if a prefix of the input was parsed successfully.
     */
    boolean prefixSucceeded();

    /** Returns the number of milliseconds spent parsing.
     *
     * @return the number of milliseconds
     */
    long getParseTime();

    /** After a successful prefix parse, this method continues parsing.
     *
     * @return The (next) parse result.
     */
    GearleyResult continueParsing();

    /** Return the parse forest created by the parser.
     * <p>This method returns <code>null</code> if the parse was unsuccessful.</p>
     *
     * @return The parse forest.
     */
    ParseForest getForest();

    /** Returns true if there are more trees that can be returned.
     * <p>This arises in the case of ambiguous parses.</p>
     *
     * @return true if there are more trees.
     */
    boolean hasMoreTrees();

    /** Set the tree selector to be used when walking the forest.
     * @param selector The tree selector to use.
     */
    void setTreeSelector(TreeSelector selector);

    /** Return the (next) parse tree.
     *
     * @return The tree.
     */
    ParseTree getTree();

    /** Return the (next) parse tree using the specified builder.
     *
     * @param builder The tree builder to use.
     */
    void getTree(TreeBuilder builder);

    /** Returns the set of (ids of) the nodes selected in the last tree returned.
     *
     * @return THe set of node ids.
     */
    Set<Integer> lastSelectedNodes();

    /** Returns true if the parse was ambiguous.
     *
     * @return true if the parse was ambiguous.
     */
    boolean isAmbiguous();

    /** Returns true if the parse was infinitely ambiguous.
     * <p>The parse will only be infinitely ambiguous if the grqmmar contained a loop.</p>
     *
     * @return true if the parse was infinitely ambiguous.
     */
    boolean isInfinitelyAmbiguous();

    /** Return the number of tokens parsed.
     *
     * @return the number of tokens.
     */
    int getTokenCount();

    /** Return the last token parsed.
     *
     * @return The last token.
     */
    Token getLastToken();

    /** Returns the last offset read by the parser.
     *
     * @return The offset.
     */
    int getOffset();

    /** Returns the line number of the last line read by the parser.
     *
     * @return The line number.
     */
    int getLineNumber();

    /** Returns the column number of the last character on the last line read by the parser.
     *
     * @return The column number.
     */
    int getColumnNumber();

    /** Returns the symbols predicted as possibly next in the case where a parse fails.
     *
     * @return The set of terminals.
     */
    Set<TerminalSymbol> getPredictedTerminals();
}
