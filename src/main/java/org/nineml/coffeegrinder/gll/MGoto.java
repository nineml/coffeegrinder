package org.nineml.coffeegrinder.gll;

import org.nineml.coffeegrinder.parser.State;

public class MGoto extends MStatement {
    public final State label;

    public MGoto(State label) {
        this.label = label;
    }

    protected void execute(GllParser gllParser) {
        if (gllParser.trace) {
            System.err.println("goto " + label.getInstructionPointer());
        }
        gllParser.jump(label);
    }

    @Override
    public String toString() {
        return "\t\tgoto " + label;
    }
}
