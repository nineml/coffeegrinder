package org.nineml.coffeegrinder.util;

import org.nineml.coffeegrinder.parser.NonterminalSymbol;
import org.nineml.coffeegrinder.parser.ParseTree;
import org.nineml.coffeegrinder.parser.TreeBuilder;
import org.nineml.coffeegrinder.parser.RuleChoice;
import org.nineml.coffeegrinder.tokens.Token;

import java.util.Collection;
import java.util.List;

public class ParseTreeBuilder implements TreeBuilder {
    private boolean ambiguous = false;
    private boolean infinitelyAmbiguous = false;
    ParseTree root;
    ParseTree branch;
    ParseTree leaf;

    @Override
    public boolean isAmbiguous() {
        return ambiguous;
    }

    @Override
    public boolean isInfinitelyAmbiguous() {
        ambiguous = true;
        return infinitelyAmbiguous;
    }

    public ParseTree getParseTree() {
        return root;
    }

    @Override
    public int chooseAlternative(List<RuleChoice> alternatives) {
        ambiguous = true;
        return 0;
    }

    @Override
    public void loop(RuleChoice alternative) {
        infinitelyAmbiguous = true;
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
    public void startNonterminal(NonterminalSymbol symbol, Collection<ParserAttribute> attributes, int leftExtent, int rightExtent) {
        leaf = null;
        if (root == null) {
            root = new ParseTree(symbol, attributes);
            branch = root;
        } else {
            branch = branch.addChild(symbol, attributes);
        }
    }

    @Override
    public void endNonterminal(NonterminalSymbol symbol, Collection<ParserAttribute> attributes, int leftExtent, int rightExtent) {
        leaf = null;
        if (branch != root) {
            branch = branch.getParent();
        }
    }

    @Override
    public void token(Token token, Collection<ParserAttribute> attributes) {
        branch.addChild(token, attributes);
    }
}
