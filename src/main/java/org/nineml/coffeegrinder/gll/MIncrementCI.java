package org.nineml.coffeegrinder.gll;

public class MIncrementCI extends MStatement {
    protected void execute(GllParser gllParser) {
        gllParser.c_I++;
        if (gllParser.trace) {
            System.err.println("c_I = " + gllParser.c_I);
        }

    }

    @Override
    public String toString() {
        return "\t\tc_I = c_I + 1";
    }
}
