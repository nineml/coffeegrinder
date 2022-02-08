package org.nineml.coffeegrinder;

import org.junit.Assert;
import org.junit.Test;
import org.nineml.coffeegrinder.parser.*;
import org.nineml.coffeegrinder.tokens.Token;
import org.nineml.coffeegrinder.util.Iterators;

import java.util.Iterator;

import static junit.framework.TestCase.fail;

public class PrefixTest {
    @Test
    public void testAB() {
        ParserOptions options = new ParserOptions();
        options.prefixParsing = true;
        Grammar grammar = new Grammar(options);

        NonterminalSymbol _S = grammar.getNonterminal("S");
        NonterminalSymbol _A = grammar.getNonterminal("A");
        NonterminalSymbol _B = grammar.getNonterminal("B", Symbol.OPTIONAL);
        TerminalSymbol _a = TerminalSymbol.ch('a');
        TerminalSymbol _b = TerminalSymbol.ch('b');

        grammar.addRule(_S, _A, _B);
        grammar.addRule(_A, _a);
        grammar.addRule(_B, _b, _B);
        grammar.addRule(_B, _b);

        EarleyParser parser = grammar.getParser(_S);

        Iterator<Token> input = Iterators.characterIterator("abbabbbba");

        EarleyResult result = parser.parse(input);
        Assert.assertFalse(result.succeeded());
        Assert.assertTrue(result.prefixSucceeded());

        result = result.continueParsing();
        Assert.assertFalse(result.succeeded());

        result = result.continueParsing();
        Assert.assertTrue(result.succeeded());
    }

}
