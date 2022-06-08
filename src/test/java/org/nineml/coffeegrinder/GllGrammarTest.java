package org.nineml.coffeegrinder;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.nineml.coffeegrinder.parser.*;
import org.nineml.coffeegrinder.tokens.CharacterSet;
import org.nineml.coffeegrinder.tokens.TokenCharacter;
import org.nineml.coffeegrinder.tokens.TokenCharacterSet;
import org.nineml.coffeegrinder.tokens.TokenRegex;
import org.nineml.coffeegrinder.util.ParserAttribute;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;

import static org.junit.Assert.fail;

public class GllGrammarTest {
    @Test
    public void testBsrExample54() {
        Grammar grammar = new Grammar();

        NonterminalSymbol S = grammar.getNonterminal("S");
        NonterminalSymbol A = grammar.getNonterminal("A");
        NonterminalSymbol B = grammar.getNonterminal("B");
        NonterminalSymbol C = grammar.getNonterminal("C");
        TerminalSymbol a = new TerminalSymbol(TokenCharacter.get('a'));
        TerminalSymbol b = new TerminalSymbol(TokenCharacter.get('b'));

        grammar.addRule(S, A, C, a, B);
        grammar.addRule(S, A, B, a, a);
        grammar.addRule(A, a, A);
        grammar.addRule(A, a);
        grammar.addRule(B, b, B);
        grammar.addRule(B, b);
        grammar.addRule(C, b, C);
        grammar.addRule(C, b);

        grammar.close(S);

        GearleyParser parser = grammar.getParser(ParserType.GLL, S);
        GearleyResult result = parser.parse("abaa");

        Assert.assertTrue(result.succeeded());
    }

    @Test
    public void testGrammar0() {
        org.nineml.coffeegrinder.parser.Grammar grammar = new org.nineml.coffeegrinder.parser.Grammar();

        NonterminalSymbol S = grammar.getNonterminal("S");
        NonterminalSymbol B = grammar.getNonterminal("B");
        TerminalSymbol a = new TerminalSymbol(TokenCharacter.get('a'));
        TerminalSymbol b = new TerminalSymbol(TokenCharacter.get('b'));

        grammar.addRule(S, a, B);
        grammar.addRule(B, b);
        grammar.close(S);

        GearleyParser parser = grammar.getParser(ParserType.GLL, S);
        GearleyResult result = parser.parse("ab");
        Assert.assertTrue(result.succeeded());
    }

    @Test
    public void testGrammar1() {
        org.nineml.coffeegrinder.parser.Grammar grammar = new org.nineml.coffeegrinder.parser.Grammar();

        NonterminalSymbol S = grammar.getNonterminal("S");
        TerminalSymbol a = new TerminalSymbol(TokenCharacter.get('a'));
        TerminalSymbol d = new TerminalSymbol(TokenCharacter.get('d'));

        grammar.addRule(S, d);
        grammar.addRule(S, S, a);
        grammar.close(S);

        GearleyParser parser = grammar.getParser(ParserType.GLL, S);
        GearleyResult result = parser.parse("da");

        Assert.assertTrue(result.succeeded());
    }

    @Test
    public void testGrammar2() {
        org.nineml.coffeegrinder.parser.Grammar grammar = new org.nineml.coffeegrinder.parser.Grammar();

        NonterminalSymbol S = grammar.getNonterminal("S");
        NonterminalSymbol A = grammar.getNonterminal("A");
        NonterminalSymbol B = grammar.getNonterminal("B");
        NonterminalSymbol C = grammar.getNonterminal("C");
        NonterminalSymbol X = grammar.getNonterminal("X");
        NonterminalSymbol Y = grammar.getNonterminal("Y");
        NonterminalSymbol Z = grammar.getNonterminal("Z");
        TerminalSymbol a = new TerminalSymbol(TokenCharacter.get('a'));
        TerminalSymbol b = new TerminalSymbol(TokenCharacter.get('b'));

        grammar.addRule(S, A, C, a, A);
        grammar.addRule(S, A, B, a, X);
        grammar.addRule(A, a, A);
        grammar.addRule(A, a);
        grammar.addRule(B, b, B);
        grammar.addRule(B, b);
        grammar.addRule(C, b, C);
        grammar.addRule(C, b);

        grammar.addRule(X, Y);
        grammar.addRule(X, Z);

        grammar.addRule(Y, A);
        grammar.addRule(Y, B);

        grammar.addRule(Z, A);
        grammar.addRule(Z, B);
        grammar.addRule(Z, a);

        grammar.close(S);

        GearleyParser parser = grammar.getParser(ParserType.GLL, S);
        GearleyResult result = parser.parse("abaa");

        Assert.assertTrue(result.succeeded());
    }

