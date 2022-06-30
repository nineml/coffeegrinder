package org.nineml.coffeegrinder.parser;

import org.nineml.coffeegrinder.gll.BinarySubtree;
import org.nineml.coffeegrinder.tokens.Token;
import org.nineml.coffeegrinder.tokens.TokenCharacter;
import org.nineml.logging.Logger;

import java.util.ArrayList;
import java.util.List;

// This is an experiment at generating BSRs from an Earley parser. It doesn't work.

public class XEarleyParser {
    public static final String logcategory = "Parser";
    public final ParserGrammar grammar;
    private final NonterminalSymbol seedPrime;
    private final ArrayList<State> grammarSlots;
    private final EarleyChart S = new EarleyChart();
    private final EarleyChart R = new EarleyChart();
    private final ArrayList<EarleyItem> openItems = new ArrayList<>();
    private final BinarySubtree bsr;

    private ParserOptions options = null;
    protected Logger logger = null;
    protected Token[] input = null;

    private XEarleyParser(SourceGrammar sourceGrammar, NonterminalSymbol seed) {
        SourceGrammar modifiedGrammar = new SourceGrammar(sourceGrammar);
        seedPrime = modifiedGrammar.getNonterminal("$$");
        modifiedGrammar.addRule(seedPrime, seed);
        grammar = new ParserGrammar(modifiedGrammar, ParserType.Earley, seedPrime);

        bsr = new BinarySubtree(grammar.getSeed(), options);

        options = grammar.getParserOptions();
        logger = options.getLogger();
        grammarSlots = new ArrayList<>();
        for (NonterminalSymbol symbol : grammar.getSymbols()) {
            for (Rule rule : grammar.getRulesForSymbol(symbol)) {
                List<State> slots = rule.getSlots();
                grammarSlots.addAll(slots);
            }
        }
    }

    public void parse(String input) {
        Token[] tokens = new Token[input.length()];
        for (int pos = 0; pos < input.length(); pos++) {
            tokens[pos] = TokenCharacter.get(input.charAt(pos));
        }
        parse(tokens);
    }

    public void parse(Token[] input) {
        logger = options.getLogger();
        this.input = input;

        for (State slot : grammarSlots) {
            if (seedPrime.equals(slot.symbol) && slot.position == 0) {
                EarleyItem start = new EarleyItem(slot, 0);
                openItems.add(start);
                S.add(0, start);
                R.add(0, start);
            }
        }

        int pos = 0;
        while (!openItems.isEmpty()) {
            while (!openItems.isEmpty()) {
                EarleyItem item = openItems.remove(0);
                Symbol symbol = item.state.nextSymbol();
                if (symbol == null) {
                    complete(item, pos);
                } else if (symbol instanceof TerminalSymbol) {
                    scan(item, pos);
                } else {
                    predict(item, pos);
                }
            }
            pos++;
            openItems.addAll(R.get(pos));
        }
    }

    private void predict(EarleyItem item, int j) {
        final Symbol next = item.state.nextSymbol();
        if (next == null || next instanceof TerminalSymbol) {
            return;
        }

        for (State slot : grammarSlots) {
            if (next.equals(slot.symbol) && slot.position == 0) {
                EarleyItem predict = new EarleyItem(slot, j);
                if (!S.contains(j, predict)) {
                    S.add(j, predict);
                    R.add(j, predict);
                    openItems.add(predict);
                }
            }
        }

        for (EarleyItem complete : S.get(j)) {
            if (next.equals(complete.state.symbol) && complete.state.nextSymbol() == null) {
                EarleyItem advance = new EarleyItem(item.state, item.j);
                if (!S.contains(j, advance)) {
                    S.add(j, complete);
                    R.add(j, complete);
                    openItems.add(complete);
                    bsr.add(complete.state, item.j, j, j);
                }
            }
        }
    }

    private void complete(EarleyItem item, int j) {
        List<EarleyItem> items = new ArrayList<>(S.get(item.j));
        for (EarleyItem cstate : items) {
            if (item.state.symbol.equals(cstate.state.nextSymbol())) {
                EarleyItem complete = new EarleyItem(cstate.state.advance(), cstate.j);
                if (!S.contains(j, complete)) {
                    S.add(j, complete);
                    R.add(j, complete);
                    openItems.add(complete);
                }
                bsr.add(item.state, item.j, cstate.j, j);
            }
        }
    }

    private void scan(EarleyItem item, int j) {
        if (item.state.nextSymbol() instanceof TerminalSymbol && j < input.length) {
            TerminalSymbol b = (TerminalSymbol) item.state.nextSymbol();
            if (b.matches(input[j])) {
                EarleyItem complete = new EarleyItem(item.state.advance(), item.j);
                if (!S.contains(j+1, complete)) {
                    S.add(j+1, complete);
                    R.add(j+1, complete);
                    bsr.add(complete.state, item.j, j, j+1);
                }
            }
        }
    }
}
