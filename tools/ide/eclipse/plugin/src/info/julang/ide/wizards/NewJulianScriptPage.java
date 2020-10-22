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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import info.julang.ide.themes.ColorManager;
import info.julang.ide.util.GridStyle;
import info.julang.ide.util.SWTUtil;
import info.julang.ide.widgets.EnhancedText;
import info.julang.ide.widgets.PathSelector;
import info.julang.ide.widgets.RadioButtonGroup;

/**
 * Set basic info about a new Julian script file.
 * 
 * A script file comes in one of the two flavors: non-modularized (loose) or modularized. If creating a loose script,
 * the user must specify the directory in which the file will be created. If creating a module file, the user specifies
 * the root module base, which is a directory, and a module name. The wizard will combine the two to create all the 
 * necessary directories to match the module.
 */
public class NewJulianScriptPage extends WizardPage {

	// Implementation Notes:
	// Based on the extension sample and inspired by "New Class" wizard from JDT:
	// https://github.com/eclipse/eclipse.jdt.ui/blob/master/org.eclipse.jdt.ui/ui/org/eclipse/jdt/ui/wizards/NewClassWizardPage.java
	// https://github.com/eclipse/eclipse.jdt.ui/blob/master/org.eclipse.jdt.ui/ui/org/eclipse/jdt/ui/wizards/NewTypeWizardPage.java

	// Common options
	private EnhancedText m_txt_fileName;
	private RadioButtonGroup<ScriptType> m_rgrp_fileType;
	
	// Options for non-module file
	private PathSelector m_ps_container;

	// Options for module file
	private PathSelector m_ps_moduleRoot;
	private EnhancedText m_txt_moduleName;
	private RadioButtonGroup<JulianType> m_rgrp_julianType;

	// Form I/O
	private IStructuredSelection m_selection;
	private IProject m_project;
	private UserChoices userChoices;
	
	private ColorManager m_clrMgr;
	private boolean initialized;

	/**
	 * Constructor for SampleNewWizardPage.
	 * 
	 * @param pageName
	 */
	public NewJulianScriptPage(IStructuredSelection selection) {
		super("NewJulianScriptPage");
		setTitle("New Julian Script File");
		setDescription("This wizard creates a new Julian Script file.");
		this.m_selection = selection;
	}
	
	@Override
	public void dispose() {
		if (m_clrMgr != null) {
			m_clrMgr.dispose();
		}
	}

	@Override
	public void createControl(Composite parent) {
		this.m_clrMgr = new ColorManager(null);
		this.initialized = false;
		
		Composite container = SWTUtil.createComposite(parent, 3, 1, GridStyle.FILL_HORIZONTAL, ld -> { 
			//ld.verticalSpacing = 1; 
		});
	    
		// The following code are grouped into {} blocks to help avoid accidental reuse of unrelated components.
		
		// Group A: common controls
		{
			// Row A1: File name:
			SWTUtil.createLabel(container, "File name:", 3, true);

			// Row A2: [........]
			Composite container1 = SWTUtil.createComposite(container, 3, 3, GridStyle.FILL_HORIZONTAL, null);
			Text text_fileName = new Text(container1, SWT.BORDER | SWT.SINGLE);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			text_fileName.setLayoutData(gd);
			text_fileName.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseDoubleClick(MouseEvent e) { 
					// Let's provide a little more convenience for the user - 
					// double clicking will not select the extension part, which is not supposed to be changed.
					String sel = text_fileName.getText();
					int i = sel.lastIndexOf('.');
					if (i >= 0) {
						text_fileName.setSelection(0, i);
					}
				}
			});
			
			this.m_txt_fileName = new EnhancedText(text_fileName, m_clrMgr, w -> dialogChanged());
			// (fill the remaining 1 cell)
			SWTUtil.createComposite(container1, 1, 1, GridStyle.FILL_HORIZONTAL, null);
			
