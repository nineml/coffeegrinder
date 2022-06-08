package org.nineml.coffeegrinder.gll;

import org.nineml.coffeegrinder.parser.*;
import org.nineml.coffeegrinder.tokens.Token;
import org.nineml.coffeegrinder.tokens.TokenCharacter;
import org.nineml.coffeegrinder.tokens.TokenEOF;
import org.nineml.coffeegrinder.util.StopWatch;
import org.nineml.logging.Logger;

import java.util.*;

public class GllParser implements GearleyParser {
    public static final String logcategory = "Parser";
    public final Grammar grammar;
    private final ArrayList<State> grammarSlots;
    private final HashMap<Rule,List<State>> ruleSlots;
    private Token[] I;
    protected int c_U;
    protected int c_I;
    private HashSet<Descriptor> U;
    private Stack<Descriptor> R;
    private HashSet<PoppedNode> P;
    private HashMap<ClusterNode, ArrayList<CrfNode>> crf;
    private HashMap<State, HashMap<Integer, CrfNode>> crfNodes;
    private BinarySubtree bsr;
    private HashMap<NonterminalSymbol, HashMap<Integer, ClusterNode>> clusterNodes;
    private ArrayList<MStatement> compileStatements = null;
    private int instructionPointer = -1;
    private int nextInstruction = -1;
    private boolean moreInput = false;
    private boolean done = false;
    private ParserOptions options = null;
    protected Logger logger = null;
    private int progressSize = 0;
    private int progressCount = 0;
    private int highwater = 0;

    protected int tokenCount;
    protected Token lastToken;

    public GllParser(Grammar grammar) {
        this.grammar = grammar;
        options = grammar.getParserOptions();
        logger = options.getLogger();
        grammarSlots = new ArrayList<>();
        ruleSlots = new HashMap<>();
        NonterminalSymbol seed = grammar.getSeed();
        for (Rule rule : grammar.getRulesForSymbol(seed)) {
            List<State> slots = rule.getSlots();
            grammarSlots.addAll(slots);
            ruleSlots.put(rule, slots);
        }
        for (NonterminalSymbol symbol : grammar.getSymbols()) {
            if (!symbol.equals(seed)) {
                for (Rule rule : grammar.getRulesForSymbol(symbol)) {
                    List<State> slots = rule.getSlots();
                    grammarSlots.addAll(slots);
                    ruleSlots.put(rule, slots);
                }
            }
        }
    }

    /**
     * Return the parser type.
     * @return {@link ParserType#GLL}
     */
    public ParserType getParserType() {
        return ParserType.GLL;
    }

    public Grammar getGrammar() {
        return grammar;
    }

    public Token[] getTokens() {
        return I;
    }

    public NonterminalSymbol getSeed() {
        return grammar.getSeed();
    }

    public GllResult parse(String input) {
        Token[] tokens = new Token[input.length()];
        for (int pos = 0; pos < input.length(); pos++) {
            tokens[pos] = TokenCharacter.get(input.charAt(pos));
        }
        return parse(tokens);
    }

    public GllResult parse(Iterator<Token> input) {
        ArrayList<Token> list = new ArrayList<>();
        while (input.hasNext()) {
            list.add(input.next());
        }
        Token[] tokens = new Token[list.size()];
        for (int pos = 0; pos < list.size(); pos++) {
            tokens[pos] = list.get(pos);
        }
        return parse(tokens);
    }

    public GllResult parse(Token[] input) {
        logger = options.getLogger();

        StopWatch timer = new StopWatch();

        I = new Token[input.length+1];
        System.arraycopy(input, 0, I,  0, input.length);
        I[input.length] = TokenEOF.EOF;

        U = new HashSet<>();
        R = new Stack<>();
        P = new HashSet<>();
        crf = new HashMap<>();
        crfNodes = new HashMap<>();

        crf.put(new ClusterNode(grammar.getSeed(), 0), new ArrayList<>());

        bsr = new BinarySubtree(grammar.getSeed());
        clusterNodes = new HashMap<>();
        c_U = 0;
        c_I = 0;

        if (compileStatements == null) {
            compileStatements = new ArrayList<>();
            compile();

            if (State.L0.getInstructionPointer() < 0) {
                State.L0.setInstructionPointer(1);
            }

            logger.trace(logcategory, "compiled parser:");
            for (int pos = 1; pos < compileStatements.size(); pos++) {
                MStatement statement = compileStatements.get(pos);
                logger.trace(logcategory, "%4d %s", pos, statement);
                if (statement instanceof MLabel) {
                    State label = ((MLabel) statement).label;
                    label.setInstructionPointer(pos+1);
                }
            }
        }

        ntAdd(grammar.getSeed(), 0);

        execute();

        timer.stop();

        moreInput = bsr.getRightExtent()+1 < I.length;

        tokenCount = bsr.getRightExtent();
        if (tokenCount < I.length) {
            lastToken = I[tokenCount];
        }
        tokenCount++; // 1-based for the user

        // FIXME: still debugging
        //bsr.dump();

        GllResult result = new GllResult(this, bsr.extractSPPF(grammar, I));
        result.setParseTime(timer.duration());
        return result;
    }

