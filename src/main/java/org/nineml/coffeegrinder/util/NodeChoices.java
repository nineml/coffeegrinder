package org.nineml.coffeegrinder.util;

import org.nineml.coffeegrinder.exceptions.TreeWalkerException;
import org.nineml.coffeegrinder.parser.Family;
import org.nineml.coffeegrinder.parser.ForestNode;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class NodeChoices {
    private final ForestNode node;
    private final Messages messages;
    protected final ArrayList<Family> families = new ArrayList<>();
    protected final ArrayList<BigInteger> chooseW = new ArrayList<>();
    protected final ArrayList<BigInteger> chooseV = new ArrayList<>();
    private int index = 0;

    protected NodeChoices(ForestNode node, Messages messages) {
        this.node = node;
        this.messages = messages;
        reset();
    }

    public List<Family> getFamilies() {
        return families;
    }

    private void reset() {
        index = 0;
        families.clear();
        chooseW.clear();
        chooseV.clear();

        for (Family family : node.getFamilies()) {
            if (!node.getLoops().contains(family)) {
                families.add(family);
                if (family.w == null) {
                    chooseW.add(BigInteger.ZERO);
                } else {
                    chooseW.add(family.w.getExactParsesBelow().subtract(BigInteger.ONE));
                }
                if (family.v == null) {
                    chooseV.add(BigInteger.ZERO);
                } else {
                    chooseV.add(family.v.getExactParsesBelow().subtract(BigInteger.ONE));
                }
            }
        }
    }

    protected Family currentChoice() {
        if (families.isEmpty()) {
            return null;
        }
        //System.err.printf("CUR: %d of %d / %s\n", index+1, families.size(), node);
        return families.get(index);
    }

    protected Family advance() {
        if (families.isEmpty()) {
            throw new TreeWalkerException("Error in tree walk");
        }

        Family family = families.get(index);
        BigInteger w = chooseW.get(index);
        BigInteger v = chooseV.get(index);

        if (!BigInteger.ZERO.equals(w)) {
            w = w.subtract(BigInteger.ONE);
            chooseW.set(index, w);
        } else if (!BigInteger.ZERO.equals(v)) {
            v = v.subtract(BigInteger.ONE);
            chooseV.set(index, v);
        } else {
            index++;
            if (messages != null) {
                messages.debug("Changing %s :: %d", node, index);
            }
        }

        if (index == families.size()) {
            index = 0;
            if (messages != null) {
                messages.debug("Resetting %s :: %d", node, index);
            }
            reset();
        }

        return family;
    }
}
