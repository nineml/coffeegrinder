package org.nineml.coffeegrinder.parser;

import org.nineml.coffeegrinder.tokens.Token;

import java.util.Iterator;
import java.util.List;

public interface GearleyParser {
    ParserType getParserType();
    Grammar getGrammar();
    NonterminalSymbol getSeed();
    GearleyResult parse(Token[] input);
    GearleyResult parse(Iterator<Token> input);
    GearleyResult parse(String input);
    boolean hasMoreInput();
}
