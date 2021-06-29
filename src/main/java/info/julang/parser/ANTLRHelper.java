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

import info.julang.external.exceptions.JSEError;
import info.julang.interpretation.BadSyntaxException;
import info.julang.interpretation.IllegalLiteralException;
import info.julang.langspec.ast.JulianLexer;
import info.julang.langspec.ast.JulianParser;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

public class ANTLRHelper {

	public static String getRuleName(ParserRuleContext prt){
		return JulianParser.ruleNames[prt.getRuleIndex()];
	}
	
	public static String getNodeName(TerminalNode tnode){
		return JulianLexer.VOCABULARY.getDisplayName(tnode.getSymbol().getType());
	}
	
	public static String getTokenTypeName(Token tok){
		return JulianLexer.VOCABULARY.getDisplayName(tok.getType());
	}
	
	public static JSEError getUnrecognizedError(ParserRuleContext prt){
		return new JSEError("Illegal rule in the current context: " + getRuleName(prt) + ".");
	}
	
	public static JSEError getUnrecognizedTerminalError(TerminalNode tnode){
		return new JSEError("Illegal node in the current context: " + getNodeName(tnode) + ".");
	}
	
	/**
	 * Convert regex literal into a properly escape string to feed to Regex constructor.
	 * The enclosing '/'s will be removed; certain escaping sequences, such as '\/', will be replaced by the intended character.
	 * 
	 * @param input
	 * @return the string with escape sequences properly substituted.
	 */
	public static String convertRegexLiteral(String input){
		// We will scan the chars between the leading and trailing char, which would be '/'.
		int len = input.length() - 1;
		
		// Allocate a string builder with sufficient capacity
		StringBuilder sb = new StringBuilder(len);
		
		for(int i = 1; i < len; i++){
			char c1 = input.charAt(i);
			if (c1 != '\\'){
				sb.append(c1);
				continue;
			}
			
			// Escape mode
			try {
				// This check is necessary, since the chance is that we are hitting a '\' at the end, which
				// is followed be the enclosing quote symbol. We must not take into account this symbol.
				if (i+1 >= len){
					throw new IndexOutOfBoundsException();
				}
				char c2 = input.charAt(i+1); //IndexOutOfBoundsException 
				boolean found = true;
				
				// Except for '\/', preserve the original sequence.
				// Make sure this synchronizes with "fragment FRG_ESC_" lexer rules in Regex.g4
				switch (c2){
				case '/':
					sb.append('/'); 
					break;
				case '[': 
				case ']': 
				case '(': 
				case ')': 
				case '*': 
				case '+': 
				case '?': 
				case '-': 
				case '|': 
				case '^': 
				case '$': 
				case '.': 
				case '\\': 
				case 'n': 
				case 't': 
				case 'r': 
				case 'f': 
				case 'v': 
				case 'b':
					sb.append(c1); 
					sb.append(c2); 
					break;
				default:
					found = false;
				}
				
				if (found){
					i++;
					continue;
				}	
			} catch (IndexOutOfBoundsException ie){
				throw new BadSyntaxException("Unexpected termination of regex literal value.");
			} catch (NumberFormatException fe) {
				throw new BadSyntaxException("Cannot convert an escape sequences in regex literal.");
			}
			
			throw new BadSyntaxException("Regex literal value contains undefined escape sequences.");
		}
		
		return sb.toString();
	}
	
	/**
	 * Convert escape sequences to the target char in a string. For example, if the input is ['\', 'n', 'a', '\', 't'],
	 * the result would be ['\n', 'a', '\t']. Escape sequences include control chars, ASCII code and Unicode. 
	 * 
	 * @param input
	 * @return the string with escape sequences properly substituted.
	 */
	public static String reEscapeAsString(String input, boolean removeQuotes){
		// We will scan the chars between the leading and trailing char, which would be " or '.
		int len = input.length() - 1;
		
		// Allocate a string builder with sufficient capacity
		StringBuilder sb = new StringBuilder(len);
		if(!removeQuotes){
			sb.append(input.charAt(0));
		}
		
		for(int i = 1; i < len; i++){
			char c = input.charAt(i);
			if (c != '\\'){
				sb.append(c);
				continue;
			}

			// Escape mode
			try {
				// This check is necessary, since the chance is that we are hitting a '\' at the end, which
				// is followed be the enclosing quote symbol. We must not take into account this symbol.
				if (i+1 >= len){
					throw new IndexOutOfBoundsException();
				}
				char c2 = input.charAt(i+1); //IndexOutOfBoundsException 
				boolean found = true;
				
				// Case 1: "\x"
				switch (c2){
				case '\\': sb.append('\\'); break;
				case '\'': sb.append('\''); break;
				case 'n': sb.append('\n'); break;
				case 'f': sb.append('\f'); break;
				case 't': sb.append('\t'); break;
				case 'b': sb.append('\b'); break;
				case 'r': sb.append('\r'); break;
				case '"': sb.append('"'); break;
				default: found = false;
				}
				
				if (found){
					i++;
					continue;
				}
				
				// Case 2: "\nnn" (octet: 000 - 177)
				if ('0' <= c2 && c2 <= '1'){
					if (i+2 == len){
						// Special, we accept '\0'
						if (c2 == '0') {
							sb.append('\0');
							i++;
							continue;
						} else {
							throw new BadSyntaxException("Literal value contains undefined escape sequences.");
						}
					}
					
					char c3 = input.charAt(i+2); //IndexOutOfBoundsException 
					if (c3 < '0' || '7' < c3){
						// Special, we accept '\0'
						if (c2 == '0') {
							sb.append('\0');
							i++;
							continue;
						} else {
							throw new BadSyntaxException("Literal value contains undefined escape sequences.");
						}
					}
					
					char c4 = input.charAt(i+3); //IndexOutOfBoundsException 
					String s = new String(new char[]{c2, c3, c4});
					int sv = Integer.parseInt(s, 8); //NumberFormatException
					if (0 <= sv && sv <= 127){
						sb.append((char)sv);
						i += 3;
						continue;
					} else {
						throw new BadSyntaxException("Literal value contains undefined escape sequences.");
					}
				}
				
				// Case 3: "\u4e00" (CJK for "one")
				if (c2 == 'u'){
					char c3 = input.charAt(i+2); //IndexOutOfBoundsException 
					char c4 = input.charAt(i+3); //IndexOutOfBoundsException 
					char c5 = input.charAt(i+4); //IndexOutOfBoundsException 
					char c6 = input.charAt(i+5); //IndexOutOfBoundsException 
					String s = new String(new char[]{c3, c4, c5, c6});
					int sv = Integer.parseInt(s, 16); //NumberFormatException
					sb.append((char)sv);
					i += 5;
					continue;
				}				
			} catch (IndexOutOfBoundsException ie){
				throw new BadSyntaxException("Unexpected termination of literal value.");
			} catch (NumberFormatException fe) {
				throw new BadSyntaxException("Cannot convert an escape sequences.");
			}
			
			throw new BadSyntaxException("Literal value contains undefined escape sequences.");
		}

		if(!removeQuotes){
			sb.append(input.charAt(len));
		}
		
		return sb.toString();
	}
	
