package org.nineml.coffeegrinder.gll;

import org.nineml.coffeegrinder.parser.NonterminalSymbol;

public class MFollow extends MStatement {
    public final NonterminalSymbol symbol;

    public MFollow(NonterminalSymbol symbol) {
        this.symbol = symbol;
    }

    protected void execute(GllParser gllParser) {
        gllParser.follow(symbol);
    }

    @Override
    public String toString() {
        return "\t\tif (I[c_I] ∈ follow(" + symbol + ") then rtn(" + symbol + ", c_U, c_I)";
    }
}
