package org.nineml.coffeegrinder.parser;

public interface RuleChoice {
    Symbol getSymbol();
    Symbol[] getRightHandSide();
    ForestNode getLeftNode();
    ForestNode getRightNode();
}
