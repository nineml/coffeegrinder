package org.nineml.coffeegrinder.gll;

import org.nineml.coffeegrinder.parser.ParseForest;

import java.math.BigInteger;

public class GllForest {
    private static final BigInteger MAX_LONG = new BigInteger(String.valueOf(Long.MAX_VALUE));

    public final GllForestNode root;
    private final boolean ambiguous;
    private final boolean infinitelyAmbiguous;

    protected GllForest(GllForestNode root, boolean ambiguous, boolean infinitelyAmbiguous) {
        this.root = root;
        this.ambiguous = ambiguous;
        this.infinitelyAmbiguous = infinitelyAmbiguous;
    }

    public boolean isAmbiguous() {
        return ambiguous;
    }

    public boolean isInfinitelyAmbiguous() {
        return infinitelyAmbiguous;
    }

    public long getTotalParses() {
        if (root.parseCount.compareTo(MAX_LONG) > 0) {
            return Long.MAX_VALUE;
        }
        return root.parseCount.longValue();
    }

    public BigInteger getExactTotalParses() {
        return root.parseCount;
    }

    public long getRemainingParses() {
        if (root.remainingParseCount.compareTo(MAX_LONG) > 0) {
            return Long.MAX_VALUE;
        }
        return root.remainingParseCount.longValue();
    }

    public BigInteger getExactRemainingParses() {
        return root.remainingParseCount;
    }
}
