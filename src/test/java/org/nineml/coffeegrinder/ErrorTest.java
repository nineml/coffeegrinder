package org.nineml.coffeegrinder;

import org.junit.Assert;
import org.junit.Test;
import org.nineml.coffeegrinder.exceptions.GrammarException;
import org.nineml.coffeegrinder.parser.EarleyParser;
import org.nineml.coffeegrinder.parser.EarleyResult;
import org.nineml.coffeegrinder.parser.Grammar;
import org.nineml.coffeegrinder.util.GrammarParser;
import org.nineml.coffeegrinder.util.Iterators;

import static junit.framework.TestCase.fail;

public class ErrorTest {

    @Test
    public void missingSymbol() {
        GrammarParser gparser = new GrammarParser();
        Grammar grammar = gparser.parse(
                "  S => A, B\n" +
                        "A => 'a'");

        try {
            grammar.getParser(grammar.getNonterminal("S"));
        } catch (GrammarException ex) {
            Assert.assertEquals("E001", ex.getCode());
        }

    }

    @Test
    public void invalidCharacterClassLq() {
        GrammarParser gparser = new GrammarParser();
        try {
            gparser.parse(
                    "  S => A, B\n" +
                            "A => 'a'\n" +
                            "B => [Lq]");
            fail();
        } catch (GrammarException ex) {
            Assert.assertEquals("E002", ex.getCode());
        }
    }
}