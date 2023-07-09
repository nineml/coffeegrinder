package org.nineml.coffeegrinder;

import org.junit.Assert;
import org.nineml.coffeegrinder.parser.ForestWalker;
import org.nineml.coffeegrinder.parser.ParseForest;
import org.nineml.coffeegrinder.parser.ParserOptions;
import org.nineml.coffeegrinder.trees.StringTreeBuilder;

import java.util.HashMap;
import java.util.List;

import static junit.framework.TestCase.fail;

public class CoffeeGrinderTest {
    protected final ParserOptions globalOptions = new ParserOptions();

    protected void expectForestCount(ParseForest forest, int expected) {
        int count = 0;
        ForestWalker xxx = forest.getWalker();
        while (xxx.hasMoreTrees()) {
            StringTreeBuilder builder = new StringTreeBuilder();
            xxx.getNextTree(builder);
            count++;
        }
        Assert.assertEquals(expected, count);
    }

    protected void expectTrees(ForestWalker walker, List<String> trees) {
        HashMap<String, Integer> expected = new HashMap<>();
        for (String tree : trees) {
            if (expected.containsKey(tree)) {
                fail("Duplicate tree in expected list");
            }
            expected.put(tree, 0);
        }
        int count = 0;
        StringTreeBuilder builder = new StringTreeBuilder(true);
        while (walker.hasMoreTrees()) {
            walker.getNextTree(builder);
            String tree = builder.getTree();
            if (!expected.containsKey(tree)) {
                fail(String.format("Unexpected tree: %s", tree));
            }
            if (expected.get(tree) != 0) {
                fail(String.format("Duplicate tree: %s", tree));
            }
            count++;
        }
        if (count < expected.size()) {
            fail(String.format("Expected %d trees, got %d", trees.size(), count));
        }
    }

    protected void showTrees(ForestWalker walker) {
        showTrees(walker, false);
    }

    protected void showTrees(ForestWalker walker, boolean asList) {
        if (asList) {
            System.err.println("expectTrees(result.getForest().getWalker(), Arrays.asList(");
        }

        int count = 0;
        StringTreeBuilder builder = new StringTreeBuilder(true);
        while (walker.hasMoreTrees()) {
            walker.getNextTree(builder);
            String tree = builder.getTree();
            if (asList) {
                if (count > 0) {
                    System.err.println(",");
                }
                System.err.printf("\"%s\"", tree);
            } else {
                System.err.println(tree);
            }
            count++;
            if (count > 40) {
                fail("Unreasonable number of trees?");
            }
        }

        if (asList) {
            System.err.println("));");
        }
    }
}
