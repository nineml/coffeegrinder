/**
 * The parser classes.
 *
 * <p>In outline:</p>
 * <ul>
 *     <li>Create a {@link org.nineml.coffeegrinder.parser.Grammar Grammar}.</li>
 *     <li>Use the grammar to create {@link org.nineml.coffeegrinder.parser.NonterminalSymbol NonterminalSymbols}.</li>
 *     <li>Use the grammar (or {@link org.nineml.coffeegrinder.parser.Rule Rule} directly) to create rules and add them to the grammar.</li>
 *     <li>Create an {@link org.nineml.coffeegrinder.parser.EarleyParser EarleyParser} from the grammar.</li>
 *     <li>Parse a sequence of input tokens to obtain an {@link org.nineml.coffeegrinder.parser.EarleyResult}.</li>
 *     <li>From the result, you can get information about the success or failure of the parse, and possibly continue parsing.</li>
 *     <li>If the parse was successful, get the {@link org.nineml.coffeegrinder.parser.ParseForest SPPF} (shared packed parse forest) from the parse.</li>
 *     <li>Use the SPPF to get {@link org.nineml.coffeegrinder.parser.ParseTree SPPFTree} parse tree(s).</li>
 *     <li>The SPPF will also tell you about the ambiguity of the result, the number of parse trees available, etc.</li>
 * </ul>
 */
package org.nineml.coffeegrinder.parser;

