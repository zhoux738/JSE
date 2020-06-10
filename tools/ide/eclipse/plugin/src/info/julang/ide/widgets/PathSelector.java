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

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

import info.julang.ide.themes.ColorManager;

/**
 * A path selector provides a text box and a button. Clicking on the button will prompt the user to
 * select a path to be filled into the text box.
 * 
 * @author Ming Zhou
 */
public class PathSelector implements MVText.ILabelProvider {

	private MVText text;
	private Button btn;
	
	public PathSelector(Text text, Button btn, ColorManager clrMgr, IChangeListener listener) {
		this.text = new MVText(text, clrMgr, this, listener);
		this.btn = btn;
		btn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				ContainerSelectionDialog dialog = new ContainerSelectionDialog(
					btn.getShell(), 
					ResourcesPlugin.getWorkspace().getRoot(), 
					false, // Do not allow creating new directory
					"Select a directory");
				
				if (dialog.open() == ContainerSelectionDialog.OK) {
					Object[] result = dialog.getResult();
					if (result.length == 1) {
						PathSelector.this.text.setText(((IPath) result[0]).toString());
					}
				}
			}
		});
	}

	@Override
	public String show(String text) {
		return text.startsWith("F/") ? text.substring(1) : text;
	}
	
	public void setPath(IPath path) {
		this.text.setText(path.toString());
	}
	
	public IPath getPath() {
		String text = this.text.getText();
		return text == null || text.isBlank() ? null : new Path(text);
	}

	public void enable() {
		text.enable();
		btn.setEnabled(true);
	}
	
	public void disable() {
		text.disable();
		btn.setEnabled(false);
	}
}
