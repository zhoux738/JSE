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
import info.julang.ide.themes.PluginColor;

/**
 * A Text widget that can enable and disable with color enhancement.
 * 
 * @author Ming Zhou
 */
abstract class TextBase {

	protected Text textWidget;
	protected ColorManager clrMgr;
	
	protected TextBase(Text text, ColorManager clrMgr, IChangeListener listener) {
		this.textWidget = text;
		this.clrMgr = clrMgr;

		if (listener != null) {
			this.textWidget.addModifyListener(e -> listener.onChange(this.textWidget));
		}
	}
	
	public abstract void setText(String text);
	
	public abstract String getText();
	
	public void enable() {
		this.textWidget.setBackground(null);
		this.textWidget.setEnabled(true);
	}
	
	public void disable() {
		this.textWidget.setEnabled(false);
		this.textWidget.setBackground(this.clrMgr.getColor(PluginColor.LIGHT_GRAY));
	}
}
