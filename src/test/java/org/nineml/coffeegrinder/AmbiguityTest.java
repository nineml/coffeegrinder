package org.nineml.coffeegrinder;

import org.junit.Assert;
import org.junit.Test;
import org.nineml.coffeegrinder.parser.*;
import org.nineml.coffeegrinder.tokens.CharacterSet;
import org.nineml.coffeegrinder.tokens.TokenCharacter;
import org.nineml.coffeegrinder.tokens.TokenCharacterSet;
import org.nineml.coffeegrinder.util.*;

import java.util.Arrays;
import java.util.Collections;

import static junit.framework.TestCase.fail;

public class AmbiguityTest extends CoffeeGrinderTest {

    @Test
    public void testAmbiguity() {
        SourceGrammar grammar = new SourceGrammar();

        NonterminalSymbol _letter1 = grammar.getNonterminal("letter", new ParserAttribute("L", "1"));
        NonterminalSymbol _letter2 = grammar.getNonterminal("letter", new ParserAttribute("L", "2"));
        NonterminalSymbol _letterOrNumber = grammar.getNonterminal("letterOrNumber");
        NonterminalSymbol _expr = grammar.getNonterminal("expr");
        NonterminalSymbol _number1 = grammar.getNonterminal("number", new ParserAttribute("N", "1"));
        NonterminalSymbol _number2 = grammar.getNonterminal("number", new ParserAttribute("N", "2"));
        NonterminalSymbol _number3 = grammar.getNonterminal("number", new ParserAttribute("N", "3"));

        grammar.addRule(_expr, TerminalSymbol.ch('x'), _letter1, _letterOrNumber, _letter1);
        grammar.addRule(_letter1, TerminalSymbol.regex("[a-z]"));
        grammar.addRule(_letter2, TerminalSymbol.regex("[a-z]"));
        grammar.addRule(_number1, TerminalSymbol.regex("[0-9]"));
        grammar.addRule(_number2, TerminalSymbol.regex("[0-9]"));
        grammar.addRule(_letterOrNumber, _letter2);
        grammar.addRule(_letterOrNumber, _number2);

        grammar.addRule(_number3, TerminalSymbol.ch('b'));

        GearleyParser parser = grammar.getParser(options, _expr);
        GearleyResult result = parser.parse(Iterators.characterIterator("xabb"));

        //result.getForest().serialize("ambiguity.xml");

        Assert.assertTrue(result.succeeded());
        Assert.assertEquals(2, result.getForest().getParseTreeCount());

        expectTrees(result.getForest().getWalker(), Arrays.asList(
                "<expr>x<letter L='1'>a</letter><letterOrNumber><letter L='2'>b</letter></letterOrNumber><letter L='1'>b</letter></expr>",
                "<expr>x<letter L='1'>a</letter><letterOrNumber><number N='2'>b</number></letterOrNumber><letter L='1'>b</letter></expr>"));
    }

    @Test
    public void testAmbiguity2() {
        SourceGrammar grammar = new SourceGrammar();

        NonterminalSymbol _letter = grammar.getNonterminal("letter");
        NonterminalSymbol _letterOrNumber = grammar.getNonterminal("letterOrNumber");
        NonterminalSymbol _expr = grammar.getNonterminal("expr");
        NonterminalSymbol _number = grammar.getNonterminal("number");
        NonterminalSymbol _other = grammar.getNonterminal("other");

        grammar.addRule(_expr, _letter, _letterOrNumber, _letter, _letterOrNumber);
        grammar.addRule(_letter, TerminalSymbol.regex("[a-z]"));
        grammar.addRule(_number, TerminalSymbol.regex("[0-9]"));
        grammar.addRule(_other, TerminalSymbol.regex("[A-Z]"));
        grammar.addRule(_letterOrNumber, _letter);
        grammar.addRule(_letterOrNumber, _number);
        grammar.addRule(_letterOrNumber, _other);

        grammar.addRule(_number, TerminalSymbol.ch('b'));
        grammar.addRule(_other, TerminalSymbol.ch('b'));

        GearleyParser parser = grammar.getParser(options, _expr);
        GearleyResult result = parser.parse(Iterators.characterIterator("abab"));

        //result.getForest().serialize("ambiguity2.xml");

        Assert.assertTrue(result.succeeded());
        Assert.assertEquals(9, result.getForest().getParseTreeCount());

        expectTrees(result.getForest().getWalker(), Arrays.asList(
                "<expr><letter>a</letter><letterOrNumber><letter>b</letter></letterOrNumber><letter>a</letter><letterOrNumber><letter>b</letter></letterOrNumber></expr>",
                "<expr><letter>a</letter><letterOrNumber><letter>b</letter></letterOrNumber><letter>a</letter><letterOrNumber><number>b</number></letterOrNumber></expr>",
                "<expr><letter>a</letter><letterOrNumber><letter>b</letter></letterOrNumber><letter>a</letter><letterOrNumber><other>b</other></letterOrNumber></expr>",
                "<expr><letter>a</letter><letterOrNumber><number>b</number></letterOrNumber><letter>a</letter><letterOrNumber><letter>b</letter></letterOrNumber></expr>",
                "<expr><letter>a</letter><letterOrNumber><number>b</number></letterOrNumber><letter>a</letter><letterOrNumber><number>b</number></letterOrNumber></expr>",
                "<expr><letter>a</letter><letterOrNumber><number>b</number></letterOrNumber><letter>a</letter><letterOrNumber><other>b</other></letterOrNumber></expr>",
                "<expr><letter>a</letter><letterOrNumber><other>b</other></letterOrNumber><letter>a</letter><letterOrNumber><letter>b</letter></letterOrNumber></expr>",
                "<expr><letter>a</letter><letterOrNumber><other>b</other></letterOrNumber><letter>a</letter><letterOrNumber><number>b</number></letterOrNumber></expr>",
                "<expr><letter>a</letter><letterOrNumber><other>b</other></letterOrNumber><letter>a</letter><letterOrNumber><other>b</other></letterOrNumber></expr>"));
    }

