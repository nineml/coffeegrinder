package org.nineml.coffeegrinder;

import org.junit.Assert;
import org.junit.Test;
import org.nineml.coffeegrinder.parser.*;
import org.nineml.coffeegrinder.tokens.Token;
import org.nineml.coffeegrinder.tokens.TokenString;
import org.nineml.coffeegrinder.util.GrammarParser;
import org.nineml.coffeegrinder.util.Iterators;
import org.nineml.coffeegrinder.util.NopTreeBuilder;
import org.nineml.coffeegrinder.util.ParserAttribute;

import java.util.ArrayList;
import java.util.Objects;

import static org.junit.Assert.fail;

public class AttributesTest {
    private final ParserOptions options = new ParserOptions();

    @Test
    public void ifThenElseTest() {
        SourceGrammar grammar = new SourceGrammar();

        NonterminalSymbol _statement = grammar.getNonterminal("statement");
        NonterminalSymbol _condition = grammar.getNonterminal("condition", new ParserAttribute("test", "this"));
        TerminalSymbol _if = new TerminalSymbol(TokenString.get("if"));
        TerminalSymbol _then = TerminalSymbol.s("then");
        TerminalSymbol _else = TerminalSymbol.s("else");
        TerminalSymbol _variable = TerminalSymbol.regex("[a-z]+");
        TerminalSymbol _eqeq = TerminalSymbol.s("==");
        TerminalSymbol _eq = TerminalSymbol.s("=");
        TerminalSymbol _op = new TerminalSymbol(TokenString.get("("), new ParserAttribute("open", "op"));
        TerminalSymbol _cp = TerminalSymbol.s(")");

        grammar.addRule(_statement, _if, _condition, _then, _statement);
        grammar.addRule(_statement, _if, _condition, _then, _statement, _else, _statement);
        grammar.addRule(_statement, _variable, _eq, _variable);
        grammar.addRule(_condition, _op, _variable, _eqeq, _variable, _cp);
        GearleyParser parser = grammar.getParser(options, _statement);

        ArrayList<ParserAttribute> attrs = new ArrayList<>();
        attrs.add(new ParserAttribute("line", "1"));
        attrs.add(new ParserAttribute("column", "5"));

        Token[] inputTokens = new Token[] {
                TokenString.get("if", attrs),
                TokenString.get("("),
                TokenString.get("a"),
                TokenString.get("=="),
                TokenString.get("b"),
                TokenString.get(")"),
                TokenString.get("then"),
                TokenString.get("c"),
                TokenString.get("="),
                TokenString.get("d")
        };

        GearleyResult result = parser.parse(inputTokens);
        Assert.assertTrue(result.succeeded());

        ParseTree tree = result.getForest().getTree();

        Token t_if = tree.getChildren().get(0).getToken();
        Assert.assertEquals("if", t_if.getValue());
        Assert.assertEquals("1", Objects.requireNonNull(t_if.getAttribute("line").getValue()));
        Assert.assertEquals("5", Objects.requireNonNull(t_if.getAttribute("column").getValue()));

        ParseTree nt_condition = tree.getChildren().get(1);
        Assert.assertEquals(grammar.getNonterminal("condition"), nt_condition.getSymbol());
        Assert.assertEquals("this", Objects.requireNonNull(nt_condition.getSymbol().getAttribute("test")).getValue());

        ParseTree s_paren = nt_condition.getChildren().get(0);
        Assert.assertEquals("(", s_paren.getToken().getValue());
        Assert.assertEquals("op", Objects.requireNonNull(s_paren.getAttribute("open", "fail")));
    }

    @Test
    public void testChoice() {
        GrammarParser gparser = new GrammarParser();
        SourceGrammar grammar = gparser.parse(
                "start => X\n" +
                        "X => A, Y\n" +
                        "A => 'a'\n" +
                        "B => 'b'\n" +
                        "C => 'c'\n" +
                        "Y => 'a'\n" +
                        "Y => 'b'\n" +
                        "Y => 'c'\n");
        // grammar.getParseListener().setMessageLevel(ParseListener.DEBUG);

        GearleyParser parser = grammar.getParser(options, grammar.getNonterminal("start"));
        GearleyResult result = parser.parse(Iterators.characterIterator("ab"));
        Assert.assertTrue(result.succeeded());
    }

    @Test
    public void testRHSAttributes() {
        SourceGrammar grammar = new SourceGrammar();

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _B0 = grammar.getNonterminal("B", new ParserAttribute("N", "0"));
        NonterminalSymbol _B1 = grammar.getNonterminal("B", new ParserAttribute("N", "1"));
        NonterminalSymbol _B2 = grammar.getNonterminal("B", new ParserAttribute("N", "2"));
        NonterminalSymbol _C = grammar.getNonterminal("C");
        NonterminalSymbol _D = grammar.getNonterminal("D");

        grammar.addRule(_S, _A, _B1, _C);
        grammar.addRule(_S, _A, _B2, _D);
        grammar.addRule(_A, TerminalSymbol.ch('a'));
        grammar.addRule(_B0, TerminalSymbol.ch('b'));
        grammar.addRule(_B1, TerminalSymbol.ch('b'));
        grammar.addRule(_B2, TerminalSymbol.ch('b'));
        grammar.addRule(_C, TerminalSymbol.ch('c'));
        grammar.addRule(_D, TerminalSymbol.ch('c'));

        GearleyParser parser = grammar.getParser(options, _S);
        GearleyResult result = parser.parse(Iterators.characterIterator("abc"));
        Assert.assertTrue(result.succeeded());

        TreeBuilder builder = new NopTreeBuilder();
        result.getTree(builder);
        Assert.assertEquals(2, builder.getRevealedParses());
    }
}
