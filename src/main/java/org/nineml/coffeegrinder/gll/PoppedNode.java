package org.nineml.coffeegrinder.gll;

import org.nineml.coffeegrinder.parser.NonterminalSymbol;

public class PoppedNode {
    public final NonterminalSymbol symbol;
    public final int k;
    public final int j;

    protected PoppedNode(NonterminalSymbol symbol, int k, int j) {
        this.symbol = symbol;
        this.k = k;
        this.j = j;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PoppedNode) {
            PoppedNode other = (PoppedNode) obj;
            return k == other.k && j == other.j && symbol.equals(other.symbol);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return symbol.hashCode() + (31 * k) + (19 * j);
    }

    @Override
    public String toString() {
        return "(" + symbol + ", " + k + ", " + j + ")";
    }
}