    @Test
    public void testAmbiguity3() {
        SourceGrammar grammar = new SourceGrammar();

        NonterminalSymbol _word = grammar.getNonterminal("word");
        NonterminalSymbol _letter = grammar.getNonterminal("letter");
        NonterminalSymbol _expr = grammar.getNonterminal("expr");

        grammar.addRule(_expr, _word);
        grammar.addRule(_letter, TerminalSymbol.regex("[a-z]"));
        grammar.addRule(_letter);
        grammar.addRule(_word, _letter, _word);
        grammar.addRule(_word);

        GearleyParser parser = grammar.getParser(options, _expr);
        GearleyResult result = parser.parse(Iterators.characterIterator("word"));

        //result.getForest().serialize("ambiguity3.xml");

        Assert.assertTrue(result.succeeded());
        Assert.assertTrue(result.getForest().isInfinitelyAmbiguous());

        Assert.assertEquals(6, result.getForest().getParseTreeCount());

        expectTrees(result.getForest().getWalker(), Arrays.asList(
                "<expr><word><letter>w</letter><word><letter>o</letter><word><letter>r</letter><word><letter>d</letter><word></word></word></word></word></word></expr>",
                "<expr><word><letter>w</letter><word><letter>o</letter><word><letter>r</letter><word><letter>d</letter><word><letter></letter><word></word></word></word></word></word></word></expr>",
                "<expr><word><letter>w</letter><word><letter>o</letter><word><letter>r</letter><word><letter></letter><word><letter>d</letter><word></word></word></word></word></word></word></expr>",
                "<expr><word><letter>w</letter><word><letter>o</letter><word><letter>r</letter><word><letter></letter><word><letter>d</letter><word><letter></letter><word></word></word></word></word></word></word></word></expr>",
                "<expr><word><letter>w</letter><word><letter>o</letter><word><letter></letter><word><letter>r</letter><word><letter>d</letter><word></word></word></word></word></word></word></expr>",
                "<expr><word><letter>w</letter><word><letter>o</letter><word><letter></letter><word><letter>r</letter><word><letter>d</letter><word><letter></letter><word></word></word></word></word></word></word></word></expr>",
                "<expr><word><letter>w</letter><word><letter>o</letter><word><letter></letter><word><letter>r</letter><word><letter></letter><word><letter>d</letter><word></word></word></word></word></word></word></word></expr>",
                "<expr><word><letter>w</letter><word><letter>o</letter><word><letter></letter><word><letter>r</letter><word><letter></letter><word><letter>d</letter><word><letter></letter><word></word></word></word></word></word></word></word></word></expr>",
                "<expr><word><letter>w</letter><word><letter></letter><word><letter>o</letter><word><letter>r</letter><word><letter>d</letter><word></word></word></word></word></word></word></expr>",
                "<expr><word><letter>w</letter><word><letter></letter><word><letter>o</letter><word><letter>r</letter><word><letter>d</letter><word><letter></letter><word></word></word></word></word></word></word></word></expr>",
                "<expr><word><letter>w</letter><word><letter></letter><word><letter>o</letter><word><letter>r</letter><word><letter></letter><word><letter>d</letter><word></word></word></word></word></word></word></word></expr>",
                "<expr><word><letter>w</letter><word><letter></letter><word><letter>o</letter><word><letter>r</letter><word><letter></letter><word><letter>d</letter><word><letter></letter><word></word></word></word></word></word></word></word></word></expr>",
                "<expr><word><letter>w</letter><word><letter></letter><word><letter>o</letter><word><letter></letter><word><letter>r</letter><word><letter>d</letter><word></word></word></word></word></word></word></word></expr>",
                "<expr><word><letter>w</letter><word><letter></letter><word><letter>o</letter><word><letter></letter><word><letter>r</letter><word><letter>d</letter><word><letter></letter><word></word></word></word></word></word></word></word></word></expr>",
                "<expr><word><letter>w</letter><word><letter></letter><word><letter>o</letter><word><letter></letter><word><letter>r</letter><word><letter></letter><word><letter>d</letter><word></word></word></word></word></word></word></word></word></expr>",
                "<expr><word><letter>w</letter><word><letter></letter><word><letter>o</letter><word><letter></letter><word><letter>r</letter><word><letter></letter><word><letter>d</letter><word><letter></letter><word></word></word></word></word></word></word></word></word></word></expr>",
                "<expr><word><letter></letter><word><letter>w</letter><word><letter>o</letter><word><letter>r</letter><word><letter>d</letter><word></word></word></word></word></word></word></expr>",
                "<expr><word><letter></letter><word><letter>w</letter><word><letter>o</letter><word><letter>r</letter><word><letter>d</letter><word><letter></letter><word></word></word></word></word></word></word></word></expr>",
                "<expr><word><letter></letter><word><letter>w</letter><word><letter>o</letter><word><letter>r</letter><word><letter></letter><word><letter>d</letter><word></word></word></word></word></word></word></word></expr>",
                "<expr><word><letter></letter><word><letter>w</letter><word><letter>o</letter><word><letter>r</letter><word><letter></letter><word><letter>d</letter><word><letter></letter><word></word></word></word></word></word></word></word></word></expr>",
                "<expr><word><letter></letter><word><letter>w</letter><word><letter>o</letter><word><letter></letter><word><letter>r</letter><word><letter>d</letter><word></word></word></word></word></word></word></word></expr>",
                "<expr><word><letter></letter><word><letter>w</letter><word><letter>o</letter><word><letter></letter><word><letter>r</letter><word><letter>d</letter><word><letter></letter><word></word></word></word></word></word></word></word></word></expr>",
                "<expr><word><letter></letter><word><letter>w</letter><word><letter>o</letter><word><letter></letter><word><letter>r</letter><word><letter></letter><word><letter>d</letter><word></word></word></word></word></word></word></word></word></expr>",
                "<expr><word><letter></letter><word><letter>w</letter><word><letter>o</letter><word><letter></letter><word><letter>r</letter><word><letter></letter><word><letter>d</letter><word><letter></letter><word></word></word></word></word></word></word></word></word></word></expr>",
                "<expr><word><letter></letter><word><letter>w</letter><word><letter></letter><word><letter>o</letter><word><letter>r</letter><word><letter>d</letter><word></word></word></word></word></word></word></word></expr>",
                "<expr><word><letter></letter><word><letter>w</letter><word><letter></letter><word><letter>o</letter><word><letter>r</letter><word><letter>d</letter><word><letter></letter><word></word></word></word></word></word></word></word></word></expr>",
                "<expr><word><letter></letter><word><letter>w</letter><word><letter></letter><word><letter>o</letter><word><letter>r</letter><word><letter></letter><word><letter>d</letter><word></word></word></word></word></word></word></word></word></expr>",
                "<expr><word><letter></letter><word><letter>w</letter><word><letter></letter><word><letter>o</letter><word><letter>r</letter><word><letter></letter><word><letter>d</letter><word><letter></letter><word></word></word></word></word></word></word></word></word></word></expr>",
                "<expr><word><letter></letter><word><letter>w</letter><word><letter></letter><word><letter>o</letter><word><letter></letter><word><letter>r</letter><word><letter>d</letter><word></word></word></word></word></word></word></word></word></expr>",
                "<expr><word><letter></letter><word><letter>w</letter><word><letter></letter><word><letter>o</letter><word><letter></letter><word><letter>r</letter><word><letter>d</letter><word><letter></letter><word></word></word></word></word></word></word></word></word></word></expr>",
                "<expr><word><letter></letter><word><letter>w</letter><word><letter></letter><word><letter>o</letter><word><letter></letter><word><letter>r</letter><word><letter></letter><word><letter>d</letter><word></word></word></word></word></word></word></word></word></word></expr>",
                "<expr><word><letter></letter><word><letter>w</letter><word><letter></letter><word><letter>o</letter><word><letter></letter><word><letter>r</letter><word><letter></letter><word><letter>d</letter><word><letter></letter><word></word></word></word></word></word></word></word></word></word></word></expr>"));
    }

