package org.nineml.coffeegrinder.gll;

import org.nineml.coffeegrinder.parser.State;

public class MBsrAdd extends MStatement {
    public final State slot;

    public MBsrAdd(State slot) {
        this.slot = slot;
    }

    protected void execute(GllParser gllParser) {
        if (gllParser.trace) {
            System.err.printf("bsrAdd(%s, %d, %d, %d)\n", slot, gllParser.c_U, gllParser.c_I, gllParser.c_I+1);
        }
        gllParser.bsrAdd(slot, gllParser.c_U, gllParser.c_I, gllParser.c_I+1);
    }

    @Override
    public String toString() {
        return "\t\tbsrAdd(" + slot + ", c_U, c_I, c_I+1)";
    }
}
