package org.nineml.coffeegrinder.parser;

import org.nineml.coffeegrinder.util.DefaultParseListener;
import org.nineml.logging.DefaultLogger;
import org.nineml.logging.Logger;

/**
 * Options to the parser.
 * <p>This object is intentionally just a collection of public fields.</p>
 */
public class ParserOptions {
    /**
     * Return the Earley chart even for a successful parse?
     */
    public boolean returnChart = false;

    /**
     * If a parse fails, but some prefix of the input was successfully parsed, make that available.
     *
     * <p>This is optional mostly because it requires internally buffering some of the input tokens.
     * (Probably no more than two, but I haven't tried to prove that.)</p>
     */
    public boolean prefixParsing = false;

    /**
     * Return intermediate "state" nodes in parse trees?
     * <p>If true, intermediate nodes in the forest that represent states (as distinct from
     * nodes that actually represent a symbol) will be returned.</p>
     */
    public boolean treesWithStates = false;

    /**
     * The parser logger.
     * <p>The logger controls what messages are issued, and how. This component is also used by
     * higher-level components such as CoffeeFilter, CoffeePot, and CoffeeSacks.</p>
     * <p>Must not be null.</p>
     */
    public Logger logger;

    /**
     * The progress monitor.
     * <p>If this option is not null, the monitor will be called before, during, and after
     * the parse.</p>
     */
    public ProgressMonitor monitor = null;

    /**
     * The default constructor.
     * <p>This object is intended to be just a collection of publicly modifiable fields. The
     * default constructor creates a {@link DefaultLogger} and uses {@link DefaultLogger#readSystemProperties readSystemProperties()}
     * to initialize it.</p>
     */
    public ParserOptions() {
        logger = new DefaultLogger();
        logger.readSystemProperties();
    }
}
