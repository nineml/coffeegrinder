package org.nineml.coffeegrinder.util;

import org.nineml.coffeegrinder.parser.EarleyParser;
import org.nineml.coffeegrinder.parser.ProgressMonitor;

public class DefaultProgressMonitor implements ProgressMonitor {
    public static int frequency = 100;
    private final int size;

    public DefaultProgressMonitor() {
        this(frequency);
    }

    public DefaultProgressMonitor(int size) {
        this.size = size;
    }

    @Override
    public int starting(EarleyParser parser) {
        return size;
    }

    @Override
    public void progress(EarleyParser parser, long tokens) {
        System.out.printf("Processed %d tokens.%n", tokens);
    }

    @Override
    public void finished(EarleyParser parser) {
        // nop
    }
}
