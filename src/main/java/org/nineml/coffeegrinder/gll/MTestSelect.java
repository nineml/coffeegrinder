package org.nineml.coffeegrinder.gll;

import org.nineml.coffeegrinder.parser.State;

public class MTestSelect extends MStatement {
    public final State slot;

    public MTestSelect(State slot) {
        this.slot = slot;
    }

    protected void execute(GllParser gllParser) {
        gllParser.testSelect(slot);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\t\tif (!testSelect(I[c_I], ").append(slot.symbol).append(", ");
        int pos = slot.position;
        while (pos < slot.rhs.length) {
            if (pos > slot.position) {
                sb.append(" ");
            }
            sb.append(slot.rhs.get(pos));
            pos++;
        }
        sb.append(")) goto L₀");
        return sb.toString();
    }
}
