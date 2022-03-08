package org.nineml.coffeegrinder.util;

import org.nineml.coffeegrinder.exceptions.CompilerException;
import org.nineml.coffeegrinder.parser.*;
import org.nineml.coffeegrinder.tokens.*;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.nineml.coffeegrinder.exceptions.ForestException;
import org.nineml.coffeegrinder.exceptions.GrammarException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * Store and retrieve CoffeeGrinder grammar files.
 *
 * <p>The {@link #compile} method will return a string representation for a grammar that
 * can be loaded with the {@link #parse} method.</p>
 */
public class GrammarCompiler {
    private static final String formatVersion="0.9.2";
    private static final char nameEscape = 'ǝ';
    private static final String NS = "http://nineml.org/coffeegrinder/ns/grammar/compiled";
    private static final HashMap<Character,String> entities = new HashMap<>();
    static {
        // not &apos; because we always use " to delimit attributes
        entities.put('"', "&quot;");
        entities.put('&', "&amp;");
        entities.put('<', "&lt;");
        entities.put('>', "&gt;");
    }

    private final HashMap<String,Collection<ParserAttribute>> agroups = new HashMap<>();
    private StringBuilder sb = null;
    private MessageDigest xdigest = null;
    private Grammar grammar = null;
    private ParserOptions options = null;
    // A list rather than a Properties object so that order is preserved
    private ArrayList<Property> properties = new ArrayList<>();

    /**
     * Construct a grammar compiler with default parser options.
     */
    public GrammarCompiler() {
        this(new ParserOptions());
    }

    /**
     * Construct a grammar compiler.
     * @param options the parse options.
     */
    public GrammarCompiler(ParserOptions options) {
        this.options = options;
    }

    public Grammar getGrammar() {
        return grammar;
    }

    private void initializeDigest() {
        try {
            xdigest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ex) {
            throw CompilerException.messageDigestError("SHA-256", ex.getMessage());
        }
    }

    private void updateDigest(String value) {
        //System.err.println("DIGEST: " + value);
        xdigest.update(value.getBytes(StandardCharsets.UTF_8));
    }

    private void updateDigest(char value) {
        updateDigest(""+value);
    }

    private Property findProperty(String name) {
        for (Property property : properties) {
            if (property.name.equals(name)) {
                return property;
            }
        }
        return null;
    }

    public void setProperty(String name, String value) {
        if (name == null) {
            throw new NullPointerException("Name must not be null");
        }
        if ("".equals(name)) {
            throw new IllegalArgumentException("Name must not be the empty string");
        }
        if (value == null) {
            throw new NullPointerException("Value must not be null");
        }
        Property property = findProperty(name);
        if (property == null) {
            properties.add(new Property(name, value));
        } else {
            property.value = value;
        }
    }

    public String getProperty(String name) {
        Property property = findProperty(name);
        if (property == null) {
            return null;
        }
        return property.value;
    }

    /**
     * Compile a grammar.
     *
     * @param grammar the grammar to compile
     * @return a "compiled" string format of the grammar
     */
    public String compile(Grammar grammar) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        compile(grammar, ps);
        try {
            return baos.toString("UTF-8");
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalArgumentException("Unexpected (i.e. impossible) unsupported encoding exception", ex);
        }
    }

    /**
     * Compile a grammar.
     *
     * @param grammar the grammar to compile
     * @param ps the print stream where the compiled grammar is written
     */
    public void compile(Grammar grammar, PrintStream ps) {
        initializeDigest();
        agroups.clear();
        this.grammar = null;
        sb = new StringBuilder();
        sb.append("<grammar xmlns=\"").append(NS).append("\"");
        sb.append(" version=\"").append(formatVersion).append("\">\n");

        for (Property property : properties) {
            if (!"".equals(property.name)) {
                sb.append("<meta name='").append(xmlString(property.name));
                sb.append("' value='");
                sb.append(xmlString(property.value));
                sb.append("'/>\n");
            }
        }

        // Reinitialize the digest after we output the properties
        initializeDigest();
        updateDigest(formatVersion);

        for (Rule rule : grammar.getRules()) {
            // Before we output the rule, make sure we output any new attribute groups
            atgroup(rule.getSymbol().getAttributes());
            for (Symbol symbol : rule.getRhs()) {
                atgroup(symbol.getAttributes());
                if (symbol instanceof TerminalSymbol) {
                    atgroup(((TerminalSymbol) symbol).getToken().getAttributes());
                }
            }

            sb.append("<r n=\"").append(xmlString(rule.getSymbol().getName())).append("\"");
            standardAttributes(rule.getSymbol().getAttributes());
            sb.append(" ag=\"").append(atgroup(rule.getSymbol().getAttributes())).append("\"");
            sb.append(">");

            for (Symbol symbol : rule.getRhs()) {
                if (symbol instanceof TerminalSymbol) {
                    sb.append("<t");
                    standardAttributes(symbol.getAttributes());
                    sb.append(" ag=\"").append(atgroup(symbol.getAttributes())).append("\"");
                    sb.append(">");

                    Token token = ((TerminalSymbol) symbol).getToken();
                    if (token instanceof TokenCharacterSet) {
                        sb.append("<cs");
                        standardAttributes(token.getAttributes());
                        sb.append(" ag=\"").append(atgroup(token.getAttributes())).append("\"");

                        TokenCharacterSet tcs = (TokenCharacterSet) token;
                        if (tcs.isInclusion()) {
                            sb.append(" inclusion=\"");
                            updateDigest("inclusion");
                        } else {
                            sb.append(" exclusion=\"");
                            updateDigest("exclusion");
                        }

                        StringBuilder csvalue = new StringBuilder();
                        boolean csfirst = true;
                        for (CharacterSet cs : tcs.getCharacterSets()) {
                            if (!csfirst) {
                                csvalue.append(";");
                            }
                            csfirst = false;

                            if (cs.isRange()) {
                                if (cs.getRangeFrom() == cs.getRangeTo()) {
                                    csvalue.append(String.format("'%s'", (char) cs.getRangeFrom()));
                                } else {
                                    csvalue.append(String.format("'%s'-'%s'", (char) cs.getRangeFrom(), (char) cs.getRangeTo()));
                                }
                            } else if (cs.isUnicodeCharacterClass()) {
                                csvalue.append(cs.getUnicodeCharacterClass());
                            } else if (cs.isSetOfCharacters()) {
                                // Quoted strings need to be *doubly* escaped
                                String value = cs.getCharacters().replace("&", "&amp;").replace("\"", "&quot;");
                                csvalue.append('"').append(value).append('"');
                            } else {
                                throw CompilerException.unexpectedCharacterSet(cs.toString());
                            }
                        }
                        sb.append(xmlString(csvalue.toString()));
                        sb.append("\"/>");
                    } else if (token instanceof TokenCharacter) {
                        char ch = ((TokenCharacter) token).getValue();
                        sb.append("<c");
                        standardAttributes(token.getAttributes());
                        sb.append(" ag=\"").append(atgroup(token.getAttributes())).append("\"");
                        sb.append(" v=\"").append(xmlChar(ch)).append("\"/>");
                    } else if (token instanceof TokenString) {
                        String str = ((TokenString) token).getValue();
                        sb.append("<s");
                        standardAttributes(token.getAttributes());
                        sb.append(" ag=\"").append(atgroup(token.getAttributes())).append("\"");
                        sb.append(" v=\"").append(xmlString(str)).append("\"/>");
                    } else if (token instanceof TokenRegex) {
                        String regex = ((TokenRegex) token).getValue();
                        sb.append("<re");
                        standardAttributes(token.getAttributes());
                        sb.append(" ag=\"").append(atgroup(token.getAttributes())).append("\"");
                        sb.append(" v=\"").append(xmlString(regex)).append("\"/>");
                    } else {
                        throw CompilerException.unexpectedTerminalTokenClass(token.toString());
                    }

                    sb.append("</t>");
                } else {
                    NonterminalSymbol nt = (NonterminalSymbol) symbol;
                    sb.append("<nt n=\"").append(xmlString(nt.getName())).append("\"");
                    standardAttributes(nt.getAttributes());
                    sb.append(" ag=\"").append(atgroup(symbol.getAttributes())).append("\"");
                    sb.append("/>");
                }
            }
            sb.append("</r>\n");
        }

        sb.append("<check Σ=\"");
        byte[] hash = xdigest.digest();
        for (int pos = hash.length - 8; pos < hash.length; pos++) {
            sb.append(Integer.toString((hash[pos] & 0xff) + 0x100, 16).substring(1));
        }
        sb.append("\"/>\n</grammar>\n");

        ps.print(sb);
    }

    public void compile(Grammar grammar, String filename) {
        try {
            FileOutputStream fos = new FileOutputStream(filename);
            PrintStream stream = new PrintStream(fos);
            compile(grammar, stream);
            stream.close();
            fos.close();
        } catch (IOException ex) {
            throw ForestException.ioError(filename, ex);
        }
    }

    private List<ParserAttribute> filtered(Collection<ParserAttribute> attributes) {
        if (attributes.isEmpty()) {
            return Collections.emptyList();
        }
        ArrayList<ParserAttribute> filtered = new ArrayList<>();
        for (ParserAttribute attr : attributes) {
            if (!Symbol.OPTIONAL.getName().equals(attr.getName())
                && !ParserAttribute.PRUNING.equals(attr.getName())) {
                filtered.add(attr);
            }
        }

        return filtered;
    }

    private String xmlString(String str) {
        if (str.length() == 1) {
            return xmlChar(str.charAt(0));
        }
        StringBuilder sb = new StringBuilder();
        for (int pos = 0; pos < str.length(); pos++) {
            sb.append(xmlChar(str.charAt(pos)));
        }
        return sb.toString();
    }

    private String xmlChar(char ch) {
        updateDigest(ch);
        if (entities.containsKey(ch)) {
            return entities.get(ch);
        }

        if (ch == '\\') {
            return "&#x5c;";
        }

        // Java characters that are also valid XML characters
        boolean ok = ch == 0x09 /*tab*/ || ch == 0x0a /*lf*/ || ch == 0x0d /*cr*/ ;
        ok = ok || (ch >= 0x20 && ch <= 0xd7ff);
        ok = ok || (ch >= 0xe000 && ch <= 0xfffd);

        if (ok) {
            if (ch < 32 || (ch >= 0x80 && ch <= 0x9f)) {
                return String.format("&#x%x;", (int) ch);
            }
            StringBuilder sb = new StringBuilder();
            sb.appendCodePoint(ch);
            return sb.toString();
        }

        return String.format("\\U+%04x;", (int) ch);
    }

    private String unxmlString(String xml) {
        return unxmlString(xml, true);
    }

    private String unxmlString(String xml, boolean updateDigest) {
        String value = xml;

        for (char ch : entities.keySet()) {
            String esc = entities.get(ch);
            if (value.contains(esc)) {
                value = value.replace(esc, ""+ch);
            }
        }

        // Do \U+ first because there may be "\"'s in the hex encoded chars
        value = unescape("\\U+", value);
        value = unescape("&#x", value);

        if (updateDigest) {
            updateDigest(value);
        }

        return value;
    }

    private String unescape(String prefix, String value) {
        int pos = value.indexOf(prefix);
        if (pos >= 0) {
            StringBuilder nsb = new StringBuilder();
            while (pos >= 0) {
                nsb.append(value, 0, pos);
                value = value.substring(pos+prefix.length());
                pos = value.indexOf(";");
                if (pos <= 0) {
                    throw CompilerException.invalidNameEscaping(value, value);
                }
                String hex = value.substring(0, pos);
                nsb.appendCodePoint(Integer.parseInt(hex, 16));
                value = value.substring(pos+1);
                pos = value.indexOf(prefix);
            }
            nsb.append(value);
            return nsb.toString();
        }
        return value;
    }

    private void standardAttributes(Collection<ParserAttribute> attributes) {
        String value = "";
        for (ParserAttribute attr : attributes) {
            if (attr.getName().equals(Symbol.OPTIONAL.getName())
                    && attr.getValue().equals(Symbol.OPTIONAL.getValue())) {
                value += "?";
            }
            if (attr.getName().equals(ParserAttribute.PRUNING)) {
                if (attr.getValue().equals(ParserAttribute.PRUNING_ALLOWED.getValue())) {
                    value += "p";
                } else {
                    value += "f";
                }
            }
        }

        if (!"".equals(value)) {
            sb.append(" a=\"").append(value).append("\"");
            updateDigest(value);
        }
    }

    private boolean sameCollection(Collection<ParserAttribute> group, Collection<ParserAttribute> candidate) {
        if (group.size() == candidate.size()) {
            for (ParserAttribute attr : candidate) {
                if (!group.contains(attr)) {
                    return false;
                };
            }
            return true;
        }
        return false;
    }

    private String atgroup(Collection<ParserAttribute> attributes) {
        for (String gid : agroups.keySet()) {
            if (sameCollection(agroups.get(gid), attributes)) {
                return gid;
            }
        }
        String gid = "g" + (agroups.size()+1);
        agroups.put(gid, attributes);
        outputAttributes(gid, attributes);
        return gid;
    }

    private void outputAttributes(String gid, Collection<ParserAttribute> attributes) {
        sb.append("<ag xml:id=\"").append(gid).append("\"");
        updateDigest(gid);
        for (ParserAttribute attr : attributes) {
            sb.append(" ").append(nameString(attr.getName())).append("=\"").append(xmlString(attr.getValue())).append("\"");
        }
        sb.append("/>\n");
    }

    private String nameString(String name) {
        updateDigest(name);
        StringBuilder nsb = new StringBuilder();
        for (int pos = 0; pos < name.length(); pos++) {
            nsb.append(nameChar(name.charAt(pos), pos == 0));
        }
        return nsb.toString();
    }

    private String nameChar(char ch, boolean first) {
        // FIXME: check if this is wholly correct for XML names
        if (ch == nameEscape || (!Character.isLetter(ch) && !Character.isDigit(ch)) || (first && !Character.isLetter(ch))) {
            return String.format("%s%x.", nameEscape, (int) ch);
        }
        return ""+ch;
    }

    private String unnameString(String name) {
        return unnameString(name, true);
    }

    private String unnameString(String name, boolean updateDigest) {
        String value = name;
        StringBuilder nsb = new StringBuilder();
        int pos = value.indexOf(nameEscape);
        while (pos >= 0) {
            nsb.append(value, 0, pos);
            value = value.substring(pos+1);
            pos = value.indexOf(".");
            if (pos <= 0) {
                throw CompilerException.invalidNameEscaping(value, name);
            }
            String hex = value.substring(0, pos);
            nsb.appendCodePoint(Integer.parseInt(hex, 16));
            value = value.substring(pos+1);
            pos = value.indexOf(nameEscape);
        }
        nsb.append(value);
        if (updateDigest) {
            updateDigest(nsb.toString());
        }
        return nsb.toString();
    }

    /**
     * Parse a compiled grammar to reconstruct a {@link Grammar} object.
     * @param compiled A file containing a grammar
     * @return The grammar stored in the compiled file
     * @throws GrammarException if there are errors in the compiled form
     * @throws IOException if the file cannot be read
     * @throws NullPointerException if the file is null
     */
    public Grammar parse(File compiled) throws IOException {
        if (compiled == null) {
            throw new NullPointerException("File must not be null");
        }
        return parse(new FileInputStream(compiled), compiled.toURI().toString());
    }

    /**
     * Parse a compiled grammar to reconstruct a {@link Grammar} object.
     * @param compiled A file containing a grammar
     * @param systemId the systemId of the grammar file
     * @return The grammar stored in the compiled file
     * @throws GrammarException if there are errors in the compiled form
     * @throws IOException if the file cannot be read
     * @throws NullPointerException if the file is null
     */
    public Grammar parse(InputStream compiled, String systemId) throws IOException {
        if (compiled == null) {
            throw new NullPointerException("File must not be null");
        }
        InputSource source = new InputSource(compiled);
        source.setSystemId(systemId);
        return parse(source);
    }

    /**
     * Parse a compiled grammar to reconstruct a {@link Grammar} object.
     * @param input A string containing a compiled grammar
     * @return The grammar stored in the compiled file
     * @throws GrammarException if there are errors in the compiled form
     * @throws NullPointerException if the input is null
     */
    public Grammar parse(String input) {
        if (input == null) {
            throw new NullPointerException("Input must not be null");
        }
        ByteArrayInputStream bais = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        return parse(new InputSource(bais));
    }

    private Grammar parse(InputSource source) {
        grammar = new Grammar(options);
        properties = new ArrayList<>();

        initializeDigest();

        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(false);
            factory.setNamespaceAware(true);
            SAXParser parser = factory.newSAXParser();
            GrammarContentHandler handler = new GrammarContentHandler(grammar);
            parser.parse(source, handler);
        } catch (IOException|SAXException|ParserConfigurationException ex) {
            throw CompilerException.errorReadingGrammar(ex.getMessage());
        }

        return grammar;
    }

    private class GrammarContentHandler extends DefaultHandler {
        private final Grammar grammar;
        private final Stack<String> elementStack;
        private final ArrayList<Symbol> symbolList;
        private final HashMap<String,Collection<ParserAttribute>> agroups;
        private NonterminalSymbol rulename = null;
        private Token token = null;
        private Collection<ParserAttribute> tspa = null;

        public GrammarContentHandler(Grammar grammar) {
            this.grammar = grammar;
            elementStack = new Stack<>();
            symbolList = new ArrayList<>();
            agroups = new HashMap<>();
        }

        @Override
        public void startElement (String uri, String localName,
                                  String qName, Attributes attributes)
                throws SAXException
        {
            if (!NS.equals(uri)) {
                if (elementStack.isEmpty()) {
                    throw CompilerException.notAGrammar(uri);
                }
                elementStack.push("-");
                return;
            }
            elementStack.push(localName);

            switch (localName) {
                case "r":
                    handle_start_r(attributes);
                    break;
                case "nt":
                    handle_nt(attributes);
                    break;
                case "t":
                    handle_start_t(attributes);
                    break;
                case "c":
                    handle_c(attributes);
                    break;
                case "s":
                    handle_s(attributes);
                    break;
                case "cs":
                    handle_cs(attributes);
                    break;
                case "re":
                    handle_re(attributes);
                    break;
                case "ag":
                    handle_ag(attributes);
                    break;
                case "meta":
                    handle_meta(attributes);
                    break;
                case "grammar":
                    if (elementStack.size() != 1) {
                        throw CompilerException.unexpectedElement(localName);
                    }
                    String version = attributes.getValue("version");
                    if (version == null) {
                        throw CompilerException.noVersionProvided();
                    } else {
                        if (!formatVersion.equals(version)) {
                            throw CompilerException.unsupportedVersion(version);
                        }
                    }
                    updateDigest(version);
                    break;
                case "check":
                    StringBuilder sb = new StringBuilder();
                    byte[] hash = xdigest.digest();
                    for (int pos = hash.length - 8; pos < hash.length; pos++) {
                        sb.append(Integer.toString((hash[pos] & 0xff) + 0x100, 16).substring(1));
                    }
                    String checksum = sb.toString();
                    String expected = attributes.getValue("Σ");
                    if (!checksum.equals(expected)) {
                        throw CompilerException.checkumFailed();
                    }
                    break;
                default:
                    throw CompilerException.unexpectedElement(localName);
            }
        }

        @Override
        public void endElement (String uri, String localName, String qName)
                throws SAXException
        {
            elementStack.pop();
            if (!NS.equals(uri)) {
                return;
            }

            switch (localName) {
                case "r":
                    handle_end_r();
                    break;
                case "t":
                    handle_end_t();
                    break;
                case "nt":
                case "c":
                case "s":
                case "re":
                case "cs":
                case "ag":
                case "grammar":
                case "check":
                case "meta":
                    break;
                default:
                    throw CompilerException.unexpectedElement(localName);
            }
        }

        private void handle_ag(Attributes attributes) {
            String id = attributes.getValue("xml:id");
            if (id == null) {
                throw CompilerException.missingXmlId("ag");
            }
            updateDigest(id);

            ArrayList<ParserAttribute> pattrs = new ArrayList<>();

            for (int pos = 0; pos < attributes.getLength(); pos++) {
                if (!"xml:id".equals(attributes.getQName(pos))) {
                    String name = unnameString(attributes.getQName(pos));
                    String value = unxmlString(attributes.getValue(pos));
                    pattrs.add(new ParserAttribute(name, value));
                }
            }

            agroups.put(id, pattrs);
        }

        private Collection<ParserAttribute> handleAttributes(String ag, String a) {
            ArrayList<ParserAttribute> rattr = null;
            if (ag != null) {
                if (!agroups.containsKey(ag)) {
                    throw CompilerException.missingAttributeGroup(ag);
                }
                rattr = new ArrayList<>(agroups.get(ag));
            }

            if (a != null) {
                if (rattr == null) {
                    rattr = new ArrayList<>();
                }
                for (int pos = 0; pos < a.length(); pos++) {
                    switch (a.charAt(pos)) {
                        case '?':
                            rattr.add(Symbol.OPTIONAL);
                            break;
                        case 'p':
                            rattr.add(ParserAttribute.PRUNING_ALLOWED);
                            break;
                        case 'f':
                            rattr.add(ParserAttribute.PRUNING_FORBIDDEN);
                            break;
                        default:
                            throw CompilerException.unexpectedFlag("" + a.charAt(pos));
                    }
                }
                updateDigest(a);
            }

            return rattr;
        }

        private void handle_start_r(Attributes attributes) {
            if (!symbolList.isEmpty()) {
                throw CompilerException.invalidGramamr("symbol list isn't empty");
            }
            String name = attributes.getValue("n");
            String ag = attributes.getValue("ag");
            String a = attributes.getValue("a");

            if (name == null) {
                throw CompilerException.invalidGramamr("r without @n");
            } else {
                name = unxmlString(name);
            }

            Collection<ParserAttribute> rattr = handleAttributes(ag, a);
            rulename = grammar.getNonterminal(name, rattr);
        }

        private void handle_end_r() {
            if (rulename == null) {
                throw CompilerException.invalidGramamr("no rule name");
            }

            grammar.addRule(rulename, symbolList);
            rulename = null;
            symbolList.clear();
        }

        private void handle_nt(Attributes attributes) {
            String name = attributes.getValue("n");
            String ag = attributes.getValue("ag");
            String a = attributes.getValue("a");

            if (name == null) {
                throw CompilerException.invalidGramamr("nt without @n");
            } else {
                name = unxmlString(name);
            }

            Collection<ParserAttribute> ntattr = handleAttributes(ag, a);
            symbolList.add(grammar.getNonterminal(name, ntattr));
        }

        private void handle_start_t(Attributes attributes) {
            if (token != null) {
                throw CompilerException.invalidGramamr("nested tokens");
            }

            String ag = attributes.getValue("ag");
            String a = attributes.getValue("a");

            tspa = handleAttributes(ag, a);
        }

        private void handle_end_t() {
            if (token == null) {
                throw CompilerException.invalidGramamr("no token in t");
            }
            symbolList.add(new TerminalSymbol(token, tspa));
            tspa = null;
            token = null;
        }

        private void handle_c(Attributes attributes) {
            String value = attributes.getValue("v");
            String ag = attributes.getValue("ag");
            String a = attributes.getValue("a");

            if (value == null) {
                throw CompilerException.invalidGramamr("c without @v");
            } else {
                value = unxmlString(value);
                if (value.length() != 1) {
                    throw CompilerException.invalidGramamr("bad value for c: " + value);
                }
            }

            Collection<ParserAttribute> cattr = handleAttributes(ag, a);
            token = TokenCharacter.get(value.charAt(0), cattr);
        }

        private void handle_s(Attributes attributes) {
            String value = attributes.getValue("v");
            String ag = attributes.getValue("ag");
            String a = attributes.getValue("a");

            if (value == null) {
                throw CompilerException.invalidGramamr("s without @v");
            } else {
                value = unxmlString(value);
            }

            Collection<ParserAttribute> sattr = handleAttributes(ag, a);
            token = TokenString.get(value, sattr);
        }

        private void handle_cs(Attributes attributes) {
            boolean inclusion;
            String value = attributes.getValue("inclusion");
            if (value == null) {
                inclusion = false;
                value = attributes.getValue("exclusion");
                if (value == null) {
                    throw CompilerException.invalidGramamr("cs without value");
                }
                updateDigest("exclusion");
            } else {
                inclusion = true;
                updateDigest("inclusion");
            }

            String ag = attributes.getValue("ag");
            String a = attributes.getValue("a");

            updateDigest(value);
            List<CharacterSet> sets = parseCharacterSets(value);

            Collection<ParserAttribute> sattr = handleAttributes(ag, a);

            if (inclusion) {
                token = TokenCharacterSet.inclusion(sets, sattr);
            } else {
                token = TokenCharacterSet.exclusion(sets, sattr);
            }
        }

        private List<CharacterSet> parseCharacterSets(String value) {
            ArrayList<CharacterSet> ranges = new ArrayList<>();
            boolean done = false;
            while (!done) {
                if (value.startsWith("\"")) {
                    value = value.substring(1);
                    int pos = value.indexOf("\"");
                    if (pos <= 0) {
                        throw new RuntimeException("Invalid compiled grammar");
                    }
                    String text = value.substring(0, pos);
                    // Undo the double-escaping without calling update digest again
                    text = text.replace("&quot;", "\"").replace("&amp;", "&");
                    value = value.substring(pos + 1);
                    CharacterSet set = CharacterSet.literal(text);
                    ranges.add(set);
                } else if (value.startsWith("'")) {
                    value = value.substring(1);
                    int pos = value.indexOf("'");
                    if (pos <= 0) {
                        throw new RuntimeException("Invalid compiled grammar");
                    }
                    String text = value.substring(0, pos);
                    value = value.substring(pos + 1);
                    int from = text.charAt(0);

                    int to = from;
                    if (value.startsWith("-'")) {
                        value = value.substring(2);
                        pos = value.indexOf("'");
                        if (pos <= 0) {
                            throw new RuntimeException("Invalid compiled grammar");
                        }
                        text = value.substring(0, pos);
                        value = value.substring(pos + 1);
                        to = text.charAt(0);
                    }

                    CharacterSet set = CharacterSet.range(from, to);
                    ranges.add(set);
                } else {
                    String uclass = value.substring(0, 1);
                    value = value.substring(1);
                    if (!"".equals(value) && !value.startsWith(";") && !value.startsWith("]")) {
                        uclass += value.substring(0, 1);
                        value = value.substring(1);
                    }
                    CharacterSet set = CharacterSet.unicodeClass(uclass);
                    ranges.add(set);
                }
                done = !value.startsWith(";");
                if (!done) {
                    value = value.substring(1);
                }
            }

            return ranges;
        }

        private void handle_re(Attributes attributes) {
            String value = attributes.getValue("v");
            String ag = attributes.getValue("ag");
            String a = attributes.getValue("a");

            if (value == null) {
                throw CompilerException.invalidGramamr("re without @v");
            } else {
                value = unxmlString(value);
            }

            Collection<ParserAttribute> reattr = handleAttributes(ag, a);
            token = TokenRegex.get(value, reattr);
        }

        private void handle_meta(Attributes attributes) {
            String name = attributes.getValue("name");
            String value = attributes.getValue("value");
            if (name == null || value == null) {
                return;
            }
            name = unnameString(name, false);
            value = unxmlString(value, false);
            setProperty(name, value);
        }

        @Override
        public void characters (char ch[], int start, int length)
                throws SAXException
        {
            if ("-".equals(elementStack.peek())) {
                return;
            }
            String text = new String(ch, start, length);
            if (!"".equals(text.trim())) {
                throw CompilerException.textNotAllowed(text);
            }
        }
    }

    private static final class Property {
        public String name;
        public String value;
        public Property(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }
}
