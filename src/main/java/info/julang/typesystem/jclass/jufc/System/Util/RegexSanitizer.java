/*
MIT License

Copyright (c) 2017 Ming Zhou

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

package info.julang.typesystem.jclass.jufc.System.Util;

import info.julang.langspec.regex.ast.RegexBaseVisitor;
import info.julang.langspec.regex.ast.RegexParser.RegexContext;
import info.julang.parser.ANTLRRegexParser;

import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * A utility class used to sanitize the regex input.
 * <p>
 * The regex supported by Julian is a subset of Java Pattern or C# Regex. To utilize 
 * Pattern class as the backend, we must make sure the input doesn't accidentally 
 * trigger semantics that is not supported by Julian, but recognized by Pattern class.
 * 
 * @author Ming Zhou
 */
public class RegexSanitizer {

	public static String sanitize(String input){
		ANTLRRegexParser parser = new ANTLRRegexParser();
		RegexContext rc = parser.parse(input);
		
		if (rc != null) {
			return sanitize(rc);
		}
		
		throw new UnrecognizedRegexException("The input cannot be parsed: " + input);
	}

	private static String sanitize(RegexContext rc) {
		// Iterate over the entire tree, and escape any meta-chars which are 
		// recognized by Java Pattern but not by Julian
		RegexSanitizationVisitor visitor = new RegexSanitizationVisitor();
		rc.accept(visitor);
		String res = visitor.getResult();
		return res;
	}
	
}

// This class must filter out all the cases only supported by Java Pattern.
// See: https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html
// The RegexParser is able to filter out the cases where the '\' precedes a 
// non-metachar, such as 'p', so an input with '\p' is already filtered out
// by the parser.
class RegexSanitizationVisitor extends RegexBaseVisitor<Void> {
	
	private StringBuilder sb;
	
	RegexSanitizationVisitor(){
		sb = new StringBuilder();
	}
	
	String getResult(){
		return sb.toString();
	}
	
	@Override
	public Void visitTerminal(TerminalNode node) {
		char[] cs = node.getText().toCharArray();
		for (char c : cs) {
			switch(c){
			case '{': // meta-char in Java, not Julian
			case '}': // (same)
				sb.append("\\");
				// fall thru
			default:
				sb.append(c);
			}
		}
		
		return null;
	}
}
