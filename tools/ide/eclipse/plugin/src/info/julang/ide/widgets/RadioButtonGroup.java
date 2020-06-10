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

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import info.julang.ide.util.GridStyle;
import info.julang.ide.util.SWTUtil;

/**
 * Binds a group of radio buttons with an array of enum values.
 * 
 * Each button has its text set to the enum's <code>toString()</code> value (<b>NOT</b> <code>name()</code>). 
 * The <i>select</i> and <i>unselect</i> operation are based on enum value.
 * 
 * @param <T> An enum type.
 * 
 * @author Ming Zhou
 */
public class RadioButtonGroup<T extends Enum<?>> {

	public enum Style {
		ONE_PER_LINE,
		ALL_IN_ONE_LINE
	}
	
	private Button[] buttons;
	private Object[] values;
	
	/**
	 * Create a new RadioButtonGroup with given enum values. No duplication check is performed, 
	 * so it's possible, although undesired, to pass along more one with the same value.
	 * 
	 * @param container The container control in which this group resides.
	 * @param hspan The horizontal span in the grid layout of the container this group should take.
	 * @param listener Invoked when one of the buttons is clicked.
	 * @param vals An array of enum values, of type T.
	 */
	@SafeVarargs
	public RadioButtonGroup(Composite container, Style style, int hspan, IChangeListener listener, T... vals){
		int len = vals != null ? vals.length : 0;
		buttons = new Button[len];
		values = new Object[len];
		
		int clmns = style == Style.ONE_PER_LINE ? 1 : len;
		Composite grp = SWTUtil.createComposite(container, clmns, hspan, GridStyle.FILL_HORIZONTAL, null);
		for (int i = 0; i < len; i++) {
			T val = vals[i];
			Button btn = SWTUtil.createRadioButton(grp, val.toString());
			btn.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					if (listener != null) {
						listener.onChange(btn);
					}
				}
			});
			
			buttons[i] = btn;
			values[i] = val;
		}
	}

	/**
	 * Get the enum value matching the selected button.
	 */
	@SuppressWarnings("unchecked")
	public T getSelected() {
		for (int i = 0; i < buttons.length; i++) {
			if (buttons[i].getSelection()) {
				return (T)values[i];
			}
		}
		
		return null;
	}
	
	/**
	 * Select the button matching the given enum value; unselect others;
	 */
	public void select(T e) {
		if (e != null) {
			for (int i = 0; i < buttons.length; i++) {
				if (values[i] == e) {
					buttons[i].setSelection(true);
				} else {
					buttons[i].setSelection(false);
				}
			}
		}
	}
	
	public void enable() {
		for (int i = 0; i < buttons.length; i++) {
			buttons[i].setEnabled(true);
		}
	}
	
	public void disable() {
		for (int i = 0; i < buttons.length; i++) {
			buttons[i].setEnabled(false);
		}
	}
}
