package org.nineml.coffeegrinder.parser;

import org.nineml.coffeegrinder.exceptions.ForestException;
import org.nineml.coffeegrinder.tokens.Token;
import org.nineml.coffeegrinder.util.NopTreeBuilder;
import org.nineml.coffeegrinder.util.ParseTreeBuilder;
import org.nineml.coffeegrinder.util.ParserAttribute;
import org.nineml.coffeegrinder.util.StopWatch;

import java.io.*;
import java.util.*;

import static java.lang.Math.abs;

/**
 * An SPPF is a shared packed parse forest.
 * <p>The SPPF is a graph representation of all the (possibly infinite) parses that can be used
 * to recognize the input sequence as a sentence in the grammar.</p>
 */
public class ParseForest {
    public static final String logcategory = "Forest";

    protected final ArrayList<ForestNode> graph = new ArrayList<>();
    protected final ArrayList<ForestNode> roots = new ArrayList<>();
    protected final HashSet<Integer> graphIds = new HashSet<>();
    protected final HashSet<Integer> rootIds = new HashSet<>();
    protected final ParserOptions options;
    protected Boolean ambiguous = null;
    protected Boolean infinitelyAmbiguous = null;
    protected Long totalParses = null;
    private Stack<ArrayList<Long>> choices = new Stack<>();

    public ParseForest(ParserOptions options) {
        this.options = options;
    }

    /**
     * Is the grammar represented by this graph ambiguous?
     * <p>A grammar is ambiguous if there are more than two parses that will recognize the input.</p>
     *
     * @return true if the grammar is ambiguous
     */
    public boolean isAmbiguous() {
        if (ambiguous == null) {
            ambiguous = roots.size() > 1;
            NopTreeBuilder builder = new NopTreeBuilder();
            getTree(builder);
            ambiguous = ambiguous || builder.isAmbiguous();
            infinitelyAmbiguous = builder.isInfinitelyAmbiguous();
        }
        return ambiguous;
    }

    /**
     * Is the grammar represented by this graph infinitely ambiguous?
     * <p>If the answer is "true", then the graph is infinitely ambiguous. If the graph is ambiguous
     * and the anwer is "false", then all that can be said is the single parse explored to check
     * ambiguity did not encounter infinite ambiguity. It is not an assertion that no unexplored
     * part of the graph contains a loop.</p>
     *
     * @return true if the parse forest is known to be infinitely ambiguous
     */
    public boolean isInfinitelyAmbiguous() {
        return infinitelyAmbiguous;
    }

    /**
     * How big is the graph?
     *
     * @return the number of nodes in the graph
     */
    public int size() {
        return graph.size();
    }

    /**
     * Get the nodes in the graph.
     *
     * @return the nodes in the graph.
     */
    public List<ForestNode> getNodes() {
        return graph;
    }

    /**
     * Get the root nodes in the graph.
     *
     * @return the nodes in the graph.
     */
    public List<ForestNode> getRoots() {
        return roots;
    }

    public long getTotalParses() {
        if (totalParses == null) {
            NopTreeBuilder builder = new NopTreeBuilder();
            getTree(builder);
        }
        return totalParses;
    }

    /**
     * Get the options for this forest.
     *
     * @return the options.
     */
    public ParserOptions getOptions() {
        return options;
    }

    public ParseTree getTree() {
        ParseTreeBuilder parseTreeBuilder = new ParseTreeBuilder();
        getTree(parseTreeBuilder);
        return parseTreeBuilder.getParseTree();
    }

    public void getTree(TreeBuilder builder) {
        if (roots.isEmpty()) {
            return;
        }

        builder.startTree();

        ArrayList<RuleChoice> rootChoice = new ArrayList<>(roots);
        final ForestNode root;
        if (roots.size() > 1) {
            int idx = builder.startAlternative(rootChoice);
            if (idx < 0 || idx >= roots.size()) {
                throw new IllegalStateException("Invalid alternative selected");
            }
            root = roots.get(idx);
        } else {
            root = roots.get(0);
        }

        choices = new Stack<>();
        choices.push(new ArrayList<>());
        constructTree(builder, root, null, new HashSet<>());

        if (roots.size() > 1) {
            builder.endAlternative(root);
        }

        builder.endTree();

        long count = choices.pop().get(0);
        if (count <= 0) {
            totalParses = Long.MAX_VALUE;
        } else {
            if (totalParses == null || count > totalParses) {
                totalParses = count;
            }
        }
        choices = null;

        ambiguous = builder.isAmbiguous();
        infinitelyAmbiguous = builder.isInfinitelyAmbiguous();
    }

