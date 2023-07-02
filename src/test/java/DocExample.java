import org.nineml.coffeegrinder.trees.ParseTree;
import org.nineml.coffeegrinder.trees.ParseTreeBuilder;
import org.nineml.coffeegrinder.parser.*;

import static org.junit.Assert.fail;

/**
 * Documentation example.
 *
 * This short program doesn't do anything useful, but it's the source for the example
 * in the documentation. Having it here assures that if the API changes, so will
 * the example!
 */

public class DocExample {
    public static void main(String[] args) {
        ParserOptions options = new ParserOptions();
        SourceGrammar grammar = new SourceGrammar(options);

        NonterminalSymbol S = grammar.getNonterminal("S");
        NonterminalSymbol A = grammar.getNonterminal("A");
        NonterminalSymbol B = grammar.getNonterminal("B");
        NonterminalSymbol X = grammar.getNonterminal("X");
        NonterminalSymbol Y = grammar.getNonterminal("Y");

        Rule s1 = new Rule(S, A);
        grammar.addRule(s1);

        grammar.addRule(S, B);
        grammar.addRule(A, TerminalSymbol.ch('a'), X);
        grammar.addRule(A, TerminalSymbol.ch('b'), X);
        grammar.addRule(B, TerminalSymbol.ch('b'), X);
        grammar.addRule(X, TerminalSymbol.ch('x'));
        grammar.addRule(Y, TerminalSymbol.ch('y'));

        ParserGrammar pgrammar = grammar.getCompiledGrammar(S);

        HygieneReport report = pgrammar.getHygieneReport();
        if (!report.isClean()) {
            // TODO: deal with undefined, unused, and unproductive items
        }

        GearleyParser parser = pgrammar.getParser(options);

        GearleyResult result = parser.parse("bx");

        if (result.succeeded()) {
            ParseTreeBuilder builder = new ParseTreeBuilder();
            ParseForest forest = result.getForest();
            forest.getWalker().getNextTree(builder);
            ParseTree tree = builder.getTree();

            // TODO: do something with the tree.

            if (forest.isAmbiguous()) {
                long totalParses = forest.getParseTreeCount();
                // TODO: deal with multiple parses
            }
        } else {
            // TODO: deal with failure
        }
    }
}
