package org.nineml.coffeegrinder;

import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.nineml.coffeegrinder.parser.*;
import org.nineml.coffeegrinder.tokens.*;
import org.nineml.coffeegrinder.util.GrammarCompiler;
import org.nineml.coffeegrinder.util.ParserAttribute;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static junit.framework.TestCase.fail;

public class CompilerTest {
    @Test
    public void testAttributes() {
        Grammar grammar = new Grammar();

        ArrayList<ParserAttribute> attrs = new ArrayList<>();
        attrs.add(new ParserAttribute("name", "start"));
        attrs.add(new ParserAttribute("mark", "?"));
        NonterminalSymbol _S = grammar.getNonterminal("S", attrs);
        NonterminalSymbol _E = grammar.getNonterminal("E", new ParserAttribute("http://example.com/name", "a\n'b'"));
        TerminalSymbol _plus = new TerminalSymbol(TokenCharacter.get('+', new ParserAttribute("name", "plus")));

        grammar.addRule(_S, _E);
        grammar.addRule(_E, _E, _plus, _E);
        grammar.addRule(_E, TerminalSymbol.regex("[0-9]"));

        ArrayList<CharacterSet> numbers = new ArrayList<>();
        numbers.add(CharacterSet.unicodeClass("Nd"));
        numbers.add(CharacterSet.unicodeClass("Nl"));
        numbers.add(CharacterSet.unicodeClass("No"));
        numbers.add(CharacterSet.range('0','9'));
        numbers.add(CharacterSet.literal("Sp\"o&on!"));

        grammar.addRule(_E, new TerminalSymbol(TokenCharacterSet.inclusion(numbers)));

        GrammarCompiler compiler = new GrammarCompiler();

        String compiled = compiler.compile(grammar);

        grammar = compiler.parse(compiled);

        Assert.assertNotNull(grammar);

        String compiledAgain = compiler.compile(grammar);

        Assert.assertEquals(compiled, compiledAgain);
    }

    @Test
    public void loadCompiledGrammar() {
        // To recreate hash.cxml, see hashGrammar in ParserTest
        try {
            GrammarCompiler compiler = new GrammarCompiler();
            Grammar grammar = compiler.parse(new File("src/test/resources/hash.cxml"));
            NonterminalSymbol start = grammar.getNonterminal("hashes");
            EarleyParser parser = grammar.getParser(start);

            String input = "#12.";

            EarleyResult result = parser.parse(input);

            //result.getForest().serialize("graph.xml");

            //Assert.assertEquals(1, result.getForest().getTotalParseCount());
            Assert.assertTrue(result.succeeded());
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
            fail();
        }
    }

    @Test
    public void compileGrammar() {
        String cxml = null;
        try {
            InputStreamReader reader = new InputStreamReader(new FileInputStream("src/test/resources/hash.cxml"), StandardCharsets.UTF_8);
            StringBuilder sb = new StringBuilder();
            char[] buffer = new char[4096];
            int len = reader.read(buffer);
            while (len >= 0) {
                sb.append(buffer, 0, len);
                len = reader.read(buffer);
            }
            cxml = sb.toString();
        } catch (IOException ex) {
            fail();
        }

        GrammarCompiler compiler = new GrammarCompiler();
        Grammar grammar = compiler.parse(cxml);

        /*
        System.err.println("=====");
        System.err.println(compiler.compile(grammar));
        System.err.println("=====");
         */

        NonterminalSymbol start = grammar.getNonterminal("hashes");
        EarleyParser parser = grammar.getParser(start);
        EarleyResult result = parser.parse("#12.");

        Assert.assertTrue(result.succeeded());

        compiler.setProperty("Source", "Invisible XML test suite");
        compiler.setProperty("Date", "2022-01-30");
        String compiled = compiler.compile(grammar);

        /*
        System.err.println("=====");
        System.err.println(compiled);
        System.err.println("=====");
        */

        Assert.assertEquals(cxml, compiled);
    }

    @Test
    public void compileNulls() {
        GrammarCompiler compiler = new GrammarCompiler();
        Grammar grammar = new Grammar();

        /*
        letters: letter+#0.
        letter: ['A'-'Z'; 'a'-'z'].
         */

        NonterminalSymbol letters = grammar.getNonterminal("letters");
        NonterminalSymbol letter = grammar.getNonterminal("letter");
        NonterminalSymbol optmore = grammar.getNonterminal("optmore");

        grammar.addRule(letter, new TerminalSymbol(TokenRegex.get("[a-z]")));
        grammar.addRule(letters, letter, optmore);
        grammar.addRule(optmore);
        grammar.addRule(optmore, new TerminalSymbol(TokenCharacter.get('\0')), letter, optmore);
        grammar.addRule(optmore, new TerminalSymbol(TokenString.get("\1\2\3" + (char) 0x81 + "test")), letter, optmore);

        String cxml = compiler.compile(grammar);

        grammar = compiler.parse(cxml);

        EarleyParser parser = grammar.getParser(letters);
        EarleyResult result = parser.parse("a\0b\0c");

        Assert.assertTrue(result.succeeded());

        Assertions.assertEquals(cxml, compiler.compile(grammar));
    }
}