    @Test
    public void testGrammar3() {
        org.nineml.coffeegrinder.parser.Grammar grammar = new org.nineml.coffeegrinder.parser.Grammar();

        NonterminalSymbol S = grammar.getNonterminal("S");
        NonterminalSymbol A = grammar.getNonterminal("A");
        NonterminalSymbol B = grammar.getNonterminal("B");
        NonterminalSymbol C = grammar.getNonterminal("C");
        NonterminalSymbol X = grammar.getNonterminal("X");
        NonterminalSymbol Y = grammar.getNonterminal("Y");
        NonterminalSymbol Z = grammar.getNonterminal("Z");
        TerminalSymbol a = new TerminalSymbol(TokenCharacter.get('a'));
        TerminalSymbol b = new TerminalSymbol(TokenCharacter.get('b'));

        grammar.addRule(S, A, C, a, A);
        grammar.addRule(S, A, B, a, X);
        grammar.addRule(A, a, A);
        grammar.addRule(A, a);
        grammar.addRule(B, b, B);
        grammar.addRule(B, b);
        grammar.addRule(C, b, C);
        grammar.addRule(C, b);

        grammar.addRule(X, Y);
        grammar.addRule(X, Z);

        grammar.addRule(Y, A);
        grammar.addRule(Y, B);

        grammar.addRule(Z, A);
        grammar.addRule(Z, B);
        grammar.addRule(Z, a);

        grammar.close(S);

        GearleyParser parser = grammar.getParser(ParserType.GLL, S);
        GearleyResult result = parser.parse("abaa");

        Assert.assertTrue(result.succeeded());
    }

    @Test
    public void testGrammar4() {
        org.nineml.coffeegrinder.parser.Grammar grammar = new org.nineml.coffeegrinder.parser.Grammar();

        NonterminalSymbol S = grammar.getNonterminal("S");
        NonterminalSymbol A = grammar.getNonterminal("A");
        NonterminalSymbol B = grammar.getNonterminal("B");
        TerminalSymbol a = new TerminalSymbol(TokenCharacter.get('a'));
        TerminalSymbol b = new TerminalSymbol(TokenCharacter.get('b'));
        TerminalSymbol c = new TerminalSymbol(TokenCharacter.get('c'));

        grammar.addRule(S, a, A, B);
        grammar.addRule(S, a, A, b);
        grammar.addRule(A, a);
        grammar.addRule(A, c);
        grammar.addRule(A);
        grammar.addRule(B, b);
        grammar.addRule(B, B, c);
        grammar.addRule(B);

        grammar.close(S);

        GearleyParser parser = grammar.getParser(ParserType.GLL, S);
        GearleyResult result = parser.parse("aab");

        Assert.assertTrue(result.succeeded());
    }

    @Test
    public void testGrammar5() {
        Grammar grammar = new org.nineml.coffeegrinder.parser.Grammar();

        NonterminalSymbol S = grammar.getNonterminal("S");
        NonterminalSymbol A = grammar.getNonterminal("A");
        NonterminalSymbol B = grammar.getNonterminal("B");
        NonterminalSymbol C = grammar.getNonterminal("C");
        TerminalSymbol semi = new TerminalSymbol(TokenCharacter.get(';'));
        TerminalSymbol value = new TerminalSymbol(TokenRegex.get("[^;]"));

        grammar.addRule(S, A, semi, B, semi, C);
        grammar.addRule(A, value);
        grammar.addRule(B, value);
        grammar.addRule(B);
        grammar.addRule(C, value);

        grammar.close(S);

        GearleyParser parser = grammar.getParser(ParserType.GLL, S);
        GearleyResult result = parser.parse("a;;c");

        Assert.assertTrue(result.succeeded());
    }

