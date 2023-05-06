package org.nineml.coffeegrinder.parser;

import java.util.List;

public class RuleChoiceImpl implements RuleChoice {
    private final Symbol symbol;
    private final Symbol[] rhs;
    private final ForestNode left;
    private final ForestNode right;

    public RuleChoiceImpl(Symbol symbol, List<Symbol> rhs, ForestNode left, ForestNode right) {
        this.symbol = symbol;
        this.rhs = new Symbol[rhs.size()];
        rhs.toArray(this.rhs);
        this.left = left;
        this.right = right;
    }

    public RuleChoiceImpl(Family family) {
        this.symbol = family.getSymbol();
        this.rhs = family.getRightHandSide();
        this.left = family.v;
        this.right = family.w;
    }

    public Symbol getSymbol() {
        return symbol;
    };

    public Symbol[] getRightHandSide() {
        return rhs;
    }

    public ForestNode getLeftNode() {
        return left;
    }
    public ForestNode getRightNode() {
        return right;
    };
}
