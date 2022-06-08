package org.nineml.coffeegrinder;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.nineml.coffeegrinder.parser.*;
import org.nineml.coffeegrinder.util.GrammarCompiler;
import org.nineml.coffeegrinder.util.Iterators;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import static junit.framework.TestCase.fail;

public class AmbiguityTest {
    @Test
    public void testProgram() {
        try {
            GrammarCompiler compiler = new GrammarCompiler();
            Grammar grammar = compiler.parse(Files.newInputStream(Paths.get("src/test/resources/program.cxml")), "program.cxml");
            GearleyParser parser = grammar.getParser(grammar.getNonterminal("$$"));
            GearleyResult result = parser.parse(Iterators.fileIterator("src/test/resources/program.inp"));
            Assert.assertTrue(result.succeeded());
            //result.getForest().serialize("program-graph.xml");
            Assert.assertTrue(result.succeeded());
            Assert.assertEquals(1, result.getForest().getTotalParses());
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void testCss() {
        try {
            GrammarCompiler compiler = new GrammarCompiler();
            Grammar grammar = compiler.parse(Files.newInputStream(Paths.get("src/test/resources/css.cxml")), "css.cxml");
            GearleyParser parser = grammar.getParser(grammar.getNonterminal("$$"));
            GearleyResult result = parser.parse(Iterators.fileIterator("src/test/resources/css.inp"));
            Assert.assertTrue(result.succeeded());

            Assert.assertEquals(2, result.getForest().getTotalParses());
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void testAmbiguity() {
        Grammar grammar = new Grammar();

        NonterminalSymbol _letter = grammar.getNonterminal("letter");
        NonterminalSymbol _letterOrNumber = grammar.getNonterminal("letterOrNumber");
        NonterminalSymbol _expr = grammar.getNonterminal("expr");
        NonterminalSymbol _number = grammar.getNonterminal("number");

        grammar.addRule(_expr, TerminalSymbol.ch('x'), _letter, _letterOrNumber, _letter);
        grammar.addRule(_letter, TerminalSymbol.regex("[a-z]"));
        grammar.addRule(_number, TerminalSymbol.regex("[0-9]"));
        grammar.addRule(_letterOrNumber, _letter);
        grammar.addRule(_letterOrNumber, _number);

        grammar.addRule(_number, TerminalSymbol.ch('b'));

        GearleyParser parser = grammar.getParser(_expr);
        GearleyResult result = parser.parse(Iterators.characterIterator("xabb"));
        //result.getForest().serialize("ambiguity.xml");

        Assert.assertTrue(result.succeeded());
        Assert.assertEquals(2, result.getForest().getTotalParses());

        Assert.assertNotNull(result.getForest().parse());
        Assert.assertNotNull(result.getForest().parse());
        Assert.assertNull(result.getForest().parse());
        Assert.assertNotNull(result.getForest().parse());
        Assert.assertNotNull(result.getForest().parse());
        Assert.assertNull(result.getForest().parse());
    }

    @Test
    public void testAmbiguity2() {
        Grammar grammar = new Grammar();

        NonterminalSymbol _letter = grammar.getNonterminal("letter");
        NonterminalSymbol _letterOrNumber = grammar.getNonterminal("letterOrNumber");
        NonterminalSymbol _expr = grammar.getNonterminal("expr");
        NonterminalSymbol _number = grammar.getNonterminal("number");
        NonterminalSymbol _other = grammar.getNonterminal("other");

        grammar.addRule(_expr, _letter, _letterOrNumber, _letter, _letterOrNumber);
        grammar.addRule(_letter, TerminalSymbol.regex("[a-z]"));
        grammar.addRule(_number, TerminalSymbol.regex("[0-9]"));
        grammar.addRule(_other, TerminalSymbol.regex("[A-Z]"));
        grammar.addRule(_letterOrNumber, _letter);
        grammar.addRule(_letterOrNumber, _number);
        grammar.addRule(_letterOrNumber, _other);

        grammar.addRule(_number, TerminalSymbol.ch('b'));
        grammar.addRule(_other, TerminalSymbol.ch('b'));

        GearleyParser parser = grammar.getParser(_expr);
        GearleyResult result = parser.parse(Iterators.characterIterator("abab"));
        //result.getForest().serialize("ambiguity2.xml");

        Assert.assertTrue(result.succeeded());
        Assert.assertEquals(9, result.getForest().getTotalParses());
    }

    // I'm unconvinced that this grammar has more than one parse. It's infinitely
    // ambiguous, but I think the previous analysis that there were too parses
    // was faulty.
    @Ignore
    public void testAmbiguity3() {
        Grammar grammar = new Grammar();

        NonterminalSymbol _word = grammar.getNonterminal("word");
        NonterminalSymbol _letter = grammar.getNonterminal("letter");
        NonterminalSymbol _expr = grammar.getNonterminal("expr");

        grammar.addRule(_expr, _word);
        grammar.addRule(_letter, TerminalSymbol.regex("[a-z]"));
        grammar.addRule(_letter);
        grammar.addRule(_word, _letter, _word);
        grammar.addRule(_word);

        GearleyParser parser = grammar.getParser(_expr);
        GearleyResult result = parser.parse(Iterators.characterIterator("word"));

        //result.getForest().serialize("ambiguity3.xml");

        Assert.assertTrue(result.succeeded());
        Assert.assertTrue(result.getForest().isInfinitelyAmbiguous());
        Assert.assertEquals(2, result.getForest().getTotalParses());
    }
}
