package org.nineml.coffeegrinder.gll;

import org.nineml.coffeegrinder.parser.State;

public class MCall extends MStatement {
    public final State slot;

    public MCall(State slot) {
        this.slot = slot;
    }

    protected void execute(GllParser gllParser) {
        if (gllParser.trace) {
            System.err.printf("call(%s, %d, %d)\n", slot, gllParser.c_U, gllParser.c_I);
        }
        gllParser.call(slot, gllParser.c_U, gllParser.c_I);
    }

    @Override
    public String toString() {
        return "\t\tcall(" + slot + ", c_U, c_I)";
    }
}
