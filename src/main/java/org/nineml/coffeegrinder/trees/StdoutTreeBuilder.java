package org.nineml.coffeegrinder.trees;

public class StdoutTreeBuilder extends PrintStreamTreeBuilder {
    public StdoutTreeBuilder() {
        super(System.out);
    }
}
