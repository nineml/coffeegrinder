package org.nineml.coffeegrinder;

import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.nineml.coffeegrinder.parser.*;
import org.nineml.coffeegrinder.tokens.CharacterSet;
import org.nineml.coffeegrinder.tokens.Token;
import org.nineml.coffeegrinder.tokens.TokenCharacterSet;
import org.nineml.coffeegrinder.util.Iterators;
import org.nineml.coffeegrinder.util.ParserAttribute;
import org.nineml.coffeegrinder.util.GrammarParser;

import java.util.ArrayList;
import java.util.Iterator;

public class ParserTest {
    @Test
    public void ifThenElseTest() {
        Grammar grammar = new Grammar();

        NonterminalSymbol _statement = grammar.getNonterminal("statement");
        NonterminalSymbol _condition = grammar.getNonterminal("condition");
        TerminalSymbol _if = TerminalSymbol.s("if");
        TerminalSymbol _then = TerminalSymbol.s("then");
        TerminalSymbol _else = TerminalSymbol.s("else");
        TerminalSymbol _variable = TerminalSymbol.regex("[a-z]+");
        TerminalSymbol _eqeq = TerminalSymbol.s("==");
        TerminalSymbol _eq = TerminalSymbol.s("=");
        TerminalSymbol _op = TerminalSymbol.s("(");
        TerminalSymbol _cp = TerminalSymbol.s(")");

        grammar.addRule(_statement, _if, _condition, _then, _statement);
        grammar.addRule(_statement, _if, _condition, _then, _statement, _else, _statement);
        grammar.addRule(_statement, _variable, _eq, _variable);
        grammar.addRule(_condition, _op, _variable, _eqeq, _variable, _cp);
        EarleyParser parser = grammar.getParser(_statement);

        // N.B. this is the sequence of tokens, not characters.
        Iterator<Token> input = Iterators.stringIterator(
                "if", "(", "a", "==", "b", ")",
                "then", "if", "(", "c", "==", "d", ")",
                "then", "c", "=", "d",
                "else", "e", "=", "f");

        EarleyResult result = parser.parse(input);
        Assert.assertTrue(result.succeeded());
    }

    @Test
    public void testExpression() {
        // https://loup-vaillant.fr/tutorials/earley-parsing/parser
        Grammar grammar = new Grammar(new ParserOptions());

        NonterminalSymbol _Sum = grammar.getNonterminal("Sum");
        NonterminalSymbol _Product = grammar.getNonterminal("Product");
        NonterminalSymbol _Factor = grammar.getNonterminal("Factor");
        NonterminalSymbol _Number = grammar.getNonterminal("Number");

        TerminalSymbol _op = TerminalSymbol.ch('(');
        TerminalSymbol _cp = TerminalSymbol.ch(')');

        grammar.addRule(_Sum, _Sum, TerminalSymbol.regex("[+-]"), _Product);
        grammar.addRule(_Sum, _Product);
        grammar.addRule(_Product, _Product, TerminalSymbol.regex("[\\*/]"), _Factor);
        grammar.addRule(_Product, _Factor);
        grammar.addRule(_Factor, _op, _Sum, _cp);
        grammar.addRule(_Factor, _Number);
        grammar.addRule(_Number, TerminalSymbol.regex("[0-9]"), _Number);
        grammar.addRule(_Number, TerminalSymbol.regex("[0-9]"));

        String input = "1+(2*3-4)";

        EarleyParser parser = grammar.getParser(_Sum);
        EarleyResult result = parser.parse(input);
        Assert.assertTrue(result.succeeded());
    }

