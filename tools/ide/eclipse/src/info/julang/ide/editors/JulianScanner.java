package info.julang.ide.editors;

import org.eclipse.jface.text.rules.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;

import info.julang.langspec.ast.JulianLexer;
import info.julang.parser.ANTLRParser;
import info.julang.parser.FilterableTokenStream;

import java.io.ByteArrayInputStream;
import java.util.List;

import org.eclipse.jface.text.*;

/**
 * Backed by JSE's ANTLRParser.
 * 
 * @author Ming Zhou
 */
public class JulianScanner implements ITokenScanner {

	private ColorManager manager;
	private List<org.antlr.v4.runtime.Token> tokens;
	private org.antlr.v4.runtime.Token currentToken;
	private int index;
	private int maxIndex;
	private int startPos;
	private int endPos;
	
	public JulianScanner(ColorManager manager) {
		this.manager = manager;
		clear();
	}
	
	private void clear() {
		tokens = null;
		index = -1;
		maxIndex = -1;
		currentToken = null;
		startPos = 0;
		endPos = 0;
	}

	@Override
	public void setRange(IDocument document, int offset, int length) {
		clear();
		ANTLRParser parser = null;
		try {
			String text = document.get(
				offset >= 0 ? offset : 0, 
				length < document.getLength() ? length : document.getLength());

//			byte[] bs = text.getBytes();
//  		bais = new ByteArrayInputStream(bs);
			
			parser = ANTLRParser.createMemoryParser(text);
		} catch (BadLocationException e) {
			// This shouldn't happen. TODO - Log it.
		}
		
		if (parser != null) {
			//ANTLRParser parser = new ANTLRParser("<unknown>", bais, false);
			FilterableTokenStream fts = parser.getTokenStream();
			fts.fill();
			tokens = fts.getTokens();
			index = -1;
			maxIndex = tokens.size() - 1;
			currentToken = null;
			startPos = offset;
			endPos = offset + length;
		}
	}

	@Override
	public IToken nextToken() {
		index++;
		if (index > maxIndex) {
			currentToken = null;
			return null;
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
			return makeColoredToken(JulianSourceColors.CommentColor, false);
			
		case JulianLexer.STRING_LITERAL:
		case JulianLexer.CHAR_LITERAL:
			return makeColoredToken(JulianSourceColors.LiteralColor, false);
			
		case JulianLexer.REGEX_LITERAL:
			return makeColoredToken(JulianSourceColors.RegexLiteralColor, false);

		default:
			if (t.getType() != JulianLexer.IDENTIFIER) {
				char c = t.getText().charAt(0);
				if ('a' <= c && c <= 'z') {
					return makeColoredToken(JulianSourceColors.KeywordColor, true);
				}
			}
		}
		
		return new Token(null); // No interesting data to be attached.
	}
	
	private IToken makeColoredToken(RGB color, boolean bold) {
		return new Token(new TextAttribute(manager.getColor(color), null, bold ? SWT.BOLD : 0));
	}
}
