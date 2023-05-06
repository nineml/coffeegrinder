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

    private enum PendingType { START, END, TREE, END_ALTERNATIVES };

    protected final ArrayList<ForestNode> graph = new ArrayList<>();
    protected final ArrayList<ForestNode> roots = new ArrayList<>();
    protected final HashSet<Integer> graphIds = new HashSet<>();
    protected final HashSet<Integer> rootIds = new HashSet<>();
    protected final ParserOptions options;
    protected Boolean ambiguous = null;
    protected Boolean infinitelyAmbiguous = null;
    private Stack<PendingAction> pendingActions = null;

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
            int idx = builder.startAlternative(null, rootChoice);
            if (idx < 0 || idx >= roots.size()) {
                throw new IllegalStateException("Invalid alternative selected");
            }
            root = roots.get(idx);
        } else {
            root = roots.get(0);
        }

        constructTree(builder, root);

        if (roots.size() > 1) {
            builder.endAlternative(root);
        }

        builder.endTree();

        ambiguous = builder.isAmbiguous();
        infinitelyAmbiguous = builder.isInfinitelyAmbiguous();
    }

    public void constructTree(TreeBuilder builder, ForestNode root) {
        pendingActions = new Stack<>();
        pendingActions.push(new PendingTree(root, null));
        HashSet<Family> seen = new HashSet<>();
        while (!pendingActions.isEmpty()) {
            PendingAction top = pendingActions.pop();
            switch (top.action) {
                case START:
                    PendingStart start = (PendingStart) top;
                    builder.startNonterminal(start.symbol, start.attributes, start.leftExtent, start.rightExtent);
                    break;
                case END:
                    PendingEnd end = (PendingEnd) top;
                    builder.endNonterminal(end.symbol, end.attributes, end.leftExtent, end.rightExtent);
                    break;
                case TREE:
                    PendingTree tree = (PendingTree) top;
                    constructTree(builder, tree.tree, tree.xsymbol, seen);
                    break;
                case END_ALTERNATIVES:
                    builder.endAlternative(((PendingEndAlternatives) top).selectedAlternative);
                    break;
                default:
                    throw new IllegalStateException("Unexpected pending action: " + top.action);

            }
        }
    }

    //private int depth = 0;
    private void constructTree(TreeBuilder builder, ForestNode tree, Symbol xsymbol, HashSet<Family> selected) {
        //System.err.printf("IN  %4d %s%n", depth, tree);
        //depth++;

        ForestNode child0 = null;
        Symbol child0Symbol = null;
        ForestNode child1 = null;
        Symbol child1Symbol = null;

        assert tree != null;
        State state = tree.getState();

        int index = 0;
        boolean alternatives = false;
        int lowest = Integer.MAX_VALUE;
        RuleChoice selectedAlternative = null;
        final ArrayList<Family> families;
        final HashMap<Family,Integer> edgeCounts;
        switch (tree.families.size()) {
            case 0:
            case 1:
                edgeCounts = null;
                families = tree.families;
                break;
            default:
                edgeCounts = builder.getEdgeCounts(tree);
                for (Integer count : edgeCounts.values()) {
                    if (count < lowest) {
                        lowest = count;
                    }
                }

                families = new ArrayList<>();
                for (Family family : tree.families) {
                    // Don't rule out epsilon transitions; they all compare the same so testing
                    // selected doesn't help. But they don't have descendants, so they can't loop.
                    if ((family.v==null && family.w==null)) {
                        families.add(family);
                    } else {
                        if (selected.contains(family)) {
                            builder.loop(family.v);
                        }
                        if (edgeCounts.get(family) == lowest) {
                            families.add(family);
                        }
                    }
                }

                if (families.size() > 1) {
                    // We need to find the actual symbols in the right hand sides of the rules
                    // and copy those for use in making choices because they may have different
                    // properties (marks, pragmas, etc.) than the "ordinary" versions on the
                    // left hand side.
                    ArrayList<RuleChoice> choices = new ArrayList<>();
                    for (Family fam : families) {
                        if (tree.getSymbol() == null || tree.getSymbol().equals(fam.state.getSymbol())) {
                            final ForestNode c0, c1;
                            if (fam.w == null) {
                                c0 = fam.v;
                                c1 = null;
                            } else {
                                c0 = fam.w;
                                c1 = fam.v;
                            }

                            Symbol c0symbol = c0 != null ? c0.getSymbol() : null;
                            Symbol c1symbol = c1 != null ? c1.getSymbol() : null;
                            int c0pos = -1;
                            int c1pos = -1;

                            if (c1 != null) {
                                c1pos = getSymbol(c1.getSymbol(), fam.state, fam.state.getPosition());
                                if (c1pos >= 0) {
                                    c1symbol = fam.state.getRhs().get(c1pos);
                                } else {
                                    c1pos = fam.state.getPosition();
                                }

                                c0pos = getSymbol(c0.getSymbol(), fam.state, c1pos); // don't "pass" the second symbol
                                if (c0pos >= 0) {
                                    c0symbol = fam.state.getRhs().get(c0pos);
                                }
                            } else {
                                if (c0 != null) {
                                    c0pos = getSymbol(c0.getSymbol(), fam.state, fam.state.getPosition());
                                    if (c0pos >= 0) {
                                        c0symbol = fam.state.getRhs().get(c0pos);
                                    }
                                }
                            }

                            ArrayList<Symbol> rhs = new ArrayList<>();
                            for (int pos = 0; pos < fam.state.getRhs().length; pos++) {
                                if (pos == c0pos) {
                                    rhs.add(c0symbol);
                                } else if (pos == c1pos) {
                                    rhs.add(c1symbol);
                                } else {
                                    rhs.add(fam.state.getRhs().get(pos));
                                }
                            }

                            choices.add(new RuleChoiceImpl(fam.getSymbol(), rhs, c1, c0));
                        } else {
                            choices.add(new RuleChoiceImpl(fam));
                        }
                    }

                    index = builder.startAlternative(tree, choices);
                    if (index < 0 || index >= choices.size()) {
                        throw new IllegalStateException("Invalid alternative selected");
                    }
                    selectedAlternative = families.get(index);
                    alternatives = true;
                }
        }

        State selectedState = state;
        if (!families.isEmpty()) {
            Family family = families.get(index);

            // The GLL parser and the Earley parser build the forest differently. During construction,
            // we need to work out which symbols on the RHS were matched (so that we can get any
            // attributes they might have). For the GLL parser, the state from the tree is correct.
            // For the Earley parser, the state from the selected family is correct.
            if ("Earley".equals(options.getParserType())) {
                selectedState = family.state;
            }

            // Don't advance an epsilon edge, leave it as an escape hatch.
            if (edgeCounts != null && (family.v != null || family.w != null)) {
                edgeCounts.put(family, lowest+1);
            }
            selected.add(family);
            if (family.w == null) {
                child0 = family.v;
            } else {
                child0 = family.w;
                child1 = family.v;
            }
        }

        // When the GLL parser builds the forest, the states associated with nodes in the tree
        // are sometimes associated with the node's parent symbol. This doesn't seem to occur
        // in circumstances where it matters. (But I could be wrong). Nevertheless, if the
        // state symbol isn't the same as the tree symbol, don't go looking at its RHS.
        if (tree.getSymbol() == null || (selectedState != null && tree.getSymbol().equals(selectedState.getSymbol()))) {
            if (child1 != null) {
                int pos = getSymbol(child1.getSymbol(), selectedState, selectedState.getPosition());
                if (pos >= 0) {
                    child1Symbol = selectedState.getRhs().get(pos);
                } else {
                    pos = selectedState.getPosition();
                }

                pos = getSymbol(child0.getSymbol(), selectedState, pos); // don't "pass" the second symbol
                if (pos >= 0) {
                    child0Symbol = selectedState.getRhs().get(pos);
                }
            } else {
                if (child0 != null) {
                    int pos = getSymbol(child0.getSymbol(), selectedState, selectedState.getPosition());
                    if (pos >= 0) {
                        child0Symbol = selectedState.getRhs().get(pos);
                    }
                }
            }
        }

        Symbol symbol = tree.getSymbol();
        if (symbol == null) {
            assert child0 != null;

            if (alternatives) {
                pendingActions.push(new PendingEndAlternatives(selectedAlternative));
            }

            if (child1 != null) {
                pendingActions.push(new PendingTree(child1, child1Symbol));
            }

            pendingActions.push(new PendingTree(child0, child0Symbol));

            return;
        }

        final Map<String,String> atts;
        if (symbol.getAttributes().isEmpty() && (xsymbol == null || xsymbol.getAttributes().isEmpty())) {
            atts = Collections.emptyMap();
        } else {
            HashMap<String,String> xatts = new HashMap<>(symbol.getAttributesMap());
            if (xsymbol != null) {
                xatts.putAll(xsymbol.getAttributesMap());
            }
            atts = xatts;
        }

        if (symbol instanceof TerminalSymbol) {
            Token token = ((TerminalSymbol) symbol).getToken();
            builder.token(token, atts);
        } else {
            if (alternatives) {
                pendingActions.push(new PendingEndAlternatives(selectedAlternative));
            }

            pendingActions.push(new PendingEnd((NonterminalSymbol) symbol, atts, tree.leftExtent, tree.rightExtent));

            if (child1 != null) {
                pendingActions.push(new PendingTree(child1, child1Symbol));
            }

            if (child0 != null) {
                pendingActions.push(new PendingTree(child0, child0Symbol));
            }

            pendingActions.push(new PendingStart((NonterminalSymbol) symbol, atts, tree.leftExtent, tree.rightExtent));
        }

        //depth--;
        //System.err.printf("OUT %4d %s%n", depth, tree);
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
            if (!node.families.isEmpty()) {
                stream.printf(" trees='%d'", node.families.size());
            }

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

    protected void prune() {
        StopWatch timer = new StopWatch();

        /*
        options.getLogger().debug(logcategory, "Pruning forest of %,d nodes with %,d roots", graph.size(), roots.size());
        // Step 1. Trim epsilon twigs
        for (ForestNode node : roots) {
            node.trimEpsilon();
        }
        options.getLogger().debug(logcategory, "Trimmed ε twigs: %,d nodes remain", graph.size());
         */

        // Step 2. Find all the reachable nodes
        for (ForestNode root : roots) {
            root.reach();
        }

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
        options.getLogger().debug(logcategory, "Pruned %,d unreachable nodes from graph in %,dms; %,d remain", count, timer.duration(), graph.size());
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

    private abstract static class PendingAction {
        private final PendingType action;
        public PendingAction(PendingType action) {
            this.action = action;
        }
    }

    private static class PendingStart extends PendingAction {
        public final NonterminalSymbol symbol;
        public final Map<String,String> attributes;
        public final int leftExtent;
        public final int rightExtent;
        public PendingStart(NonterminalSymbol symbol, Map<String,String> attributes, int leftExtent, int rightExtent) {
            super(PendingType.START);
            this.symbol = symbol;
            this.attributes = attributes;
            this.leftExtent = leftExtent;
            this.rightExtent = rightExtent;
        }
    }

    private static class PendingEnd extends PendingAction {
        public final NonterminalSymbol symbol;
        public final Map<String,String> attributes;
        public final int leftExtent;
        public final int rightExtent;
        public PendingEnd(NonterminalSymbol symbol, Map<String,String> attributes, int leftExtent, int rightExtent) {
            super(PendingType.END);
            this.symbol = symbol;
            this.attributes = attributes;
            this.leftExtent = leftExtent;
            this.rightExtent = rightExtent;
        }
    }

    private static class PendingTree extends PendingAction {
        public final ForestNode tree;
        public final Symbol xsymbol;
        public PendingTree(ForestNode tree, Symbol xsymbol) {
            super(PendingType.TREE);
            this.tree = tree;
            this.xsymbol = xsymbol;
        }
    }

    private static class PendingEndAlternatives extends PendingAction {
        public final RuleChoice selectedAlternative;
        public PendingEndAlternatives(RuleChoice selectedAlternative) {
            super(PendingType.END_ALTERNATIVES);
            this.selectedAlternative = selectedAlternative;
        }
    }

}
