package org.nineml.coffeegrinder;

import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.nineml.coffeegrinder.parser.*;
import org.nineml.coffeegrinder.util.GrammarCompiler;
import org.nineml.coffeegrinder.util.StdoutTreeBuilder;
import org.nineml.coffeegrinder.util.StringTreeBuilder;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static junit.framework.TestCase.fail;

public class MarkTest {
    private String loadCxml(String filename) {
        String cxml = null;
        try {
            InputStreamReader reader = new InputStreamReader(Files.newInputStream(Paths.get(filename)), StandardCharsets.UTF_8);
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
        return cxml;
    }

    @Test
    public void parseEarley() {
        String cxml = loadCxml("src/test/resources/sg54bis.cxml");
        GrammarCompiler compiler = new GrammarCompiler();
        SourceGrammar grammar = compiler.parse(cxml);

        NonterminalSymbol S = grammar.getNonterminal("Number");

        ParserOptions options = new ParserOptions();
        options.setParserType("Earley");

        GearleyParser parser = grammar.getParser(options, S);
        GearleyResult result = parser.parse("32.5e+1");
        Assertions.assertTrue(result.succeeded());

        StringTreeBuilder builder = new StringTreeBuilder();
        result.getTree(builder);

        String tree = builder.getTree();
        int pos = tree.indexOf("<Integer");
        tree = tree.substring(pos);
        pos = tree.indexOf("<Fraction");
        tree = tree.substring(0, pos);

        Assertions.assertEquals("<Integer mark=\"^\"><Integer mark=\"-\">3</Integer><Digit mark=\"-\">2</Digit></Integer>", tree);
    }

    @Test
    public void parseSg54bisGLL() {
        String cxml = loadCxml("src/test/resources/sg54bis.cxml");
        GrammarCompiler compiler = new GrammarCompiler();
        SourceGrammar grammar = compiler.parse(cxml);

        NonterminalSymbol S = grammar.getNonterminal("Number");

        ParserOptions options = new ParserOptions();
        options.setParserType("GLL");

        GearleyParser parser = grammar.getParser(options, S);
        GearleyResult result = parser.parse("32.5e+1");
        Assertions.assertTrue(result.succeeded());

        StringTreeBuilder builder = new StringTreeBuilder();
        result.getTree(builder);

        String tree = builder.getTree();

        int pos = tree.indexOf("<Integer");
        tree = tree.substring(pos);
        pos = tree.indexOf("<Fraction");
        tree = tree.substring(0, pos);

        Assertions.assertEquals("<Integer mark=\"^\"><Integer mark=\"-\">3</Integer><Digit mark=\"-\">2</Digit></Integer>", tree);
    }

    @Test
    public void parseInsMultAttGLL() {
        String cxml = loadCxml("src/test/resources/insmultatt.cxml");
        GrammarCompiler compiler = new GrammarCompiler();
        SourceGrammar grammar = compiler.parse(cxml);

        NonterminalSymbol S = grammar.getNonterminal("S");

        ParserOptions options = new ParserOptions();
        options.setParserType("GLL");

        GearleyParser parser = grammar.getParser(options, S);
        GearleyResult result = parser.parse("a");
        Assertions.assertTrue(result.succeeded());

        StringTreeBuilder builder = new StringTreeBuilder();
        result.getTree(builder);
        String tree = builder.getTree();

        Assertions.assertTrue(tree.contains("<b mark=\"@\">"));
    }

}
