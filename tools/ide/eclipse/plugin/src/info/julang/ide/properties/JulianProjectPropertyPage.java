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

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.dialogs.PropertyPage;

import info.julang.ide.builder.JulianBuilder;
import info.julang.ide.builder.JulianBuilderConfig;
import info.julang.ide.builder.ParsingLevel;
import info.julang.ide.util.GridStyle;
import info.julang.ide.util.SWTUtil;
import info.julang.ide.util.WindowUtil;
import info.julang.ide.widgets.RadioButtonGroup;

/**
 * Project-level property page (right-click project in the navigator view -> properties -> Julian) for general configuration.
 * 
 * @author Ming Zhou
 */
public class JulianProjectPropertyPage extends PropertyPage implements IWorkbenchPropertyPage {

	private RadioButtonGroup<ParsingLevel> parsingLevelGrp;
	private Button processPragmasBtn;
	private JulianBuilderConfig originalConf;
	
	private IProject project;
	
	@Override
	protected Control createContents(Composite parent) {
		project = getElement().getAdapter(IProject.class);
		
		Composite container = SWTUtil.createComposite(parent, 2, 1, GridStyle.FILL_HORIZONTAL);
		
		SWTUtil.createLabel(container, "Choose a build level", 2, true);
		
	    this.parsingLevelGrp = new RadioButtonGroup<ParsingLevel>(
	    	container,
	    	RadioButtonGroup.Style.ONE_PER_LINE,
	    	2,
	    	null, 
	    	ParsingLevel.LEXICAL,
	    	ParsingLevel.SYNTAX,
	    	ParsingLevel.ADV_SYNTAX
		);
	    
	    this.processPragmasBtn = SWTUtil.createCheckBox(container, "Process in-source directives");
		SWTUtil.createLabel(container, "(Only " + JulianBuilder.DIRECTIVE_NO_PARSING + " is supported.)", 2, true);
	    
	    performDefaults();
		
		return null;
	}
	
	@Override
	protected void performDefaults() {
		super.performDefaults();
		originalConf = JulianBuilderConfig.loadFromProject(project);
		parsingLevelGrp.select(originalConf.getParsingLevel());
		processPragmasBtn.setSelection(originalConf.shouldProcessPragma());
	}
	
	@Override
	public boolean performOk() {
		JulianBuilderConfig newConf = new JulianBuilderConfig(
			this.parsingLevelGrp.getSelected(),
			this.processPragmasBtn.getSelection());
		
		if (newConf.equals(originalConf)) {
			return true;
		}
		
		boolean succ = false;
		if (project != null) {
			succ = newConf.saveToProject(project);
			if (succ) {
				// Launch a rebuild
				IWorkbenchWindow win = WindowUtil.getActiveWindow();
				if (win != null) {
					JulianBuilder.buildAll(win, project);
				}
			}
		}
		
		return succ;
	}

}
