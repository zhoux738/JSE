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

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;

import info.julang.ide.themes.IColorManager;

/**
 * Treat the entire region as a single token.
 * 
 * @author Ming Zhou
 */
public class SingleTokenScanner extends JulianScannerBase {
	
	private FontStyle[] styles;
	
	public SingleTokenScanner(IColorManager manager, ISourceViewer srcViewer, FontStyle style) {
		super(manager, srcViewer);
		
		styles = new FontStyle[] { style };
	}
	
	@Override
	protected FontStyle[] getFontStyles() {
		return styles;
	}

	@Override
	public int setMaxIndex(IDocument document, int offset, int length) {
		return 0;
	}

	@Override
	public IToken nextToken() {
		index++;
		if (index > 0) {
			return Token.EOF;
		} else {
			return etoken_instances[0];
		}
	}

	@Override
	public int getTokenOffset() {
		return startPos;
	}

	@Override
	public int getTokenLength() {
		return endPos - startPos;
	}
}
