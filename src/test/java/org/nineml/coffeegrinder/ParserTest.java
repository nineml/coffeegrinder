package org.nineml.coffeegrinder;

import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.nineml.coffeegrinder.gll.GllParser;
import org.nineml.coffeegrinder.gll.GllResult;
import org.nineml.coffeegrinder.parser.*;
import org.nineml.coffeegrinder.tokens.*;
import org.nineml.coffeegrinder.util.Iterators;
import org.nineml.coffeegrinder.util.ParserAttribute;
import org.nineml.coffeegrinder.util.GrammarParser;

import java.util.ArrayList;
import java.util.Calendar;
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
        GearleyParser parser = grammar.getParser(_statement);

        // N.B. this is the sequence of tokens, not characters.
        Iterator<Token> input = Iterators.stringIterator(
                "if", "(", "a", "==", "b", ")",
                "then", "if", "(", "c", "==", "d", ")",
                "then", "c", "=", "d",
                "else", "e", "=", "f");

        GearleyResult result = parser.parse(input);
        Assert.assertTrue(result.succeeded());
    }

    @Test
    public void testExpression() {
        // https://loup-vaillant.fr/tutorials/earley-parsing/parser
        org.nineml.coffeegrinder.parser.Grammar grammar = new org.nineml.coffeegrinder.parser.Grammar(new ParserOptions());

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

        GearleyParser parser = grammar.getParser(ParserType.Earley, _Sum);
        GearleyResult result = parser.parse(input);
        Assert.assertTrue(result.succeeded());

        //result.getForest().serialize("/tmp/earley.xml");

        GearleyParser gparser = grammar.getParser(ParserType.GLL, _Sum);
        GearleyResult gresult = parser.parse(input);
        Assert.assertTrue(gresult.succeeded());
    }

    @Test
    public void testSimplifiedExpression() {
        // https://loup-vaillant.fr/tutorials/earley-parsing/parser
        org.nineml.coffeegrinder.parser.Grammar grammar = new org.nineml.coffeegrinder.parser.Grammar(new ParserOptions());

        NonterminalSymbol _Sum = grammar.getNonterminal("Sum");
        NonterminalSymbol _Product = grammar.getNonterminal("Product");
        NonterminalSymbol _Factor = grammar.getNonterminal("Factor");
        NonterminalSymbol _Number = grammar.getNonterminal("Number");
        NonterminalSymbol _Digit = grammar.getNonterminal("Digit");

        TerminalSymbol _op = TerminalSymbol.ch('(');
        TerminalSymbol _cp = TerminalSymbol.ch(')');

        grammar.addRule(_Sum, _Sum, TerminalSymbol.ch('+'), _Product);
        grammar.addRule(_Sum, _Product);
        grammar.addRule(_Product, _Product, TerminalSymbol.ch('*'), _Factor);
        grammar.addRule(_Product, _Factor);
        grammar.addRule(_Factor, _op, _Sum, _cp);
        grammar.addRule(_Factor, _Number);
        grammar.addRule(_Number, _Digit, _Number);
        grammar.addRule(_Number, _Digit);
        grammar.addRule(_Digit, TerminalSymbol.ch('1'));

        String input = "1+(1*1+1)";

        GearleyParser parser = grammar.getParser(ParserType.Earley, _Sum);
        GearleyResult result = parser.parse(input);

        //result.getForest().serialize("/tmp/earley.xml");

        Assert.assertTrue(result.succeeded());

        GearleyParser gparser = grammar.getParser(ParserType.GLL, _Sum);
        GearleyResult gresult = parser.parse(input);

        Assert.assertTrue(gresult.succeeded());
    }

    @Test
    public void testLetterDigitLetter() {
        org.nineml.coffeegrinder.parser.Grammar grammar = new org.nineml.coffeegrinder.parser.Grammar();

        NonterminalSymbol _letter = grammar.getNonterminal("letter");
        NonterminalSymbol _letterOrNumber = grammar.getNonterminal("letterOrNumber");
        NonterminalSymbol _expr = grammar.getNonterminal("expr");
        NonterminalSymbol _number = grammar.getNonterminal("number");

        grammar.addRule(_expr, _letter, _letterOrNumber, _letter);
        grammar.addRule(_letter, TerminalSymbol.regex("[a-z]"));
        grammar.addRule(_number, TerminalSymbol.regex("[0-9a-z]"));
        grammar.addRule(_letterOrNumber, _letter);
        grammar.addRule(_letterOrNumber, _number);

        GearleyParser parser = grammar.getParser(_expr);
        GearleyResult result = parser.parse(Iterators.characterIterator("aab"));
        Assert.assertTrue(result.succeeded());
    }

    @Test
    public void testNumber() {
        GrammarParser gparser = new GrammarParser();
        org.nineml.coffeegrinder.parser.Grammar grammar = gparser.parse(
                "number => 'b', digit\n" +
                        "digit => '0'\n" +
                        "digit => '1'\n" +
                        "digit => digit, digit\n" +
                        "digit => digit");
        // grammar.getParseListener().setMessageLevel(ParseListener.DEBUG);

        GearleyParser parser = grammar.getParser(grammar.getNonterminal("number"));
        GearleyResult result = parser.parse(Iterators.characterIterator("b101"));
        Assert.assertTrue(result.succeeded());
    }

    @Test
    public void testLongNumber() {
        org.nineml.coffeegrinder.parser.Grammar grammar = new org.nineml.coffeegrinder.parser.Grammar();

        NonterminalSymbol _digit = grammar.getNonterminal("digit");
        NonterminalSymbol _number = grammar.getNonterminal("number");

        grammar.addRule(_number, _digit);
        grammar.addRule(_digit, TerminalSymbol.ch('0'));
        grammar.addRule(_digit, TerminalSymbol.ch('1'));
        grammar.addRule(_digit, TerminalSymbol.ch('2'));
        grammar.addRule(_digit, TerminalSymbol.ch('3'));
        grammar.addRule(_digit, _digit, _digit);
        grammar.addRule(_digit);

        GearleyParser parser = grammar.getParser(_number);
        GearleyResult result = parser.parse(Iterators.characterIterator("123123123"));
        Assert.assertTrue(result.succeeded());
    }

    @Test
    public void testExample() {
        // https://web.stanford.edu/class/archive/cs/cs143/cs143.1128/lectures/07/Slides07.pdf
        org.nineml.coffeegrinder.parser.Grammar grammar = new org.nineml.coffeegrinder.parser.Grammar();

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

        GearleyParser parser = grammar.getParser(_S);
        GearleyResult result = parser.parse(Iterators.characterIterator("aad"));

        Assert.assertTrue(result.succeeded());
    }

    @Test
    public void testEEE() {
        org.nineml.coffeegrinder.parser.Grammar grammar = new org.nineml.coffeegrinder.parser.Grammar();

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _E = grammar.getNonterminal("E");
        TerminalSymbol _plus = TerminalSymbol.ch('+');

        grammar.addRule(_S, _E);
        grammar.addRule(_E, _E, _plus, _E);
        grammar.addRule(_E, TerminalSymbol.regex("[0-9]"));

        GearleyParser parser = grammar.getParser(_S);
        GearleyResult result = parser.parse(Iterators.characterIterator("1+2"));
        //result.getForest().serialize("/tmp/testEEE.xml");
        Assert.assertTrue(result.succeeded());

        GllParser gllParser = new GllParser(grammar);
        Token[] tokens = new Token[]{ TokenCharacter.get('1') , TokenCharacter.get('+'), TokenCharacter.get('2') };

        GllResult gresult = gllParser.parse(tokens);
    }

    @Test
    public void testHighlyAmibiguous() {
        org.nineml.coffeegrinder.parser.Grammar grammar = new org.nineml.coffeegrinder.parser.Grammar();

        NonterminalSymbol _X = grammar.getNonterminal("X");
        TerminalSymbol _a = TerminalSymbol.ch('a');

        grammar.addRule(_X, _X, _X);
        grammar.addRule(_X, _a);

        GearleyParser parser = grammar.getParser(ParserType.Earley, _X);
        GearleyResult result = parser.parse(Iterators.characterIterator("aaaaa"));
        Assert.assertTrue(result.succeeded());

        //result.getForest().serialize("/tmp/ambig.xml");

        parser = grammar.getParser(ParserType.GLL, _X);
        result = parser.parse("aaaa");
        Assert.assertTrue(result.succeeded());
    }

    @Test
    public void Saabbaa() {
        GrammarParser gparser = new GrammarParser();
        org.nineml.coffeegrinder.parser.Grammar grammar = gparser.parse(
                "  S => A, C, 'a', B\n" +
                        "S => A, B, 'a', 'a'\n" +
                        "A => 'a', A\n" +
                        "A => 'a'\n" +
                        "B => 'b', B\n" +
                        "B => 'b'\n" +
                        "C => 'b', C\n" +
                        "C => 'b'");

        String input = "abaa";

        GearleyParser parser = grammar.getParser(grammar.getNonterminal("S"));
        GearleyResult result = parser.parse(Iterators.characterIterator(input));
        Assert.assertTrue(result.succeeded());
    }

    @Test
    public void empty() {
        GrammarParser gparser = new GrammarParser();
        org.nineml.coffeegrinder.parser.Grammar grammar = gparser.parse(
                "  S => 'a'\n" +
                        "S => 'b'\n" +
                        "S =>");

        String input = "";

        GearleyParser parser = grammar.getParser(grammar.getNonterminal("S"));
        GearleyResult result = parser.parse(Iterators.characterIterator(input));
        Assert.assertTrue(result.succeeded());
    }

    @Test
    public void hash() {
        org.nineml.coffeegrinder.parser.Grammar grammar = new org.nineml.coffeegrinder.parser.Grammar();

        NonterminalSymbol _expr = grammar.getNonterminal("expr");
        NonterminalSymbol _letter = grammar.getNonterminal("letter");

        ArrayList<ParserAttribute> atts = new ArrayList<>();
        atts.add(ParserAttribute.PRUNING_ALLOWED);

        NonterminalSymbol _letterOrNumber = grammar.getNonterminal("letterOrNumber", atts);
        NonterminalSymbol _number = grammar.getNonterminal("number");

        grammar.addRule(_expr, _letter, _letterOrNumber, _letter);
        grammar.addRule(_expr, _letter, _letter);
        grammar.addRule(_letter, TerminalSymbol.regex("[a-z]"));
        grammar.addRule(_number, TerminalSymbol.regex("[0-9]"));
        grammar.addRule(_letterOrNumber, _letter);
        grammar.addRule(_letterOrNumber, _number);

        GearleyParser parser = grammar.getParser(_expr);
        GearleyResult result = parser.parse(Iterators.characterIterator("xx"));

        Assert.assertTrue(result.succeeded());
    }

    @Test
    public void hashGrammar() {
        // See also loadCompiledGrammar in CompilerTest

        org.nineml.coffeegrinder.parser.Grammar grammar = new org.nineml.coffeegrinder.parser.Grammar();

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
        grammar.addRule(d6, d_req);
        grammar.addRule(d6, d_req, d_req);
        grammar.addRule(d6, d_req, d_req, d_req);
        grammar.addRule(d6, d_req, d_req, d_req, d_req);
        grammar.addRule(d6, d_req, d_req, d_req, d_req, d_req);
        grammar.addRule(d6, d_req, d_req, d_req, d_req, d_req, d_req);

        // -d: ["0"-"9"].
        grammar.addRule(d_req, new TerminalSymbol(TokenCharacterSet.inclusion(CharacterSet.range('0', '9'))));

        // -S: " "+.
        NonterminalSymbol _s = grammar.getNonterminal("_s");
        TerminalSymbol space = TerminalSymbol.ch(' ');
        grammar.addRule(S, space, _s);
        grammar.addRule(_s, space, _s);
        grammar.addRule(_s);

        GearleyParser parser = grammar.getParser(hashes);
        String input = "#12  #1234.";

        GearleyResult result = parser.parse(input);

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
        org.nineml.coffeegrinder.parser.Grammar grammar = gparser.parse(
                "Word => consonant, vowel, consonant\n" +
                        "Word => consonant, vowel, vowel, consonant\n" +
                        "Word => consonant, vowel, consonant, vowel\n" +
                        "Word => consonant, consonant, vowel\n" +
                        "consonant => [\"b\"-\"d\"; \"f\"-\"h\"; \"j\"-\"n\"; \"p\"-\"t\"; \"v\"-\"z\"]\n" +
                        "vowel => [\"a\"; \"e\"; \"i\"; \"o\"; \"u\"; \"y\"]");

        String input = "why";

        GearleyParser parser = grammar.getParser(grammar.getNonterminal("Word"));
        GearleyResult result = parser.parse(Iterators.characterIterator(input));
        Assert.assertTrue(result.succeeded());
    }

    @Test
    public void docExample() {
        org.nineml.coffeegrinder.parser.Grammar grammar = new org.nineml.coffeegrinder.parser.Grammar();

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

        grammar.close(S);

        HygieneReport report = grammar.checkHygiene(S);
        if (!report.isClean()) {
            // TODO: deal with undefined, unused, and unproductive items
        }

        GearleyParser parser = grammar.getParser(S);

        GearleyResult result = parser.parse("bx");

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

    @Test
    public void lettersAndNumbers() {
        org.nineml.coffeegrinder.parser.Grammar grammar = new org.nineml.coffeegrinder.parser.Grammar(new ParserOptions());

        ParserAttribute greedyLetters = new ParserAttribute("regex", "[^0-9]");
        ParserAttribute greedyNumbers = new ParserAttribute("regex", "[0-9]");

        NonterminalSymbol _String = grammar.getNonterminal("String");
        NonterminalSymbol _Numbers = grammar.getNonterminal("Numbers");
        NonterminalSymbol _Letters = grammar.getNonterminal("Letters");

        TerminalSymbol _number = new TerminalSymbol(TokenRegex.get("[0-9]"), greedyNumbers);
        TerminalSymbol _letter = new TerminalSymbol(TokenRegex.get("[^0-9]"), greedyLetters);

        grammar.addRule(_String, _Numbers, _String);
        grammar.addRule(_String, _Letters, _String);
        grammar.addRule(_String, _Numbers);
        grammar.addRule(_String, _Letters);

        grammar.addRule(_Numbers, _number, _Numbers);
        grammar.addRule(_Numbers, _number);
        grammar.addRule(_Letters, _letter, _Letters);
        grammar.addRule(_Letters, _letter);

        long last = 0;
        for (int len = 19; len < 20; len++) {
            StringBuilder sb = new StringBuilder();
            for (int count = 0; count < len*5; count++) {
                sb.append("abcde");
                sb.append("01234");
            }

            String input = sb.toString();

            long start = Calendar.getInstance().getTimeInMillis();
            GearleyParser parser = grammar.getParser(_String);
            GearleyResult result = parser.parse(input);
            long dur = Calendar.getInstance().getTimeInMillis() - start;

            Assert.assertTrue(result.succeeded());

            //System.out.printf("%d took %dms (%d) %n", input.length(), dur, dur - last);
            last = dur;
        }
    }

    @Test
    public void infiniteLoop() {
        Grammar grammar = new Grammar(new ParserOptions());

/*
S: A .
A: 'a', B ; 'x' .
B: 'b', A ; LDOE, A .
LDOE: M; 'l' .
M: 'm'; LDOE .
*/

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _B = grammar.getNonterminal("B");
        NonterminalSymbol _LDOE = grammar.getNonterminal("LDOE");
        NonterminalSymbol _M = grammar.getNonterminal("M");

        TerminalSymbol _a = TerminalSymbol.ch('a');
        TerminalSymbol _b = TerminalSymbol.ch('b');
        TerminalSymbol _x = TerminalSymbol.ch('x');
        TerminalSymbol _l = TerminalSymbol.ch('l');
        TerminalSymbol _m = TerminalSymbol.ch('m');

        grammar.addRule(_S, _A);
        grammar.addRule(_A, _a, _B);
        grammar.addRule(_A, _x);
        grammar.addRule(_B, _b, _A);
        grammar.addRule(_B, _LDOE, _A);
        grammar.addRule(_LDOE, _M);
        grammar.addRule(_LDOE, _l);
        grammar.addRule(_M, _m);
        grammar.addRule(_M, _LDOE);

        GearleyParser parser = grammar.getParser(_S);
        GearleyResult result = parser.parse("amalx");
        Assertions.assertTrue(result.getForest().isAmbiguous());
        Assertions.assertTrue(result.getForest().isInfinitelyAmbiguous());
        ParseTree tree = result.getForest().parse();
        Assertions.assertNotNull(tree);
    }

    @Test
    public void simpleOverlap() {
        Grammar grammar = new Grammar(new ParserOptions());

/*
S = A, x, B, C.
A = d, e, f.
A = d, e, f, g.
B = g, h.
B = g.
C = h, i.
C = i.
*/

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _B = grammar.getNonterminal("B");
        NonterminalSymbol _C = grammar.getNonterminal("C");

        TerminalSymbol _d = TerminalSymbol.ch('d');
        TerminalSymbol _e = TerminalSymbol.ch('e');
        TerminalSymbol _f = TerminalSymbol.ch('f');
        TerminalSymbol _g = TerminalSymbol.ch('g');
        TerminalSymbol _h = TerminalSymbol.ch('h');
        TerminalSymbol _i = TerminalSymbol.ch('i');
        TerminalSymbol _x = TerminalSymbol.ch('x');

        grammar.addRule(_S, _A, _x, _B, _C);
        grammar.addRule(_A, _d, _e, _f);
        grammar.addRule(_A, _d, _e, _f, _g);
        grammar.addRule(_B, _g, _h);
        grammar.addRule(_B, _g);
        grammar.addRule(_C, _h, _i);
        grammar.addRule(_C, _i);

        GearleyParser parser = grammar.getParser(_S);
        GearleyResult result = parser.parse("defxghi");
        Assert.assertTrue(result.succeeded());
    }
}
