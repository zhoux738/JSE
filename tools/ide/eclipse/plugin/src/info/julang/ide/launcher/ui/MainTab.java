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

package info.julang.ide.launcher.ui;

import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;

import info.julang.ide.Constants;
import info.julang.ide.PluginImages;
import info.julang.ide.launcher.JulianLaunchConfigHelper;
import info.julang.ide.util.GridStyle;
import info.julang.ide.util.ResourceUtil;
import info.julang.ide.util.SWTUtil;
import info.julang.ide.util.WindowUtil;

/**
 * The main tab contains a File Selector to let the user choose the script file to run. 
 * This script is not necessarily inside a project.
 * 
 * @author Ming Zhou
 */
public class MainTab extends AbstractLaunchConfigurationTab {

	// Implementation Notes:
	// (1) UI layout inspired by JDT's launch configuration. See:
	// https://github.com/eclipse/eclipse.platform.debug/blob/master/org.eclipse.debug.ui/ui/org/eclipse/debug/ui/WorkingDirectoryBlock.java
	// (2) On how to interact with the default tab buttons: 
	//     - 'Apply' is enabled when the working copy becomes different from the persisted copy.
	//     - The comparison occurs when performApply(ILaunchConfigurationWorkingCopy) is called.
	//     - performApply(ILaunchConfigurationWorkingCopy) is triggered by calling scheduleUpdateJob().
	//     - so, must call scheduleUpdateJob() when the change occurs to a widget. There is no automatic association provided by the framework.
	// (3) On initialization - when creating a new config:
	//     - setDefaults is called against a working copy on an a tab instance
	//	   - createControl is called for a *different* tab instance 
	//     - initializeFrom is called from the config which was prepared from the working copy configrued by setDefaults
	
	private Text filePathText;
	private Button workspaceButton;
	
	private class WorkspaceButtonListener extends SelectionAdapter implements ModifyListener {
		
		@Override
		public void modifyText(ModifyEvent e) {
			scheduleUpdateJob();
		}
		
		@Override
		public void widgetSelected(SelectionEvent e) {
			// Is there a better dialog?
			FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
			dialog.setFilterExtensions(new String [] {".jul"});
			IWorkspaceRoot currentContainer = ResourcesPlugin.getWorkspace().getRoot();
			String rootOsPath = ResourceUtil.getAbsoluteFSPath(currentContainer);
			dialog.setFilterPath(rootOsPath);
			String path = dialog.open();
			if (path != null) {
				MainTab.this.setFileAbsolutePath(path);
			}
		}
	}

	// When creating a new tab instance, this happens first
	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		IFile file = null;
		
		// 1) Choose the current selection
		IStructuredSelection sel = WindowUtil.getCurrentSelection();
		file = ResourceUtil.toSingleResource(sel, IFile.class);
		
		// 2) Choose the active editor, if it's a Julian script.
		if (file == null) {
			IWorkbenchPage page = WindowUtil.getActivePage();
			if (page != null) {
				try {
					IEditorPart editor = page.getActiveEditor();
					IEditorInput input = editor.getEditorInput();
					if (input instanceof IFileEditorInput) {
						file = ((IFileEditorInput)input).getFile();
					}
				} catch (Exception e) {
					// Best effort. It's OK if we can't get a default file to fill in.
				}
			}
		}
		
		// 3) Populate the path with the chosen file
		if (file != null && Constants.FILE_EXT.equals(file.getFileExtension())) {
			// Store into the variable since at this moment it's possible the controls have not been initialized.
			// Will port the value over to UI at the end of createControl. 
			String initFilePath = ResourceUtil.getAbsoluteFSPath(file);
			JulianLaunchConfigHelper.setScriptFile(configuration, new File(initFilePath));
		}
	}
	
	// Then GUI will be created
	@Override
	public void createControl(Composite parent) {
        Composite comp = new Group(parent, SWT.BORDER);
        setControl(comp);

        GridLayoutFactory.swtDefaults().numColumns(1).applyTo(comp);

        Label label = new Label(comp, SWT.NONE);
        label.setText("Script file to run:");
        GridDataFactory.swtDefaults().applyTo(label);

        filePathText = new Text(comp, SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(filePathText);
		
		Composite buttonComp = SWTUtil.createComposite(
			comp, 3, 2, GridStyle.HORIZONTAL_ALIGN_END, layout -> {
		    	layout.marginHeight = 1;
		    	layout.marginWidth = 0;		
			});
		
		workspaceButton = createPushButton(buttonComp, "Workspace...", null);
		WorkspaceButtonListener listener = new WorkspaceButtonListener();
		workspaceButton.addSelectionListener(listener);
		
		filePathText.addModifyListener(new ModifyListener() {    		
			@Override
			public void modifyText(ModifyEvent e) {
				scheduleUpdateJob();
			}
        });
	}

	// Then it initializes from a config that has been prepared from the working copy
	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		boolean set = false;
		File file = JulianLaunchConfigHelper.getScriptFile(configuration);
		if (file != null && file.exists()) {
			set = setFileAbsolutePath(file.getAbsolutePath());
		}
		
		if (!set) {
			check(configuration);
		}
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		JulianLaunchConfigHelper.setScriptFile(configuration, new File(this.filePathText.getText()));
		check(configuration);
	}

	@Override
	public String getName() {
		return "Script";
	}
	
	@Override
	public Image getImage() {
		return PluginImages.IMG_SCRIPT_FILE.createImage();
	}
	
	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		return checked;
	}
	
	// In this class the argument comes from three different sources: 
	//  - File (persisted launch config)
	//  - IFile (IDE's active editor)
	//  - File Dialog (file chosen by user)
	private boolean setFileAbsolutePath(String absPath) {
    	if (absPath != null && this.filePathText != null) {
            this.filePathText.setText(absPath);
            return true;
    	}
    	
    	return false;
	}
	
	// State checking
	
	private boolean checked;
	
	private synchronized void check(ILaunchConfiguration launchConfig) {
		String errorMsg = null;
		File file = JulianLaunchConfigHelper.getScriptFile(launchConfig);
		if (file == null) {
			errorMsg = "Running file is not set.";
		} else {
			if (!file.exists()) {
				errorMsg = "Running file doesn't exist.";
			} else if (!file.isFile()) {
				errorMsg = "Must specify a file to run.";
			}
		}
		
		checked = errorMsg == null;
		this.setErrorMessage(errorMsg);
	}
}
