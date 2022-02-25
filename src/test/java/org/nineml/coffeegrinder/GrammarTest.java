package org.nineml.coffeegrinder;

import org.junit.Assert;
import org.junit.Test;
import org.nineml.coffeegrinder.parser.*;
import org.nineml.logging.Logger;

public class GrammarTest {

    @Test
    public void unusedNonterminal() {
        Grammar grammar = new Grammar();

        /*
        S: A
        A: 'a'
        B: 'b'
         */

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _B = grammar.getNonterminal("B");

        grammar.addRule(_S, _A);
        grammar.addRule(_A, TerminalSymbol.ch('a'));
        grammar.addRule(_B, TerminalSymbol.ch('b'));
        grammar.close();

        HygieneReport report = grammar.checkHygiene(_S);

        Assert.assertFalse(report.isClean());
        Assert.assertEquals(0, report.getUnproductiveRules().size());
        Assert.assertEquals(1, report.getUnreachableSymbols().size());
        Assert.assertTrue(report.getUnreachableSymbols().contains(_B));
    }

    @Test
    public void unproductiveNonterminals() {
        // https://zerobone.net/blog/cs/non-productive-cfg-rules/
        /*
         S → H∣C∣XE∣XEGb
         C → D
         D → ε∣aF∣aSb∣S
         H → bF∣H
         F → Fa
         E → ab∣G
         G → aG
         X → a∣b∣Y
         Y → a∣X
         */

        Grammar grammar = new Grammar();
        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _C = grammar.getNonterminal("C");
        NonterminalSymbol _D = grammar.getNonterminal("D");
        NonterminalSymbol _H = grammar.getNonterminal("H");
        NonterminalSymbol _F = grammar.getNonterminal("F");
        NonterminalSymbol _E = grammar.getNonterminal("E");
        NonterminalSymbol _G = grammar.getNonterminal("G");
        NonterminalSymbol _X = grammar.getNonterminal("X");
        NonterminalSymbol _Y = grammar.getNonterminal("Y");
        TerminalSymbol _a = TerminalSymbol.ch('a');
        TerminalSymbol _b = TerminalSymbol.ch('b');

        grammar.addRule(_S, _H);
        grammar.addRule(_S, _C);
        grammar.addRule(_S, _X, _E);
        grammar.addRule(_S, _X, _E, _G, _b);
        grammar.addRule(_C, _D);
        grammar.addRule(_D);
        grammar.addRule(_D, _a, _F);
        grammar.addRule(_D, _a, _S, _b);
        grammar.addRule(_D, _S);
        grammar.addRule(_H, _b, _F);
        grammar.addRule(_H, _H);
        grammar.addRule(_F, _F, _a);
        grammar.addRule(_E, _a, _b);
        grammar.addRule(_E, _G);
        grammar.addRule(_G, _a, _G);
        grammar.addRule(_X, _a);
        grammar.addRule(_X, _b);
        grammar.addRule(_X, _Y);
        grammar.addRule(_Y, _a);
        grammar.addRule(_Y, _X);

        HygieneReport report = grammar.checkHygiene(_S);

        Assert.assertFalse(report.isClean());
        Assert.assertEquals(8, report.getUnproductiveRules().size());
        Assert.assertTrue(report.getUnproductiveSymbols().contains(_F));
        Assert.assertTrue(report.getUnproductiveSymbols().contains(_G));
        Assert.assertTrue(report.getUnproductiveSymbols().contains(_H));
        Assert.assertEquals(3, report.getUnproductiveSymbols().size());
        Assert.assertTrue(report.getUnreachableSymbols().isEmpty());
    }

    @Test
    public void unproductiveNonterminals2() {
        // https://slidetodoc.com/how-to-find-and-remove-unproductive-rules-in/
        /*
         S → A B | D E
         A → a
         B → b C
         C → c
         D → d F
         E → e
         F → f D
         */

        Grammar grammar = new Grammar();
        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _B = grammar.getNonterminal("B");
        NonterminalSymbol _C = grammar.getNonterminal("C");
        NonterminalSymbol _D = grammar.getNonterminal("D");
        NonterminalSymbol _E = grammar.getNonterminal("E");
        NonterminalSymbol _F = grammar.getNonterminal("F");
        TerminalSymbol _a = TerminalSymbol.ch('a');
        TerminalSymbol _b = TerminalSymbol.ch('b');
        TerminalSymbol _c = TerminalSymbol.ch('c');
        TerminalSymbol _d = TerminalSymbol.ch('d');
        TerminalSymbol _e = TerminalSymbol.ch('e');
        TerminalSymbol _f = TerminalSymbol.ch('f');

        grammar.addRule(_S, _A, _B);
        grammar.addRule(_S, _D, _E);
        grammar.addRule(_A, _a);
        grammar.addRule(_B, _b, _C);
        grammar.addRule(_C, _c);
        grammar.addRule(_D, _d, _F);
        grammar.addRule(_E, _e);
        grammar.addRule(_F, _f, _D);

        HygieneReport report = grammar.checkHygiene(_S);

        Assert.assertFalse(report.isClean());
        Assert.assertEquals(3, report.getUnproductiveRules().size());
        Assert.assertTrue(report.getUnproductiveSymbols().contains(_F));
        Assert.assertTrue(report.getUnproductiveSymbols().contains(_D));
        Assert.assertEquals(2, report.getUnproductiveSymbols().size());
        Assert.assertTrue(report.getUnreachableSymbols().isEmpty());
    }

    @Test
    public void checkMessages() {
        Grammar grammar = new Grammar();

        /*
        S: A
        A: 'a'
        B: 'b'
         */

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _B = grammar.getNonterminal("B");

        grammar.addRule(_S, _A);
        grammar.addRule(_A, TerminalSymbol.ch('a'));
        grammar.addRule(_B, TerminalSymbol.ch('b'));

        TestLogger logger = new TestLogger();
        grammar.getParserOptions().logger = logger;

        HygieneReport report = grammar.checkHygiene(_S);
        Assert.assertEquals(0, logger.warncount);

        grammar.close();

        report = grammar.checkHygiene(_S);
        Assert.assertEquals(1, logger.warncount);

        Assert.assertFalse(report.isClean());
    }

    private static class TestLogger extends Logger {
        public int warncount = 0;

        @Override
        public void error(String category, String format, Object... params) {
            // nop
        }

        @Override
        public void warn(String category, String format, Object... params) {
            warncount++;
        }

        @Override
        public void info(String category, String format, Object... params) {
            // nop
        }

        @Override
        public void debug(String category, String format, Object... params) {
            // nop
        }

        @Override
        public void trace(String category, String format, Object... params) {
            // nop
        }
    }
}