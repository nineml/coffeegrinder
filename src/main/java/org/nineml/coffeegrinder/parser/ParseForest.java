package org.nineml.coffeegrinder.parser;

import java.io.PrintStream;
import java.math.BigInteger;
import java.util.List;

public interface ParseForest {
    boolean isAmbiguous();
    boolean isInfinitelyAmbiguous();
    int size();
    long getTotalParses();
    BigInteger getExactTotalParses();
    Ambiguity getAmbiguity();
    List<ForestNode> getNodes();
    List<ForestNode> getRoots();
    ParseTree parse();
    ParserOptions getOptions();
    void resetParses();
    String serialize();
    void serialize(PrintStream stream);
    void serialize(String filename);
}