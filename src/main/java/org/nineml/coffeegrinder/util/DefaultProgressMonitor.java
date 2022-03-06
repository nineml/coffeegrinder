package org.nineml.coffeegrinder.util;

import org.nineml.coffeegrinder.parser.EarleyParser;
import org.nineml.coffeegrinder.parser.ProgressMonitor;

/**
 * A default implementation of {@link ProgressMonitor}.
 */
public class DefaultProgressMonitor implements ProgressMonitor {
    /** The default update interval (number of tokens). */
    public static int frequency = 100;

    private final int size;

    /**
     * Create a monitor with the default update interval.
     *
     */
    public DefaultProgressMonitor() {
        this(frequency);
    }

    /**
     * Create a monitor with a specific update interval.
     * @param size the update frequency
     */
    public DefaultProgressMonitor(int size) {
        this.size = size;
    }

    /**
     * Start the monitor.
     * @param parser the parser
     * @return the update interval.
     */
    @Override
    public int starting(EarleyParser parser) {
        return size;
    }

    /**
     * Report progress.
     * <p>This implementation just prints a simple message to <code>System.out</code>.</p>
     * @param parser the parser
     * @param tokens the number of tokens processed so far.
     */
    @Override
    public void progress(EarleyParser parser, long tokens) {
        System.out.printf("Processed %d tokens.%n", tokens);
    }

    /**
     * Finish the monitor.
     * @param parser the parser
     */
    @Override
    public void finished(EarleyParser parser) {
        // nop
    }
}
