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

package info.julang.ide.properties;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

import info.julang.ide.reporting.SafeRunner;
import info.julang.ide.util.FSUtil;
import info.julang.ide.util.GridStyle;
import info.julang.ide.util.SWTUtil;

/**
 * Project-level property page (rightclick project in the navigator view -> properties -> Julian) for module paths configuration.
 * 
 * When running a script, the runner will use these paths as the root module paths, unless overridden through Run Configuration page.
 * 
 * @author Ming Zhou
 */
public class JulianProjectPropertyModulePathsPage extends PropertyPage implements IWorkbenchPropertyPage {

	private IProject project;
	private ModulePathsBlock block;
	
	@Override
	protected Control createContents(Composite parent) {
		project = getProject();
		
		if (project == null) {
			Label label= new Label(parent, SWT.LEFT);
			label.setText("Not a Julian project");
			return label;
		} else {
			Composite mainComposite = SWTUtil.createComposite(parent, 2, 1, GridStyle.FILL_HORIZONTAL);
			block = new ModulePathsBlock(mainComposite, null, false);
			
			try {
				File[] dirs = FSUtil.fromFSPathArray(
					project.getPersistentProperty(
						JulianScriptProperties.MODULE_PATHS_PATHARRAY_PROPERTY));
				
				block.setModulePaths(dirs);
			} catch (CoreException e) {
				// TODO - show a warning
			}
			
			return mainComposite;
		}
	}
	
	private IProject getProject() {
        IAdaptable adaptable = getElement();
		return adaptable == null ? null : (IProject)adaptable.getAdapter(IProject.class);
	}
	
	@Override
	public boolean performOk() {
		return SafeRunner.produce(
			this.getShell(),
			() -> {
				File[] dirs = block.getModulePaths();
				project.setPersistentProperty(
					JulianScriptProperties.MODULE_PATHS_PATHARRAY_PROPERTY,
					FSUtil.toAbsoluteFSPathArray(dirs));
				
				return true;
			},
			false);
	}

}