    @Test
    public void testGrammar6() {
        org.nineml.coffeegrinder.parser.Grammar grammar = new org.nineml.coffeegrinder.parser.Grammar();

        NonterminalSymbol S = grammar.getNonterminal("S");
        NonterminalSymbol A = grammar.getNonterminal("A");
        NonterminalSymbol B = grammar.getNonterminal("B");
        NonterminalSymbol C = grammar.getNonterminal("C");
        TerminalSymbol a = new TerminalSymbol(TokenCharacter.get('a'));
        TerminalSymbol b = new TerminalSymbol(TokenCharacter.get('b'));
        TerminalSymbol c = new TerminalSymbol(TokenCharacter.get('c'));

        grammar.addRule(S, A, B, C);
        grammar.addRule(A, a, A);
        grammar.addRule(A, a);
        grammar.addRule(B, b, B);
        grammar.addRule(B, b);
        grammar.addRule(B);
        grammar.addRule(C, c, C);
        grammar.addRule(C, c);

        grammar.close(S);

        GearleyParser parser = grammar.getParser(ParserType.GLL, S);
        GearleyResult result = parser.parse("ac");

        Assert.assertTrue(result.succeeded());
    }

    @Test
    public void testGrammar7() {
        org.nineml.coffeegrinder.parser.Grammar grammar = new org.nineml.coffeegrinder.parser.Grammar();

        NonterminalSymbol S = grammar.getNonterminal("S");
        NonterminalSymbol A = grammar.getNonterminal("A");
        NonterminalSymbol B = grammar.getNonterminal("B");
        NonterminalSymbol C = grammar.getNonterminal("C");
        TerminalSymbol a = new TerminalSymbol(TokenCharacter.get('a'));
        TerminalSymbol b = new TerminalSymbol(TokenCharacter.get('b'));
        TerminalSymbol c = new TerminalSymbol(TokenCharacter.get('c'));

        grammar.addRule(S, A, B, C);
        grammar.addRule(A, a, A);
        grammar.addRule(A, a);
        grammar.addRule(B, b, B);
        grammar.addRule(B, b);
        grammar.addRule(B, C);
        grammar.addRule(B);
        grammar.addRule(C, c, C);
        grammar.addRule(C, c);
        grammar.addRule(C, B);

        grammar.close(S);

        GearleyParser parser = grammar.getParser(ParserType.GLL, S);
        GearleyResult result = parser.parse("ac");

        Assert.assertTrue(result.succeeded());
    }

    @Test
    public void testAttributes() {
        Grammar grammar = new Grammar();

        NonterminalSymbol S = grammar.getNonterminal("S");
        NonterminalSymbol A = grammar.getNonterminal("A");
        NonterminalSymbol B1 = grammar.getNonterminal("B", new ParserAttribute("number", "one"));
        NonterminalSymbol B2 = grammar.getNonterminal("B", new ParserAttribute("number", "two"));
        TerminalSymbol a = new TerminalSymbol(TokenCharacter.get('a'));
        TerminalSymbol b = new TerminalSymbol(TokenCharacter.get('b'));

        grammar.addRule(S, A, B1);
        grammar.addRule(A, a);
        grammar.addRule(B2, b);

        grammar.close(S);

        GearleyParser parser = grammar.getParser(ParserType.Earley, S);
        GearleyResult result = parser.parse("ab");

        Assert.assertTrue(result.succeeded());
    }

    @Test
    public void testFirstAndFollow() {
        org.nineml.coffeegrinder.parser.Grammar grammar = new org.nineml.coffeegrinder.parser.Grammar();

        NonterminalSymbol S = grammar.getNonterminal("S");
        NonterminalSymbol ixml = grammar.getNonterminal("ixml");
        NonterminalSymbol rule = grammar.getNonterminal("rule");
        TerminalSymbol namestart = new TerminalSymbol(TokenCharacterSet.inclusion(CharacterSet.range('A', 'Z')));
        TerminalSymbol namefollow = new TerminalSymbol(TokenCharacterSet.inclusion(CharacterSet.range('a', 'z')));
        NonterminalSymbol name = grammar.getNonterminal("name");
        NonterminalSymbol more = grammar.getNonterminal("more");

        grammar.addRule(S, ixml);
        grammar.addRule(ixml, rule);
        grammar.addRule(rule, name);
        grammar.addRule(name, namestart, more);
        grammar.addRule(more, namestart, more);
        grammar.addRule(more, namefollow, more);
        grammar.addRule(more);

        grammar.close(S);

        GearleyParser parser = grammar.getParser(ParserType.GLL, S);
        GearleyResult result = parser.parse("Ab");

        Assert.assertTrue(result.succeeded());
    }

