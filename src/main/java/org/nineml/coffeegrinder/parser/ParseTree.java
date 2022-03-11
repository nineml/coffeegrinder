package org.nineml.coffeegrinder.parser;

import org.nineml.coffeegrinder.exceptions.ForestException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A single parse of the input against the grammar.
 */
public class ParseTree {
    private static int nextNodeId = 0;
    private final ParseForest forest;
    private final ForestNode node;
    private final ArrayList<ParseTree> children;
    private final int id;
    protected ParseTree parent;

    protected ParseTree(ForestNode root) {
        if (root == null) {
            throw new NullPointerException("Node must not be null");
        }
        parent = null;
        node = root;
        id = nextNodeId++;
        this.forest = node.graph;
        children = new ArrayList<>();
    }

    /**
     * Get the forest this tree is in.
     * @return the forest
     */
    public ParseForest getForest() {
        return forest;
    }

    /**
     * Get the {@link ForestNode} associated with this node in the tree.
     *
     * @return the node
     */
    public ForestNode getNode() {
        return node;
    }

    /**
     * Get the symbol associated with this node in the tree.
     * <p>See {@link ForestNode#getSymbol}.</p>
     *
     * @return the symbol, or null
     */
    public Symbol getSymbol() {
        return node.getSymbol();
    }

    /**
     * Get the state associated with this node in the tree.
     * <p>See {@link ForestNode#getState}.</p>
     *
     * @return the state, or null
     */
    public State getState() {
        return node.getState();
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

    protected void addChild(ParseTree node) {
        children.add(node);
        node.parent = this;
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
        String localName = "symbol";
        boolean nonterminal = true;
        if (getSymbol() != null || forest.options.treesWithStates) {
            if (getSymbol() != null) {
                nonterminal = !(getSymbol() instanceof TerminalSymbol);
                String xml = getSymbol().toString().replaceAll("&", "&amp;");
                xml = xml.replace("<", "&lt;").replace("\"", "&quot;");
                stream.printf("<symbol id=\"node%04d\" label=\"%s\" type=\"%s\"",
                        id, xml, getSymbol() instanceof NonterminalSymbol ? "nonterminal" : "terminal");
                if (getState() != null) {
                    xml = getState().toString().replaceAll("\"", "&quot;");
                    stream.printf(" state=\"%s\"", xml);
                }
            } else {
                String xml = getState().toString().replaceAll("&", "&amp;");
                xml = xml.replace("<", "&lt;").replace("\"", "&quot;");
                localName = "state";
                stream.printf("<state id=\"node%04d\" label=\"%s\" type=\"%s\"",
                        id, xml, getSymbol() instanceof NonterminalSymbol ? "nonterminal" : "terminal");
            }

            if (children.isEmpty()) {
                if (nonterminal) {
                    stream.printf("><epsilon/></%s>\n", localName);

                } else {
                    stream.println("/>");
                }
            } else {
                stream.println(">");
            }
        }

        for (ParseTree node : children) {
            node.serialize(stream);
        }

        if (getSymbol() != null || forest.options.treesWithStates) {
            if (!children.isEmpty()) {
                stream.printf("</%s>\n", localName);
            }
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
        return node.toString();
    }
}