    @Test
    public void testAmbiguity4() {
        SourceGrammar grammar = new SourceGrammar();

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _B0 = grammar.getNonterminal("B", new ParserAttribute("N", "0"));
        NonterminalSymbol _B1 = grammar.getNonterminal("B", new ParserAttribute("N", "1"));
        NonterminalSymbol _B2 = grammar.getNonterminal("B", new ParserAttribute("N", "2"));
        NonterminalSymbol _C = grammar.getNonterminal("C");

        grammar.addRule(_S, _A, _B1, _C);
        grammar.addRule(_S, _A, _B2, _C);
        grammar.addRule(_A, TerminalSymbol.ch('a'));
        grammar.addRule(_B0, TerminalSymbol.ch('b'));
        grammar.addRule(_C, TerminalSymbol.ch('c'));

        GearleyParser parser = grammar.getParser(options, _S);
        GearleyResult result = parser.parse(Iterators.characterIterator("abc"));

        Assert.assertTrue(result.succeeded());
        Assert.assertEquals(2, result.getForest().getParseTreeCount());

        //result.getForest().serialize("ambiguity4.xml");

        expectTrees(result.getForest().getWalker(), Arrays.asList(
                "<S><A>a</A><B N='1'>b</B><C>c</C></S>",
                "<S><A>a</A><B N='2'>b</B><C>c</C></S>"));
    }

    @Test
    public void testAmbiguity5() {
        SourceGrammar grammar = new SourceGrammar();

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _B = grammar.getNonterminal("B");
        NonterminalSymbol _C = grammar.getNonterminal("C");

        grammar.addRule(_S, _A, _B, _C);
        grammar.addRule(_S, _A, _C);
        grammar.addRule(_A, TerminalSymbol.ch('a'));
        grammar.addRule(_B, TerminalSymbol.ch('b'));
        grammar.addRule(_C, TerminalSymbol.ch('c'));

        GearleyParser parser = grammar.getParser(options, _S);
        GearleyResult result = parser.parse(Iterators.characterIterator("abc"));

        //result.getForest().serialize("ambiguity5.xml");

        Assert.assertTrue(result.succeeded());
        Assert.assertEquals(1, result.getForest().getParseTreeCount());

        expectTrees(result.getForest().getWalker(), Collections.singletonList("<S><A>a</A><B>b</B><C>c</C></S>"));
    }

    @Test
    public void horiz1() {
        SourceGrammar grammar = new SourceGrammar();

        /*
        {[+pragma n "https://nineml.org/ns/pragma/"]}
                         S = Z .
        {[n priority 1]} Z = "x", A, B .
                         A = "a" | () .
                         B = "a", "y" | "y" .
         */

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _Z = grammar.getNonterminal("Z");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _B = grammar.getNonterminal("B");

        grammar.addRule(_S, _Z);
        grammar.addRule(_Z, TerminalSymbol.ch('x'), _A, _B);
        grammar.addRule(_A, TerminalSymbol.ch('a'));
        grammar.addRule(_A);
        grammar.addRule(_B, TerminalSymbol.ch('a'), TerminalSymbol.ch('y'));
        grammar.addRule(_B, TerminalSymbol.ch('y'));

        GearleyParser parser = grammar.getParser(options, _S);
        GearleyResult result = parser.parse(Iterators.characterIterator("xay"));

        Assert.assertTrue(result.succeeded());
        Assert.assertEquals(2, result.getForest().getParseTreeCount());

        //result.getForest().serialize("horiz1.xml");

        expectTrees(result.getForest().getWalker(), Arrays.asList(
                "<S><Z>x<A></A><B>ay</B></Z></S>",
                "<S><Z>x<A>a</A><B>y</B></Z></S>"));
    }

