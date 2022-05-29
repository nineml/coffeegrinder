package org.nineml.coffeegrinder;

import org.junit.Assert;
import org.junit.Test;
import org.nineml.coffeegrinder.parser.*;
import org.nineml.coffeegrinder.tokens.TokenCharacter;

import java.util.ArrayList;
import java.util.Collections;

public class RewriteTest {
    @Test
    public void optionalSymbol() {
        Grammar grammar = new Grammar();
        NonterminalSymbol S = grammar.getNonterminal("S");
        NonterminalSymbol A = grammar.getNonterminal("A", Symbol.OPTIONAL);
        NonterminalSymbol B = grammar.getNonterminal("B");
        grammar.addRule(S, A, B);

        Rule newRule = new Rule(S, B);
        Assert.assertFalse(grammar.contains(newRule));

        grammar.close(S);

        Assert.assertTrue(grammar.contains(newRule));
    }

    @Test
    public void optionalTerminalSymbol() {
        Grammar grammar = new Grammar();
        NonterminalSymbol S = grammar.getNonterminal("S");
        TerminalSymbol a = new TerminalSymbol(TokenCharacter.get('a'), Collections.singletonList(Symbol.OPTIONAL));
        NonterminalSymbol B = grammar.getNonterminal("B");
        grammar.addRule(S, a, B);

        Rule newRule = new Rule(S, B);
        Assert.assertFalse(grammar.contains(newRule));

        grammar.close(S);

        Assert.assertTrue(grammar.contains(newRule));
    }

    @Test
    public void optionalTerminalSymbols() {
        Grammar grammar = new Grammar();
        NonterminalSymbol S = grammar.getNonterminal("S");
        TerminalSymbol a = new TerminalSymbol(TokenCharacter.get('a'), Collections.singletonList(Symbol.OPTIONAL));
        TerminalSymbol b = new TerminalSymbol(TokenCharacter.get('b'), Collections.singletonList(Symbol.OPTIONAL));
        TerminalSymbol c = new TerminalSymbol(TokenCharacter.get('c'), Collections.singletonList(Symbol.OPTIONAL));
        grammar.addRule(S, a, b, c);

        ArrayList<Rule> rules = new ArrayList<>();
        rules.add(new Rule(S, b, c));
        rules.add(new Rule(S, b));
        rules.add(new Rule(S, c));
        rules.add(new Rule(S, a, c));
        rules.add(new Rule(S, a));
        rules.add(new Rule(S, c));
        rules.add(new Rule(S, a, b));
        rules.add(new Rule(S, a));
        rules.add(new Rule(S, b));

        for (Rule rule : rules) {
            Assert.assertFalse(grammar.contains(rule));
        }

        grammar.close(S);

        for (Rule rule : rules) {
            Assert.assertTrue(grammar.contains(rule));
        }

        Assert.assertTrue(grammar.isNullable(S));
    }

    @Test
    public void optionalSymbols() {
        Grammar grammar = new Grammar();
        NonterminalSymbol S = grammar.getNonterminal("S");
        NonterminalSymbol A = grammar.getNonterminal("A", Symbol.OPTIONAL);
        NonterminalSymbol C = grammar.getNonterminal("C", Symbol.OPTIONAL);
        TerminalSymbol b = TerminalSymbol.ch('b');
        grammar.addRule(S, A, b, C);

        Rule newRule1 = new Rule(S, b, C);
        Rule newRule2 = new Rule(S, A, b);
        Rule newRule3 = new Rule(S, b);
        Assert.assertFalse(grammar.contains(newRule1));
        Assert.assertFalse(grammar.contains(newRule2));
        Assert.assertFalse(grammar.contains(newRule3));

        grammar.close(S);

        Assert.assertTrue(grammar.contains(newRule1));
        Assert.assertTrue(grammar.contains(newRule2));
        Assert.assertTrue(grammar.contains(newRule3));
    }

    @Test
    public void optionalNullableSymbol() {
        Grammar grammar = new Grammar();
        NonterminalSymbol S = grammar.getNonterminal("S");
        NonterminalSymbol A = grammar.getNonterminal("A", Symbol.OPTIONAL);
        NonterminalSymbol B = grammar.getNonterminal("B");
        grammar.addRule(S, A, B);
        grammar.addRule(A);

        Rule newRule = new Rule(S, B);
        Assert.assertFalse(grammar.contains(newRule));

        grammar.close(S);

        Assert.assertFalse(grammar.contains(newRule));
    }

    @Test
    public void optionalNullableSymbols() {
        Grammar grammar = new Grammar();
        NonterminalSymbol S = grammar.getNonterminal("S");
        NonterminalSymbol A = grammar.getNonterminal("A", Symbol.OPTIONAL);
        NonterminalSymbol C = grammar.getNonterminal("C", Symbol.OPTIONAL);
        TerminalSymbol b = TerminalSymbol.ch('b');
        grammar.addRule(S, A, b, C);
        grammar.addRule(A);

        Rule newRule1 = new Rule(S, b, C);
        Rule newRule2 = new Rule(S, A, b);
        Rule newRule3 = new Rule(S, b);
        Assert.assertFalse(grammar.contains(newRule1));
        Assert.assertFalse(grammar.contains(newRule2));
        Assert.assertFalse(grammar.contains(newRule3));

        grammar.close(S);

        Assert.assertFalse(grammar.contains(newRule1));
        Assert.assertTrue(grammar.contains(newRule2));
        Assert.assertFalse(grammar.contains(newRule3));
    }

    @Test
    public void optionalBothNullableSymbols() {
        Grammar grammar = new Grammar();
        NonterminalSymbol S = grammar.getNonterminal("S");
        NonterminalSymbol A = grammar.getNonterminal("A", Symbol.OPTIONAL);
        NonterminalSymbol C = grammar.getNonterminal("C", Symbol.OPTIONAL);
        TerminalSymbol b = TerminalSymbol.ch('b');
        grammar.addRule(S, A, b, C);
        grammar.addRule(A);
        grammar.addRule(C);

        Rule newRule1 = new Rule(S, b, C);
        Rule newRule2 = new Rule(S, A, b);
        Rule newRule3 = new Rule(S, b);
        Assert.assertFalse(grammar.contains(newRule1));
        Assert.assertFalse(grammar.contains(newRule2));
        Assert.assertFalse(grammar.contains(newRule3));

        grammar.close(S);

        Assert.assertFalse(grammar.contains(newRule1));
        Assert.assertFalse(grammar.contains(newRule2));
        Assert.assertFalse(grammar.contains(newRule3));
    }

}