    public boolean hasMoreInput() {
        return moreInput;
    }

    private void compile() {
        compileStatements.add(new MLabel(State.L0));
        compileStatements.add(new MNextDescriptor());

        // I don't think the seed symbol has to come first, but I like it better that way
        for (Rule rule : grammar.getRulesForSymbol(grammar.getSeed())) {
            compile(rule);
        }
        for (NonterminalSymbol symbol : grammar.getSymbols()) {
            if (!symbol.equals(grammar.getSeed())) {
                for (Rule rule : grammar.getRulesForSymbol(symbol)) {
                    compile(rule);
                }
            }
        }
    }

    private void compile(Rule rule) {
        ArrayList<State> slots = new ArrayList<>(ruleSlots.get(rule));
        compileStatements.add(new MLabel(slots.get(0)));
        int pos = 0;
        for (State slot : slots) {
            compile(slot);
            if (pos > 0 && pos < slot.rhs.length) {
                compileStatements.add(new MTestSelect(slot));
            }
            pos++;
        }
        compileStatements.add(new MFollow(rule.symbol));
        compileStatements.add(new MGoto(State.L0));
    }

    private void compile(State slot) {
        if (slot.position == 0) {
            if (slot.rhs.isEmpty()) {
                compileEpsilon(slot);
            }
            return;
        }

        Symbol prev = slot.prevSymbol();
        if (prev instanceof TerminalSymbol) {
            compileTerminal(slot);
        } else {
            compileNonterminal(slot);
        }
    }

    private void compileEpsilon(State slot) {
        compileStatements.add(new MBsrAdd(slot, true));
    }

    private void compileTerminal(State slot) {
        compileStatements.add(new MBsrAdd(slot));
        compileStatements.add(new MIncrementCI());
    }

    private void compileNonterminal(State slot) {
        compileStatements.add(new MCall(slot));
        compileStatements.add(new MGoto(State.L0));
        compileStatements.add(new MLabel(slot));
    }

    private void execute() {
        nextInstruction = 1;
        done = false;

        logger.trace(logcategory, "execute parser:");

        ProgressMonitor monitor = options.getProgressMonitor();
        if (monitor != null) {
            progressSize = monitor.starting(this);
            progressCount = progressSize;
        }

        while (!done) {
            if (monitor != null) {
                if (bsr.getRightExtent() > highwater) {
                    progressCount -= (bsr.getRightExtent() - highwater);
                    highwater = bsr.getRightExtent();
                    if (progressCount <= 0) {
                        monitor.progress(this, highwater);
                        progressCount = progressSize;
                    }
                }
            }

            instructionPointer = nextInstruction;
            MStatement stmt = compileStatements.get(instructionPointer);
            nextInstruction++;
            stmt.execute(this);
        }

        if (monitor != null) {
            monitor.finished(this);
        }

        logger.trace(logcategory, "execution finished");
    }

    protected void nextDescriptor() {
        done = R.isEmpty();
        if (done) {
            logger.trace(logcategory, "%4d exit", instructionPointer);
        } else {
            Descriptor desc = R.pop();
            c_U = desc.k;
            c_I = desc.j;
            nextInstruction = desc.slot.getInstructionPointer();
            // The nextDescriptor statement is always at position 1 in the program
            logger.trace(logcategory, "%4d c_U = %d; c_I = %d; goto %d", 1, c_U, c_I, nextInstruction);
        }
    }

    protected void jump(State label) {
        nextInstruction = label.getInstructionPointer();
        logger.trace(logcategory, "%4d goto %d", instructionPointer, nextInstruction);
    }

    protected void incrementC_I() {
        c_I++;
        logger.trace(logcategory, "%4d c_I = %d", instructionPointer, c_I);
    }

    protected void follow(NonterminalSymbol symbol) {
        Token token = c_I >= I.length ? null : I[c_I];
        boolean inFollow = false;
        for (Symbol sym : grammar.getFollow(symbol)) {
            if (sym.matches(token)) {
                inFollow = true;
                break;
            }
        }
        if (inFollow) {
            logger.trace(logcategory, "%4d if (%s ∈ follow(%s)) then rtn(%s, %d, %d)", instructionPointer, token, symbol, symbol, c_U, c_I);
            rtn(symbol, c_U, c_I);
        } else {
            logger.trace(logcategory, "%4d if (%s ∉ follow(%s)) then nop", instructionPointer, token, symbol);
        }
    }

    protected void ntAdd(NonterminalSymbol X, int j) {
        for (State slot : grammarSlots) {
            if (X.equals(slot.symbol) && slot.position == 0) {
                if (testSelect(I[j], X, slot)) {
                    dscAdd(slot, j, j);
                }
            }
        }
    }

