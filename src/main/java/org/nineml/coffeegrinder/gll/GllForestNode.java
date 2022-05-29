package org.nineml.coffeegrinder.gll;

import org.nineml.coffeegrinder.parser.NonterminalSymbol;
import org.nineml.coffeegrinder.parser.Symbol;
import org.nineml.coffeegrinder.parser.TerminalSymbol;

import java.math.BigInteger;
import java.util.ArrayList;

public class GllForestNode {
    public final Symbol symbol;
    public final int leftExtent;
    public final int rightExtent;
    public final String id;
    protected final ArrayList<GllForestBranch> alternatives;
    protected BigInteger parseCount = BigInteger.ZERO.subtract(BigInteger.ONE);
    protected BigInteger remainingParseCount = parseCount;
    protected int choice = -1;
    protected boolean moreChoices = false;

    public GllForestNode(Symbol symbol, int left, int right) {
        if (symbol instanceof TerminalSymbol) {
            this.id = "" + (int) ((TerminalSymbol) symbol).getToken().getValue().charAt(0) + "_" + left + "_" + right;
        } else {
            this.id = symbol.toString().replaceAll("'", "") + "_" + left + "_" + right;
        }

        this.symbol = symbol;
        this.leftExtent = left;
        this.rightExtent = right;
        this.alternatives = new ArrayList<>();
        if (symbol instanceof TerminalSymbol) {
            choice = 0;
        }
    }

    @Override
    public String toString() {
        return symbol.toString() + " " + alternatives.size() + " (" + parseCount + ")";
    }

    private static class Context {
        public boolean branched = false;
    }

}