    @Test
    public void ambigprop() {
        SourceGrammar grammar = new SourceGrammar();

        /*
           S = A, D | X, Y .
           A = "a" | "c" .
           D = "d" .
           X = "a" | "c" .
           Y = Z .
           Z = "d" .
         */

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A", new ParserAttribute(ForestNode.PRIORITY_ATTRIBUTE, "1"));
        NonterminalSymbol _D = grammar.getNonterminal("D", new ParserAttribute(ForestNode.PRIORITY_ATTRIBUTE, "4"));
        NonterminalSymbol _X = grammar.getNonterminal("X", new ParserAttribute(ForestNode.PRIORITY_ATTRIBUTE, "3"));
        NonterminalSymbol _Y = grammar.getNonterminal("Y");
        NonterminalSymbol _Z = grammar.getNonterminal("Z");

        grammar.addRule(_S, _A, _D);
        grammar.addRule(_S, _X, _Y);
        grammar.addRule(_A, TerminalSymbol.ch('a'));
        grammar.addRule(_A, TerminalSymbol.ch('c'));
        grammar.addRule(_D, TerminalSymbol.ch('d'));
        grammar.addRule(_X, TerminalSymbol.ch('a'));
        grammar.addRule(_X, TerminalSymbol.ch('c'));
        grammar.addRule(_Y, _Z);
        grammar.addRule(_Z, TerminalSymbol.ch('d'));

        GearleyParser parser = grammar.getParser(options, _S);
        GearleyResult result = parser.parse(Iterators.characterIterator("ad"));

        Assert.assertTrue(result.succeeded());
        Assert.assertEquals(2, result.getForest().getParseTreeCount());

        //result.getForest().serialize("ambigprop.xml");

        final String p = ForestNode.PRIORITY_ATTRIBUTE;
        expectTrees(result.getForest().getWalker(), Arrays.asList(
                String.format("<S><A %s='1'>a</A><D %s='4'>d</D></S>", p, p),
                String.format("<S><X %s='3'>a</X><Y><Z>d</Z></Y></S>", p)));
    }

    @Test
    public void ambigcharclass() {
        SourceGrammar grammar = new SourceGrammar();

        /*
           S = A | B .
           A = "a" .
           B = [Ll] .
         */

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _B = grammar.getNonterminal("B");

        grammar.addRule(_S, _A);
        grammar.addRule(_S, _B);
        grammar.addRule(_A, TerminalSymbol.ch('a'));
        grammar.addRule(_A, new TerminalSymbol(TokenCharacter.get(128587)));
        grammar.addRule(_B, new TerminalSymbol(TokenCharacterSet.inclusion(CharacterSet.unicodeClass("Ll"))));

        GearleyParser parser = grammar.getParser(options, _S);

        HygieneReport report = parser.getGrammar().getHygieneReport();
        report.checkAmbiguity();
        Assert.assertFalse(report.reliablyUnambiguous());

        GearleyResult result = parser.parse(Iterators.characterIterator("a"));

        Assert.assertTrue(result.succeeded());
        Assert.assertEquals(2, result.getForest().getParseTreeCount());

        //result.getForest().serialize("ambigcharclass.xml");

        expectTrees(result.getForest().getWalker(), Arrays.asList(
                "<S><A>a</A></S>",
                "<S><B>a</B></S>"));
    }

    @Test
    public void ambigproploop() {
        SourceGrammar grammar = new SourceGrammar();

    /*
    S = A .
    A = "a" | B | () .
    B = C .
    C = A | () .
     */

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _B = grammar.getNonterminal("B");
        NonterminalSymbol _C = grammar.getNonterminal("C");

        grammar.addRule(_S, _A);
        grammar.addRule(_A, new TerminalSymbol(TokenCharacter.get('a', new ParserAttribute("terminal", "true"))));
        grammar.addRule(_A, _B);
        grammar.addRule(_A);
        grammar.addRule(_B, _C);
        grammar.addRule(_C, _A);
        grammar.addRule(_C);

        GearleyParser parser = grammar.getParser(options, _S);
        GearleyResult result = parser.parse(Iterators.characterIterator("a"));

        Assert.assertTrue(result.succeeded());
        Assert.assertEquals(2, result.getForest().getParseTreeCount());

        //result.getForest().serialize("ambigproploop.xml");

        expectTrees(result.getForest().getWalker(), Arrays.asList(
                "<S><A>a</A></S>",
                "<S><A><B><C><A>a</A></C></B></A></S>"));
    }

    @Test
    public void simpleambiguity() {
        SourceGrammar grammar = new SourceGrammar();

    /*
    S = A | B .
    A = "a" .
    B = "a" .
     */

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _B = grammar.getNonterminal("B");

        grammar.addRule(_S, _A);
        grammar.addRule(_S, _B);
        grammar.addRule(_A, TerminalSymbol.ch('a'));
        grammar.addRule(_B, TerminalSymbol.ch('a'));

        GearleyParser parser = grammar.getParser(options, _S);
        GearleyResult result = parser.parse(Iterators.characterIterator("a"));

        Assert.assertTrue(result.succeeded());
        Assert.assertTrue(result.getForest().isAmbiguous());
        Assert.assertFalse(result.getForest().isInfinitelyAmbiguous());
        Assert.assertEquals(2, result.getForest().getParseTreeCount());

        //result.getForest().serialize("simple.xml");

        expectTrees(result.getForest().getWalker(), Arrays.asList(
                "<S><A>a</A></S>",
                "<S><B>a</B></S>"));
    }