	public static char reEscapeAsChar(String input, boolean removeQuotes){
		String svalue = reEscapeAsString(input, removeQuotes);
		char[] chars = svalue.toCharArray();		
		return chars[0];
	}
	
	/**
	 * Parse a int literal to an int.
	 * 
	 * @param text [-][0x|0b][0..9]+
	 * @return
	 */
	public static int parseIntLiteral(String text){
		String val = text;
		int start = text.charAt(0) == '-' ? 1 : 0;
		int offset = 2 + start;
		int base = 10;
		if (text.length() > offset){
			String head = text.substring(start, offset);
			if ("0x".equals(head)){
				base = 16;
			} else if ("0b".equals(head)){
				base = 2;
			}
		}
		
		if (base != 10){
			val = text.substring(offset, text.length());
			if (start == 1) {
				val = '-' + val;
			}
		}
		
		try {
			return Integer.parseInt(val, base);
		} catch (NumberFormatException nfe){
			throw new IllegalLiteralException(
				"Cannot parse " + val + "(base " + base + ") to integer.");
		}
	}
	
	public static float parseFloatLiteral(String text){
		if (text.endsWith("f")){
			text = text.substring(0, text.length() - 1);
		}
		
		try {
			return Float.parseFloat(text);
		} catch (NumberFormatException nfe){
			throw new IllegalLiteralException(
				"Cannot parse " + text + " to float.");
		}
	}
	
	/**
	 * Synthesize a degenerate tree. A degenerate tree is a singly-linked tree that starts with a node of root type, 
	 * with all the inner nodes having exactly one child, and finally reaching to the leaf node. So calling this 
	 * with (leaf, R.class, T1.class, T2.class) will get a tree that has this structure:<pre><code>
	 *  r
	 *  |
	 * \|/
	 *  t1
	 *  |
	 * \|/
	 *  t2
	 *  |
	 * \|/
	 * leaf</code></pre>
	 * @param leaf the real, non-synthesized node that would be sitting at the bottom of returned tree
	 * @param rootType the type of root node, which is also the node to be returned
	 * @param innerNodeTypes the type of each inner node, in the order they would inherit from each other
	 * @return A instance of root type.
	 */
	@SafeVarargs
	public static <R extends ParserRuleContext> R synthesizeDegenerateAST(
		ParserRuleContext leaf, Class<R> rootType, Class<? extends ParserRuleContext>... innerNodeTypes){
		org.antlr.v4.runtime.Token start = leaf.start;
		org.antlr.v4.runtime.Token stop = leaf.stop;
		
		// Create the root node, which doesn't have a parent
		R r = createNode(rootType, null, start, stop);
		
		// Each node along the chain serves as parent for the next one
		ParserRuleContext parent = r;
		for (Class<? extends ParserRuleContext> t : innerNodeTypes) {
			parent = createNode(t, parent, start, stop);
		}
		
		parent.addChild(leaf);
		
		return r;
	}
	
	private static <T extends ParserRuleContext> T createNode(
		Class<T> nodeType, 
		ParserRuleContext parent, 
		org.antlr.v4.runtime.Token start,
		org.antlr.v4.runtime.Token stop) {
		try {
			T t = null;
			if (nodeType.getSuperclass() == JulianParser.ExpressionContext.class) {
				Constructor<T> ctor = nodeType.getDeclaredConstructor(JulianParser.ExpressionContext.class);
				t = ctor.newInstance(new JulianParser.ExpressionContext());
			} else {
				Constructor<T> ctor = nodeType.getDeclaredConstructor(ParserRuleContext.class, int.class);
				t = ctor.newInstance(parent, 0);
			}
			
			t.start = start;
			t.stop = stop;
			if (parent != null){
				parent.addChild(t);
			}
			
			return t;
		} catch (IllegalArgumentException  | InstantiationException | NoSuchMethodException | 
				 InvocationTargetException | IllegalAccessException | SecurityException e){
			throw new JSEError("Cannot synthesize a syntax tree: " + e.getMessage());
		}
	}
	
}