    @Test
    public void testLetterDigitLetter() {
        Grammar grammar = new Grammar();

        NonterminalSymbol _letter = grammar.getNonterminal("letter");
        NonterminalSymbol _letterOrNumber = grammar.getNonterminal("letterOrNumber");
        NonterminalSymbol _expr = grammar.getNonterminal("expr");
        NonterminalSymbol _number = grammar.getNonterminal("number");

        grammar.addRule(_expr, _letter, _letterOrNumber, _letter);
        grammar.addRule(_letter, TerminalSymbol.regex("[a-z]"));
        grammar.addRule(_number, TerminalSymbol.regex("[0-9a-z]"));
        grammar.addRule(_letterOrNumber, _letter);
        grammar.addRule(_letterOrNumber, _number);

        EarleyParser parser = grammar.getParser(_expr);
        EarleyResult result = parser.parse(Iterators.characterIterator("aab"));
        Assert.assertTrue(result.succeeded());
    }

    @Test
    public void testNumber() {
        GrammarParser gparser = new GrammarParser();
        Grammar grammar = gparser.parse(
                "number => 'b', digit\n" +
                        "digit => '0'\n" +
                        "digit => '1'\n" +
                        "digit => digit, digit\n" +
                        "digit => digit");
        // grammar.getParseListener().setMessageLevel(ParseListener.DEBUG);

        EarleyParser parser = grammar.getParser(grammar.getNonterminal("number"));
        EarleyResult result = parser.parse(Iterators.characterIterator("b101"));
        Assert.assertTrue(result.succeeded());
    }

    @Test
    public void testLongNumber() {
        Grammar grammar = new Grammar();

        NonterminalSymbol _digit = grammar.getNonterminal("digit");
        NonterminalSymbol _number = grammar.getNonterminal("number");

        grammar.addRule(_number, _digit);
        grammar.addRule(_digit, TerminalSymbol.ch('0'));
        grammar.addRule(_digit, TerminalSymbol.ch('1'));
        grammar.addRule(_digit, TerminalSymbol.ch('2'));
        grammar.addRule(_digit, TerminalSymbol.ch('3'));
        grammar.addRule(_digit, _digit, _digit);
        grammar.addRule(_digit);

        EarleyParser parser = grammar.getParser(_number);
        EarleyResult result = parser.parse(Iterators.characterIterator("123123123"));
        Assert.assertTrue(result.succeeded());
    }

    @Test
    public void testExample() {
        // https://web.stanford.edu/class/archive/cs/cs143/cs143.1128/lectures/07/Slides07.pdf
        Grammar grammar = new Grammar();

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _B = grammar.getNonterminal("B");
        NonterminalSymbol _C = grammar.getNonterminal("C");

        TerminalSymbol _a = TerminalSymbol.ch('a');
        TerminalSymbol _b = TerminalSymbol.ch('b');
        TerminalSymbol _d = TerminalSymbol.ch('d');

        grammar.addRule(_S, _A);
        grammar.addRule(_A, _B,_a);
        grammar.addRule(_A, _B, _b);
        grammar.addRule(_A, _C, _a, _b);
        grammar.addRule(_A, _A, _d);
        grammar.addRule(_B, _a);
        grammar.addRule(_C, _a);

        EarleyParser parser = grammar.getParser(_S);
        EarleyResult result = parser.parse(Iterators.characterIterator("aad"));

        Assert.assertTrue(result.succeeded());
    }

    @Test
    public void testEEE() {
        Grammar grammar = new Grammar();

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _E = grammar.getNonterminal("E");
        TerminalSymbol _plus = TerminalSymbol.ch('+');

        grammar.addRule(_S, _E);
        grammar.addRule(_E, _E, _plus, _E);
        grammar.addRule(_E, TerminalSymbol.regex("[0-9]"));

        EarleyParser parser = grammar.getParser(_S);
        EarleyResult result = parser.parse(Iterators.characterIterator("1+2+3"));
        //result.getForest().serialize("testEEE.xml");
        Assert.assertTrue(result.succeeded());
    }

