package org.nineml.coffeegrinder;

import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.nineml.coffeegrinder.parser.EarleyResult;
import org.nineml.coffeegrinder.parser.Grammar;
import org.nineml.coffeegrinder.parser.EarleyParser;
import org.nineml.coffeegrinder.parser.NonterminalSymbol;
import org.nineml.coffeegrinder.parser.TerminalSymbol;
import org.nineml.coffeegrinder.tokens.TokenCharacter;
import org.nineml.coffeegrinder.tokens.TokenRegex;
import org.nineml.coffeegrinder.util.GrammarCompiler;
import org.nineml.coffeegrinder.util.Iterators;

import java.io.File;

import static org.junit.Assert.fail;

public class FailedParseTest {
    @Test
    public void testFailEEE() {
        Grammar grammar = new Grammar();

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _E = grammar.getNonterminal("E");
        TerminalSymbol _plus = TerminalSymbol.ch('+');

        grammar.addRule(_S, _E);
        grammar.addRule(_E, _E, _plus, _E);
        grammar.addRule(_E, new TerminalSymbol(TokenRegex.get("[0-9]")));

        EarleyParser parser = grammar.getParser(_S);
        EarleyResult result = parser.parse(Iterators.characterIterator("1+2-3"));
        Assert.assertFalse(result.succeeded());
        Assert.assertEquals(4, result.getTokenCount());
        Assert.assertEquals('-', ((TokenCharacter) result.getLastToken()).getCharacter());
    }

    @Test
    public void testMonths_March() {
        try {
            GrammarCompiler compiler = new GrammarCompiler();
            Grammar grammar = compiler.parse(new File("src/test/resources/month.cxml"));
            NonterminalSymbol start = grammar.getNonterminal("$$");
            EarleyParser parser = grammar.getParser(start);
            EarleyResult result = parser.parse("March");
            Assertions.assertTrue(result.succeeded());
            Assertions.assertTrue(result.predictedTerminals().isEmpty());
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void testMonths_Max() {
        try {
            GrammarCompiler compiler = new GrammarCompiler();
            Grammar grammar = compiler.parse(new File("src/test/resources/month.cxml"));
            NonterminalSymbol start = grammar.getNonterminal("$$");
            EarleyParser parser = grammar.getParser(start);
            EarleyResult result = parser.parse("Max");
            Assertions.assertFalse(result.succeeded());
            Assertions.assertEquals(2, result.predictedTerminals().size());
            for (TerminalSymbol t : result.predictedTerminals()) {
                String value = t.getToken().getValue();
                Assertions.assertTrue("r".equals(value) || "y".equals(value));
            }
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void testMonths_Marsh() {
        try {
            GrammarCompiler compiler = new GrammarCompiler();
            Grammar grammar = compiler.parse(new File("src/test/resources/month.cxml"));
            NonterminalSymbol start = grammar.getNonterminal("$$");
            EarleyParser parser = grammar.getParser(start);
            EarleyResult result = parser.parse("Marsh");
            Assertions.assertFalse(result.succeeded());
            Assertions.assertEquals(1, result.predictedTerminals().size());
            for (TerminalSymbol t : result.predictedTerminals()) {
                String value = t.getToken().getValue();
                Assertions.assertTrue("c".equals(value));
            }
        } catch (Exception ex) {
            fail();
        }
    }

}
