package org.nineml.coffeegrinder;

import org.junit.Assert;
import org.junit.Test;
import org.nineml.coffeegrinder.parser.*;
import org.nineml.coffeegrinder.util.GrammarParser;
import org.nineml.coffeegrinder.util.Iterators;

public class GrammarParserTest {
    @Test
    public void testParse1() {
        String input = "-_0 => ixml\n" +
                "^ixml => s, _3, s\n" +
                "-s => _18\n" +
                "-whitespace => [Zs]\n" +
                "-whitespace => tab\n" +
                "-tab => #9\n" +
                "^comment => \"{\", _20, \"}\"\n" +
                "-cchar => ~[\"{\"; \"}\"]\n" +
                "^rule => _32_option, name, s, [\"=\"; \":\"], s, alts, \".\"\n" +
                "@mark => [\"@\"; \"^\"; \"-\"]\n" +
                "-factor => \"(\", s, alts, \")\", s\n" +
                "^repeat0 => factor, \"*\", s, sep\n" +
                "^repeat1 => factor, \"+\", s, sep\n" +
                "-namefollower => [\"-\"; \".\"; \"·\"; \"‿\"; \"⁀\"; Nd; Mn]\n" +
                "^string => '\"', dstring, '\"', s\n" +
                "^string => \"'\", sstring, \"'\", s\n" +
                "-_15 => [\"0\"-\"9\"; \"a\"-\"f\"; \"A\"-\"F\"], _16\n";
        GrammarParser parser = new GrammarParser();
        Grammar grammar = parser.parse(input);
        Assert.assertNotNull(grammar);
    }

    @Test
    public void testParseExpression() {
        GrammarParser gparser = new GrammarParser();
        Grammar grammar = gparser.parseFile("src/test/resources/expression.grammar");
        Assert.assertNotNull(grammar);
        GearleyParser parser = grammar.getParser(grammar.getNonterminal("Sum"));
        GearleyResult result = parser.parse(Iterators.characterIterator("1+(2*3-4)"));
        Assert.assertTrue(result.succeeded());
    }


}
