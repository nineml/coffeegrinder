package org.nineml.coffeegrinder.parser;

import org.nineml.coffeegrinder.util.DefaultParseListener;
import org.nineml.logging.DefaultLogger;
import org.nineml.logging.Logger;

/**
 * Options to the parser.
 * <p>This object is extended by other members of the NineML family to provide additional options.
 * It started out as a collection of public fields, but changed to a more traditional collection of
 * getters and setters when it began to develop options that were not entirely independent.</p>
 */
public class ParserOptions {
    private Logger logger;
    private boolean returnChart = false;
    private boolean prefixParsing = false;
    private boolean treesWithStates = false;
    private ProgressMonitor monitor = null;

    /**
     * Create the parser options.
     * <p>The initial logger will be a {@link DefaultLogger} initialized with
     * {@link DefaultLogger#readSystemProperties readSystemProperties()}.</p>
     */
    public ParserOptions() {
        logger = new DefaultLogger();
        logger.readSystemProperties();
    }

    /**
     * Create the parser options with an explicit logger.
     * @param logger the logger.
     */
    public ParserOptions(Logger logger) {
        this.logger = logger;
    }

    /**
     * Return the Earley chart even for a successful parse?
     * @return true if the Earley chart should be returned even for an unsuccessful parse
     */
    public boolean getReturnChart() {
        return returnChart;
    }

    /**
     * Set the {@link #getReturnChart()} property.
     * @param returnChart return the chart?
     */
    public void setReturnChart(boolean returnChart) {
        this.returnChart = returnChart;
    }

    /**
     * If a parse fails, but some prefix of the input was successfully parsed, make that available.
     *
     * <p>This is optional mostly because it requires internally buffering some of the input tokens.
     * (Probably no more than two, but I haven't tried to prove that.)</p>
     *
     * @return true if prefix parsing is enabled.
     */
    public boolean getPrefixParsing() {
        return prefixParsing;
    }

    /**
     * Set the {@link #getPrefixParsing()} property.
     * @param prefixParsing prefix parsing?
     */
    public void setPrefixParsing(boolean prefixParsing) {
        this.prefixParsing = prefixParsing;
    }

    /**
     * Return intermediate "state" nodes in parse trees?
     * <p>If true, intermediate nodes in the forest that represent states (as distinct from
     * nodes that actually represent a symbol) will be returned.</p>
     *
     * @return true if trees with states should be constructed.
     */
    public boolean getTreesWithStates() {
        return treesWithStates;
    }

    /**
     * Set the {@link #getTreesWithStates()} property.
     * @param treesWithStates trees with states?
     */
    public void setTreesWithStates(boolean treesWithStates) {
        this.treesWithStates = treesWithStates;
    }

    /**
     * The parser logger.
     * <p>The logger controls what messages are issued, and how. This component is also used by
     * higher-level components such as CoffeeFilter, CoffeePot, and CoffeeSacks.</p>
     * @return the logger.
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * Set the logger.
     * @param logger the logger.
     * @throws NullPointerException if the logger is null.
     */
    public void setLogger(Logger logger) {
        if (logger == null) {
            throw new NullPointerException("Logger must not be null");
        }
        this.logger = logger;
    }

    /**
     * The progress monitor.
     * <p>If this option is not null, the monitor will be called before, during, and after
     * the parse.</p>
     * @return the monitor
     */
    public ProgressMonitor getProgressMonitor() {
        return monitor;
    }

    /**
     * Set the progress monitor.
     * <p>Setting the monitor to <code>null</code> disables monitoring.</p>
     * @param monitor the monitor.
     */
    public void setProgressMonitor(ProgressMonitor monitor) {
        this.monitor = monitor;
    }
}
