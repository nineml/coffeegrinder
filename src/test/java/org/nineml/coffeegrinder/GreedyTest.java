package org.nineml.coffeegrinder;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.nineml.coffeegrinder.trees.StdoutTreeBuilder;
import org.nineml.coffeegrinder.trees.TreeBuilder;
import org.nineml.coffeegrinder.parser.*;
import org.nineml.coffeegrinder.tokens.TokenCharacter;
import org.nineml.coffeegrinder.util.ParserAttribute;

public class GreedyTest {
    private final ParserOptions options = new ParserOptions();

    @Before
    public void before() {
        options.setParserType("GLL");
    }

    @Ignore
    public void testABC() {
        SourceGrammar grammar = new SourceGrammar();

        NonterminalSymbol S = grammar.getNonterminal("S");
        NonterminalSymbol A = grammar.getNonterminal("A");
        NonterminalSymbol B = grammar.getNonterminal("B");
        NonterminalSymbol C = grammar.getNonterminal("C");
        NonterminalSymbol bplus = grammar.getNonterminal("bplus");
        NonterminalSymbol bstar = grammar.getNonterminal("bstar");
        NonterminalSymbol boption = grammar.getNonterminal("boption");
        TerminalSymbol a = new TerminalSymbol(TokenCharacter.get('a'));
        TerminalSymbol b = new TerminalSymbol(TokenCharacter.get('b'));
        TerminalSymbol c = new TerminalSymbol(TokenCharacter.get('c'));
        TerminalSymbol d = new TerminalSymbol(TokenCharacter.get('d'));

        grammar.addRule(S, A, B, C);
        grammar.addRule(A, a);
        grammar.addRule(C, c);
        /*
        grammar.addRule(B, b);
        grammar.addRule(B, b, B);
         */
        grammar.addRule(B, bplus);
        grammar.addRule(bplus, b, boption);
        grammar.addRule(bstar, boption);
        grammar.addRule(boption);
        grammar.addRule(boption, b, bstar);

        //options.getLogger().setDefaultLogLevel(99);
        GearleyParser parser = grammar.getParser(options, S);
        GearleyResult result = parser.parse("abbbbbc");
        Assert.assertTrue(result.succeeded());

        TreeBuilder builder = new StdoutTreeBuilder();
        result.getTree(builder);
    }

    @Ignore
    public void testABorC() {
        SourceGrammar grammar = new SourceGrammar();

        NonterminalSymbol S = grammar.getNonterminal("S");
        NonterminalSymbol A = grammar.getNonterminal("A");
        NonterminalSymbol B = grammar.getNonterminal("B");
        NonterminalSymbol altplus = grammar.getNonterminal("altplus");
        NonterminalSymbol alt = grammar.getNonterminal("alt");
        NonterminalSymbol altstar = grammar.getNonterminal("altstar");
        NonterminalSymbol altoption = grammar.getNonterminal("altoption");
        TerminalSymbol a = new TerminalSymbol(TokenCharacter.get('a'));
        TerminalSymbol b = new TerminalSymbol(TokenCharacter.get('b'));
        TerminalSymbol c = new TerminalSymbol(TokenCharacter.get('c'));

        grammar.addRule(S, A, B);
        grammar.addRule(A, b, c);
        grammar.addRule(B, altplus);
        grammar.addRule(alt, b);
        grammar.addRule(alt, c);
        grammar.addRule(altplus, alt, altstar);
        grammar.addRule(altstar, altoption);
        grammar.addRule(altoption);
        grammar.addRule(altoption, alt, altstar);

        //options.getLogger().setDefaultLogLevel(99);
        GearleyParser parser = grammar.getParser(options, S);
        GearleyResult result = parser.parse("abbbbbc");
        Assert.assertTrue(result.succeeded());

        TreeBuilder builder = new StdoutTreeBuilder();
        result.getTree(builder);
    }
}
