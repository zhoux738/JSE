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

import java.io.PrintWriter;
import java.io.StringWriter;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

import info.julang.ide.Constants;

/**
 * This is a protective measure for user experience. If there is an unhandled error we will not let it 
 * go silent with Eclipse's error handling where the exception eventually ends up buried in the sea 
 * of platform tracing files.
 * 
 * @author Ming Zhou
 */
public final class SafeRunner {

	public static void run(IShellProvider provider, IRunnable runnable) {
		try {
			runnable.run();
		} catch (Throwable t) {
			try {
				showErrorBox(provider, t);
			} catch (Throwable t2) {
				throw new RuntimeException("Failed at showing error. Original exception re-propogated as the cause.", t);
			}
		}
	}
	
	public static void run(IRunnable runnable) {
		run(() -> null, runnable);
	}
	
	public static void run(Shell shell, IRunnable runnable) {
		run(() -> shell, runnable);
	}
	
	public static <T> T produce(IShellProvider provider, IReturnable<T> returnable, T defaultValue) {
		try {
			return returnable.run();
		} catch (Throwable t) {
			try {
				showErrorBox(provider, t);
				return defaultValue;
			} catch (Throwable t2) {
				throw new RuntimeException("Failed at showing error. Original exception re-propogated as the cause.", t);
			}
		}
	}
	
	public static <T> T produce(Shell shell, IReturnable<T> returnable, T defaultValue) {
		return produce(() -> shell, returnable, defaultValue);
	}
	
	private static void showErrorBox(IShellProvider provider, Throwable t) {
		Shell shell = null;
		if (provider != null) {
			shell = provider.getShell();
		} else {
			shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		}

		StringBuilder sb = new StringBuilder();
		
		if (t instanceof CoreException) {
			sb.append("An error occurred and was not handled by Julian Development Environment (JDE).");
		} else {
			sb.append("An error occurred and was not handled by Julian Development Environment (JDE). This is likely a ");
			sb.append("bug with JDE. Please report this issue, along with the stacktrace below, to ");
			sb.append(Constants.AUTHOR_MAIL);
		}

		sb.append(System.lineSeparator());
		sb.append(System.lineSeparator());
		
		StringWriter sw = new StringWriter();
		try (PrintWriter pw = new PrintWriter(sw)){
			t.printStackTrace(pw);
			sb.append(sw.toString());
		}
		
		if (sb.length() > MAX_LENGTH) {
			sb.replace(
				MAX_LENGTH - 10, 
				sb.length(),
				" ... ... ");
			sb.append(System.lineSeparator());
			sb.append("\t... ... ");
			sb.append(System.lineSeparator());
			sb.append("\t... ... ");
			sb.append(System.lineSeparator());
			sb.append("\t(truncated)");
			sb.append(System.lineSeparator());
		}
		
		CopyableErrorMessageDialog dialog = new CopyableErrorMessageDialog(
			shell,
			"Unhandled error",
			sb.toString());
		
		dialog.open();
	}
	
	private static final int MAX_LENGTH = 2000;
	
	// A dialog allowing copyable message.
	// Inspired by https://stackoverflow.com/a/30630475 (original author: greg-449)
	private static class CopyableErrorMessageDialog extends MessageDialog {
		
		private CopyableErrorMessageDialog(Shell parentShell, String dialogTitle, String dialogMessage) {
			super(parentShell, dialogTitle, null, dialogMessage, MessageDialog.ERROR, new String[] { IDialogConstants.OK_LABEL }, 0);
		}

		@Override
		protected Control createMessageArea(final Composite composite) {
			Text msg = new Text(composite, SWT.READ_ONLY | SWT.MULTI | SWT.WRAP);
			
			msg.setText(message);
			
			GridData data = new GridData(SWT.FILL, SWT.TOP, true, false);
			data.widthHint = convertHorizontalDLUsToPixels(IDialogConstants.MINIMUM_MESSAGE_AREA_WIDTH);
			msg.setLayoutData(data);

			return composite;
		}
	}
}