    @Test
    public void testHighlyAmibiguous() {
        Grammar grammar = new Grammar();

        NonterminalSymbol _X = grammar.getNonterminal("X");
        TerminalSymbol _a = TerminalSymbol.ch('a');

        grammar.addRule(_X, _X, _X);
        grammar.addRule(_X, _a);

        EarleyParser parser = grammar.getParser(_X);
        EarleyResult result = parser.parse(Iterators.characterIterator("aaaaa"));
        Assert.assertTrue(result.succeeded());
    }

    @Test
    public void Saabbaa() {
        GrammarParser gparser = new GrammarParser();
        Grammar grammar = gparser.parse(
                "  S => A, C, 'a', B\n" +
                        "S => A, B, 'a', 'a'\n" +
                        "A => 'a', A\n" +
                        "A => 'a'\n" +
                        "B => 'b', B\n" +
                        "B => 'b'\n" +
                        "C => 'b', C\n" +
                        "C => 'b'");

        String input = "abaa";

        EarleyParser parser = grammar.getParser(grammar.getNonterminal("S"));
        EarleyResult result = parser.parse(Iterators.characterIterator(input));
        Assert.assertTrue(result.succeeded());
    }

    @Test
    public void empty() {
        GrammarParser gparser = new GrammarParser();
        Grammar grammar = gparser.parse(
                "  S => 'a'\n" +
                        "S => 'b'\n" +
                        "S =>");

        String input = "";

        EarleyParser parser = grammar.getParser(grammar.getNonterminal("S"));
        EarleyResult result = parser.parse(Iterators.characterIterator(input));
        Assert.assertTrue(result.succeeded());
    }

    @Test
    public void hash() {
        Grammar grammar = new Grammar();

        NonterminalSymbol _expr = grammar.getNonterminal("expr");
        NonterminalSymbol _letter = grammar.getNonterminal("letter");

        ArrayList<ParserAttribute> atts = new ArrayList<>();
        atts.add(ParserAttribute.PRUNING_ALLOWED);
        atts.add(Symbol.OPTIONAL);

        NonterminalSymbol _letterOrNumber = grammar.getNonterminal("letterOrNumber", atts);
        NonterminalSymbol _number = grammar.getNonterminal("number");

        grammar.addRule(_expr, _letter, _letterOrNumber, _letter);
        grammar.addRule(_letter, TerminalSymbol.regex("[a-z]"));
        grammar.addRule(_number, TerminalSymbol.regex("[0-9]"));
        grammar.addRule(_letterOrNumber, _letter);
        grammar.addRule(_letterOrNumber, _number);

        EarleyParser parser = grammar.getParser(_expr);
        EarleyResult result = parser.parse(Iterators.characterIterator("xx"));

        Assert.assertTrue(result.succeeded());
    }

