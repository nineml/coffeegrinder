package org.nineml.coffeegrinder.parser;

import org.nineml.coffeegrinder.exceptions.ForestException;
import org.nineml.coffeegrinder.util.DefaultTreeWalker;
import org.nineml.coffeegrinder.util.Messages;

import java.io.*;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.abs;

/**
 * An SPPF is a shared packed parse forest.
 * <p>The SPPF is a graph representation of all the (possibly infinite) parses that can be used
 * to recognize the input sequence as a sentence in the grammar.</p>
 */
public class ParseForest {
    protected final ArrayList<ForestNode> graph = new ArrayList<>();
    protected final ArrayList<ForestNode> roots = new ArrayList<>();
    protected final ParserOptions options;
    protected final Messages messages;
    private Ambiguity ambiguity = null;
    private boolean ambiguous = false;
    private boolean infinitelyAmbiguous = false;
    private TreeWalker treeWalker = null;

    protected ParseForest(ParserOptions options, Messages messages) {
        this.options = options;
        this.messages = messages;
    }

    /**
     * Is the grammar represented by this graph ambiguous?
     * <p>A grammar is ambiguous if there are more than two parses that will recognize the input.</p>
     * @return true if the grammar is ambiguous
     */
    public boolean isAmbiguous() {
        return ambiguous;
    }

    /**
     * Is the grammar represented by this graph infinitely ambiguous?
     * <p>Briefly: if the graph contains a loop. Consider: if a graph contains a nonterminal that can match the
     * empty string, then a parse that uses that nonterminal 0 times will match the sentence. But it will
     * also match the sentence if it uses that nonterminal 1, 2, 3...or any arbitrary number of times.</p>
     * @return true if the parse forest is infinitely ambiguous
     */
    public boolean isInfinitelyAmbiguous() {
        return infinitelyAmbiguous;
    }

    /**
     * How big is the graph?
     * @return the number of nodes in the graph
     */
    public int size() {
        return graph.size();
    }

    /**
     * Get the nodes in the graph.
     * @return the nodes in the graph.
     */
    public List<ForestNode> getNodes() {
        return graph;
    }

    /**
     * Get the root nodes in the graph.
     * @return the nodes in the graph.
     */
    public List<ForestNode> getRoots() {
        return roots;
    }

    public long getTotalParses() {
        if (treeWalker == null) {
            treeWalker = new DefaultTreeWalker(this, new ParseTreeBuilder());
        }

        if (treeWalker.getExactTotalParses().compareTo(ForestNode.MAX_LONG) < 0) {
            return Long.parseLong(treeWalker.getExactTotalParses().toString());
        } else {
            return Long.MAX_VALUE;
        }
    }

    public BigInteger getExactTotalParses() {
        if (treeWalker == null) {
            treeWalker = new DefaultTreeWalker(this, new ParseTreeBuilder());
        }
        return treeWalker.getExactTotalParses();
    }

    public Ambiguity getAmbiguity() {
        if (ambiguity == null) {
            if (!ambiguous) {
                ambiguity = new Ambiguity(getRoots().get(0));
            } else {
                DefaultTreeWalker walker = new DefaultTreeWalker(this, new ParseTreeBuilder());
                walker.next();
                ambiguity = new Ambiguity(getRoots(), ambiguous, infinitelyAmbiguous, walker.getAmbiguityMap());
            }
        }
        return ambiguity;
    }

    public ParseTree parse() {
        if (treeWalker == null) {
            treeWalker = new DefaultTreeWalker(this, new ParseTreeBuilder());
            treeWalker.next();
            return ((ParseTreeBuilder) treeWalker.getTreeBuilder()).getTree();
        }

        if (treeWalker.hasNext()) {
            treeWalker.next();
            return ((ParseTreeBuilder) treeWalker.getTreeBuilder()).getTree();
        }

        treeWalker = null;
        return null;
    }

    public void resetParses() {
        treeWalker = null;
    }

