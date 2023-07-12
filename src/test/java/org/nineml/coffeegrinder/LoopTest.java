package org.nineml.coffeegrinder;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.nineml.coffeegrinder.parser.*;
import org.nineml.coffeegrinder.trees.StringTreeBuilder;
import org.nineml.coffeegrinder.trees.TreeSelector;

import java.util.List;
import java.util.Map;

public class LoopTest extends CoffeeGrinderTest {
    @Test
    public void longLoop() {
        SourceGrammar grammar = new SourceGrammar(new ParserOptions());

            /*
            S = X, A, Z.
            X = 'x'.
            Z = 'z'.
            A = A | 'y'.
             */

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _X = grammar.getNonterminal("X");
        NonterminalSymbol _Z = grammar.getNonterminal("Z");

        TerminalSymbol _x = TerminalSymbol.ch('x');
        TerminalSymbol _y = TerminalSymbol.ch('y');
        TerminalSymbol _z = TerminalSymbol.ch('z');

        grammar.addRule(_S, _X, _A, _Z);
        grammar.addRule(_X, _x);
        grammar.addRule(_Z, _z);
        grammar.addRule(_A, _A);
        grammar.addRule(_A, _y);

        try {
            GearleyParser parser = grammar.getParser(globalOptions, _S);
            GearleyResult result = parser.parse("xyz");

            TreeSelector loopingTreeSelector = new LoopingTreeSelector();
            StringTreeBuilder builder = new StringTreeBuilder();
            ForestWalker walker = result.getForest().getWalker(loopingTreeSelector);
            walker.getNextTree(builder);

            Assertions.assertTrue(result.getForest().isAmbiguous());
            Assertions.assertTrue(result.getForest().isInfinitelyAmbiguous());
            Assertions.assertEquals(2, result.getForest().getParseTreeCount());

            Assertions.assertEquals("<S><X>x</X><A><A><A><A><A><A><A><A><A><A>y</A></A></A></A></A></A></A></A></A></A><Z>z</Z></S>", builder.getTree());
        } catch (Exception ex) {
            Assertions.fail();
        }
    }

    private static class LoopingTreeSelector implements TreeSelector {
        private int count = 1;

        @Override
        public boolean getMadeAmbiguousChoice() {
            return false;
        }

        @Override
        public void startNonterminal(NonterminalSymbol symbol, Map<String, String> attributes, int leftExtent, int rightExtent) {
            // nop
        }

        @Override
        public void endNonterminal(NonterminalSymbol symbol, Map<String, String> attributes, int leftExtent, int rightExtent) {
            // nop
        }

        @Override
        public Family select(List<Family> choices, List<Family> otherChoices) {
            if (count < 10) {
                count++;
                return otherChoices.get(0);
            }
            return choices.get(0);
        }

        @Override
        public void reset() {
            // nop
        }
    }

}
