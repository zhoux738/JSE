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

package info.julang.ide.wizards;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import info.julang.ide.Constants;
import info.julang.ide.PluginImages;
import info.julang.ide.reporting.SafeRunner;
import info.julang.ide.util.WindowUtil;

/**
 * A wizard to help create a new .jul file, either as a non-module script file that can be placed arbitrarily
 * in the file system, or a module script file that must be placed under the right folder hierarchy. The file
 * will be filled with a template based on the module and type selection, and open in a new editor.
 * 
 * @author Ming Zhou
 */
public class NewJulianScriptWizard extends Wizard implements INewWizard {
	
	private NewJulianScriptPage page;
	private IStructuredSelection selection;

	public NewJulianScriptWizard() {
		super();
		setNeedsProgressMonitor(true);
	}

	// The injection only occurs if the wizard is opened from the wizard main page. 
	// Launching the wizard programmatically won't end up calling this method.
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}
	
	@Override
	public void addPages() {
		// If we didn't launch this page from the wizard main page, try to inject the selection ourselves.
		if (selection == null) {
			selection = WindowUtil.getCurrentSelection();
		}
		
		page = new NewJulianScriptPage(selection);
		addPage(page);
	}

	@Override
	public boolean performFinish() {
		final UserChoices choices = page.getUserChoices();
		
		// (Debug)
		// System.out.println(choices);
		
		return SafeRunner.produce(
			this.getShell(),
			() -> {
				IRunnableWithProgress op = monitor -> {
					try {
						doFinish(choices, monitor);
					} catch (CoreException | IOException e) {
						throw new InvocationTargetException(e);
					} finally {
						monitor.done();
					}
				};
				
				try {
					getContainer().run(true, false, op);
				} catch (Exception e) {
					WindowUtil.showError(getShell(), e);
					return false;
				}
				
				return true;
			},
			false);
	}
	
	private void doFinish(UserChoices uc, IProgressMonitor monitor) throws CoreException, IOException {
		ScriptType sc = uc.getScriptType();
		String fname = uc.getFileName();
		String contents = null;
		IFile file = null;
		if (sc == ScriptType.LOOSE) {
			monitor.beginTask("Creating " + fname, 2);
			
			IContainer container = uc.getContainerDir();
			if (container == null || !container.exists()) {
				throwCoreException("Container \"" + container.getFullPath().toString() + "\" does not exist.");
			}
			
			file = container.getFile(new Path(fname));
			// Non-module file doesn't need a template
			contents = "";
		} else if (sc == ScriptType.MODULE) {
			monitor.beginTask("Creating " + fname, 3);
			
			// Create the hierarchical module directory
			ModuleName modName = new ModuleName(uc.getModuleName());
			String path = modName.toPathString();
			if (path == null) {
				throwCoreException("Module name \"" + uc.getModuleName() + "\" is illegal.");
			}
			
			IContainer container = uc.getModuleBaseDir();
			IFolder folder = container.getFolder(new Path(path));
			if (!folder.exists()) {
				monitor.setTaskName("Creating folders ...");
				// Create the entire tree
				createFolderTree(folder);
			}
			
			monitor.worked(1);
			
			file = folder.getFile(new Path(fname));
			
			contents = getContents(modName, uc.getJulianType(), fname);
		}
		
		// Create the file
		if (file.exists()) {
			throwCoreException("A file of same name already exists in the target direcotry.");
		} else {
			InputStream stream = openContentStream(contents);
			file.create(stream, true, monitor);
			stream.close();
			monitor.worked(1);
		}
		
		// Store the choices
		uc.saveToSession();
		
		monitor.setTaskName("Opening ...");
		final IFile fileToOpen = file;
		getShell().getDisplay().asyncExec(() -> {
			IWorkbenchPage page =
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			try {
				IDE.openEditor(page, fileToOpen, true);
			} catch (PartInitException e) {
			}
		});
		monitor.worked(1);
	}
	
	private void createFolderTree(IFolder folder) throws CoreException {
		if (!folder.exists()) {
			IContainer parent = folder.getParent();
			if (parent instanceof IFolder) {
				createFolderTree((IFolder)parent);
			}
			
			folder.create(true, true, null);
		}
	}

	private String getContents(ModuleName modName, JulianType julianType, String fileName) {
		// File name: "newfile.jul"
		// Type name: |<----->|
		String typeName = fileName.substring(0, fileName.length() - 1 - Constants.FILE_EXT.length());
		StringBuilder sb = new StringBuilder();
		sb.append("module ");
		sb.append(modName);
		sb.append(";");
		sb.append(System.lineSeparator());
		sb.append(System.lineSeparator());
		if (julianType != null) {
			sb.append(julianType.name().toLowerCase());
			sb.append(" ");
			sb.append(typeName);
			sb.append(" {");
			sb.append(System.lineSeparator());
			sb.append(System.lineSeparator());
			sb.append("}");
		}
		
		return sb.toString();
	}

	private InputStream openContentStream(String contents) {
		return new ByteArrayInputStream(contents.getBytes());
	}

	private void throwCoreException(String message) throws CoreException {
		IStatus status = new Status(IStatus.ERROR, Constants.PLUGIN_ID, IStatus.OK, message, null);
		throw new CoreException(status);
	}
	
    @Override
	public Image getDefaultPageImage() {
        return PluginImages.IMG_MASCOT_64_64.createImage();
    }
}