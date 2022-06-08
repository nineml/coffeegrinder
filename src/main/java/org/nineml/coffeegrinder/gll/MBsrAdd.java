package org.nineml.coffeegrinder.gll;

import org.nineml.coffeegrinder.parser.State;

public class MBsrAdd extends MStatement {
    public final State slot;
    public final boolean epsilon;

    public MBsrAdd(State slot) {
        this.slot = slot;
        epsilon = false;
    }

    public MBsrAdd(State slot, boolean epsilon) {
        this.slot = slot;
        this.epsilon = epsilon;
    }

    protected void execute(GllParser gllParser) {
        if (epsilon) {
            gllParser.bsrAddEpsilon(slot, gllParser.c_I);
        } else {
            gllParser.bsrAdd(slot, gllParser.c_U, gllParser.c_I, gllParser.c_I+1);
        }
    }

    @Override
    public String toString() {
        if (epsilon) {
            return "\t\tbsrAdd(" + slot + ", c_I, c_I, c_I)";
        }
        return "\t\tbsrAdd(" + slot + ", c_U, c_I, c_I+1)";
    }
}
