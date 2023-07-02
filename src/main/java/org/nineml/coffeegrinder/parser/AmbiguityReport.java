package org.nineml.coffeegrinder.parser;

import org.nineml.coffeegrinder.util.BricsAmbiguity;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

public class AmbiguityReport {
    final Grammar grammar;
    final NonterminalSymbol seed;
    final BricsAmbiguity ambiguityChecker;
    private String ambiguityReport = null;

    protected AmbiguityReport(ParserGrammar grammar) {
        this.grammar = grammar;
        this.seed = grammar.getSeed();
        ambiguityChecker = new BricsAmbiguity();
    }

    protected AmbiguityReport(SourceGrammar grammar, NonterminalSymbol seed) {
        this.grammar = grammar;
        this.seed = seed;
        ambiguityChecker = new BricsAmbiguity();
    }

    public boolean getReliable() {
        return ambiguityChecker.getReliable();
    }

    public boolean getUnambiguous() {
        return ambiguityChecker.getUnambiguous();
    }

    public boolean getCheckSucceeded() {
        return ambiguityChecker.getCheckSucceeded();
    }

    public String getAmbiguityReport() {
        return ambiguityReport;
    }

    public void check() {
        ByteArrayOutputStream reportbytes = new ByteArrayOutputStream();
        PrintWriter report = new PrintWriter(reportbytes);
        ambiguityChecker.checkGrammar(grammar, seed, report);
        report.close();
        try {
            ambiguityReport = reportbytes.toString("utf-8");
        } catch (UnsupportedEncodingException ex) {
            // This can't happen.
            throw new RuntimeException(ex);
        }
    }



}
