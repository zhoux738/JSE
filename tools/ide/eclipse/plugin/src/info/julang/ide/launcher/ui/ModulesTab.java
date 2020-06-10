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

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import info.julang.ide.PluginImages;
import info.julang.ide.launcher.JulianLaunchConfigHelper;
import info.julang.ide.properties.ModulePathsBlock;
import info.julang.ide.util.GridStyle;
import info.julang.ide.util.SWTUtil;

/**
 * This tab shows the list of all the modules paths to be used, in addition to, 
 * or replacing those which are already added to the project configuration.
 * 
 * @author Ming Zhou
 */
public class ModulesTab extends AbstractLaunchConfigurationTab {
	
	//------------- Implementing AbstractLaunchConfigurationTab -------------//

	private ModulePathsBlock block;
	
	@Override
	public void createControl(Composite parent) {
		// Create main composite
		Composite mainComposite = SWTUtil.createComposite(parent, 2, 1, GridStyle.FILL_HORIZONTAL);
		setControl(mainComposite);

		block = new ModulePathsBlock(
			mainComposite,
			blk -> {
				updateLaunchConfigurationDialog();
			}, 
			true);
	}

	// Config => UI
	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		JulianLaunchConfigHelper.setModulePaths(configuration, null);
		JulianLaunchConfigHelper.setAppendProjectLevelModulePaths(configuration, true);
	}

	// Config => UI
	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		JulianLaunchConfigHelper.setModulePaths(configuration, block.getModulePaths());
		JulianLaunchConfigHelper.setAppendProjectLevelModulePaths(configuration, block.isAppendOrReplaceProjectLevelSetting());
	}

	// UI => Config
	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		block.setModulePaths(JulianLaunchConfigHelper.getModulePaths(configuration));
		block.setAppendOrReplaceProjectLevelSetting(JulianLaunchConfigHelper.isAppendProjectLevelModulePaths(configuration));
	}

	@Override
	public String getName() {
		return "Module paths";
	}
	
	@Override
	public Image getImage() {
		return PluginImages.IMG_MODULE.createImage();
	}
}