    @Test
    public void deeperambiguity() {
        SourceGrammar grammar = new SourceGrammar();

    /*
    S = A, B .
    A = Q | R .
    B = V | W .
    V = X | Y .
    W = X | Y .
    Q = "a" .
    R = "a" .
    X = "b" .
    Y = "b" .
     */

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _B = grammar.getNonterminal("B");
        NonterminalSymbol _Q = grammar.getNonterminal("Q");
        NonterminalSymbol _R = grammar.getNonterminal("R");
        NonterminalSymbol _V = grammar.getNonterminal("V");
        NonterminalSymbol _W = grammar.getNonterminal("W");
        NonterminalSymbol _X = grammar.getNonterminal("X");
        NonterminalSymbol _Y = grammar.getNonterminal("Y");

        grammar.addRule(_S, _A, _B);
        grammar.addRule(_A, _Q);
        grammar.addRule(_A, _R);
        grammar.addRule(_B, _V);
        grammar.addRule(_B, _W);
        grammar.addRule(_V, _X);
        grammar.addRule(_V, _Y);
        grammar.addRule(_W, _X);
        grammar.addRule(_W, _Y);

        grammar.addRule(_Q, TerminalSymbol.ch('a'));
        grammar.addRule(_R, TerminalSymbol.ch('a'));
        grammar.addRule(_X, TerminalSymbol.ch('b'));
        grammar.addRule(_Y, TerminalSymbol.ch('b'));

        GearleyParser parser = grammar.getParser(options, _S);
        GearleyResult result = parser.parse(Iterators.characterIterator("ab"));

        Assert.assertTrue(result.succeeded());
        Assert.assertTrue(result.getForest().isAmbiguous());
        Assert.assertFalse(result.getForest().isInfinitelyAmbiguous());
        Assert.assertEquals(8, result.getForest().getParseTreeCount());

        //result.getForest().serialize("deeper.xml");

        expectTrees(result.getForest().getWalker(), Arrays.asList(
                "<S><A><Q>a</Q></A><B><V><X>b</X></V></B></S>",
                "<S><A><Q>a</Q></A><B><V><Y>b</Y></V></B></S>",
                "<S><A><Q>a</Q></A><B><W><X>b</X></W></B></S>",
                "<S><A><Q>a</Q></A><B><W><Y>b</Y></W></B></S>",
                "<S><A><R>a</R></A><B><V><X>b</X></V></B></S>",
                "<S><A><R>a</R></A><B><V><Y>b</Y></V></B></S>",
                "<S><A><R>a</R></A><B><W><X>b</X></W></B></S>",
                "<S><A><R>a</R></A><B><W><Y>b</Y></W></B></S>"));
    }

    @Test
    public void loopambiguity() {
        SourceGrammar grammar = new SourceGrammar();

    /*
    S = A .
    A = X | "a" .
    X = Y .
    Y = Z .
    Z = A | () .
     */

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _X = grammar.getNonterminal("X");
        NonterminalSymbol _Y = grammar.getNonterminal("Y");
        NonterminalSymbol _Z = grammar.getNonterminal("Z");

        grammar.addRule(_S, _A);
        grammar.addRule(_A, _X);
        grammar.addRule(_A, TerminalSymbol.ch('a'));
        grammar.addRule(_X, _Y);
        grammar.addRule(_Y, _Z);
        grammar.addRule(_Z, _A);
        grammar.addRule(_Z, TerminalSymbol.ch('a'));
        grammar.addRule(_Z);

        GearleyParser parser = grammar.getParser(options, _S);
        GearleyResult result = parser.parse(Iterators.characterIterator("a"));

        Assert.assertTrue(result.succeeded());
        Assert.assertTrue(result.getForest().isAmbiguous());
        Assert.assertTrue(result.getForest().isInfinitelyAmbiguous());
        Assert.assertEquals(3, result.getForest().getParseTreeCount());

        //result.getForest().serialize("loop.xml");

        expectTrees(result.getForest().getWalker(), Arrays.asList(
                "<S><A>a</A></S>",
                "<S><A><X><Y><Z>a</Z></Y></X></A></S>",
                "<S><A><X><Y><Z><A>a</A></Z></Y></X></A></S>"
        ));
    }

    @Test
    public void fourparses() {
        SourceGrammar grammar = new SourceGrammar();
    /*
                  S = 'x', (A | B1), 'y' .
                  A = 'a' | B2 .
                 B1 = 'b' | A .
                 B2 = 'b' | A .
     */

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _B1 = grammar.getNonterminal("B1");
        NonterminalSymbol _B2 = grammar.getNonterminal("B2");

        grammar.addRule(_S, TerminalSymbol.ch('x'), _A, TerminalSymbol.ch('y'));
        grammar.addRule(_S, TerminalSymbol.ch('x'), _B1, TerminalSymbol.ch('y'));
        grammar.addRule(_A, TerminalSymbol.ch('a'));
        grammar.addRule(_A, _B2);
        grammar.addRule(_B1, TerminalSymbol.ch('b'));
        grammar.addRule(_B1, _A);
        grammar.addRule(_B2, TerminalSymbol.ch('b'));
        grammar.addRule(_B2, _A);

        GearleyParser parser = grammar.getParser(options, _S);
        GearleyResult result = parser.parse(Iterators.characterIterator("xay"));

        Assert.assertTrue(result.succeeded());
        Assert.assertEquals(4, result.getForest().getParseTreeCount());

        //result.getForest().serialize("fourparses.xml");

        expectTrees(result.getForest().getWalker(), Arrays.asList(
                "<S>x<A>a</A>y</S>",
                "<S>x<A><B2><A>a</A></B2></A>y</S>",
                "<S>x<B1><A>a</A></B1>y</S>",
                "<S>x<B1><A><B2><A>a</A></B2></A></B1>y</S>"
        ));
    }

    @Test
    public void fourparsesnoloop() {
        SourceGrammar grammar = new SourceGrammar();
    /*
                  S = 'x', (A | B1), 'y' .
                  A = 'a' | B2 .
                 B1 = 'b' | A .
                 B2 = 'b' | 'a' .
     */

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _B1 = grammar.getNonterminal("B1");
        NonterminalSymbol _B2 = grammar.getNonterminal("B2");

        grammar.addRule(_S, TerminalSymbol.ch('x'), _A, TerminalSymbol.ch('y'));
        grammar.addRule(_S, TerminalSymbol.ch('x'), _B1, TerminalSymbol.ch('y'));
        grammar.addRule(_A, TerminalSymbol.ch('a'));
        grammar.addRule(_A, _B2);
        grammar.addRule(_B1, TerminalSymbol.ch('b'));
        grammar.addRule(_B1, _A);
        grammar.addRule(_B2, TerminalSymbol.ch('b'));
        grammar.addRule(_B2, TerminalSymbol.ch('a'));

        GearleyParser parser = grammar.getParser(options, _S);
        GearleyResult result = parser.parse(Iterators.characterIterator("xay"));

        Assert.assertTrue(result.succeeded());
        Assert.assertEquals(4, result.getForest().getParseTreeCount());

        //result.getForest().serialize("noloops.xml");

        expectTrees(result.getForest().getWalker(), Arrays.asList(
                "<S>x<A>a</A>y</S>",
                "<S>x<A><B2>a</B2></A>y</S>",
                "<S>x<B1><A>a</A></B1>y</S>",
                "<S>x<B1><A><B2>a</B2></A></B1>y</S>"
        ));
    }