    @Test
    public void testInsertionNT() {
        Grammar grammar = new Grammar();

        NonterminalSymbol S = grammar.getNonterminal("S");
        NonterminalSymbol A = grammar.getNonterminal("A");
        NonterminalSymbol B = grammar.getNonterminal("B");
        NonterminalSymbol I = grammar.getNonterminal("INSERTION", new ParserAttribute("insert", "text"));
        TerminalSymbol a = new TerminalSymbol(TokenCharacter.get('a'));
        TerminalSymbol b = new TerminalSymbol(TokenCharacter.get('b'));

        grammar.addRule(S, A, I, B);
        grammar.addRule(A, a);
        grammar.addRule(B, b);
        grammar.addRule(I);

        grammar.close(S);

        GearleyParser parser = grammar.getParser(ParserType.GLL, S);
        GearleyResult result = parser.parse("ab");

        Assert.assertTrue(result.succeeded());

        ParseTree tree = result.getForest().parse();
        Symbol s_insertion = tree.getChildren().get(0).getChildren().get(1).getSymbol();

        Assert.assertEquals(s_insertion, grammar.getNonterminal("INSERTION"));
        Assert.assertEquals("text", s_insertion.getAttributeValue("insert", "failed"));
    }

    @Ignore
    public void testParseUnicode() {
        Grammar grammar = new Grammar();

/*
S ⇒ UnicodeData
UnicodeData ⇒ record_pp_LF, LF
record ⇒ codepoint, ';', name, ';', category, ';', combining, ';', bidi, ';', decomposition, ';', decimal, ';', digit, ';', numeric, ';', mirrored, ';', name_1_0, ';', comment, ';', uppercase, ';', lowercase, ';', titlecase
LF ⇒ '\n'
bidi ⇒ value
category ⇒ value
codepoint ⇒ hexPlus
combining ⇒ value
comment ⇒ value
decimal ⇒ value
decomposition ⇒ value
digit ⇒ value
hexPlus ⇒ ["0"-"9";"A"-"F"], hex_opt
hex_opt ⇒
hex_opt ⇒ hex_rep
hex_rep ⇒ ["0"-"9";"A"-"F"], hex_opt
lowercase ⇒ value
mirrored ⇒ value
name ⇒ notSemiPlus
name_1_0 ⇒ value
notSemiPlus ⇒ ~[";"], optMoreNotSemi
notSemiRep ⇒ ~[";"]
notSemiRep ⇒ ~[";"], notSemiRep
notSemiStar ⇒
notSemiStar ⇒ notSemiRep
numeric ⇒ value
optMoreNotSemi ⇒
optMoreNotSemi ⇒ optNextNotSemi
optNextNotSemi ⇒ ~[";"], optMoreNotSemi
opt_LF_record ⇒
opt_LF_record ⇒ opt_record
opt_record ⇒ LF, record
opt_record ⇒ LF, record, opt_record
record_pp_LF ⇒ record
record_pp_LF ⇒ record, opt_LF_record
titlecase ⇒ value
uppercase ⇒ value
value ⇒
value ⇒ notSemiStar
*/

        NonterminalSymbol S = grammar.getNonterminal("S");
        NonterminalSymbol UnicodeData = grammar.getNonterminal("UnicodeData");
        NonterminalSymbol bidi = grammar.getNonterminal("bidi");
        NonterminalSymbol category = grammar.getNonterminal("category");
        NonterminalSymbol codepoint = grammar.getNonterminal("codepoint");
        NonterminalSymbol combining = grammar.getNonterminal("combining");
        NonterminalSymbol comment = grammar.getNonterminal("comment");
        NonterminalSymbol decimal = grammar.getNonterminal("decimal");
        NonterminalSymbol decomposition = grammar.getNonterminal("decomposition");
        NonterminalSymbol digit = grammar.getNonterminal("digit");
        NonterminalSymbol hexPlus = grammar.getNonterminal("hexPlus");
        NonterminalSymbol hex_opt = grammar.getNonterminal("hex_opt");
        NonterminalSymbol hex_rep = grammar.getNonterminal("hex_rep");
        NonterminalSymbol lowercase = grammar.getNonterminal("lowercase");
        NonterminalSymbol mirrored = grammar.getNonterminal("mirrored");
        NonterminalSymbol name = grammar.getNonterminal("name");
        NonterminalSymbol name_1_0 = grammar.getNonterminal("name_1_0");
        NonterminalSymbol notSemiPlus = grammar.getNonterminal("notSemiPlus");
        NonterminalSymbol notSemiRep = grammar.getNonterminal("notSemiRep");
        NonterminalSymbol notSemiStar = grammar.getNonterminal("notSemiStar");
        NonterminalSymbol numeric = grammar.getNonterminal("numeric");
        NonterminalSymbol optMoreNotSemi = grammar.getNonterminal("optMoreNotSemi");
        NonterminalSymbol optNextNotSemi = grammar.getNonterminal("optNextNotSemi");
        NonterminalSymbol opt_LF_record = grammar.getNonterminal("opt_LF_record");
        NonterminalSymbol opt_record = grammar.getNonterminal("opt_record");
        NonterminalSymbol record = grammar.getNonterminal("record");
        NonterminalSymbol record_pp_LF = grammar.getNonterminal("record_pp_LF");
        NonterminalSymbol titlecase = grammar.getNonterminal("titlecase");
        NonterminalSymbol uppercase = grammar.getNonterminal("uppercase");
        NonterminalSymbol value = grammar.getNonterminal("value");

        TerminalSymbol LF = TerminalSymbol.ch('\n');
        TerminalSymbol semi = TerminalSymbol.ch(';');
        TerminalSymbol notsemi = TerminalSymbol.regex("[^;]");
        TerminalSymbol notsemix = TerminalSymbol.regex("[^;]*");
        TerminalSymbol hexdigit = TerminalSymbol.regex("[0-9,A-F]");

        // 0000;<control>;Cc;0;BN;;;;;N;NULL;;;;
        grammar.addRule(S, UnicodeData);
        grammar.addRule(UnicodeData, record_pp_LF, LF);
        grammar.addRule(record, codepoint, semi, name, semi, category, semi, combining, semi, bidi, semi,
                decomposition, semi, decimal, semi, digit, semi, numeric, semi, mirrored, semi, name_1_0,
                semi, comment, semi, uppercase, semi, lowercase, semi, titlecase);
        grammar.addRule(bidi, value);
        grammar.addRule(category, value);
        grammar.addRule(codepoint, hexPlus);
        grammar.addRule(combining, value);
        grammar.addRule(comment, value);
        grammar.addRule(decimal, value);
        grammar.addRule(decomposition, value);
        grammar.addRule(digit, value);
        grammar.addRule(hexPlus, hexdigit, hex_opt);
        grammar.addRule(hex_opt);
        grammar.addRule(hex_opt, hex_rep);
        grammar.addRule(hex_rep, hexdigit, hex_opt);
        grammar.addRule(lowercase, value);
        grammar.addRule(mirrored, value);
        grammar.addRule(name, notSemiPlus);
        grammar.addRule(name_1_0, value);
        grammar.addRule(notSemiPlus, notsemi, optMoreNotSemi);
        grammar.addRule(notSemiRep, notsemi);
        grammar.addRule(notSemiRep, notsemi, notSemiRep);
        grammar.addRule(notSemiStar);
        grammar.addRule(notSemiStar, notSemiRep);
        grammar.addRule(numeric, value);
        grammar.addRule(optMoreNotSemi);
        grammar.addRule(optMoreNotSemi, optNextNotSemi);
        grammar.addRule(optNextNotSemi, notsemi, optMoreNotSemi);
        grammar.addRule(opt_LF_record);
        grammar.addRule(opt_LF_record, opt_record);
        grammar.addRule(opt_record, LF, record);
        grammar.addRule(opt_record, LF, record, opt_record);
        //grammar.addRule(record_pp_LF, record);
        grammar.addRule(record_pp_LF, record, opt_LF_record);
        grammar.addRule(titlecase, value);
        grammar.addRule(uppercase, value);
        grammar.addRule(value);
        grammar.addRule(value, notSemiStar);

        grammar.close(S);

        try {
            GearleyParser gllParser = grammar.getParser(ParserType.GLL, S);

            int lineCount = 0;

            //BufferedInputStream stream = new BufferedInputStream(Files.newInputStream(Paths.get("src/test/resources/SmallData.txt")));
            BufferedReader reader = new BufferedReader(new FileReader("src/test/resources/SmallData.txt"));
            StringBuilder sb = new StringBuilder();
            String line = reader.readLine();
            while (line != null) {
                sb.append(line).append("\n");
                lineCount++;
                line = reader.readLine();
            }

            long start = Calendar.getInstance().getTimeInMillis();

            GearleyResult gresult = gllParser.parse(sb.toString());

            long duration = Calendar.getInstance().getTimeInMillis() - start;
            double rate = (1.0 * lineCount) / duration;

            System.err.printf("%s: %dms (%5.2f lps)%n", gresult.succeeded(), duration, rate * 1000);
            Assert.assertTrue(gresult.succeeded());
        } catch (IOException ex) {
            fail();
        }
    }
}
