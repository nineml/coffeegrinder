package org.nineml.coffeegrinder;

import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.nineml.coffeegrinder.parser.*;
import org.nineml.logging.Logger;

import static org.junit.Assert.fail;

public class GrammarTest {
    private final ParserOptions options = new ParserOptions();

    @Test
    public void undefinedUnusedNonterminal() {
        SourceGrammar grammar = new SourceGrammar(options);

        /*
        S: A ; B
        A: 'a', X
        B: 'b', Y
        X: 'x'
        // Y is undefined
         */

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _B = grammar.getNonterminal("B");
        NonterminalSymbol _X = grammar.getNonterminal("X");
        NonterminalSymbol _Y = grammar.getNonterminal("Y");

        grammar.addRule(_S, _A);
        grammar.addRule(_S, _B);
        grammar.addRule(_A, TerminalSymbol.ch('a'), _X);
        grammar.addRule(_B, TerminalSymbol.ch('b'), _Y);
        grammar.addRule(_X, TerminalSymbol.ch('x'));

        HygieneReport report = grammar.getHygieneReport(_S);
        Assertions.assertFalse(report.isClean());
        Assertions.assertEquals(1, report.getUndefinedSymbols().size());
        Assertions.assertTrue(report.getUndefinedSymbols().contains(_Y));

        String input = "ax";

        GearleyParser parser = grammar.getParser(options, _S);
        GearleyResult result = parser.parse(input);
        Assert.assertTrue(result.succeeded());

        input = "by";
        result = parser.parse(input);
        Assert.assertFalse(result.succeeded());
    }

    @Test
    public void undefinedReferencedRequiredNonterminal() {
        SourceGrammar grammar = new SourceGrammar();

        /*
        S: A ; B
        A: 'a', X
        B: 'b', Y
        X: 'x'
        // Y is undefined
         */

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _B = grammar.getNonterminal("B");
        NonterminalSymbol _X = grammar.getNonterminal("X");
        NonterminalSymbol _Y = grammar.getNonterminal("Y");

        grammar.addRule(_S, _A);
        grammar.addRule(_S, _B);
        grammar.addRule(_A, TerminalSymbol.ch('a'), _X);
        grammar.addRule(_B, TerminalSymbol.ch('b'), _Y);
        grammar.addRule(_X, TerminalSymbol.ch('x'));

        HygieneReport report = grammar.getHygieneReport(_S);
        Assertions.assertFalse(report.isClean());
        Assertions.assertEquals(1, report.getUndefinedSymbols().size());
        Assertions.assertTrue(report.getUndefinedSymbols().contains(_Y));

        // This fails because B depends on Y so B is effectively absent
        String input = "bx";

        GearleyParser parser = grammar.getParser(options, _S);
        GearleyResult result = parser.parse(input);
        Assert.assertFalse(result.succeeded());
    }

    @Test
    public void undefinedReferencedUnnecessaryNonterminal() {
        SourceGrammar grammar = new SourceGrammar();

        /*
        S: A ; B
        A: 'a', X
        B: 'b', Y
        B: 'b', X
        X: 'x'
        // Y is undefined
         */

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _B = grammar.getNonterminal("B");
        NonterminalSymbol _X = grammar.getNonterminal("X");
        NonterminalSymbol _Y = grammar.getNonterminal("Y");

        grammar.addRule(_S, _A);
        grammar.addRule(_S, _B);
        grammar.addRule(_A, TerminalSymbol.ch('a'), _X);
        grammar.addRule(_B, TerminalSymbol.ch('b'), _Y);
        grammar.addRule(_B, TerminalSymbol.ch('b'), _X);
        grammar.addRule(_X, TerminalSymbol.ch('x'));

        HygieneReport report = grammar.getHygieneReport(_S);
        Assertions.assertFalse(report.isClean());
        Assertions.assertEquals(1, report.getUndefinedSymbols().size());
        Assertions.assertTrue(report.getUndefinedSymbols().contains(_Y));

        // This succeeds even though there's a B rule that uses Y which doesn't exist.
        String input = "bx";

        GearleyParser parser = grammar.getParser(options, _S);
        GearleyResult result = parser.parse(input);
        Assert.assertTrue(result.succeeded());
    }

    @Test
    public void unusedNonterminal() {
        SourceGrammar grammar = new SourceGrammar();

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

        HygieneReport report = grammar.getHygieneReport(_S);

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

        SourceGrammar grammar = new SourceGrammar();
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

        HygieneReport report = grammar.getHygieneReport(_S);

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

        SourceGrammar grammar = new SourceGrammar();
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

        HygieneReport report = grammar.getHygieneReport(_S);

        Assert.assertFalse(report.isClean());
        Assert.assertEquals(3, report.getUnproductiveRules().size());
        Assert.assertTrue(report.getUnproductiveSymbols().contains(_F));
        Assert.assertTrue(report.getUnproductiveSymbols().contains(_D));
        Assert.assertEquals(2, report.getUnproductiveSymbols().size());
        Assert.assertTrue(report.getUnreachableSymbols().isEmpty());
    }

    @Test
    public void checkMessages() {
        SourceGrammar grammar = new SourceGrammar();

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
        grammar.getParserOptions().setLogger(logger);

        HygieneReport report = grammar.getHygieneReport(_S);
        Assert.assertEquals(0, logger.warncount);

        ParserGrammar cgrammar = grammar.getCompiledGrammar(_S);
        report = cgrammar.getHygieneReport();
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
