package org.nineml.coffeegrinder.util;

import org.nineml.coffeegrinder.parser.EarleyParser;
import org.nineml.coffeegrinder.parser.GearleyParser;
import org.nineml.coffeegrinder.parser.ParserType;
import org.nineml.coffeegrinder.parser.ProgressMonitor;

/**
 * A default implementation of {@link ProgressMonitor}.
 */
public class DefaultProgressMonitor implements ProgressMonitor {
    /** The default update interval (number of tokens). */
    public static int earleyFrequency = 100;

    /** The default update interval (number of states). */
    public static int gllFrequency = 10000;

    /**
     * Create a progress monitor.
     */
    public DefaultProgressMonitor() {
    }

    /**
     * Start the monitor.
     * @param parser the parser
     * @return the update interval.
     */
    @Override
    public int starting(GearleyParser parser, int tokens) {
        if (parser.getParserType() == ParserType.GLL) {
            return gllFrequency;
        }
        return earleyFrequency;
    }

    /**
     * Report progress.
     * <p>This implementation just prints a simple message to <code>System.out</code>.</p>
     * @param parser the parser
     * @param tokens the number of tokens processed so far.
     */
    @Override
    public void progress(GearleyParser parser, int tokens) {
        System.out.printf("Processed %,d tokens.%n", tokens);
    }

    /**
     * Report progress.
     * <p>This implementation just prints a simple message to <code>System.out</code>.</p>
     * @param parser the parser
     * @param size the number of items that remain in the working set.
     */
    @Override
    public void workingSet(GearleyParser parser, int size) {
        System.out.printf("Remaining %,d items.%n", size);
    }

    /**
     * Finish the monitor.
     * @param parser the parser
     */
    @Override
    public void finished(GearleyParser parser) {
        // nop
    }
}
