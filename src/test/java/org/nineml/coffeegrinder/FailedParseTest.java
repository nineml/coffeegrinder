package org.nineml.coffeegrinder;

import org.junit.Assert;
import org.junit.Test;
import org.nineml.coffeegrinder.parser.EarleyResult;
import org.nineml.coffeegrinder.parser.Grammar;
import org.nineml.coffeegrinder.parser.EarleyParser;
import org.nineml.coffeegrinder.parser.NonterminalSymbol;
import org.nineml.coffeegrinder.parser.TerminalSymbol;
import org.nineml.coffeegrinder.tokens.TokenCharacter;
import org.nineml.coffeegrinder.tokens.TokenRegex;
import org.nineml.coffeegrinder.util.Iterators;

public class FailedParseTest {
    @Test
    public void testFailEEE() {
        Grammar grammar = new Grammar();

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _E = grammar.getNonterminal("E");
        TerminalSymbol _plus = TerminalSymbol.ch('+');

        grammar.addRule(_S, _E);
        grammar.addRule(_E, _E, _plus, _E);
        grammar.addRule(_E, new TerminalSymbol(TokenRegex.get("[0-9]")));

        EarleyParser parser = grammar.getParser(_S);
        EarleyResult result = parser.parse(Iterators.characterIterator("1+2-3"));
        Assert.assertFalse(result.succeeded());
        Assert.assertEquals(4, result.getTokenCount());
        Assert.assertEquals('-', ((TokenCharacter) result.getLastToken()).getValue());
    }
}