    @Test
    public void hashGrammar() {
        // See also loadCompiledGrammar in CompilerTest

        Grammar grammar = new Grammar();

        // hashes: hash*S, ".".
        NonterminalSymbol hashes = grammar.getNonterminal("hashes");
        NonterminalSymbol hash = grammar.getNonterminal("hash");
        NonterminalSymbol S = grammar.getNonterminal("S");
        NonterminalSymbol s_hash = grammar.getNonterminal("_s_hash");
        grammar.addRule(hashes, hash, s_hash, TerminalSymbol.ch('.'));
        grammar.addRule(s_hash, S, hash, s_hash);
        grammar.addRule(s_hash);

        // hash: "#", d6.
        NonterminalSymbol d6 = grammar.getNonterminal("d6");
        TerminalSymbol octo = TerminalSymbol.ch('#');
        grammar.addRule(hash, octo, d6);

        // @d6: d, (d, (d, (d, (d, d?)?)?)?)?.
        NonterminalSymbol d_req = grammar.getNonterminal("d");
        NonterminalSymbol d_opt = grammar.getNonterminal("d", Symbol.OPTIONAL);
        NonterminalSymbol d56 = grammar.getNonterminal("_d56", Symbol.OPTIONAL);
        NonterminalSymbol d456 = grammar.getNonterminal("_d456", Symbol.OPTIONAL);
        NonterminalSymbol d3456 = grammar.getNonterminal("_d3456", Symbol.OPTIONAL);
        NonterminalSymbol d23456 = grammar.getNonterminal("_d23456", Symbol.OPTIONAL);
        grammar.addRule(d56, d_req, d_opt);
        grammar.addRule(d456, d_req, d56);
        grammar.addRule(d3456, d_req, d456);
        grammar.addRule(d23456, d_req, d3456);
        grammar.addRule(d6, d_req, d23456);

        // -d: ["0"-"9"].
        grammar.addRule(d_req, new TerminalSymbol(TokenCharacterSet.inclusion(CharacterSet.range('0', '9'))));

        // -S: " "+.
        NonterminalSymbol _s = grammar.getNonterminal("_s");
        TerminalSymbol space = TerminalSymbol.ch(' ');
        grammar.addRule(S, space, _s);
        grammar.addRule(_s, space, _s);
        grammar.addRule(_s);

        EarleyParser parser = grammar.getParser(hashes);
        String input = "#12  #1234.";

        EarleyResult result = parser.parse(input);

        Assert.assertEquals(1, result.getForest().getTotalParses());
        Assert.assertTrue(result.succeeded());

        /*
        GrammarCompiler compiler = new GrammarCompiler();
        compiler.setProperty("Source", "Invisible XML test suite");
        compiler.setProperty("Date", "2022-01-30");
        compiler.compile(grammar, "src/test/resources/hash.cxml");
         */
    }

    @Test
    public void words() {
        GrammarParser gparser = new GrammarParser();
        Grammar grammar = gparser.parse(
                "Word => consonant, vowel, consonant\n" +
                        "Word => consonant, vowel, vowel, consonant\n" +
                        "Word => consonant, vowel, consonant, vowel\n" +
                        "Word => consonant, consonant, vowel\n" +
                        "consonant => [\"b\"-\"d\"; \"f\"-\"h\"; \"j\"-\"n\"; \"p\"-\"t\"; \"v\"-\"z\"]\n" +
                        "vowel => [\"a\"; \"e\"; \"i\"; \"o\"; \"u\"; \"y\"]");

        String input = "why";

        EarleyParser parser = grammar.getParser(grammar.getNonterminal("Word"));
        EarleyResult result = parser.parse(Iterators.characterIterator(input));
        Assert.assertTrue(result.succeeded());
    }

    @Test
    public void docExample() {
        Grammar grammar = new Grammar();

        NonterminalSymbol S = grammar.getNonterminal("S");
        NonterminalSymbol A = grammar.getNonterminal("A");
        NonterminalSymbol B = grammar.getNonterminal("B");
        NonterminalSymbol X = grammar.getNonterminal("X");
        NonterminalSymbol Y = grammar.getNonterminal("Y");

        Rule s1 = new Rule(S, A);
        grammar.addRule(s1);

        grammar.addRule(S, B);
        grammar.addRule(A, TerminalSymbol.ch('a'), X);
        grammar.addRule(A, TerminalSymbol.ch('b'), X);
        grammar.addRule(B, TerminalSymbol.ch('b'), X);
        grammar.addRule(X, TerminalSymbol.ch('x'));
        grammar.addRule(Y, TerminalSymbol.ch('y'));

        grammar.close();

        HygieneReport report = grammar.checkHygiene(S);
        if (!report.isClean()) {
            // TODO: deal with undefined, unused, and unproductive items
        }

        EarleyParser parser = grammar.getParser(S);

        EarleyResult result = parser.parse("bx");

        if (result.succeeded()) {
            ParseForest forest = result.getForest();
            ParseTree tree = forest.parse();

            // TODO: do something with the tree.

            if (forest.isAmbiguous()) {
                long totalParses = forest.getTotalParses();
                // TODO: deal with multiple parses
            }
        } else {
            // TODO: deal with failure
        }

        Assertions.assertFalse(report.isClean());
        Assertions.assertTrue(result.succeeded());
    }

}
