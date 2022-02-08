package org.nineml.coffeegrinder;

import org.junit.Assert;
import org.junit.Test;
import org.nineml.coffeegrinder.parser.EarleyResult;
import org.nineml.coffeegrinder.parser.Grammar;
import org.nineml.coffeegrinder.parser.EarleyParser;
import org.nineml.coffeegrinder.util.GrammarParser;

// N.B. THESE TESTS ARE CAKE! (These tests are a lie.)
//
// There's a bug in my computation of the SPPF forest, so the results are bogus when grammars are ambiguous.

public class SppfTest {
    @Test
    public void Sabcd() {
        GrammarParser gparser = new GrammarParser();
        Grammar grammar = gparser.parse(
                "S => A, B, C, D\n" +
                "A => 'a'\n" +
                "B => 'b'\n" +
                "C => 'c'\n" +
                "D => 'd'");
        // grammar.getParseListener().setMessageLevel(ParseListener.DEBUG);

        String input = "abcd";
        EarleyParser parser = grammar.getParser(grammar.getNonterminal("S"));
        EarleyResult result = parser.parse("abcd");
        Assert.assertTrue(result.succeeded());
    }

    @Test
    public void SabcdOptional() {
        GrammarParser gparser = new GrammarParser();
        Grammar grammar = gparser.parse(
                "S => A, B, C, D\n" +
                        "S => \n" +
                        "A => 'a'\n" +
                        "B => 'b'\n" +
                        "C => 'c'\n" +
                        "D => 'd'");
        // grammar.getParseListener().setMessageLevel(ParseListener.DEBUG);

        String input = "abcd";
        EarleyParser parser = grammar.getParser(grammar.getNonterminal("S"));
        EarleyResult result = parser.parse(input);
        Assert.assertTrue(result.succeeded());
    }

    @Test
    public void bbb() {
        GrammarParser gparser = new GrammarParser();
        Grammar grammar = gparser.parse(
                "S => S, S\n" +
                        "S => 'b'");
        // grammar.getParseListener().setMessageLevel(ParseListener.DEBUG);

        String input = "bbb";

        EarleyParser parser = grammar.getParser(grammar.getNonterminal("S"));
        EarleyResult result = parser.parse(input);
        Assert.assertTrue(result.succeeded());
    }

    @Test
    public void Sabbb() {
        GrammarParser gparser = new GrammarParser();
        Grammar grammar = gparser.parse(
                "S => A, T\n" +
                        "S => 'a', T\n" +
                        "A => 'a'\n" +
                        "A => B?, A\n" +
                        "B => 'b'\n" +
                        "T => 'b', 'b', 'b'\n" +
                        "");
        // grammar.getParseListener().setMessageLevel(ParseListener.DEBUG);

        String input = "abbb";

        EarleyParser parser = grammar.getParser(grammar.getNonterminal("S"));
        EarleyResult result = parser.parse(input);
        Assert.assertTrue(result.succeeded());
    }

    @Test
    public void sum() {
        GrammarParser gparser = new GrammarParser();
        Grammar grammar = gparser.parseFile("src/test/resources/expression.grammar");
        // grammar.getParseListener().setMessageLevel(ParseListener.DEBUG);

        String input = "1+(2*3-4)";

        EarleyParser parser = grammar.getParser(grammar.getNonterminal("Sum"));
        EarleyResult result = parser.parse(input);
        Assert.assertTrue(result.succeeded());
    }
}
