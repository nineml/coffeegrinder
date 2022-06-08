package org.nineml.coffeegrinder;

import org.junit.Assert;
import org.junit.Test;
import org.nineml.coffeegrinder.parser.*;
import org.nineml.coffeegrinder.util.GrammarCompiler;
import org.nineml.coffeegrinder.util.Iterators;
import org.nineml.coffeegrinder.util.ParserAttribute;

import java.io.File;

import static junit.framework.TestCase.fail;

public class TreeTest {
    @Test
    public void testEmpty() {
        Grammar grammar = new Grammar();

        // S: a, b, c. a:. b:. c:.

        NonterminalSymbol S = grammar.getNonterminal("S");
        //NonterminalSymbol T = grammar.getNonterminal("T");
        NonterminalSymbol a = grammar.getNonterminal("a");
        NonterminalSymbol b = grammar.getNonterminal("b");
        NonterminalSymbol c = grammar.getNonterminal("c");

        //grammar.addRule(S, T);
        grammar.addRule(S, a, b, c);
        grammar.addRule(S);
        grammar.addRule(a);
        grammar.addRule(b);
        grammar.addRule(c);

        GearleyParser parser = grammar.getParser(S);
        GearleyResult result = parser.parse(Iterators.characterIterator(""));

        //result.getForest().serialize("/tmp/graph.xml");
        //result.getForest().parse().serialize("tree.xml");

        Assert.assertTrue(result.succeeded());
        Assert.assertEquals(2, result.getForest().getTotalParses());
    }

    @Test
    public void test1() {
        try {
            GrammarCompiler compiler = new GrammarCompiler();
            Grammar grammar = compiler.parse(new File("src/test/resources/ixml.cxml"));
            GearleyParser parser = grammar.getParser(grammar.getNonterminal("$$"));

            String input = "S:'x',e. e:.";
            GearleyResult result = parser.parse(Iterators.characterIterator(input));

            result.getForest().serialize("/tmp/graph.xml");
            //ParseTree tree = result.getForest().parse();
            //tree.serialize("tree.xml");

            Assert.assertTrue(result.succeeded());
            Assert.assertEquals(1, result.getForest().getTotalParses());
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void walker1() {
        try {
            // This example caused an error in the tree walker. It attempted to find a choice
            // for a node that had nothing but loops. Since loops are ignored, it attempted to
            // setup a choice when there were not choices.
            GrammarCompiler compiler = new GrammarCompiler();
            Grammar grammar = compiler.parse(new File("src/test/resources/property-file.cxml"));

            GearleyParser parser = grammar.getParser("$$");
            GearleyResult result = parser.parse(Iterators.fileIterator("src/test/resources/short-example.properties"));

            //result.getForest().serialize("/tmp/walker1.xml");

            Ambiguity ambiguity = result.getForest().getAmbiguity();

            Assert.assertTrue(ambiguity.getAmbiguous());

            ParseTree tree1 = result.getForest().parse();
            ParseTree tree2 = result.getForest().parse();

            Assert.assertNotNull(tree1);
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void testAmbiguous() {
        Grammar grammar = new Grammar();

        NonterminalSymbol S = grammar.getNonterminal("S");
        NonterminalSymbol A = grammar.getNonterminal("A");
        NonterminalSymbol d1 = grammar.getNonterminal("$1"); //, true);
        NonterminalSymbol d2 = grammar.getNonterminal("$2"); //, true);
        NonterminalSymbol d3 = grammar.getNonterminal("$3"); //, true);
        NonterminalSymbol d4 = grammar.getNonterminal("$4"); //, true);

        grammar.addRule(S, A);
        grammar.addRule(A, d1);
        grammar.addRule(A, d3);
        grammar.addRule(d1, d2);
        grammar.addRule(d1);
        grammar.addRule(d2, TerminalSymbol.ch('a'), d2);
        grammar.addRule(d3, d4);
        grammar.addRule(d3);
        grammar.addRule(d4, TerminalSymbol.ch('b'), d4);

        GearleyParser parser = grammar.getParser(S);
        GearleyResult result = parser.parse(Iterators.characterIterator(""));

        result.getForest().serialize("/tmp/graph.xml");
        //result.getForest().parse().serialize("tree.xml");

        Assert.assertTrue(result.succeeded());
        Assert.assertEquals(2, result.getForest().getTotalParses());
    }


}
