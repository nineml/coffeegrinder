package org.nineml.coffeegrinder.parser;

import org.nineml.coffeegrinder.util.DefaultParseListener;

/**
 * Options to the parser.
 */
public class ParserOptions {
    /**
     * The default constructor.
     * <p>This object is intended to be just a collection of publicly modifiable fields.</p>
     */
    public ParserOptions() {
        // nop
    }

    /**
     * A copy constructor.
     * @param copy the options to copy.
     */
    public ParserOptions(ParserOptions copy) {
        returnChart = copy.returnChart;
        prefixParsing = copy.prefixParsing;
        treesWithStates = copy.treesWithStates;
        listener = copy.listener;
    }

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
     * <p>The parser sends messages to the listener which may be helpful in understanding what the parser is doing.
     * It's mostly for debugging, but may be useful in other contexts.</p>
     */
    public ParseListener listener = new DefaultParseListener();
}