    @Test
    public void wordhex() {
        SourceGrammar grammar = new SourceGrammar();

        /*
        id => word; hex
        word => letter, letter, letter
        hex => digit, digit, digit
        letter => ["a"-"z"; "A"-"Z"]
        digit => ["0"-"9"; "a"-"f"; "A"-"F"]
         */

        NonterminalSymbol _id = grammar.getNonterminal("id");
        NonterminalSymbol _word = grammar.getNonterminal("word");
        NonterminalSymbol _hex = grammar.getNonterminal("hex");
        TerminalSymbol _letter = TerminalSymbol.regex("[a-zA-Z]");
        TerminalSymbol _digit = TerminalSymbol.regex("[0-9a-fA-F]");

        grammar.addRule(_id, _word);
        grammar.addRule(_id, _hex);
        grammar.addRule(_word, _letter, _letter, _letter);
        grammar.addRule(_hex, _digit, _digit, _digit);

        GearleyParser parser = grammar.getParser(options, _id);
        GearleyResult result = parser.parse(Iterators.characterIterator("fab"));

        Assert.assertTrue(result.succeeded());
        Assert.assertEquals(2, result.getForest().getParseTreeCount());

        //result.getForest().serialize("wordhex.xml");

        expectTrees(result.getForest().getWalker(), Arrays.asList(
                "<id><word>fab</word></id>",
                "<id><hex>fab</hex></id>"
        ));
    }

    @Test
    public void disjointparses1() {
        SourceGrammar grammar = new SourceGrammar();
    /*
                  S = 'x', (A | B), 'y' .
                  A = A1 | A2 | A3 .
                  B = B1 | B2 .
                 A1 = 'a' .
                 A2 = 'a' .
                 A3 = 'a' .
                 B1 = 'a' .
                 B2 = 'a' .
     */

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _B = grammar.getNonterminal("B");
        NonterminalSymbol _A1 = grammar.getNonterminal("A1");
        NonterminalSymbol _A2 = grammar.getNonterminal("A2");
        NonterminalSymbol _A3 = grammar.getNonterminal("A3");
        NonterminalSymbol _B1 = grammar.getNonterminal("B1");
        NonterminalSymbol _B2 = grammar.getNonterminal("B2");

        grammar.addRule(_S, TerminalSymbol.ch('x'), _A, TerminalSymbol.ch('y'));
        grammar.addRule(_S, TerminalSymbol.ch('x'), _B, TerminalSymbol.ch('y'));
        //grammar.addRule(_S, TerminalSymbol.ch('x'), _B, TerminalSymbol.ch('y'));
        grammar.addRule(_A, _A1);
        grammar.addRule(_A, _A2);
        grammar.addRule(_A, _A3);
        grammar.addRule(_B, _B1);
        grammar.addRule(_B, _B2);
        grammar.addRule(_A1, TerminalSymbol.ch('a'));
        grammar.addRule(_A2, TerminalSymbol.ch('a'));
        grammar.addRule(_A3, TerminalSymbol.ch('a'));
        grammar.addRule(_B1, TerminalSymbol.ch('a'));
        grammar.addRule(_B2, TerminalSymbol.ch('a'));

        GearleyParser parser = grammar.getParser(options, _S);
        GearleyResult result = parser.parse(Iterators.characterIterator("xay"));

        Assert.assertTrue(result.succeeded());
        Assert.assertEquals(5, result.getForest().getParseTreeCount());

        //result.getForest().serialize("disjoint1.xml");

        expectTrees(result.getForest().getWalker(), Arrays.asList(
                "<S>x<A><A1>a</A1></A>y</S>",
                "<S>x<A><A2>a</A2></A>y</S>",
                "<S>x<A><A3>a</A3></A>y</S>",
                "<S>x<B><B1>a</B1></B>y</S>",
                "<S>x<B><B2>a</B2></B>y</S>"));
    }

    @Test
    public void disjointparses2() {
        SourceGrammar grammar = new SourceGrammar();
    /*
                  S = 'x', (A , B), 'y' .
                  A = A1 | A2 | A3 .
                  B = B1 | B2 .
                 A1 = 'a' .
                 A2 = 'a' .
                 A3 = 'a' .
                 B1 = 'a' .
                 B2 = 'a' .
     */

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _B = grammar.getNonterminal("B");
        NonterminalSymbol _A1 = grammar.getNonterminal("A1");
        NonterminalSymbol _A2 = grammar.getNonterminal("A2");
        NonterminalSymbol _A3 = grammar.getNonterminal("A3");
        NonterminalSymbol _B1 = grammar.getNonterminal("B1");
        NonterminalSymbol _B2 = grammar.getNonterminal("B2");

        grammar.addRule(_S, TerminalSymbol.ch('x'), _A, _B, TerminalSymbol.ch('y'));
        //grammar.addRule(_S, TerminalSymbol.ch('x'), _B, TerminalSymbol.ch('y'));
        grammar.addRule(_A, _A1);
        grammar.addRule(_A, _A2);
        grammar.addRule(_A, _A3);
        grammar.addRule(_B, _B1);
        grammar.addRule(_B, _B2);
        grammar.addRule(_A1, TerminalSymbol.ch('a'));
        grammar.addRule(_A2, TerminalSymbol.ch('a'));
        grammar.addRule(_A3, TerminalSymbol.ch('a'));
        grammar.addRule(_B1, TerminalSymbol.ch('b'));
        grammar.addRule(_B2, TerminalSymbol.ch('b'));

        GearleyParser parser = grammar.getParser(options, _S);
        GearleyResult result = parser.parse(Iterators.characterIterator("xaby"));

        Assert.assertTrue(result.succeeded());
        Assert.assertEquals(6, result.getForest().getParseTreeCount());

        //result.getForest().serialize("disjoint2.xml");

        expectTrees(result.getForest().getWalker(), Arrays.asList(
                "<S>x<A><A1>a</A1></A><B><B1>b</B1></B>y</S>",
                "<S>x<A><A1>a</A1></A><B><B2>b</B2></B>y</S>",
                "<S>x<A><A2>a</A2></A><B><B1>b</B1></B>y</S>",
                "<S>x<A><A2>a</A2></A><B><B2>b</B2></B>y</S>",
                "<S>x<A><A3>a</A3></A><B><B1>b</B1></B>y</S>",
                "<S>x<A><A3>a</A3></A><B><B2>b</B2></B>y</S>"));
    }

