package org.nineml.coffeegrinder.gll;

public class MNextDescriptor extends MStatement {
    protected void execute(GllParser gllParser) {
        gllParser.nextDescriptor();
    }

    @Override
    public String toString() {
        return "\t\tif (R == ∅) goto end else process a descriptor";
    }
}