			// Row A3: Create a ...		
		    SWTUtil.createLabel(container, "This is a ...", 3, true);
		    
			// Row A4: () non-module file () module file
		    this.m_rgrp_fileType = new RadioButtonGroup<ScriptType>(
		    	container, 
		    	RadioButtonGroup.Style.ALL_IN_ONE_LINE, 
		    	3, 
		    	w -> dialogChanged(), 
		    	ScriptType.LOOSE,
		    	ScriptType.MODULE
		    );	
		}

		// Sep Row: ----------
	    SWTUtil.createSeparator(container, true);

		// Group B: non-module controls
	    {
			// Row B1: Choose a directory:
			SWTUtil.createLabel(container, "Non-module file - choose a directory:", 3, true);

			// Row B2: [........] <Browse...>
			Composite container2 = SWTUtil.createComposite(container, 3, 3, GridStyle.FILL_HORIZONTAL, null);
			Text m_text_containerOfNonModuleFile = new Text(container2, SWT.BORDER | SWT.SINGLE);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			m_text_containerOfNonModuleFile.setLayoutData(gd);
			gd.horizontalSpan = 2;

			Button m_btn_containerOfNonModuleFile = new Button(container2, SWT.PUSH);
			m_btn_containerOfNonModuleFile.setText("Browse...");
			
			m_ps_container = new PathSelector(
				m_text_containerOfNonModuleFile, 
				m_btn_containerOfNonModuleFile, 
				this.m_clrMgr, 
				w -> dialogChanged());
	    }
		
		// Sep Row: ----------
	    SWTUtil.createSeparator(container, true);

		// Group C: module controls
	    {
			// Row C1: Choose a module path:
			SWTUtil.createLabel(container, "Module file - choose a root module path:", 3, true);

			// Row C2: [........] <Browse...>
			Composite container3 = SWTUtil.createComposite(container, 3, 3, GridStyle.FILL_HORIZONTAL, null);
			Text m_text_rootModulePath = new Text(container3, SWT.BORDER | SWT.SINGLE);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			m_text_rootModulePath.setLayoutData(gd);
			gd.horizontalSpan = 2;

			Button m_btn_rootModulePath = new Button(container3, SWT.PUSH);
			m_btn_rootModulePath.setText("Browse...");
			
			m_ps_moduleRoot = new PathSelector(
				m_text_rootModulePath, 
				m_btn_rootModulePath, 
				this.m_clrMgr, 
				w -> dialogChanged());
		
			// Row C3: Module name:
			SWTUtil.createLabel(container, "Module name:", 3, true);

			// Row C4: [........]
			Composite container4 = SWTUtil.createComposite(container, 3, 3, GridStyle.FILL_HORIZONTAL, null);
			Text text_moduleName = new Text(container4, SWT.BORDER | SWT.SINGLE);
			gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 2;
			text_moduleName.setLayoutData(gd);
			this.m_txt_moduleName = new EnhancedText(text_moduleName, m_clrMgr, w -> dialogChanged());
			// (fill the remaining 1 cell)
			SWTUtil.createComposite(container4, 1, 1, GridStyle.FILL_HORIZONTAL, null);
			
			// Row C4: Also add a type of same name:
		    SWTUtil.createLabel(container, "Also add a type of same name:", 3, true);
		    
			// Row C5: () non-module file () module file
			this.m_rgrp_julianType = new RadioButtonGroup<JulianType>(
		    	container, 
		    	RadioButtonGroup.Style.ALL_IN_ONE_LINE,
		    	3, 
		    	w -> dialogChanged(), 
		    	JulianType.Class,
		    	JulianType.Interface,
		    	JulianType.Enum,
		    	JulianType.Attribute
		    );	
	    }
		
		initialize();
		dialogChanged();

		setControl(container);
	}

	private void initialize() {
		this.userChoices = new UserChoices();
		
		// 1. Try to load from the most recent choices
		m_project = fromSelection(IProject.class);
		if (m_project != null) {
			this.userChoices.loadFromSession(m_project);
		}
		
		m_txt_fileName.setText("new_script.jul");
		
		if (userChoices == null) {
			return;
		}
		
		// 2. For those which don't have a recent choice, try to default to something reasonable
		
		// Directory for non-module file
		// Default: the selected directory
		if (userChoices.getContainerDir() == null) {
			IFolder folder = fromSelection(IFolder.class);
			if (folder != null) {
				userChoices.setContainerDir(folder);
			}
		}
		
		// Module path directory
		// Default: project root
		if (userChoices.getModuleBaseDir() == null) {
			if (m_project != null) {
				userChoices.setModuleBaseDir(m_project);
			}
		}
		
		// No defaults for 
		// - Module name
		// - Julian type to start the script with
		
		// 3. Initialize the widgets with the defaults
		
		ScriptType st = userChoices.getScriptType();
		if (st == null) {
			// 3.1) If the user has never created a new file.
			this.m_rgrp_fileType.select(ScriptType.LOOSE);
			
			// Set a default location if this is a folder of the project
			if (userChoices.getContainerDir() != null) {
				this.m_ps_container.setPath(userChoices.getContainerDir().getFullPath());
			}
		} else if (st == ScriptType.LOOSE) {
			// 3.2) If the user last created a loose file, show the directory.
			this.m_rgrp_fileType.select(ScriptType.LOOSE);
			if (userChoices.getContainerDir() != null) {
				this.m_ps_container.setPath(userChoices.getContainerDir().getFullPath());
			}
		} else if (st == ScriptType.MODULE) {
			// 3.3) If the user last created a module file, show a few related settings.
			this.m_rgrp_fileType.select(ScriptType.MODULE);
			
			if (userChoices.getModuleBaseDir() != null) {
				this.m_ps_moduleRoot.setPath(userChoices.getModuleBaseDir().getFullPath());
			}
			
			if (userChoices.getModuleName() != null) {
				this.m_txt_moduleName.setText(userChoices.getModuleName());
			}
			
			if (userChoices.getJulianType() != null) {
				this.m_rgrp_julianType.select(userChoices.getJulianType());
			}
		}
		
		this.initialized = true;
	}
	
	/**
	 * Try to convert the selection, if present, to a resource type.
	 */
	@SuppressWarnings("unchecked")
	private <T> T fromSelection(Class<T> clazz) {
		if (m_selection != null 
			&& !m_selection.isEmpty()) {
			if (m_selection.size() == 1) {
				Object obj = m_selection.getFirstElement();
				if (obj.getClass() == clazz) {
					return (T) obj;
				} else if (clazz == IProject.class && obj instanceof IResource) {
					// IResource cannot convert to IProject via IAdaptable
					return (T)((IResource) obj).getProject();
				} else if (obj instanceof IAdaptable) {
					return ((IAdaptable) obj).getAdapter(clazz);
				}
			}
		}
		
		return null;
	}

	// Should be called whenever the data model changes.
	private void dialogChanged() {
		if (!initialized) {
			return;
		}
		
		// UI disable
		ScriptType sc = this.getScriptType();
		if (sc == null) {
			updateStatus("Must select a file type.");
			return;
		}
		
		switch (this.getScriptType()) {
		case LOOSE:
			this.m_rgrp_julianType.disable();
			this.m_txt_moduleName.disable();
			this.m_ps_moduleRoot.disable();
			this.m_ps_container.enable();
			break;
		case MODULE:
			this.m_rgrp_julianType.enable();
			this.m_txt_moduleName.enable();
			this.m_ps_moduleRoot.enable();
			this.m_ps_container.disable();
			break;
		}
		
		// Common check
		String fileName = getFileName();
	
		if (fileName.length() == 0) {
			updateStatus("File name must be specified");
			return;
		}
		
		if (fileName.replace('\\', '/').indexOf('/', 1) > 0) {
			updateStatus("File name must be valid");
			return;
		}
		
		int dotLoc = fileName.lastIndexOf('.');
		if (dotLoc != -1) {
			String ext = fileName.substring(dotLoc + 1);
			if (ext.equalsIgnoreCase("jul") == false) {
				updateStatus("File extension must be \".jul\"");
				return;
			}
		} else {
			updateStatus("File must end with extenion \".jul\"");
			return;
		}
		
		switch (this.getScriptType()) {
		case LOOSE:
			{
				// Check for non-module file.
				
				IPath dirPath = getContainerPath();
				IContainer container = checkDirectory("Directory", dirPath);

				if (container == null) {
					return;
				}
				
				if (container.getFile(new Path(fileName)).exists()) {
					updateStatus("A file with same name already exists in the specified directory.");
					return;
				}
			}
			
			break;
			
		case MODULE:
			{
				// Check for module file.
				
				String moduleName = getModuleName();
				if (moduleName.length() == 0) {
					updateStatus("Module name must be specified");
					return;
				}
				
				IPath moduleRootPath = getRootModulePath();
				IContainer container = checkDirectory("Root module path", moduleRootPath);
				
				ModuleName modName = new ModuleName(moduleName);
				String modPath = modName.toPathString();
				if (modPath == null) {
					updateStatus("Module name is illegal");
					return;
				}
				
				// Check if a file already exists at {container}/{modPath}/{fileName}.
				IFolder folder = container.getFolder(new Path(modPath));
				if (folder.exists()) {
					IFile file = folder.getFile(new Path(fileName));
					if (file.exists()) {
						updateStatus("A file with same name already exists in the specified module.");
						return;
					}
				}
			}
		}
		
		updateStatus(null);
	}
	
	private IContainer checkDirectory(String itemNname, IPath dirPath) {
		if (dirPath == null) {
			updateStatus(itemNname + " must be specified");
			return null;
		}
		
		IResource container = findResourceByPath(dirPath);
		if (container == null 
			|| (container.getType() & (IResource.PROJECT | IResource.FOLDER)) == 0) {
			updateStatus(itemNname + " must exist");
			return null;
		}
		
		if (!container.isAccessible()) {
			updateStatus(itemNname + " must be writable");
			return null;
		}
		
		if (!(container instanceof IContainer)) {
			updateStatus(itemNname + " must be a container");
			return null;
		}
		
		return (IContainer)container;
	}
	
	// The path must be an absolute path against the workspace.
	private IResource findResourceByPath(IPath path) {
		return ResourcesPlugin.getWorkspace().getRoot().findMember(path);
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	private IPath getContainerPath() {
		return this.m_ps_container.getPath();
	}

	private String getFileName() {
		String ret = this.m_txt_fileName.getText();
		return ret != null ? ret : "";
	}
	
	private String getModuleName() {
		String ret = this.m_txt_moduleName.getText();
		return ret != null ? ret : "";
	}

	private IPath getRootModulePath() {
		return this.m_ps_moduleRoot.getPath();
	}
	
	private ScriptType getScriptType() {
		// return this.m_btn_createNoneModuleFile.getSelection() == true ? ScriptType.LOOSE : ScriptType.MODULE;
		return this.m_rgrp_fileType.getSelected();
	}
	
	JulianType getTypeSelection() {
		return this.m_rgrp_julianType.getSelected();
	}
	
	UserChoices getUserChoices() {
		userChoices.setContainerDir(toContainer(this.getContainerPath()));
		userChoices.setScriptType(this.getScriptType());
		userChoices.setModuleBaseDir(toContainer(this.getRootModulePath()));
		userChoices.setModuleName(this.getModuleName());
		userChoices.setJulianType(this.getTypeSelection());
		userChoices.setFileName(this.getFileName());
		return userChoices;
	}
	
	private IContainer toContainer(IPath path) {
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(path);
		if (resource instanceof IContainer) {
			return (IContainer) resource;
		}
		
		return null;
	}
}