    @Test
    public void screaminghorror() {
        SourceGrammar grammar = new SourceGrammar();
    /*
    S = A
    A = B | C | X
    B = D | E
    C = F | G | A
    D = H | A
    E = I
    F = I
    G = A
    H = D | K
    I = "t" | K | G
    K = A
    X = A
     */

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _B = grammar.getNonterminal("B");
        NonterminalSymbol _C = grammar.getNonterminal("C");
        NonterminalSymbol _D = grammar.getNonterminal("D");
        NonterminalSymbol _E = grammar.getNonterminal("E");
        NonterminalSymbol _F = grammar.getNonterminal("F");
        NonterminalSymbol _G = grammar.getNonterminal("G");
        NonterminalSymbol _H = grammar.getNonterminal("H");
        NonterminalSymbol _I = grammar.getNonterminal("I");
        NonterminalSymbol _K = grammar.getNonterminal("K");
        NonterminalSymbol _X = grammar.getNonterminal("X");

        grammar.addRule(_S, _A);

        grammar.addRule(_A, _B);
        grammar.addRule(_A, _C);
        grammar.addRule(_A, _X);

        grammar.addRule(_B, _D);
        grammar.addRule(_B, _E);

        grammar.addRule(_C, _F);
        grammar.addRule(_C, _G);
        grammar.addRule(_C, _A);

        grammar.addRule(_D, _H);
        grammar.addRule(_D, _A);

        grammar.addRule(_E, _I);

        grammar.addRule(_F, _I);

        grammar.addRule(_G, _A);

        grammar.addRule(_H, _D);
        grammar.addRule(_H, _K);

        grammar.addRule(_I, TerminalSymbol.ch('t'));
        grammar.addRule(_I, _K);

        grammar.addRule(_K, _A);
        grammar.addRule(_X, _A);

        GearleyParser parser = grammar.getParser(options, _S);
        GearleyResult result = parser.parse(Iterators.characterIterator("t"));

        Assert.assertTrue(result.succeeded());
        Assert.assertEquals(10, result.getForest().getParseTreeCount());

        //result.getForest().serialize("horror.xml");

        expectTrees(result.getForest().getWalker(), Arrays.asList(
                "<S><A><B><E><I>t</I></E></B></A></S>",
                "<S><A><B><E><I><K><A><B><E><I>t</I></E></B></A></K></I></E></B></A></S>",
                "<S><A><B><D><A><B><E><I>t</I></E></B></A></D></B></A></S>",
                "<S><A><B><D><A><B><E><I><K><A><B><E><I>t</I></E></B></A></K></I></E></B></A></D></B></A></S>",
                "<S><A><B><D><H><D><A><B><E><I>t</I></E></B></A></D></H></D></B></A></S>",
                "<S><A><B><D><H><D><A><B><E><I><K><A><B><E><I>t</I></E></B></A></K></I></E></B></A></D></H></D></B></A></S>",
                "<S><A><B><D><H><K><A><B><E><I>t</I></E></B></A></K></H></D></B></A></S>",
                "<S><A><B><D><H><K><A><B><E><I><K><A><B><E><I>t</I></E></B></A></K></I></E></B></A></K></H></D></B></A></S>",
                "<S><A><C><F><I>t</I></F></C></A></S>",
                "<S><A><C><F><I><K><A><B><E><I>t</I></E></B></A></K></I></F></C></A></S>",
                "<S><A><C><F><I><K><A><B><D><A><B><E><I>t</I></E></B></A></D></B></A></K></I></F></C></A></S>",
                "<S><A><C><F><I><K><A><B><D><H><D><A><B><E><I>t</I></E></B></A></D></H></D></B></A></K></I></F></C></A></S>",
                "<S><A><C><F><I><K><A><B><D><H><K><A><B><E><I>t</I></E></B></A></K></H></D></B></A></K></I></F></C></A></S>",
                "<S><A><C><A><B><E><I>t</I></E></B></A></C></A></S>",
                "<S><A><C><A><B><E><I><K><A><B><E><I>t</I></E></B></A></K></I></E></B></A></C></A></S>",
                "<S><A><C><A><B><D><A><B><E><I>t</I></E></B></A></D></B></A></C></A></S>",
                "<S><A><C><A><B><D><A><B><E><I><K><A><B><E><I>t</I></E></B></A></K></I></E></B></A></D></B></A></C></A></S>",
                "<S><A><C><A><B><D><H><D><A><B><E><I>t</I></E></B></A></D></H></D></B></A></C></A></S>",
                "<S><A><C><A><B><D><H><D><A><B><E><I><K><A><B><E><I>t</I></E></B></A></K></I></E></B></A></D></H></D></B></A></C></A></S>",
                "<S><A><C><A><B><D><H><K><A><B><E><I>t</I></E></B></A></K></H></D></B></A></C></A></S>",
                "<S><A><C><A><B><D><H><K><A><B><E><I><K><A><B><E><I>t</I></E></B></A></K></I></E></B></A></K></H></D></B></A></C></A></S>",
                "<S><A><C><G><A><B><E><I>t</I></E></B></A></G></C></A></S>",
                "<S><A><C><G><A><B><E><I><K><A><B><E><I>t</I></E></B></A></K></I></E></B></A></G></C></A></S>",
                "<S><A><C><G><A><B><D><A><B><E><I>t</I></E></B></A></D></B></A></G></C></A></S>",
                "<S><A><C><G><A><B><D><A><B><E><I><K><A><B><E><I>t</I></E></B></A></K></I></E></B></A></D></B></A></G></C></A></S>",
                "<S><A><C><G><A><B><D><H><D><A><B><E><I>t</I></E></B></A></D></H></D></B></A></G></C></A></S>",
                "<S><A><C><G><A><B><D><H><D><A><B><E><I><K><A><B><E><I>t</I></E></B></A></K></I></E></B></A></D></H></D></B></A></G></C></A></S>",
                "<S><A><C><G><A><B><D><H><K><A><B><E><I>t</I></E></B></A></K></H></D></B></A></G></C></A></S>",
                "<S><A><C><G><A><B><D><H><K><A><B><E><I><K><A><B><E><I>t</I></E></B></A></K></I></E></B></A></K></H></D></B></A></G></C></A></S>",
                "<S><A><X><A><B><E><I>t</I></E></B></A></X></A></S>",
                "<S><A><X><A><B><E><I><K><A><B><E><I>t</I></E></B></A></K></I></E></B></A></X></A></S>",
                "<S><A><X><A><B><D><A><B><E><I>t</I></E></B></A></D></B></A></X></A></S>",
                "<S><A><X><A><B><D><A><B><E><I><K><A><B><E><I>t</I></E></B></A></K></I></E></B></A></D></B></A></X></A></S>",
                "<S><A><X><A><B><D><H><D><A><B><E><I>t</I></E></B></A></D></H></D></B></A></X></A></S>",
                "<S><A><X><A><B><D><H><D><A><B><E><I><K><A><B><E><I>t</I></E></B></A></K></I></E></B></A></D></H></D></B></A></X></A></S>",
                "<S><A><X><A><B><D><H><K><A><B><E><I>t</I></E></B></A></K></H></D></B></A></X></A></S>",
                "<S><A><X><A><B><D><H><K><A><B><E><I><K><A><B><E><I>t</I></E></B></A></K></I></E></B></A></K></H></D></B></A></X></A></S>"));
    }