    private void constructTree(TreeBuilder builder, ForestNode tree, Symbol xsymbol, HashSet<Family> selected) {
        ForestNode child0 = null;
        Symbol child0Symbol = null;
        ForestNode child1 = null;
        Symbol child1Symbol = null;

        assert tree != null;
        State state = tree.getState();

        int index = 0;
        boolean alternatives = false;
        ForestNode selectedAlternative = null;
        final ArrayList<Family> families;
        switch (tree.families.size()) {
            case 0:
                families = tree.families;
                break;
            case 1:
                if (selected.contains(tree.families.get(0))) {
                    families = new ArrayList<>();
                } else {
                    families = tree.families;
                }
                break;
            default:
                families = new ArrayList<>();
                for (Family family : tree.families) {
                    // Don't rule out epsilon transitions; they all compare the same so testing
                    // selected doesn't help. But they don't have descendants, so they can't loop.
                    if ((family.v==null && family.w==null) || !selected.contains(family)) {
                        families.add(family);
                    }
                }
                if (families.size() > 1) {
                    ArrayList<RuleChoice> choices = new ArrayList<>();
                    for (Family family : families) {
                        // Can family.w ever be non-null and what does it mean if it is?
                        choices.add(family.v);
                    }
                    index = builder.startAlternative(choices);
                    if (index < 0 || index >= choices.size()) {
                        throw new IllegalStateException("Invalid alternative selected");
                    }
                    selectedAlternative = families.get(index).v;
                    alternatives = true;
                }
        }

        int choiceCount = families.size();
        if (!families.isEmpty()) {
            Family family = families.get(index);
            selected.add(family);
            if (family.w == null) {
                child0 = family.v;
            } else {
                child0 = family.w;
                child1 = family.v;
            }
        }

        if (child1 != null) {
            int pos = getSymbol(child1.getSymbol(), state, state.getPosition());
            if (pos >= 0) {
                child1Symbol = state.getRhs().get(pos);
            } else {
                pos = state.getPosition();
            }

            pos = getSymbol(child0.getSymbol(), state, pos); // don't "pass" the second symbol
            if (pos >= 0) {
                child0Symbol = state.getRhs().get(pos);
            }
        } else {
            if (child0 != null) {
                int pos = getSymbol(child0.getSymbol(), state, state.getPosition());
                if (pos >= 0) {
                    child0Symbol = state.getRhs().get(pos);
                }
            }
        }

        Symbol symbol = tree.getSymbol();
        if (symbol == null) {
            assert child0 != null;
            constructTree(builder, child0, child0Symbol, selected);
            if (child1 != null) {
                constructTree(builder, child1, child1Symbol, selected);
            }
            if (alternatives) {
                builder.endAlternative(selectedAlternative);
            }
            return;
        }

        final List<ParserAttribute> atts;
        if (symbol.getAttributes().isEmpty() && (xsymbol == null || xsymbol.getAttributes().isEmpty())) {
            atts = Collections.emptyList();
        } else {
            ArrayList<ParserAttribute> xatts = new ArrayList<>();
            if (xsymbol != null) {
                xatts.addAll(xsymbol.getAttributes());
            }
            xatts.addAll(symbol.getAttributes());
            atts = xatts;
        }

        if (symbol instanceof TerminalSymbol) {
            Token token = ((TerminalSymbol) symbol).getToken();
            builder.token(token, atts);
        } else {
            choices.push(new ArrayList<>());
            builder.startNonterminal((NonterminalSymbol) symbol, atts, tree.leftExtent, tree.rightExtent);
            if (child0 != null) {
                constructTree(builder, child0, child0Symbol, selected);
            }
            if (child1 != null) {
                constructTree(builder, child1, child1Symbol, selected);
            }
            builder.endNonterminal((NonterminalSymbol) symbol, atts, tree.leftExtent, tree.rightExtent);

            if (alternatives) {
                builder.endAlternative(selectedAlternative);
            }

            long partial = 1;
            for (long count : choices.peek()) {
                //System.err.printf("%d ", count);
                partial = partial * count;
            }
            choices.pop();
            if (choiceCount == 0) {
                choices.peek().add(1L);
                //System.err.printf(" : %d%n", 1);
            } else {
                choices.peek().add(choiceCount + partial - 1);
                //System.err.printf(" : %d%n", choiceCount+partial-1);
            }
        }
    }