    protected void testSelect(State slot) {
        String expr = "";

        // Don't go to all the trouble of constructing the string if we aren't going to output it
        if (logger.getLogLevel(logcategory) >= Logger.TRACE) {
            StringBuilder sb = new StringBuilder();
            sb.append("testSelect(").append(I[c_I]).append(", ").append(slot.symbol).append(", ");
            int pos = slot.position;
            while (pos < slot.rhs.length) {
                if (pos > slot.position) {
                    sb.append(" ");
                }
                sb.append(slot.rhs.get(pos));
                pos++;
            }
            expr = sb.toString();
        }

        if (testSelect(I[c_I], slot.symbol, slot)) {
            logger.trace(logcategory,"%4d if (%s) then nop", instructionPointer, expr);
        } else {
            logger.trace(logcategory, "%4d if (!%s) then goto %d", instructionPointer, expr, State.L0.getInstructionPointer());
            jump(State.L0);
        }
    }

    protected boolean testSelect(Token b, NonterminalSymbol X, State alpha) {
        boolean hasEpsilon = false;
        for (Symbol symbol : alpha.getFirst(grammar)) {
            if (symbol.matches(b)) {
                return true;
            }
            hasEpsilon = hasEpsilon || (symbol == TerminalSymbol.EPSILON);
        }

        if (hasEpsilon) {
            for (Symbol symbol : grammar.getFollow(X)) {
                if (symbol.matches(b)) {
                    return true;
                }
            }
        }

        return false;
    }

    protected void dscAdd(State slot, int k, int i) {
        Descriptor desc = slot.getDescriptor(k, i);
        if (!U.contains(desc)) {
            U.add(desc);
            R.push(desc);
        }
    }

    private ClusterNode getClusterNode(NonterminalSymbol X, int k) {
        if (!clusterNodes.containsKey(X)) {
            clusterNodes.put(X, new HashMap<>());
        }
        if (!clusterNodes.get(X).containsKey(k)) {
            clusterNodes.get(X).put(k, new ClusterNode(X, k));
        }

        return clusterNodes.get(X).get(k);
    }

    protected void rtn(NonterminalSymbol X, int k, int j) {
        PoppedNode pn = new PoppedNode(X, k, j);
        //PoppedNode pn = getPoppedNode(X, k, j);
        if (!P.contains(pn)) {
            P.add(pn);
            ClusterNode Xk = getClusterNode(X, k);
            if (crf.containsKey(Xk)) {
                for (CrfNode v : crf.get(Xk)) {
                    dscAdd(v.slot, v.i, j);
                    bsrAdd(v.slot, v.i, k, j);
                }
            } else {
                logger.trace(logcategory, "No key " + Xk + " in crf");
            }
        }
    }

    protected void bsrAdd(State L, int i, int k, int j) {
        if (instructionPointer >= 0) {
            logger.trace(logcategory, "%4d bsrAdd(%s, %d, %d, %d)", instructionPointer, L, i, k, j);
        } else {
            logger.trace(logcategory, "---- bsrAdd(%s, %d, %d, %d)", L, i, k, j);
        }
        bsr.add(L, i, k, j);
    }

    protected void bsrAddEpsilon(State L, int i) {
        if (instructionPointer >= 0) {
            logger.trace(logcategory, "%4d bsrAdd(%s, %d, %d, %d)", instructionPointer, L, i, i, i);
        } else {
            logger.trace(logcategory, "---- bsrAdd(%s, %d, %d, %d)", L, i, i, i);
        }
        bsr.addEpsilon(L, i);
    }

    private CrfNode getCrfNode(State L, int i) {
        if (!crfNodes.containsKey(L)) {
            crfNodes.put(L, new HashMap<>());
        }
        if (!crfNodes.get(L).containsKey(i)) {
            crfNodes.get(L).put(i, new CrfNode(L, i));
        }
        return crfNodes.get(L).get(i);
    }

    protected void call(State L, int i, int j) {
        logger.trace(logcategory, "%4d call(%s, %d, %d)", instructionPointer, L, i, j);
        CrfNode u = getCrfNode(L, i);
        NonterminalSymbol X = (NonterminalSymbol) L.prevSymbol();
        ClusterNode ndV = getClusterNode(X, j);
        if (!crf.containsKey(ndV)) {
            crf.put(ndV, new ArrayList<>());
            crf.get(ndV).add(u);
            ntAdd(X, j);
        } else {
            List<CrfNode> v = crf.get(ndV);
            if (!edgeExists(v, u)) {
                crf.get(ndV).add(u);
                for (PoppedNode pnd : P) {
                    if (X.equals(pnd.symbol) && j == pnd.k) {
                        dscAdd(L, i, pnd.j);
                        bsrAdd(L, i, j, pnd.j);
                    }
                }
            }
        }
    }

    private boolean edgeExists(List<CrfNode> nodes, CrfNode target) {
        for (CrfNode node : nodes) {
            if (node.equals(target)) {
                return true;
            }
        }
        return false;
    }

    public boolean succeeded() {
        return bsr.succeeded();
    }
}
