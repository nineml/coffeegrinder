package org.nineml.coffeegrinder;

import org.junit.Assert;
import org.junit.Test;
import org.nineml.coffeegrinder.parser.*;
import org.nineml.coffeegrinder.util.GrammarCompiler;
import org.nineml.coffeegrinder.util.Iterators;
import org.nineml.coffeegrinder.util.NodeChoices;

import java.io.File;
import java.util.Map;

import static junit.framework.TestCase.fail;

public class TreeTest {
    @Test
    public void testEmpty() {
        Grammar grammar = new Grammar();

        // S: a, b, c. a:. b:. c:.

        NonterminalSymbol S = grammar.getNonterminal("S");
        NonterminalSymbol a = grammar.getNonterminal("a");
        NonterminalSymbol b = grammar.getNonterminal("b");
        NonterminalSymbol c = grammar.getNonterminal("c");

        grammar.addRule(S, a, b, c);
        grammar.addRule(a);
        grammar.addRule(b);
        grammar.addRule(c);

        EarleyParser parser = grammar.getParser(S);
        EarleyResult result = parser.parse(Iterators.characterIterator(""));

        //result.getForest().serialize("graph.xml");
        //result.getForest().parse().serialize("tree.xml");

        Assert.assertTrue(result.succeeded());
        Assert.assertEquals(1, result.getForest().getTotalParses());
    }

    @Test
    public void test1() {
        try {
            GrammarCompiler compiler = new GrammarCompiler();
            Grammar grammar = compiler.parse(new File("src/test/resources/ixml.cxml"));
            EarleyParser parser = grammar.getParser(grammar.getNonterminal("$$"));

            String input = "ixml:'x',empty.empty:.";
            EarleyResult result = parser.parse(Iterators.characterIterator(input));

            //result.getForest().serialize("graph.xml");
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

            EarleyParser parser = grammar.getParser("$$");
            EarleyResult result = parser.parse(Iterators.fileIterator("src/test/resources/short-example.properties"));

            Ambiguity ambiguity = result.getForest().getAmbiguity();

            Assert.assertTrue(ambiguity.getAmbiguous());

            ParseTree tree1 = result.getForest().parse();
            ParseTree tree2 = result.getForest().parse();

            Assert.assertNotNull(tree1);
        } catch (Exception ex) {
            fail();
        }
    }

}
