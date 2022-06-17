package org.nineml.coffeegrinder.util;

import org.nineml.coffeegrinder.parser.NonterminalSymbol;
import org.nineml.coffeegrinder.parser.TreeBuilder;
import org.nineml.coffeegrinder.parser.RuleChoice;
import org.nineml.coffeegrinder.tokens.Token;
import org.nineml.coffeegrinder.tokens.TokenCharacter;

import java.util.Collection;
import java.util.List;

public class StdoutTreeBuilder implements TreeBuilder {
    private boolean ambiguous = false;
    private boolean infinitelyAmbiguous = false;
    private static final String tab = "  ";
    private String indent = "";
    private StringBuilder sb = null;

    @Override
    public boolean isAmbiguous() {
        return ambiguous;
    }

    @Override
    public boolean isInfinitelyAmbiguous() {
        ambiguous = true;
        return infinitelyAmbiguous;
    }

    @Override
    public int chooseAlternative(List<RuleChoice> alternatives) {
        ambiguous = true;
        return 0;
    }

    @Override
    public void loop(RuleChoice alternative) {
        ambiguous = true;
        infinitelyAmbiguous = true;
    }

    @Override
    public void startTree() {
        // nop
    }

    @Override
    public void endTree() {
        // nop
    }

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
