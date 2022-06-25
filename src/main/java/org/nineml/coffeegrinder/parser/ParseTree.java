package org.nineml.coffeegrinder.parser;

import org.nineml.coffeegrinder.exceptions.ForestException;
import org.nineml.coffeegrinder.tokens.Token;
import org.nineml.coffeegrinder.util.ParserAttribute;

import java.io.*;
import java.util.*;

/**
 * A single parse of the input against the grammar.
 */
public class ParseTree {
    private final NonterminalSymbol symbol;
    private final Token token;
    public ArrayList<ParseTree> children = null;
    private ParseTree parent = null;
    private HashMap<String,String> attributes = null;

    public ParseTree(NonterminalSymbol symbol, Map<String,String> attrs) {
        this.symbol = symbol;
        this.token = null;
        if (!attrs.isEmpty()) {
            this.attributes = new HashMap<>(attrs);
        }
    }

    public ParseTree(Token token, Map<String,String> attrs) {
        this.symbol = null;
        this.token = token;
        if (!attrs.isEmpty()) {
            this.attributes = new HashMap<>(attrs);
        }
    }

    public ParseTree addChild(NonterminalSymbol symbol, Map<String,String> attrs) {
        return addChild(new ParseTree(symbol, attrs));
    }

    public void addChild(Token token, Map<String,String> attrs) {
        addChild(new ParseTree(token, attrs));
    }

    private ParseTree addChild(ParseTree child) {
        if (symbol == null) {
            throw new IllegalStateException("Cannot add children to a leaf node.");
        }
        child.parent = this;
        if (children == null) {
            children = new ArrayList<>();
        }
        children.add(child);
        return child;
    }

    /**
     * Get the symbol associated with this node in the tree.
     *
     * @return the symbol, or null
     */
    public Symbol getSymbol() {
        return symbol;
    }

    /**
     * Get the token associated with this node in the tree.
     *
     * @return the token, or null
     */
    public Token getToken() {
        return token;
    }

    public String getAttribute(String name, String defaultValue) {
        return attributes == null ? defaultValue : attributes.getOrDefault(name, defaultValue);
    }

    public Map<String,String> getAttributes() {
        if (attributes == null) {
            return Collections.emptyMap();
        }
        return attributes;
    }

    /**
     * Get the parent.
     *
     * @return The parent node in the tree, or null if this is the root
     */
    public ParseTree getParent() {
        return parent;
    }

    /**
     * Get the children
     *
     * @return The children
     */
    public List<ParseTree> getChildren() {
        return children;
    }

    /**
     * Serialize the tree as XML.
     * <p>See {@link ParserOptions} for details about options that may be used to influence the
     * structure of trees.</p>
     * @return an XML serialization as a string
     */
    public String serialize() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        serialize(ps);
        try {
            return baos.toString("UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalArgumentException("Unexpected (i.e. impossible) unsupported encoding exception", ex);
        }
    }

    /**
     * Serialize the tree as XML.
     * <p>See {@link ParserOptions} for details about options that may be used to influence the
     * structure of trees.</p>
     * @param stream        the stream on which to write the XML serialization
     */
    public void serialize(PrintStream stream) {
        boolean nonterminal = true;
        if (symbol != null) {
            nonterminal = !(getSymbol() instanceof TerminalSymbol);
            String xml = getSymbol().toString().replace("&", "&amp;");
            xml = xml.replace("<", "&lt;").replace("\"", "&quot;");
            stream.printf("<symbol label=\"%s\"", xml);
            if (children == null) {
                stream.printf("><epsilon/></symbol>%n");
            } else {
                stream.print(">");
                for (ParseTree child : children) {
                    child.serialize(stream);
                }
                stream.printf("</symbol>%n");
            }
        } else {
            stream.print("DATA");
        }
    }

    /**
     * Serialize the tree as XML.
     * <p>This method attempts to write the XML to a file.</p>
     * <p>See {@link ParserOptions} for details about options that may be used to influence the
     * structure of trees.</p>
     * @param filename the name of the file
     * @throws ForestException if a error occurs attempt to write to the file
     */
    public void serialize(String filename) {
        try {
            FileOutputStream fos = new FileOutputStream(filename);
            PrintStream stream = new PrintStream(fos);
            serialize(stream);
            stream.close();
            fos.close();
        } catch (IOException ex) {
            throw ForestException.ioError(filename, ex);
        }
    }

    @Override
    public String toString() {
        if (symbol == null) {
            return token.getValue();
        } else {
            return symbol.toString();
        }
    }
}
