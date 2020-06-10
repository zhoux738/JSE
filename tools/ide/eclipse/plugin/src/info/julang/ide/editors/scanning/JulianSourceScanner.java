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

package info.julang.ide.editors.scanning;

import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;

import info.julang.ide.themes.IColorManager;
import info.julang.langspec.ast.JulianLexer;
import info.julang.parser.ANTLRParser;

/**
 * Backed by JSE's ANTLRParser.
 * 
 * @author Ming Zhou
 */
public class JulianSourceScanner extends JulianScannerBase {
	
	private static final int KEYWORD = 0;
	private static final int COMMENT = 1;
	private static final int LITERAL = 2;
	private static final int REGEX = 3;

	private List<org.antlr.v4.runtime.Token> tokens;
	private org.antlr.v4.runtime.Token currentToken;
	
	private FontStyle[] styles;
	
	public JulianSourceScanner(IColorManager manager, ISourceViewer srcViewer) {
		super(manager, srcViewer);
		
		styles = new FontStyle[] {
			FontStyle.KEYWORD,
			FontStyle.COMMENT,
			FontStyle.LITERAL,
			FontStyle.REGEX
		};
	}
	
	@Override
	protected void clear() {
		super.clear();
		tokens = null;
		currentToken = null;
	}
	
	@Override
	protected FontStyle[] getFontStyles() {
		return styles;
	}

	@Override
	public int setMaxIndex(IDocument document, int offset, int length) {
		ANTLRParser parser = null;
		try {
			String text = document.get(
				offset >= 0 ? offset : 0, 
				length < document.getLength() ? length : document.getLength());
			
			parser = ANTLRParser.createMemoryParser(text);
		} catch (BadLocationException e) {
			// This shouldn't happen. TODO - Log it.
		}
		
		if (parser != null) {
			tokens = parser.getAllTokens();
			return tokens.size() - 1;
		}
		
		return -1;
	}

	@Override
	public IToken nextToken() {
		index++;
		if (index > maxIndex) {
			currentToken = null;
			return Token.EOF;
		} else {
			return makeToken(currentToken = tokens.get(index));
		}
	}

	@Override
	public int getTokenOffset() {
		if (currentToken != null) {
			return startPos + currentToken.getStartIndex();
		} else {
			return endPos + 1;
		}
	}

	@Override
	public int getTokenLength() {
		if (currentToken != null) {
			return currentToken.getStopIndex() - currentToken.getStartIndex() + 1;
		} else {
			return 0;
		}
	}

	private IToken makeToken(org.antlr.v4.runtime.Token t) {
		switch(t.getType()) {
		case JulianLexer.NEW_LINE:
		case JulianLexer.WHITESPACE:
			return Token.WHITESPACE;
			
		case JulianLexer.EOF:
			return Token.EOF;
			
		case JulianLexer.LINE_COMMENT:
		case JulianLexer.BLOCK_COMMENT:
			return etoken_instances[COMMENT];
			
		case JulianLexer.STRING_LITERAL:
		case JulianLexer.CHAR_LITERAL:
			return etoken_instances[LITERAL];
			
		case JulianLexer.REGEX_LITERAL:
			return etoken_instances[REGEX];

		default:
			if (t.getType() != JulianLexer.IDENTIFIER) {
				char c = t.getText().charAt(0);
				if ('a' <= c && c <= 'z') {
					return etoken_instances[KEYWORD];
				}
			}
		}
		
		return new Token(null); // No interesting data to be attached.
	}
}
