package org.nineml.coffeegrinder.util;

import org.nineml.coffeegrinder.parser.NonterminalSymbol;
import org.nineml.coffeegrinder.parser.TreeBuilder;
import org.nineml.coffeegrinder.parser.RuleChoice;
import org.nineml.coffeegrinder.tokens.Token;
import org.nineml.coffeegrinder.tokens.TokenCharacter;
import org.nineml.coffeegrinder.tokens.TokenString;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class StdoutTreeBuilder extends PrintStreamTreeBuilder {
    public StdoutTreeBuilder() {
        super(System.out);
    }
}
