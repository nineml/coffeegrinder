package org.nineml.coffeegrinder.gll;

public class MIncrementCI extends MStatement {
    protected void execute(GllParser gllParser) {
        gllParser.incrementC_I();
    }

    @Override
    public String toString() {
        return "\t\tc_I = c_I + 1";
    }
}
