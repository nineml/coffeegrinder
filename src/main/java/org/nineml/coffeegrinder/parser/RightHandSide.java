package org.nineml.coffeegrinder.parser;

import org.nineml.coffeegrinder.tokens.TokenEmpty;

import java.util.Arrays;
import java.util.List;

public class RightHandSide {
    private final Symbol[] symbols;
    private final int hcode;

    public RightHandSide(Symbol[] symbols) {
        this.symbols = symbols;
        hcode = Arrays.hashCode(this.symbols);
    }

    public RightHandSide(List<Symbol> symbols) {
        this.symbols = new Symbol[symbols.size()];
        symbols.toArray(this.symbols);
        hcode = Arrays.hashCode(this.symbols);
    }

    public int size() {
        return symbols.length;
    }

    public Symbol[] getSymbols() {
        return symbols;
    }

    public Symbol get(int pos) {
        if (pos < 0 || pos >= symbols.length) {
            throw new IndexOutOfBoundsException("No " + pos + " item in symbol list");
        }
        return symbols[pos];
    }

    public Symbol getFirst() {
        int pos = 0;
        while (pos < symbols.length) {
            if (symbols[pos] instanceof NonterminalSymbol
                || !TokenEmpty.EMPTY.equals(((TerminalSymbol) symbols[pos]).getToken())) {
                return symbols[pos];
            }
            pos++;
        }
        return null;
    }

    public Symbol getNext(int pos) {
        int npos = getNextPosition(pos);
        if (npos < symbols.length) {
            return symbols[npos];
        }
        return null;
    }

    public int getNextPosition(int pos) {
        while (pos < symbols.length) {
            if (symbols[pos] instanceof NonterminalSymbol
                    || !TokenEmpty.EMPTY.equals(((TerminalSymbol) symbols[pos]).getToken())) {
                return pos;
            }
            pos++;
        }
        return pos;
    }

    public boolean isEmpty() {
        return symbols.length == 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RightHandSide) {
            RightHandSide other = (RightHandSide) obj;
            if (symbols.length != other.symbols.length) {
                return false;
            }
            for (int pos = 0; pos < symbols.length; pos++) {
                if (!symbols[pos].equals(other.symbols[pos])) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return hcode;
    }
}
