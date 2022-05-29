package org.nineml.coffeegrinder.gll;

public class XFamily {
    protected PackedForestNode v;
    protected PackedForestNode w;

    public XFamily(PackedForestNode v) {
        this.v = v;
        this.w = null;
    }

    public XFamily(PackedForestNode v, PackedForestNode w) {
        this.v = v;
        this.w = w;
    }

    @Override
    public String toString() {
        if (w != null) {
            return v + " / " + w;
        }
        return "" + v;
    }
}
