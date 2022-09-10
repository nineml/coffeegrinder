package org.nineml.coffeegrinder.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

public class StringTreeBuilder extends PrintStreamTreeBuilder {
    private ByteArrayOutputStream baos = new ByteArrayOutputStream();

    public StringTreeBuilder() {
        tab = "";
        nl = "";
        try {
            PrintStream stream = new PrintStream(baos, true, "UTF-8");
            setStream(stream);
        } catch (UnsupportedEncodingException ex) {
            // this can't happen...
            throw new RuntimeException(ex.getMessage());
        }
    }

    public String getTree() {
        try {
            return baos.toString("UTF-8");
        } catch (UnsupportedEncodingException ex) {
            // this can't happen...
            throw new RuntimeException(ex.getMessage());
        }
    }
}
