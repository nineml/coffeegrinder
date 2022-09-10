package org.nineml.coffeegrinder.util;

import org.nineml.coffeegrinder.parser.NonterminalSymbol;
import org.nineml.coffeegrinder.tokens.Token;
import org.nineml.coffeegrinder.tokens.TokenCharacter;
import org.nineml.coffeegrinder.tokens.TokenString;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

public class PrintStreamTreeBuilder extends PriorityTreeBuilder {
    protected String tab = "  ";
    protected String nl = "\n";
    private String indent = "";
    private StringBuilder sb = null;
    private PrintStream stream;

    public PrintStreamTreeBuilder() {
        stream = null;
    }

    public PrintStreamTreeBuilder(PrintStream stream) {
        this.stream = stream;
    }

    public void setStream(PrintStream stream) {
        if (this.stream != null) {
            throw new RuntimeException("Stream cannot be changed");
        }
        this.stream = stream;
    }

    @Override
    public void startNonterminal(NonterminalSymbol symbol, Map<String,String> attributes, int leftExtent, int rightExtent) {
        if (sb != null) {
            stream.printf("%s%s%s", indent, sb, nl);
            sb = null;
        }
        stream.printf("%s<%s", indent, symbol);
        for (String name : attributes.keySet()) {
            if (!"name".equals(name)) {
                stream.printf(" %s=\"%s\"", name, attributes.get(name));
            }
        }

        stream.printf(">%s", nl);
        indent = indent + tab;
    }

    @Override
    public void endNonterminal(NonterminalSymbol symbol, Map<String,String> attributes, int leftExtent, int rightExtent) {
        if (sb != null) {
            stream.printf("%s%s%s", indent, sb, nl);
            sb = null;
        }
        indent = indent.substring(tab.length());
        stream.printf("%s</%s>%s", indent, symbol, nl);
    }

    @Override
    public void token(Token token, Map<String,String> attributes) {
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
        } else if (token instanceof TokenString) {
            sb.append(token.getValue());
        } else {
            sb.append(token.toString());
        }
    }
}
