package org.nineml.coffeegrinder;

import org.junit.Assert;
import org.junit.Test;
import org.nineml.coffeegrinder.parser.EarleyParser;
import org.nineml.coffeegrinder.parser.EarleyResult;
import org.nineml.coffeegrinder.parser.Grammar;
import org.nineml.coffeegrinder.parser.NonterminalSymbol;
import org.nineml.coffeegrinder.util.GrammarCompiler;
import org.nineml.coffeegrinder.util.Iterators;

import java.io.File;

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


}
