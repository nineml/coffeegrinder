package org.nineml.coffeegrinder.parser;

public interface RuleChoice {
    Symbol getSymbol();
    Symbol[] getRightHandSide();
    int getLeftExtent();
    int getRightExtent();
}
