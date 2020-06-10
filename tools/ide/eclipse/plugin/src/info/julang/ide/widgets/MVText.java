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

package info.julang.ide.widgets;

import org.eclipse.swt.widgets.Text;

import info.julang.ide.themes.ColorManager;

/**
 * An enhanced Text widget that provides a lightweight model-view binding.
 * 
 * The most important feature of this class is to allow the displayed text derived from what is set. 
 * While a user only sees the derived text, the caller can still obtain the backing data.
 * 
 * This widget can only be used in a way the text is always set programmatically. Therefore it disables user input.
 * 
 * @author Ming Zhou
 */
public class MVText extends TextBase {
	
	@FunctionalInterface
	public static interface ILabelProvider {
		
		/**
		 * Get the string to actually display before the user.
		 */
		String show(String text);
	}
	
	private ILabelProvider provider;
	private String orgText;
	
	/**
	 * Create a new MVText using a standard SWT text widget.
	 * 
	 * @param text The SWT Text.
	 * @param clrMgr A color manager to provide colors. Like its parent, this class doesn't handle resource management.
	 * @param provider Provide text view. If null, the text will set shown exactly as what is set.
	 * @param listener Triggered by new text is set, regardless of the actual change of the displayed text. Can be null.
	 */
	public MVText(Text text, ColorManager clrMgr, ILabelProvider provider, IChangeListener listener) {
		super(text, clrMgr, listener);
		
		this.provider = provider;
		this.textWidget.setEditable(false);
	}
	
	@Override
	public void setText(String text) {
		this.orgText = text;
		this.textWidget.setText(provider != null ? provider.show(text) : text);
	}

	@Override
	public String getText() {
		return orgText;
	}
}
