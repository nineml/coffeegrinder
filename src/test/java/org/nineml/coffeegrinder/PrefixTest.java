package org.nineml.coffeegrinder;

import org.junit.Assert;
import org.junit.Test;
import org.nineml.coffeegrinder.parser.*;
import org.nineml.coffeegrinder.tokens.Token;
import org.nineml.coffeegrinder.util.Iterators;

import java.util.Iterator;

public class PrefixTest {
    private final ParserOptions options = new ParserOptions();

    @Test
    public void testAB() {
        ParserOptions options = new ParserOptions();
        options.setPrefixParsing(true);
        SourceGrammar grammar = new SourceGrammar(options);

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _B = grammar.getNonterminal("B");
        TerminalSymbol _a = TerminalSymbol.ch('a');
        TerminalSymbol _b = TerminalSymbol.ch('b');

        grammar.addRule(_S, _A, _B);
        grammar.addRule(_A, _a);
        grammar.addRule(_B, _b, _B);
        grammar.addRule(_B);

        GearleyParser parser = grammar.getParser(options, _S);

        if (parser.getParserType() != ParserType.Earley) {
            System.err.println("Prefix parsing is only supported by the Earley parser");
            return;
        }

        Iterator<Token> input = Iterators.characterIterator("abbabbbba");

        GearleyResult result = parser.parse(input);
        Assert.assertFalse(result.succeeded());
        Assert.assertTrue(result.prefixSucceeded());

        result = result.continueParsing();
        Assert.assertFalse(result.succeeded());

        result = result.continueParsing();
        Assert.assertTrue(result.succeeded());
    }

    @Test
    public void testParens() {
        ParserOptions options = new ParserOptions();
        options.setPrefixParsing(true);
        SourceGrammar grammar = new SourceGrammar(options);

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _B = grammar.getNonterminal("B");
        TerminalSymbol _b = TerminalSymbol.ch('b');
        TerminalSymbol _op = TerminalSymbol.ch('(');
        TerminalSymbol _cp = TerminalSymbol.ch(')');

        grammar.addRule(_S, _B);
        grammar.addRule(_S, _op, _S, _cp);
        grammar.addRule(_B, _b);
        grammar.addRule(_B, _B);

        GearleyParser parser = grammar.getParser(options, _S);

        if (parser.getParserType() != ParserType.Earley) {
            System.err.println("Prefix parsing is only supported by the Earley parser");
            return;
        }

        Iterator<Token> input = Iterators.characterIterator("(b))");

        GearleyResult result = parser.parse(input);
        Assert.assertFalse(result.succeeded());
        Assert.assertTrue(result.prefixSucceeded());
    }
}
