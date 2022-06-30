package org.nineml.coffeegrinder.parser;

import org.nineml.coffeegrinder.tokens.Token;

import java.util.Iterator;

public interface GearleyParser {
    ParserType getParserType();
    ParserGrammar getGrammar();
    NonterminalSymbol getSeed();
    GearleyResult parse(Token[] input);
    GearleyResult parse(Iterator<Token> input);
    GearleyResult parse(String input);
    boolean hasMoreInput();
    int getOffset();
    int getLineNumber();
    int getColumnNumber();
}
