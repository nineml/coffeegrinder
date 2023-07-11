package org.nineml.coffeegrinder.trees;

import org.nineml.coffeegrinder.parser.NonterminalSymbol;
import org.nineml.coffeegrinder.tokens.Token;

import java.util.Map;
import java.util.Set;

/**
 * The tree builder interface.
 */
public interface TreeBuilder {
    /**
     * Called first, when construction begins.
     * @param ambiguous is the parse ambiguous?
     * @param infinitelyAmbiguous Is the parse infinitely ambiguous?
     */
    void startTree(boolean ambiguous, boolean infinitelyAmbiguous);

    /**
     * Called last, when construction finishes.
     *
     * @param madeAmbiguousChoice is true if the tree selector reported that it made an arbitrary choice
     *                            to resolve an ambiguous parse.
     */
    void endTree(boolean madeAmbiguousChoice);

    /**
     * Called when a new nonterminal begins.
     * @param symbol The symbol.
     * @param attributes Its attributes.
     * @param leftExtent The starting position in the input.
     * @param rightExtent The ending position in the input.
     */
    void startNonterminal(NonterminalSymbol symbol, Map<String,String> attributes, int leftExtent, int rightExtent);

    /**
     * Called when a nonterminal ends.
     * @param symbol The symbol.
     * @param attributes Its attributes.
     * @param leftExtent The starting position in the input.
     * @param rightExtent The ending position in the input.
     */
    void endNonterminal(NonterminalSymbol symbol, Map<String,String> attributes, int leftExtent, int rightExtent);

    /**
     * Called when a terminal occurs.
     * @param token The terminal token.
     * @param attributes Its attributes.
     * @param leftExtent The starting position in the input.
     * @param rightExtent The ending position in the input.
     */
    void token(Token token, Map<String,String> attributes, int leftExtent, int rightExtent);
}
