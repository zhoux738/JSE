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

package info.julang.ide.reporting;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import info.julang.ide.Constants;

/**
 * Used to issue warning where an expected functionaliry is still not implemented.
 * 
 * @author Ming Zhou
 */
public final class LimitationWarner {

	public static void showLimitation(IShellProvider provider, String msg) {
		Shell shell = null;
		if (provider != null) {
			shell = provider.getShell();
		} else {
			shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		}
		
		StringBuilder sb = new StringBuilder();
		
		sb.append(msg);
		sb.append(System.lineSeparator());
		sb.append(System.lineSeparator());
		sb.append("To expedite a feature request, please contact ");
		sb.append(Constants.AUTHOR_MAIL);

		MessageDialog.openWarning(
			shell,
			"Feature limitation",
			sb.toString());
	}
}
