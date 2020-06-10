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

package info.julang.ide.launcher;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleView;

import info.julang.ide.properties.JulianScriptProperties;
import info.julang.ide.reporting.SafeRunner;
import info.julang.ide.util.FSUtil;
import info.julang.ide.util.ResourceUtil;
import info.julang.ide.util.WindowUtil;

/**
 * Launching logic to be triggered from Context Popup Menu => Run As => Julian Script.
 * 
 * @author Ming Zhou
 */
public class JulianLaunchShortcut implements ILaunchShortcut {

	// Launch from package/project explorer
	@Override
	public void launch(ISelection selection, String mode) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection iss = (IStructuredSelection)selection;
			Object obj = iss.getFirstElement(); // This is only enabled when a single element is selected.
			if (obj instanceof IFile) {
				IFile file = (IFile) obj;

				JulianRunner runner = new JulianRunner(mode);
				
				IWorkbenchPage page = WindowUtil.getActivePage();

				configureRunner(runner, page, file);

				String path = ResourceUtil.getAbsoluteFSPath(file);
				runner.run(path);
			}
		}
	}

	// Launch from file editor
	@Override
	public void launch(IEditorPart editor, String mode) {
		SafeRunner.run(editor.getEditorSite(), () -> {
			IEditorInput input = editor.getEditorInput();
			IFile file = input.getAdapter(IFile.class);
			
			if (file != null) {
				JulianRunner runner = new JulianRunner(mode);
				
				// https://wiki.eclipse.org/FAQ_How_do_I_find_the_active_workbench_page%3F
				// - Within a Workbench Part
				IWorkbenchPage page = editor.getSite().getPage();
				configureRunner(runner, page, file);

				String path = ResourceUtil.getAbsoluteFSPath(file);
				runner.run(path);
			}
		});
	}
	
	// This methods will try to have the current workbench page to show the console view. 
	// But this is the best effort. In any case the user may turn on console view manually.
	private void configureRunner(JulianRunner runner, IWorkbenchPage page, IFile file) {
		SafeRunner.run(page.getWorkbenchWindow(), () -> {
			try {
				IConsoleView view = (IConsoleView) page.showView(IConsoleConstants.ID_CONSOLE_VIEW);
				runner.setConsoleView(view);
			} catch (PartInitException e) {
				// The console view cannot be initialized - likely a problem with the IDE or environment.
			}
			
			// Set module paths per project config
			IProject project = file.getProject();
			if (project != null) {
				try {
					File[] dirs = FSUtil.fromFSPathArray(
						project.getPersistentProperty(
							JulianScriptProperties.MODULE_PATHS_PATHARRAY_PROPERTY));
					runner.AddModuleDirs(dirs);
				} catch (CoreException e) {
					// Run no matter what
				}
			}
		});
	}
}
