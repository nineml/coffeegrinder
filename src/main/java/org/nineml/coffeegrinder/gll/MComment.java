package org.nineml.coffeegrinder.gll;

public class MComment extends MStatement {
    public final String comment;

    public MComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return "\t\t# " + comment;
    }
}
