package org.nineml.coffeegrinder;

import org.junit.Assert;
import org.junit.Test;
import org.nineml.coffeegrinder.parser.*;
import org.nineml.coffeegrinder.util.DefaultProgressMonitor;
import org.nineml.coffeegrinder.util.GrammarCompiler;
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
            SourceGrammar grammar = new GrammarCompiler(options).parse(new File("src/test/resources/ixml.cxml"));

            String input = "date: s?, day, s, month, (s, year)? .";

            GearleyParser parser = grammar.getParser(grammar.getNonterminal("$$"));
            GearleyResult result = parser.parse(Iterators.characterIterator(input));

            Assert.assertTrue(result.succeeded());
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void testDateParse() {
        try {
            SourceGrammar grammar = new GrammarCompiler().parse(new File("src/test/resources/ixml.cxml"));

            String input = "date: s?, day, s, month, (s, year)? .\n" +
                    "-s: -\" \"+ .\n" +
                    "day: digit, digit? .\n" +
                    "-digit: \"0\"; \"1\"; \"2\"; \"3\"; \"4\"; \"5\"; \"6\"; \"7\"; \"8\"; \"9\".\n" +
                    "month: \"January\"; \"February\"; \"March\"; \"April\";\n" +
                    "       \"May\"; \"June\"; \"July\"; \"August\";\n" +
                    "       \"September\"; \"October\"; \"November\"; \"December\".\n" +
                    "year: ((digit, digit); -\"'\")?, digit, digit .";

            GearleyParser parser = grammar.getParser(grammar.getNonterminal("$$"));
            GearleyResult result = parser.parse(Iterators.characterIterator(input));

            //result.getForest().serialize("graph.xml");
            //result.getForest().parse().serialize("tree.xml");

            Assert.assertTrue(result.succeeded());
        } catch (Exception ex) {
            fail();
        }
    }

    @Test
    public void testIxmlParse() {
        try {
            SourceGrammar grammar = new GrammarCompiler().parse(new File("src/test/resources/ixml.cxml"));

            TestProgressMonitor monitor = new TestProgressMonitor();
            grammar.getParserOptions().setProgressMonitor(monitor);

            FileInputStream fis = new FileInputStream(new File("src/test/resources/ixml.ixml"));
            StringBuilder sb = new StringBuilder();
            int ch = fis.read();
            while (ch >= 0) {
                sb.appendCodePoint(ch);
                ch = fis.read();
            }
            fis.close();

            long start = Calendar.getInstance().getTimeInMillis();

            String input = sb.toString();
            GearleyParser parser = grammar.getParser(grammar.getNonterminal("$$"));
            GearleyResult result = parser.parse(Iterators.characterIterator(input));

            long end = Calendar.getInstance().getTimeInMillis();

            //System.err.println("Duration: " + (end-start));

            /*
            File date_xml = new File("ixml.xml");
            FileOutputStream fos = new FileOutputStream(date_xml);
            PrintStream ps = new PrintStream(fos);
            result.getSPPF().serialize(ps);
            ps.close();
            fos.close();
            */

            Assert.assertTrue(monitor.started);
            Assert.assertTrue(monitor.ran);
            Assert.assertTrue(monitor.finished);
            Assert.assertTrue(result.succeeded());
        } catch (Exception ex) {
            fail();
        }
    }

    private static class TestProgressMonitor extends DefaultProgressMonitor {
        public boolean started = false;
        public boolean ran = false;
        public boolean finished = false;

        @Override
        public int starting(GearleyParser parser) {
            int size = super.starting(parser);
            started = true;
            return size;
        }

        @Override
        public void progress(GearleyParser parser, long count) {
            super.progress(parser, count);
            ran = true;
        }

        @Override
        public void finished(GearleyParser parser) {
            super.finished(parser);
            finished = true;
        }
    }
}
