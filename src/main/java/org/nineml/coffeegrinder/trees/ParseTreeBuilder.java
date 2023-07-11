package org.nineml.coffeegrinder.trees;

import org.nineml.coffeegrinder.parser.NonterminalSymbol;
import org.nineml.coffeegrinder.tokens.Token;

import java.util.Map;

/**
 * A tree builder that constructs a {@link ParseTree}.
 */
public class ParseTreeBuilder implements TreeBuilder {
    private ParseTree tree = null;
    private ParseTree root = null;
    private ParseTree current = null;
    private boolean ambiguous = false;
    private boolean infinitelyAmbiguous = false;

    public ParseTree getTree() {
        return tree;
    }

    @Override
    public void startTree(boolean ambiguous, boolean infinitelyAmbiguous) {
        this.ambiguous = ambiguous;
        this.infinitelyAmbiguous = infinitelyAmbiguous;
        current = null;
        tree = null;
        root = null;
    }

    @Override
    public void endTree(boolean madeAmbiguousChoice) {
        root.madeAmbiguousChoice = madeAmbiguousChoice;
        current = null;
        tree = root;
        root = null;
    }

    @Override
    public void startNonterminal(NonterminalSymbol symbol, Map<String,String> attributes, int leftExtent, int rightExtent) {
        if (root == null) {
            root = new ParseTree(symbol, attributes, leftExtent, rightExtent);
            root.ambiguous = ambiguous;
            root.infinitelyAmbiguous = infinitelyAmbiguous;
            current = root;
        } else {
            ParseTree node = current.addChild(symbol, attributes, leftExtent, rightExtent);
            current = node;
        }
        // nop
    }

    @Override
    public void endNonterminal(NonterminalSymbol symbol, Map<String,String> attributes, int leftExtent, int rightExtent) {
        current = current.getParent();
    }

    @Override
    public void token(Token token, Map<String,String> attributes, int leftExtent, int rightExtent) {
         current.addChild(token, attributes, leftExtent, rightExtent);
    }
}
