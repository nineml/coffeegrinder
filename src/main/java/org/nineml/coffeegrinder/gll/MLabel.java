package org.nineml.coffeegrinder.gll;

import org.nineml.coffeegrinder.parser.State;

public class MLabel extends MStatement {
    public final State label;

    public MLabel(State label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return label.toString() + ":";
    }
}
