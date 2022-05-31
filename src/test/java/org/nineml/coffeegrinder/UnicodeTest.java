package org.nineml.coffeegrinder;

import org.junit.Assert;
import org.junit.Test;
import org.nineml.coffeegrinder.tokens.Token;
import org.nineml.coffeegrinder.tokens.TokenCharacter;
import org.nineml.coffeegrinder.util.Iterators;

import java.util.Iterator;

public class UnicodeTest {
    @Test
    public void outsideTheBMP() {
        String input = "abc\uD83D\uDE3A";
        Iterator<Token> chars = Iterators.characterIterator(input);
        int count = 0;
        while (chars.hasNext()) {
            TokenCharacter t = (TokenCharacter) chars.next();
            switch (count) {
                case 0:
                    Assert.assertEquals('a', t.getCodepoint());
                    break;
                case 1:
                    Assert.assertEquals('b', t.getCodepoint());
                    break;
                case 2:
                    Assert.assertEquals('c', t.getCodepoint());
                    break;
                case 3:
                    Assert.assertEquals(0x1F63A, t.getCodepoint());
                    break;
                default:
                    break;
            }
            count++;
        }
        Assert.assertEquals(4, count);
    }
}
