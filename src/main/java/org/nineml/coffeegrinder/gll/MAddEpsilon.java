package org.nineml.coffeegrinder.gll;

import org.nineml.coffeegrinder.parser.State;

public class MAddEpsilon extends MStatement {
    public final State slot;

    public MAddEpsilon(State slot) {
        this.slot = slot;
    }

    @Override
    public String toString() {
        return "\t\tY = Y ⋃ {(" + slot + ", c_I, c_I, c_I)}";
    }
}
