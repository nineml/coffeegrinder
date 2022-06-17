package org.nineml.coffeegrinder;

import org.junit.Assert;
import org.junit.Test;
import org.nineml.coffeegrinder.parser.*;
import org.nineml.coffeegrinder.tokens.Token;
import org.nineml.coffeegrinder.tokens.TokenString;
import org.nineml.coffeegrinder.util.GrammarParser;
import org.nineml.coffeegrinder.util.Iterators;
import org.nineml.coffeegrinder.util.ParserAttribute;

import java.util.ArrayList;
import java.util.Objects;

import static org.junit.Assert.fail;

public class AttributesTest {
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
        GearleyParser parser = grammar.getParser(_statement);

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

        GearleyParser parser = grammar.getParser(grammar.getNonterminal("start"));
        GearleyResult result = parser.parse(Iterators.characterIterator("ab"));
        Assert.assertTrue(result.succeeded());
    }
}
