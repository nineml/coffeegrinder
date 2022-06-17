package org.nineml.coffeegrinder.gll;

import org.nineml.coffeegrinder.parser.*;
import org.nineml.coffeegrinder.tokens.Token;
import org.nineml.coffeegrinder.util.ParseTreeBuilder;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class GllResult implements GearleyResult {
    private final GllParser parser;
    private final BinarySubtree bsr;
    private final ParseForest graph;
    public final boolean success;
    private final int tokenCount;
    private final Token lastToken;
    private long parseTime = -1;
    private boolean ambiguous = false;
    private boolean infinitelyAmbiguous = false;

    public GllResult(GllParser parser, BinarySubtree bsr) {
        this.parser = parser;
        this.bsr = bsr;
        this.graph = bsr.extractSPPF(parser.getGrammar(), parser.getTokens());
        tokenCount = parser.tokenCount;
        lastToken = parser.lastToken;
        success = parser.succeeded();

        if (success) {
            HashMap<Symbol, HashSet<Integer>> seen = new HashMap<>();
            for (int idx : bsr.bsrSlots.keySet()) {
                for (BinarySubtreeSlot node : bsr.bsrSlots.get(idx)) {
                    assert node.slot.symbol != null;
                    if (seen.containsKey(node.slot.symbol) && seen.get(node.slot.symbol).contains(node.rightExtent)) {
                        ambiguous = true;
                        break;
                    }
                    if (!seen.containsKey(node.slot.symbol)) {
                        seen.put(node.slot.symbol, new HashSet<>());
                    }
                    seen.get(node.slot.symbol).add(node.rightExtent);
                }
                seen.clear();
                if (ambiguous) {
                    break;
                }
            }
        }
    }

    @Override
    public GearleyResult continueParsing() {
        throw new UnsupportedOperationException("Not implemented.");
    }

    @Override
    public ParseForest getForest() {
        return graph;
    }

    @Override
    public ParseTree getTree() {
        ParseTreeBuilder builder = new ParseTreeBuilder();
        getTree(builder);
        return builder.getParseTree();
    }

    @Override
    public void getTree(TreeBuilder builder) {
        graph.getTree(builder);
        infinitelyAmbiguous = builder.isInfinitelyAmbiguous();
    }

    public BinarySubtree getBinarySubtree() {
        return bsr;
    }

    @Override
    public boolean isAmbiguous() {
        return ambiguous;
    }

    @Override
    public boolean isInfinitelyAmbiguous() {
        return infinitelyAmbiguous;
    }

    @Override
    public int getTokenCount() {
        return tokenCount;
    }

    @Override
    public Token getLastToken() {
        return lastToken;
    }

    public Set<TerminalSymbol> getPredictedTerminals() {
        return Collections.emptySet();
    }

    @Override
    public GearleyParser getParser() {
        return parser;
    }

    @Override
    public boolean prefixSucceeded() {
        return false;
    }

    @Override
    public long getParseTime() {
        return parseTime;
    }

    protected void setParseTime(long time) {
        parseTime = time;
    }

    @Override
    public boolean succeeded() {
        return success;
    }
}
