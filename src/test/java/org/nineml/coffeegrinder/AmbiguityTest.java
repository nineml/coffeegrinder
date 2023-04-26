package org.nineml.coffeegrinder;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.nineml.coffeegrinder.parser.*;
import org.nineml.coffeegrinder.util.*;

import java.nio.file.Files;
import java.nio.file.Paths;

import static junit.framework.TestCase.fail;

public class AmbiguityTest {
    private final ParserOptions options = new ParserOptions();

    @Test
    public void testProgram() {
        try {
            GrammarCompiler compiler = new GrammarCompiler();
            SourceGrammar grammar = compiler.parse(Files.newInputStream(Paths.get("src/test/resources/program.cxml")), "program.cxml");
            GearleyParser parser = grammar.getParser(options, grammar.getNonterminal("$$"));
            GearleyResult result = parser.parse(Iterators.fileIterator("src/test/resources/program.inp"));
            Assert.assertTrue(result.succeeded());
            //result.getForest().serialize("program-graph.xml");
            Assert.assertTrue(result.succeeded());
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void testCss() {
        try {
            GrammarCompiler compiler = new GrammarCompiler();
            SourceGrammar grammar = compiler.parse(Files.newInputStream(Paths.get("src/test/resources/css.cxml")), "css.cxml");
            GearleyParser parser = grammar.getParser(options, grammar.getNonterminal("$$"));
            GearleyResult result = parser.parse(Iterators.fileIterator("src/test/resources/css.inp"));
            Assert.assertTrue(result.succeeded());

            Assert.assertTrue(result.isAmbiguous());
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void testAmbiguity() {
        SourceGrammar grammar = new SourceGrammar();

        NonterminalSymbol _letter1 = grammar.getNonterminal("letter");
        _letter1.addAttribute(new ParserAttribute("L", "1"));
        NonterminalSymbol _letter2 = grammar.getNonterminal("letter");
        _letter2.addAttribute(new ParserAttribute("L", "2"));
        NonterminalSymbol _letterOrNumber = grammar.getNonterminal("letterOrNumber");
        NonterminalSymbol _expr = grammar.getNonterminal("expr");
        NonterminalSymbol _number1 = grammar.getNonterminal("number");
        _number1.addAttribute(new ParserAttribute("N", "1"));
        NonterminalSymbol _number2 = grammar.getNonterminal("number");
        _number2.addAttribute(new ParserAttribute("N", "2"));
        NonterminalSymbol _number3 = grammar.getNonterminal("number");
        _number3.addAttribute(new ParserAttribute("N", "3"));

        grammar.addRule(_expr, TerminalSymbol.ch('x'), _letter1, _letterOrNumber, _letter1);
        grammar.addRule(_letter1, TerminalSymbol.regex("[a-z]"));
        grammar.addRule(_letter2, TerminalSymbol.regex("[a-z]"));
        grammar.addRule(_number1, TerminalSymbol.regex("[0-9]"));
        grammar.addRule(_number2, TerminalSymbol.regex("[0-9]"));
        grammar.addRule(_letterOrNumber, _letter2);
        grammar.addRule(_letterOrNumber, _number2);

        grammar.addRule(_number3, TerminalSymbol.ch('b'));

        GearleyParser parser = grammar.getParser(options, _expr);
        GearleyResult result = parser.parse(Iterators.characterIterator("xabb"));
        //result.getForest().serialize("ambiguity.xml");

        Assert.assertTrue(result.succeeded());

        TreeBuilder builder = new NopTreeBuilder();
        result.getTree(builder);
        Assert.assertEquals(2, builder.getRevealedParses());
    }

    @Test
    public void testAmbiguity2() {
        SourceGrammar grammar = new SourceGrammar();

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

        GearleyParser parser = grammar.getParser(options, _expr);
        GearleyResult result = parser.parse(Iterators.characterIterator("abab"));
        //result.getForest().serialize("ambiguity2.xml");

        Assert.assertTrue(result.succeeded());

        TreeBuilder builder = new NopTreeBuilder();
        result.getTree(builder);
        Assert.assertEquals(6, builder.getRevealedParses());

        ParseTree tree = result.getForest().getTree();
        Assert.assertNotNull(tree);
    }

    // I'm unconvinced that this grammar has more than one parse. It's infinitely
    // ambiguous, but I think the previous analysis that there were too parses
    // was faulty.
    @Ignore
    public void testAmbiguity3() {
        SourceGrammar grammar = new SourceGrammar();

        NonterminalSymbol _word = grammar.getNonterminal("word");
        NonterminalSymbol _letter = grammar.getNonterminal("letter");
        NonterminalSymbol _expr = grammar.getNonterminal("expr");

        grammar.addRule(_expr, _word);
        grammar.addRule(_letter, TerminalSymbol.regex("[a-z]"));
        grammar.addRule(_letter);
        grammar.addRule(_word, _letter, _word);
        grammar.addRule(_word);

        GearleyParser parser = grammar.getParser(options, _expr);
        GearleyResult result = parser.parse(Iterators.characterIterator("word"));

        //result.getForest().serialize("ambiguity3.xml");

        Assert.assertTrue(result.succeeded());
        Assert.assertTrue(result.getForest().isInfinitelyAmbiguous());

        TreeBuilder builder = new NopTreeBuilder();
        result.getTree(builder);
        Assert.assertEquals(2, builder.getRevealedParses());
    }

    @Test
    public void testAmbiguity4() {
        SourceGrammar grammar = new SourceGrammar();

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _B = grammar.getNonterminal("B");
        NonterminalSymbol _C = grammar.getNonterminal("C");

        grammar.addRule(_S, _A);
        grammar.addRule(_S, _B);
        grammar.addRule(_S, _C);
        grammar.addRule(_A, TerminalSymbol.ch('a'));
        grammar.addRule(_B, TerminalSymbol.ch('a'));
        grammar.addRule(_C, TerminalSymbol.ch('a'));

        GearleyParser parser = grammar.getParser(options, _S);
        GearleyResult result = parser.parse(Iterators.characterIterator("a"));

        Assert.assertTrue(result.succeeded());

        TreeBuilder builder = new NopTreeBuilder();
        result.getTree(builder);
        Assert.assertEquals(3, builder.getRevealedParses());
    }
}
