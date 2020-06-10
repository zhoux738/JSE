/*
MIT License

Copyright (c) 2020 Ming Zhou

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package info.julang.ide.editors;

import org.eclipse.jface.text.rules.*;

/**
 * A scanner that is only used to partition the source text. It can identify comments and string literals, 
 * and treat everything else as default.
 * 
 * The purpose of this class is to allow confined repairing of damaged (updated) region in the source text. 
 * Whenever a change occurs, a repartitioning is triggered and for each region a corresponding scanner (see
 * JulianConfiguration) may be invoked to provide mode detailed highlighting.
 * 
 * @author Ming Zhou
 */
public class JulianPartitionScanner extends RuleBasedPartitionScanner {
	
	// IMPLEMENTATION NOTES:
	// This partitioning approach mirrors that used by JDT. See
	// https://github.com/eclipse/eclipse.jdt.ui/blob/master/org.eclipse.jdt.ui/ui/org/eclipse/jdt/internal/ui/text/JavaPartitionScanner.java

	public final static String JULIAN_BLOCK_COMMENT = "__julian_block_comment";
	public final static String JULIAN_COMMENT = "__julian_comment";
	public final static String JULIAN_STRING_LITERAL = "__julian_string_literal";
	
	// The following regions are usually small, so there is no need to partition them out.
	//public final static String JULIAN_CHAR_LITERAL = "__julian_char_literal";
	//public final static String JULIAN_REGEX_LITERAL = "__julian_regex_literal";

	public JulianPartitionScanner() {
		IPredicateRule[] rules = new IPredicateRule[] {
			new MultiLineRule("/*", "*/", new Token(JULIAN_BLOCK_COMMENT)),
			new EndOfLineRule("//", new Token(JULIAN_COMMENT)),
			new MultiLineRule("\"", "\"", new Token(JULIAN_STRING_LITERAL), '\\'), // Julian's string literal is multi-line
			//new SingleLineRule("'", "'", new Token(JULIAN_STRING_LITERAL), '\\'),
			//new MultiLineRule("/", "/", new Token(JULIAN_REGEX_LITERAL), '\\')
		};

		setPredicateRules(rules);
	}
}
