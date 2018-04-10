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

package info.julang.eng.mvnplugin.mdgen;

import info.julang.langspec.ast.JulianLexer;
import info.julang.parser.ANTLRParser;
import info.julang.parser.FilterableTokenStream;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import org.antlr.v4.runtime.Token;

/**
 * Render a code snippet enclosed by [code] and [code: end]. Supports pre-formatted output (honoring 
 * the space and other control characters) as well as minimal coloring scheme.
 * 
 * @author Ming Zhou
 */
public class CodeSnippet implements IParsedDocSection {

	static final Color KeywordColor = new Color(153, 51, 102, 255);
	static final Color CommentColor = new Color(102, 153, 102, 255);
	static final Color LiteralColor = new Color(51, 0, 255, 255);
	
	private String str;
	
	public CodeSnippet(String str) {
		this.str = str;
	}
	
	private String getFormatted() {
		int start = str.indexOf(']');
		int end = str.lastIndexOf('[');
		String inner = str.substring(start + 1, end);
		byte[] bs = inner.getBytes();
		ByteArrayInputStream bais = new ByteArrayInputStream(bs);
		ANTLRParser parser = new ANTLRParser("<unknown>", bais, false);
		
		// Use Eclipse's classic color scheme
		FilterableTokenStream fts = parser.getTokenStream();
		fts.fill();
		List<Token> tokens = fts.getTokens();
		int rightBeforeEof = tokens.size() - 2;
		int index = -1;
		StringBuilder sb = new StringBuilder("<code>");
		String text = null;
		for(Token t : tokens) {
			index++;
			switch(t.getType()) {
			case JulianLexer.NEW_LINE:
				// Replace new line with <br>, unless it's the very first token in the input
				if (!(t.getLine() == 1 && t.getCharPositionInLine() == 0 || index == rightBeforeEof)) {
					sb.append("<br>");
				}
				break;
			case JulianLexer.WHITESPACE:
				// If this a line-starting whitespace, escape all the whitespace characters using &nbsp; sequence
				text = t.getText();
				if (t.getCharPositionInLine() == 0) {
					for (int i = 0; i<text.length(); i++) {
						sb.append(i == '\t' ? "&nbsp;&nbsp;" : "&nbsp;");
					}
				} else {
					sb.append(text);
				}
				break;
			case JulianLexer.LINE_COMMENT:
			case JulianLexer.BLOCK_COMMENT:
				text = getStylizedString(t, CommentColor, false, false);
				sb.append(text);
				break;
			case JulianLexer.STRING_LITERAL:
			case JulianLexer.CHAR_LITERAL:
				text = getStylizedString(t, LiteralColor, false, false);
				sb.append(text);
				break;
			case JulianLexer.EOF:
				break;
			case JulianLexer.LEFT_BRACKET:
				sb.append("&lbrack;");
				break;
			case JulianLexer.RIGHT_BRACKET:
				sb.append("&rbrack;");
				break;	
			default:
				text = null;
				if (t.getType() != JulianLexer.IDENTIFIER) {
					char c = t.getText().charAt(0);
					if ('a' <= c && c <= 'z') {
						text = getStylizedString(t, KeywordColor, true, false);
					}
				}
				if (text == null) {
					text = t.getText();
				}
				sb.append(text);
				break;
			}
		}

		sb.append("</code>");
		
		return sb.toString();
	}
	
	private String getStylizedString(Token tok, Color color, boolean bold, boolean italic){
		String txt = tok.getText();
		txt = StylizedString.create(txt).addColor(color).toBold(bold).toItalic(italic).toString();
		return txt;
	}
	
	@Override
	public void appendTo(MarkdownWriter markdownWriter) throws IOException{
		markdownWriter.write(getFormatted());
	}
}
