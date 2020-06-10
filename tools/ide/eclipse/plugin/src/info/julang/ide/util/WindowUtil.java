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

package info.julang.ide.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.wizards.IWizardDescriptor;

public final class WindowUtil {

	/**
	 * Get the active workbench page. This is likely the beginning of every UI elements currently visible to the user.
	 * <p>
	 * Use this method only if you have no other approach to obtain the page. It's not guaranteed to work, subject to
	 * the current focus of the IDE, among other factors. Therefore this method should only be used where failing to
	 * get the active page will at most downgrade user experience, but not block practical use.
	 * 
	 * @return null if failed to obtain the page.
	 */
	public static IWorkbenchPage getActivePage() {
		try {
			// https://wiki.eclipse.org/FAQ_How_do_I_find_the_active_workbench_page%3F
			// - Default approach
			IWorkbenchWindow win = getActiveWindow();
			if (win != null) {
				return win.getActivePage();
			}
		} catch (Exception e) {
			// Ignore
		}
		
		return null;
	}
	
	/**
	 * Get the current structured selection from the workbench.
	 * 
	 * @return null if none is selected.
	 */
	public static IStructuredSelection getCurrentSelection() {
		IWorkbenchWindow window = getActiveWindow();
	    if (window != null) {
	        ISelection sel = window.getSelectionService().getSelection();
	        if (sel instanceof IStructuredSelection) {
	        	return (IStructuredSelection)sel;
	        }
	    }
	    
	    return null;
	}
	
	/**
	 * Get the active workbench window.
	 * 
	 * @return null if failed to obtain the window.
	 */
	public static IWorkbenchWindow getActiveWindow() {
		IWorkbench wb = PlatformUI.getWorkbench();
		if (wb != null) {
			return wb.getActiveWorkbenchWindow();
		}
		
		return null;
	}
	
	/**
	 * Open a New Wizard ("New" refers to the wizard type - there are also "Import" and "Export" wizards) as specified by the id.
	 */
	public static void openNewWizard(Shell shell, String id) {
		try {
			IWizardDescriptor descriptor = PlatformUI.getWorkbench().getNewWizardRegistry().findWizard(id);
			if (descriptor != null) {
				if (shell == null) {
					shell = Display.getCurrent().getActiveShell();
				}
				
				IWizard wizard = descriptor.createWizard();
				WizardDialog wd = new WizardDialog(shell, wizard);
				wd.setTitle(wizard.getWindowTitle());
				wd.open();
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Open an Error Dialog.
	 */
	public static void showError(Shell shell, Exception ex) {
		MessageDialog.openError(shell, "Error", ex.getMessage());
	}
}