    private int getSymbol(Symbol seek, State state, int maxPos) {
        // Because some nonterminals can go to epsilon, we can't always find them
        // by position. If there's only one symbol, then we want the last one before
        // the position. But if there are two, then the *second* symbol has to come
        // after the first!
        int found = -1;
        if (seek instanceof TerminalSymbol) {
            Token token = ((TerminalSymbol) seek).getToken();
            for (int pos = 0; pos < maxPos; pos++) {
                if (state.getRhs().get(pos).matches(token)) {
                    found = pos;
                }
            }
        } else {
            for (int pos = 0; pos < maxPos; pos++) {
                if (state.getRhs().get(pos).equals(seek)) {
                    found = pos;
                }
            }
        }

        return found;
    }

    /**
     * Serialize the graph as XML.
     *
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
     *
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

            StringBuilder attrs = new StringBuilder();
            if (node.symbol == null) {
                assert node.state != null;
                stream.printf(" label=\"%s\"", stastr);
                stream.print(" type='state'");
            } else {
                if (stastr == null) {
                    stream.printf(" label=\"%s\"", symstr);
                } else {
                    stream.printf(" label=\"%s\" state=\"%s\"", symstr, stastr);
                }
                if (node.symbol instanceof TerminalSymbol) {
                    stream.print(" type='terminal'");
                } else {
                    stream.print(" type='nonterminal'");
                }

                Collection<ParserAttribute> pattrs;
                if (node.symbol instanceof TerminalSymbol) {
                     pattrs = ((TerminalSymbol) node.symbol).getToken().getAttributes();
                } else {
                     pattrs = node.symbol.getAttributes();
                }
                for (ParserAttribute attr : pattrs) {
                    attrs.append("    <attr name=\"").append(attr.getName());
                    attrs.append("\" value=\"").append(attr.getValue()).append("\"/>\n");
                }
            }
            stream.printf(" leftExtent='%d' rightExtent='%d'", node.leftExtent, node.rightExtent);
            stream.printf(" trees='%d'", node.exactParsesBelow);

            if (node.families.isEmpty()) {
                if ("".equals(attrs.toString())) {
                    stream.println("/>");
                } else {
                    stream.println(">");
                    stream.print(attrs);
                    stream.printf("  </u%d>\n", count);
                }
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
     *
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
        ForestNode node = new ForestNode(this, symbol, j, i);
        graph.add(node);
        graphIds.add(node.id);
        return node;
    }

    protected ForestNode createNode(Symbol symbol, State state, int j, int i) {
        ForestNode node = new ForestNode(this, symbol, state, j, i);
        graph.add(node);
        graphIds.add(node.id);
        return node;
    }

    protected ForestNode createNode(State state, int j, int i) {
        ForestNode node = new ForestNode(this, state, j, i);
        graph.add(node);
        graphIds.add(node.id);
        return node;
    }

    protected void root(ForestNode w) {
        if (rootIds.contains(w.id)) {
            return;
        }

        if (graphIds.contains(w.id)) {
            roots.add(w);
            rootIds.add(w.id);
            return;
        }

        throw ForestException.noSuchNode(w.toString());
    }

    protected void clearRoots() {
        roots.clear();
        rootIds.clear();
    }

    protected int prune() {
        StopWatch timer = new StopWatch();
        options.getLogger().debug(logcategory, "Pruning forest of %d nodes with %d roots", graph.size(), roots.size());

        // Step 1. Trim epsilon twigs
        for (ForestNode node : roots) {
            node.trimEpsilon();
        }

        options.getLogger().debug(logcategory, "Trimmed ε twigs: %d nodes remain", graph.size());

        // Step 2. Prune unreachable nodes
        computeAmbiguity();

        int count = 0;
        ArrayList<ForestNode> prunedGraph = new ArrayList<>();
        HashSet<Integer> prunedMap = new HashSet<>();
        for (ForestNode node : graph) {
            if (node.reachable) {
                prunedGraph.add(node);
                prunedMap.add(node.id);
            } else {
                count++;
            }
        }

        graph.clear();
        graph.addAll(prunedGraph);
        graphIds.clear();
        graphIds.addAll(prunedMap);

        timer.stop();
        options.getLogger().debug(logcategory, "Graph contained %d unreachable nodes, pruned in %dms", count, timer.duration());

        return count;
    }

    protected void computeAmbiguity() {
        ambiguous = roots.size() > 1;
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
    }

    private String id(int code) {
        // Avoid "-" in hash codes. Because it confuses graphviz, basically.
        if (code < 0) {
            return "id_" + abs(code);
        } else {
            return "id" + code;
        }
    }

    protected void rollback(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Cannot rollback to less than zero nodes");
        }
        while (graph.size() > size) {
            ForestNode node = graph.remove(graph.size() - 1);
            graphIds.remove(node.id);
            rootIds.remove(node.id);
        }
    }
}
