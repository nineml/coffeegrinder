package org.nineml.coffeegrinder.util;

import org.nineml.coffeegrinder.parser.NonterminalSymbol;
import org.nineml.coffeegrinder.parser.TreeBuilder;
import org.nineml.coffeegrinder.parser.RuleChoice;
import org.nineml.coffeegrinder.tokens.Token;
import org.nineml.coffeegrinder.tokens.TokenCharacter;

import java.util.Collection;
import java.util.List;

public class StdoutTreeBuilder extends TreeBuilder {
    private static final String tab = "  ";
    private String indent = "";
    private StringBuilder sb = null;

    @Override
    public void startNonterminal(NonterminalSymbol symbol, Collection<ParserAttribute> attributes, int leftExtent, int rightExtent) {
        if (sb != null) {
            System.out.printf("%s%s%n", indent, sb);
            sb = null;
        }
        System.out.printf("%s<%s>%n", indent, symbol);
        indent = indent + tab;
    }

    @Override
    public void endNonterminal(NonterminalSymbol symbol, Collection<ParserAttribute> attributes, int leftExtent, int rightExtent) {
        if (sb != null) {
            System.out.printf("%s%s%n", indent, sb);
            sb = null;
        }
        indent = indent.substring(tab.length());
        System.out.printf("%s</%s>%n", indent, symbol);
    }

    @Override
    public void token(Token token, Collection<ParserAttribute> attributes) {
        if (sb == null) {
            sb = new StringBuilder();
        }
        if (token instanceof TokenCharacter) {
            int cp = ((TokenCharacter) token).getCodepoint();
            if (cp == '<' || cp == '>' || cp == '&' || cp <= ' ') {
                sb.append(String.format("&#x%x;", cp));
            } else {
                sb.appendCodePoint(cp);
            }
        } else {
            sb.append(token.toString());
        }
    }
}
