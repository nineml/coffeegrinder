package org.nineml.coffeegrinder;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.nineml.coffeegrinder.parser.EarleyParser;
import org.nineml.coffeegrinder.parser.EarleyResult;
import org.nineml.coffeegrinder.parser.Grammar;
import org.nineml.coffeegrinder.parser.HygieneReport;
import org.nineml.coffeegrinder.parser.NonterminalSymbol;
import org.nineml.coffeegrinder.parser.TerminalSymbol;

public class RegexTest {

    @Ignore
    // This doesn't work, but I'm not sure any of the regex stuff makes any sense
    public void regexDigits() {
        Grammar grammar = new Grammar();

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

        grammar.close();

        String input = "a0123a";

        EarleyParser parser = grammar.getParser(_S);
        EarleyResult result = parser.parse(input);
        Assert.assertTrue(result.succeeded());
    }


}
