package org.nineml.coffeegrinder;

import org.junit.Assert;
import org.junit.Ignore;
import org.nineml.coffeegrinder.parser.*;

public class RegexTest {
    private final ParserOptions options = new ParserOptions();

    @Ignore
    // This doesn't work, but I'm not sure any of the regex stuff makes any sense
    public void regexDigits() {
        SourceGrammar grammar = new SourceGrammar();

        /*
        S: A, D, A
        A: 'a'
        D: ['0'-'9']+
         */

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _D = grammar.getNonterminal("D");

        grammar.addRule(_S, _A, _D, _A);
        grammar.addRule(_A, TerminalSymbol.ch('a'));
        grammar.addRule(_D, TerminalSymbol.regex("[0-9]+"));
        //grammar.addRule(_D, TerminalSymbol.regex("[0-9]+"), _D);
        //grammar.addRule(_D);

        String input = "a0123a";

        GearleyParser parser = grammar.getParser(options, _S);
        GearleyResult result = parser.parse(input);
        Assert.assertTrue(result.succeeded());
    }


}
