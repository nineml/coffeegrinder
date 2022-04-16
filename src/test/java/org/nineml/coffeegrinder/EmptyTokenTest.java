package org.nineml.coffeegrinder;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.nineml.coffeegrinder.parser.EarleyParser;
import org.nineml.coffeegrinder.parser.EarleyResult;
import org.nineml.coffeegrinder.parser.Grammar;
import org.nineml.coffeegrinder.parser.HygieneReport;
import org.nineml.coffeegrinder.parser.NonterminalSymbol;
import org.nineml.coffeegrinder.parser.ParseForest;
import org.nineml.coffeegrinder.parser.ParseTree;
import org.nineml.coffeegrinder.parser.Rule;
import org.nineml.coffeegrinder.parser.TerminalSymbol;
import org.nineml.coffeegrinder.tokens.TokenEmpty;
import org.nineml.coffeegrinder.util.ParserAttribute;

public class EmptyTokenTest {

    @Test
    public void betweenTest() {
        Grammar grammar = new Grammar();

        NonterminalSymbol S = grammar.getNonterminal("S");
        NonterminalSymbol A = grammar.getNonterminal("A");
        NonterminalSymbol B = grammar.getNonterminal("B");

        TerminalSymbol empty = new TerminalSymbol(TokenEmpty.get());

        Rule s1 = new Rule(S, A, empty, B);
        grammar.addRule(s1);

        grammar.addRule(A, TerminalSymbol.ch('a'));
        grammar.addRule(B, TerminalSymbol.ch('b'));

        grammar.close();

        HygieneReport report = grammar.checkHygiene(S);
        Assertions.assertTrue(report.isClean());

        EarleyParser parser = grammar.getParser(S);
        EarleyResult result = parser.parse("ab");
        Assertions.assertTrue(result.succeeded());
    }

    @Test
    public void betweenTest2() {
        Grammar grammar = new Grammar();

        NonterminalSymbol S = grammar.getNonterminal("S");
        NonterminalSymbol A = grammar.getNonterminal("A");
        NonterminalSymbol B = grammar.getNonterminal("B");

        TerminalSymbol empty1 = new TerminalSymbol(TokenEmpty.get(new ParserAttribute("empty", "one")));
        TerminalSymbol empty2 = new TerminalSymbol(TokenEmpty.get(new ParserAttribute("empty", "two")));

        Rule s1 = new Rule(S, A, empty1, empty2, B);
        grammar.addRule(s1);

        grammar.addRule(A, TerminalSymbol.ch('a'));
        grammar.addRule(B, TerminalSymbol.ch('b'));

        grammar.close();

        HygieneReport report = grammar.checkHygiene(S);
        Assertions.assertTrue(report.isClean());

        EarleyParser parser = grammar.getParser(S);
        EarleyResult result = parser.parse("ab");

        ParseTree tree = result.getForest().parse();

        Assertions.assertTrue(result.succeeded());
    }

}
