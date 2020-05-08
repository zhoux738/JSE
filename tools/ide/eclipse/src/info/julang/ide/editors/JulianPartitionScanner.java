package info.julang.ide.editors;

import org.eclipse.jface.text.rules.*;

/**
 * A scanner that is only used to partition the source text. 
 * Can identify comments, string and char literals, and treat everything else as default.
 * 
 * @author Ming Zhou
 */
public class JulianPartitionScanner extends RuleBasedPartitionScanner {
	
	// IMPLEMENTATION NOTES:
	// This partitioning approach mirrors that used by JDT. See
	// https://github.com/eclipse/eclipse.jdt.ui/blob/master/org.eclipse.jdt.ui/ui/org/eclipse/jdt/internal/ui/text/JavaPartitionScanner.java
	
	public final static String JULIAN_COMMENT = "__julian_comment";
	public final static String JULIAN_STRING_LITERAL = "__julian_string_literal";
	public final static String JULIAN_CHAR_LITERAL = "__julian_char_literal";
	public final static String JULIAN_REGEX_LITERAL = "__julian_regex_literal";

	public JulianPartitionScanner() {
		IToken comment = new Token(JULIAN_COMMENT);
		IToken stringLiteral = new Token(JULIAN_STRING_LITERAL);
		IToken charLiteral = new Token(JULIAN_STRING_LITERAL);
		IToken regexLiteral = new Token(JULIAN_REGEX_LITERAL);

		IPredicateRule[] rules = new IPredicateRule[5];

		rules[0] = new MultiLineRule("/*", "*/", comment);
		rules[1] = new EndOfLineRule("//", comment);
		rules[2] = new MultiLineRule("\"", "\"", stringLiteral, '\\'); // Julian's string literal is multi-line
		rules[3] = new SingleLineRule("'", "'", charLiteral, '\\'); 
		rules[4] = new MultiLineRule("/", "/", regexLiteral, '\\');

		setPredicateRules(rules);
	}
}
