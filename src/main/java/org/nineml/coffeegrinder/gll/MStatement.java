package org.nineml.coffeegrinder.gll;

public abstract class MStatement {
    protected void execute(GllParser gllParser) {
        throw new UnsupportedOperationException(this.getClass().getName() + " is not implemented");
    }
}
