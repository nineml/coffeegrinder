package org.nineml.coffeegrinder.trees;

import org.nineml.coffeegrinder.parser.NonterminalSymbol;
import org.nineml.coffeegrinder.tokens.Token;

import java.io.PrintStream;
import java.util.Map;

public class PrintStreamTreeBuilder implements TreeBuilder {
    protected final PrintStream stream;
    private boolean trailingnl = false;

    public PrintStreamTreeBuilder(PrintStream stream) {
        this.stream = stream;
    }

    @Override
    public void startTree(boolean ambiguous, boolean infinitelyAmbiguous) {
        // nop
    }

    @Override
    public void endTree(boolean madeAmbiguousChoice) {
        if (!trailingnl) {
            stream.println();
        }
    }

    @Override
    public void startNonterminal(NonterminalSymbol symbol, Map<String,String> attributes, int leftExtent, int rightExtent) {
        stream.printf("<%s>", symbol.getName());
    }

    @Override
    public void endNonterminal(NonterminalSymbol symbol, Map<String,String> attributes, int leftExtent, int rightExtent) {
        stream.printf("</%s>", symbol.getName());
    }

    @Override
    public void token(Token token, Map<String,String> attributes, int leftExtent, int rightExtent) {
        String text = token.getValue();
        trailingnl = text.endsWith("\n");
        stream.printf(text);
    }
}