    @Test
    public void smallerhorror() {
        SourceGrammar grammar = new SourceGrammar();

    /*
    S = A
    A = B | C
    B = D
    C = D
    D ="t" | A
     */

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _B = grammar.getNonterminal("B");
        NonterminalSymbol _C = grammar.getNonterminal("C");
        NonterminalSymbol _D = grammar.getNonterminal("D");

        grammar.addRule(_S, _A);

        grammar.addRule(_A, _B);
        grammar.addRule(_A, _C);

        grammar.addRule(_B, _D);

        grammar.addRule(_C, _D);

        grammar.addRule(_D, _A);
        grammar.addRule(_D, TerminalSymbol.ch('t'));

        GearleyParser parser = grammar.getParser(options, _S);
        GearleyResult result = parser.parse(Iterators.characterIterator("t"));

        Assert.assertTrue(result.succeeded());
        Assert.assertEquals(4, result.getForest().getParseTreeCount());

        //result.getForest().serialize("smaller.xml");

        expectTrees(result.getForest().getWalker(), Arrays.asList(
                "<S><A><B><D>t</D></B></A></S>",
                "<S><A><B><D><A><B><D>t</D></B></A></D></B></A></S>",
                "<S><A><C><D>t</D></C></A></S>",
                "<S><A><C><D><A><B><D>t</D></B></A></D></C></A></S>"));
    }

    @Test
    public void mediumhorror() {
        SourceGrammar grammar = new SourceGrammar();

    /*
    S = A | X
    A = B | C
    B = D
    C = D
    D = "t" | A
    X = D
     */

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _B = grammar.getNonterminal("B");
        NonterminalSymbol _C = grammar.getNonterminal("C");
        NonterminalSymbol _D = grammar.getNonterminal("D");
        NonterminalSymbol _X = grammar.getNonterminal("X");

        grammar.addRule(_S, _A);
        grammar.addRule(_S, _X);

        grammar.addRule(_A, _B);
        grammar.addRule(_A, _C);
        grammar.addRule(_A, TerminalSymbol.ch('t'));

        grammar.addRule(_B, _D);

        grammar.addRule(_C, _D);

        grammar.addRule(_D, _A);
        grammar.addRule(_D, TerminalSymbol.ch('t'));

        grammar.addRule(_X, _D);

        GearleyParser parser = grammar.getParser(options, _S);
        GearleyResult result = parser.parse(Iterators.characterIterator("t"));

        Assert.assertTrue(result.succeeded());
        Assert.assertEquals(9, result.getForest().getParseTreeCount());

        //result.getForest().serialize("medium.xml");

        expectTrees(result.getForest().getWalker(), Arrays.asList(
                "<S><A>t</A></S>",
                "<S><A><B><D>t</D></B></A></S>",
                "<S><A><B><D><A>t</A></D></B></A></S>",
                "<S><A><C><D>t</D></C></A></S>",
                "<S><A><C><D><A>t</A></D></C></A></S>",
                "<S><X><D>t</D></X></S>",
                "<S><X><D><A>t</A></D></X></S>",
                "<S><X><D><A><B><D>t</D></B></A></D></X></S>",
                "<S><X><D><A><C><D>t</D></C></A></D></X></S>"));
    }

    @Test
    public void tinyloop() {
        SourceGrammar grammar = new SourceGrammar();

    /*
                  S = A .
                  A = B | 't' .
                  B = A .
     */

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _B = grammar.getNonterminal("B");

        grammar.addRule(_S, _A);
        grammar.addRule(_A, _B);
        grammar.addRule(_A, TerminalSymbol.ch('t'));
        grammar.addRule(_B, _A);

        GearleyParser parser = grammar.getParser(options, _S);
        GearleyResult result = parser.parse(Iterators.characterIterator("t"));

        Assert.assertTrue(result.succeeded());
        Assert.assertEquals(2, result.getForest().getParseTreeCount());

        //result.getForest().serialize("tinyloop.xml");

        expectTrees(result.getForest().getWalker(), Arrays.asList(
                "<S><A>t</A></S>",
                "<S><A><B><A>t</A></B></A></S>"));
    }

}
