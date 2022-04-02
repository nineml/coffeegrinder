package org.nineml.coffeegrinder.parser;

import org.nineml.coffeegrinder.exceptions.ParseException;
import org.nineml.coffeegrinder.tokens.TokenEmpty;

import java.util.Arrays;
import java.util.List;

/**
 * States (or Earley items) in the chart.
 *
 * FIXME: this API is in flux.
 */
public class State {
    private static int nextStateId = 0;
    private final int id;
    private final Rule rule;
    private final NonterminalSymbol symbol;
    private final RightHandSide rhs;
    private final int position;
    private Integer cachedCode = null;

    protected State(Rule rule) {
        this.rule = rule;
        this.symbol = rule.getSymbol();
        this.rhs = rule.getRhs();
        this.position = rhs.getNextPosition(0);
        id = nextStateId;
        nextStateId++;
    }

    private State(State other, int position) {
        this.rule = other.rule;
        this.symbol = other.symbol;
        this.rhs = other.rhs;
        this.position = rhs.getNextPosition(position);
        id = nextStateId;
        nextStateId++;
    }

    /**
     * Get the nonterminal associated with this state
     * @return the symbol
     */
    public NonterminalSymbol getSymbol() {
        return symbol;
    }

    /**
     * Get the rule that originated this state.
     * @return the rule
     */
    public Rule getRule() {
        return rule;
    }

    /**
     * Get the list of symbols that define this state's nonterminal symbol.
     * @return the list of symbols on the "right hand side"
     */
    public RightHandSide getRhs() {
        return rhs;
    }

    /**
     * Get the current position in this state
     * @return the position
     */
    public int getPosition() {
        return position;
    }

    /**
     * Get the next symbol
     * <p>If the state is not completed, return the symbol that occurs after the current position.
     * </p>
     * @return the next symbol, or null if the state is completed
     */
    public Symbol nextSymbol() {
        if (position < rhs.size()) {
            return rhs.get(position);
        }
        return null;
    }

    /**
     * Get a new state with the position advanced by one
     * @return a new state with the position advanced
     * @throws ParseException if an attempt is made to advance a completed state
     */
    public State advance() {
        if (position < rhs.size()) {
            return new State(this, position+1);
        } else {
            throw ParseException.internalError("Cannot advance a completed state");
        }
    }

    /**
     * Are we finished with this symbol?
     * @return true if the position indicates that we've seen all of the symbols on the "right hand side"
     */
    public boolean completed() {
        return position == rhs.size();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof State) {
            State other = (State) obj;
            return symbol.equals(other.symbol) && position == other.position
                    && rhs.equals(other.rhs);
        }
        return false;
    }

    @Override
    public int hashCode() {
        if (cachedCode == null) {
            cachedCode = symbol.hashCode() + (13 * position) + rhs.hashCode();
        }
        return cachedCode;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(symbol);
        sb.append(" ⇒ ");
        int count = 0;
        for (Symbol symbol : rhs.getSymbols()) {
            if (count > 0) {
                sb.append(" ");
            }
            if (count == position) {
                sb.append("• ");
            }
            sb.append(symbol.toString());
            count += 1;
        }
        if (count == position) {
            sb.append(" •");
        }
        return sb.toString();
    }
}