    /**
     * Serialize the graph as XML.
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
     * Serialize the graph as XML.
     * @param stream the stream on which to write the XML serialization
     */
    public void serialize(PrintStream stream) {
        stream.println("<sppf>");
        int count = 0;
        for (ForestNode node : graph) {
            stream.printf("  <u%d id='%s'", count, id(node.hashCode()));
            stream.printf(" hash='%d'", node.nodeHash);

            String symstr = null;
            String stastr = null;

            if (node.symbol != null) {
                symstr = node.symbol.toString().replace("&", "&amp;");
                symstr = symstr.replace("<", "&lt;").replace("\"", "&quot;");
            }

            if (node.state != null) {
                if (node.families.size() != 1 || node.families.get(0).v != null) {
                    stastr = node.state.toString().replace("&", "&amp;");
                    stastr = stastr.replace("<", "&lt;").replace("\"", "&quot;");
                }
            }

            if (node.symbol == null) {
                assert node.state != null;
                stream.printf(" label=\"%s, %d, %d\"", stastr, node.j, node.i);
                stream.print(" type='state'");
            } else {
                if (stastr == null) {
                    stream.printf(" label=\"%s, %d, %d\"", symstr, node.j, node.i);
                } else {
                    stream.printf(" label=\"%s, %d, %d\" state=\"%s\"", symstr, node.j, node.i, stastr);
                }
                if (node.symbol instanceof TerminalSymbol) {
                    if (node.state != null) {
                        System.err.println( node.state.toString().replaceAll("\"","&quot;"));
                    }
                    stream.print(" type='terminal'");
                } else {
                    stream.print(" type='nonterminal'");
                }
            }
            if (node.families.isEmpty()) {
                stream.println("/>");
            } else {
                stream.println(">");
                for (Family family : node.families) {
                    if (family.w != null) {
                        if (family.v != null) {
                            stream.println("    <pair>");
                            stream.printf("      <link target='%s'/>\n", id(family.w.hashCode()));
                            stream.printf("      <link target='%s'/>\n", id(family.v.hashCode()));
                            stream.println("    </pair>");
                        } else {
                            stream.printf("      <link target='%s'/>\n", id(family.w.hashCode()));
                        }
                    } else {
                        if (family.v == null) {
                            stream.println("    <epsilon/>");
                        } else {
                            stream.printf("    <link target='%s'/>\n", id(family.v.hashCode()));
                        }
                    }
                }
                stream.printf("  </u%d>\n", count);
            }
            count++;
        }
        stream.println("</sppf>");
    }

    /**
     * Serialize the graph as XML.
     * <p>This method attempts to write the XML to a file.</p>
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

    protected ForestNode createNode(Symbol symbol, int j, int i) {
        ForestNode testnode = new ForestNode(this, symbol, j, i);
        return getOrAddNode(testnode);
    }

    protected ForestNode createNode(Symbol symbol, State state, int j, int i) {
        ForestNode testnode = new ForestNode(this, symbol, state, j, i);
        return getOrAddNode(testnode);
    }

    protected ForestNode createNode(State state, int j, int i) {
        ForestNode testnode = new ForestNode(this, state, j, i);
        return getOrAddNode(testnode);
    }

    protected void root(ForestNode w) {
        for (ForestNode node : roots) {
            if (node.equals(w)) {
                return;
            }
        }

        for (ForestNode node : graph) {
            if (node.equals(w)) {
                roots.add(w);
                return;
            }
        }

        throw ForestException.noSuchNode(w.toString());
    }

    protected void clearRoots() {
        roots.clear();;
    }

    protected int prune() {
        // Step 1. Trim epsilon twigs
        for (ForestNode node : roots) {
            node.trimEpsilon();
        }

        ambiguous = roots.size() > 1;
        // Step 2. Prune unreachable nodes
        for (ForestNode node : roots) {
            int ambiguity = node.reach(node);
            if (ambiguity == ForestNode.INFINITELY_AMBIGUOUS) {
                ambiguous = true;
                infinitelyAmbiguous = true;
            }
            if (ambiguity == ForestNode.AMBIGUOUS) {
                ambiguous = true;
            }
        }

        int count = 0;
        ArrayList<ForestNode> prunedGraph = new ArrayList<>();
        for (ForestNode node : graph) {
            if (node.reachable) {
                prunedGraph.add(node);
            } else {
                count++;
            }
        }

        graph.clear();
        graph.addAll(prunedGraph);

        messages.debug("Graph contained %d unreachable nodes", count);

        return count;
    }

    private String id(int code) {
        // Avoid "-" in hash codes. Because it confuses graphviz, basically.
        if (code < 0) {
            return "id_" + abs(code);
        } else {
            return "id" + code;
        }
    }

    private ForestNode getOrAddNode(ForestNode testnode) {
        for (ForestNode node : graph) {
            if (node.equals(testnode)) {
                return node;
            }
        }
        graph.add(testnode);
        return testnode;
    }

    protected void rollback(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Cannot rollback to less than zero nodes");
        }
        while (graph.size() > size) {
            graph.remove(graph.size() - 1);
        }
    }

}
