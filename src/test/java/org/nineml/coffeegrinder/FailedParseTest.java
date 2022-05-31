package org.nineml.coffeegrinder;

import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.nineml.coffeegrinder.parser.*;
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

        GearleyParser parser = grammar.getParser(_S);
        GearleyResult result = parser.parse(Iterators.characterIterator("1+2-3"));
        Assert.assertFalse(result.succeeded());
        Assert.assertEquals(4, result.getTokenCount());
        Assert.assertEquals("-", result.getLastToken().getValue());
    }

    @Test
    public void testMonths_March() {
        try {
            GrammarCompiler compiler = new GrammarCompiler();
            Grammar grammar = compiler.parse(new File("src/test/resources/month.cxml"));
            NonterminalSymbol start = grammar.getNonterminal("$$");
            GearleyParser parser = grammar.getParser(start);
            GearleyResult result = parser.parse("March");
            Assertions.assertTrue(result.succeeded());

            if (result instanceof EarleyResult) {
                Assertions.assertTrue(((EarleyResult) result).getPredictedTerminals().isEmpty());
            }

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
            GearleyParser parser = grammar.getParser(start);
            GearleyResult result = parser.parse("Max");
            Assertions.assertFalse(result.succeeded());

            if (result instanceof EarleyResult) {
                Assertions.assertEquals(2, ((EarleyResult) result).getPredictedTerminals().size());
                for (TerminalSymbol t : ((EarleyResult) result).getPredictedTerminals()) {
                    String value = t.getToken().getValue();
                    Assertions.assertTrue("r".equals(value) || "y".equals(value));
                }
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
            GearleyParser parser = grammar.getParser(start);
            GearleyResult result = parser.parse("Marsh");
            Assertions.assertFalse(result.succeeded());
            if (result instanceof EarleyResult) {
                Assertions.assertEquals(1, ((EarleyResult) result).getPredictedTerminals().size());
                for (TerminalSymbol t : ((EarleyResult) result).getPredictedTerminals()) {
                    String value = t.getToken().getValue();
                    Assertions.assertEquals("c", value);
                }
            }
        } catch (Exception ex) {
            fail();
        }
    }

}
