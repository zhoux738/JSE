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
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.SWT;

import info.julang.ide.themes.IColorManager;
import info.julang.ide.themes.PluginColor;
import info.julang.ide.themes.ThemeChangeListener;

/**
 * A base class for all the scanners in this plugin. It provides token reuse and reactivity to theme change.
 * 
 * @author Ming Zhou
 */
public abstract class JulianScannerBase implements ITokenScanner, ThemeChangeListener {

	private IColorManager manager;
	private ISourceViewer srcViewer;
	
	protected int index;
	protected int maxIndex;
	protected int startPos;
	protected int endPos;
	
	protected JulianScannerBase(IColorManager manager, ISourceViewer srcViewer) {
		this.manager = manager;
		this.srcViewer = srcViewer;
		manager.addOnThemeChangeListener(this);
		clear();
	}
	
	public void initialze() {
		FontStyle[] styles = getFontStyles();
		
		etoken_total = styles.length;
		etoken_instances = new Token[etoken_total];

		for (int i = 0; i < etoken_total; i++) {
			etoken_instances[i] = new Token(makeTextAttribute(styles[i].getColor(), styles[i].isBold()));
		}
	}
	
	// To be inherited.
	protected void clear() {
		index = -1;
		maxIndex = -1;
		startPos = 0;
		endPos = 0;
	}

	@Override
	public void setRange(IDocument document, int offset, int length) {
		index = -1;
		startPos = offset;
		endPos = offset + length;
		
		maxIndex = setMaxIndex(document, offset, length);
	}
	
	protected abstract int setMaxIndex(IDocument document, int offset, int length);

	// React to ThemeChangeListener
	@Override
	public void onThemeChange() {
		resetUITokens();
		srcViewer.invalidateTextPresentation();
	}
	
	private TextAttribute makeTextAttribute(PluginColor color, boolean bold) {
		return new TextAttribute(
			manager.getColor(color),
			null,
			bold ? SWT.BOLD : 0);
	}
	
	// All of the Eclipse tokens, which are shared by ANTLR tokens to represent a particular style. So there are only a few of them.
	protected Token[] etoken_instances;
	private int etoken_total;
	
	protected abstract FontStyle[] getFontStyles();
	
	// Recreate all the styles
	private void resetUITokens() {
		FontStyle[] styles = getFontStyles();
		for (int i = 0; i < etoken_total; i++) {
			etoken_instances[i].setData(makeTextAttribute(styles[i].getColor(), styles[i].isBold()));
		}
	}
	
	public void dispose() {
		manager.removeOnThemeChangeListener(this);
	}
}
