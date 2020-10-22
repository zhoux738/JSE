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

package info.julang.parser;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.Token;

import info.julang.langspec.ast.JulianLexer;

/**
 * The directives for the script. 
 * <p>
 * The directives may appear only at the beginning of a script file, in the form of block comment.
 * Each directive takes exactly one line, and must comply with the format: <pre><code>/* $PRAGMA$ {directive} *&sol;</code></pre>
 * Note the format is strictly enforced. The single space between <code>/*</code> and <code>$PRAGMA$</code> is required and 
 * cannot be replaced with any other blank char sequences.
 * <p>
 * The value of the directive, which is treated case-insensitively, can be arbitrary, as their meanings are subject to the 
 * interpretation of the specific tool. Julian language specification doesn't define any directive. Its parser API only provides 
 * means of extracting these directives. The only recommendation to be given is to use reversed domain host name to distinguish 
 * the directive value. Example:<pre><code> /* $PRAGMA$ info.julang.directive1 *&sol;
 * /* $PRAGMA$ org.example.my_directives.rule1 *&sol;
 * 
 * import System.Collection;
 * ... ... (the rest of the script)</code></pre>
 * 
 * 
 * @author Ming Zhou
 */
public class Directives {

	private static final String s_prefix = "/* $PRAGMA$";
	
	private String[] directives;
	private Token finalTok;
	
	static final Directives EMPTY = new Directives(new String[0], null);
	
	private Directives(String[] directives, Token finalTok){
		this.directives = directives;
		this.finalTok = finalTok;
	}
	
	public static Directives create(FilterableTokenStream cts) {
		List<Token> list = cts.getTokens();
		Token finalTok = null;
		if (list != null) {
			List<String> strs = null;
			for (Token tok : list) {
				if (tok.getChannel() == JulianLexer.JULDOC) {
					String str = asDirective(tok);
					if(str != null) {
						if (strs == null) {
							strs = new ArrayList<>();
						}
						strs.add(str);
						finalTok = tok;
						continue;
					}
				} else if (tok.getChannel() == JulianLexer.SKIPPED) {
					continue;
				}

				// As soon as we hit the first non-directive token, abort. 
				// No tokens after this point will be treated as directives.
				break;
			}
			
			if (strs != null) {
				return new Directives(strs.toArray(new String[0]), finalTok);
			}
		}
		
		return EMPTY;
	}
	
	public boolean contains(String value) {
		for (String str : directives) {
			if (str.equalsIgnoreCase(value)) {
				return true;
			}
		}
		
		return false;
	}
	
	public String[] getAll() {
		return directives;
	}
	
	public Token getLastToken() {
		return finalTok;
	}
	
	/**
	 * Returns null if this is not a directive.
	 * 
	 * @param tok
	 * @return The text of the entire block comment, if the comment represents a directive.
	 */
	static String asDirective(Token tok) {
		if (tok.getType() == JulianLexer.BLOCK_COMMENT) { // Must be a block comment
			String str = tok.getText();
			if (str.startsWith(s_prefix) // Must start exactly with the required prefix
				&& !str.contains("\n")) { // Must not span across multiple lines 
				String value = str.substring(s_prefix.length(), str.length() - 2).trim();
				if (!value.isEmpty()) { // Must contain some string
					return value;
				} 
			}
		}
		
		return null;
	}
}
