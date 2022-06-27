package org.nineml.coffeegrinder;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.nineml.coffeegrinder.parser.*;
import org.nineml.coffeegrinder.util.GrammarCompiler;
import org.nineml.coffeegrinder.util.Iterators;
import org.nineml.coffeegrinder.util.NopTreeBuilder;
import org.nineml.coffeegrinder.util.StdoutTreeBuilder;

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
}
