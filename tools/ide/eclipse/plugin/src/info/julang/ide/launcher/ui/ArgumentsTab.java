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
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import info.julang.ide.PluginImages;
import info.julang.ide.launcher.JulianLaunchConfigHelper;

/**
 * The main tab contains a text box for the user to put arguments to run the script with.
 * 
 * @author Ming Zhou
 */
public class ArgumentsTab extends AbstractLaunchConfigurationTab {

	private Text argsText;
	
	@Override
	public void createControl(Composite parent) {
        Composite comp = new Group(parent, SWT.BORDER);
        setControl(comp);

        GridLayoutFactory.swtDefaults().numColumns(1).applyTo(comp);

        Label label = new Label(comp, SWT.NONE);
        label.setText("Arguments to run the script with:");
        GridDataFactory.swtDefaults().applyTo(label);

        argsText = new Text(comp, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(argsText);
        argsText.addModifyListener(new ModifyListener() {    		
			@Override
			public void modifyText(ModifyEvent e) {
				scheduleUpdateJob();
			}
        });
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		// Nothing
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		argsText.setText(JulianLaunchConfigHelper.getArguments(configuration));
	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		JulianLaunchConfigHelper.setArguments(configuration, argsText.getText());
	}

	@Override
	public String getName() {
		return "Arguments";
	}
	
	@Override
	public Image getImage() {
		return PluginImages.IMG_VARIABLE.createImage();
	}
}
