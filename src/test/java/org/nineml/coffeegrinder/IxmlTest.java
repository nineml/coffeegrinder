package org.nineml.coffeegrinder;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.nineml.coffeegrinder.parser.EarleyParser;
import org.nineml.coffeegrinder.parser.EarleyResult;
import org.nineml.coffeegrinder.parser.Grammar;
import org.nineml.coffeegrinder.parser.ParserOptions;
import org.nineml.coffeegrinder.util.GrammarCompiler;
import org.nineml.coffeegrinder.util.GrammarParser;
import org.nineml.coffeegrinder.util.Iterators;

import java.io.File;
import java.io.FileInputStream;
import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.fail;

public class IxmlTest {
    @Test
    public void testRuleParse() {
        try {
            ParserOptions options = new ParserOptions();
            options.treesWithStates = true;
            Grammar grammar = new GrammarCompiler().parse(new File("src/test/resources/ixml.cxml"));
            grammar.setParserOptions(options);

            String input = "date: s?, day, s, month, (s, year)? .";

            EarleyParser parser = grammar.getParser(grammar.getNonterminal("$$"));
            EarleyResult result = parser.parse(Iterators.characterIterator(input));

            Assert.assertTrue(result.succeeded());
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void testDateParse() {
        try {
            Grammar grammar = new GrammarCompiler().parse(new File("src/test/resources/ixml.cxml"));

            String input = "date: s?, day, s, month, (s, year)? .\n" +
                    "-s: -\" \"+ .\n" +
                    "day: digit, digit? .\n" +
                    "-digit: \"0\"; \"1\"; \"2\"; \"3\"; \"4\"; \"5\"; \"6\"; \"7\"; \"8\"; \"9\".\n" +
                    "month: \"January\"; \"February\"; \"March\"; \"April\";\n" +
                    "       \"May\"; \"June\"; \"July\"; \"August\";\n" +
                    "       \"September\"; \"October\"; \"November\"; \"December\".\n" +
                    "year: ((digit, digit); -\"'\")?, digit, digit .";

            EarleyParser parser = grammar.getParser(grammar.getNonterminal("$$"));
            EarleyResult result = parser.parse(Iterators.characterIterator(input));
            System.out.printf("Duration: %dms\n", result.getParseTime());

            //result.getForest().serialize("graph.xml");
            //result.getForest().parse().serialize("tree.xml");

            Assert.assertTrue(result.succeeded());
        } catch (Exception ex) {
            fail();
        }
    }

    @Ignore
    public void testIxmlParse() {
        try {
            GrammarParser gparser = new GrammarParser();
            Grammar grammar = gparser.parseFile("src/test/resources/ixml.grammar");
            //grammar.getParseListener().setMessageLevel(ParseListener.DEBUG);

            FileInputStream fis = new FileInputStream(new File("src/test/resources/ixml.ixml"));
            StringBuilder sb = new StringBuilder();
            int ch = fis.read();
            while (ch >= 0) {
                sb.appendCodePoint(ch);
                ch = fis.read();
            }

            long start = Calendar.getInstance().getTimeInMillis();

            String input = sb.toString();
            EarleyParser parser = grammar.getParser(grammar.getNonterminal("_0"));
            EarleyResult result = parser.parse(Iterators.characterIterator(input));

            long end = Calendar.getInstance().getTimeInMillis();

            System.err.println("Duration: " + (end-start));

            /*
            File date_xml = new File("ixml.xml");
            FileOutputStream fos = new FileOutputStream(date_xml);
            PrintStream ps = new PrintStream(fos);
            result.getSPPF().serialize(ps);
            ps.close();
            fos.close();
            */

            Assert.assertTrue(result.succeeded());
        } catch (Exception ex) {
            fail();
        }
    }
}
