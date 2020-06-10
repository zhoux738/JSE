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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.dialogs.PropertyPage;

import info.julang.ide.builder.JulianBuilder;
import info.julang.ide.builder.ParsingLevel;
import info.julang.ide.util.GridStyle;
import info.julang.ide.util.SWTUtil;
import info.julang.ide.util.WindowUtil;
import info.julang.ide.widgets.RadioButtonGroup;

/**
 * Project-level property page (rightclick project in the navigator view -> properties -> Julian) for general configuration.
 * 
 * @author Ming Zhou
 */
public class JulianProjectPropertyPage extends PropertyPage implements IWorkbenchPropertyPage {

	private RadioButtonGroup<ParsingLevel> parsingLevel;
	
	private IProject project;
	
	@Override
	protected Control createContents(Composite parent) {
		project = getElement().getAdapter(IProject.class);
		
		Composite container = SWTUtil.createComposite(parent, 2, 1, GridStyle.FILL_HORIZONTAL);
		
		SWTUtil.createLabel(container, "Choose a build level", 2, true);
		
	    this.parsingLevel = new RadioButtonGroup<ParsingLevel>(
	    	container,
	    	RadioButtonGroup.Style.ONE_PER_LINE,
	    	2,
	    	null, 
	    	ParsingLevel.LEXICAL,
	    	ParsingLevel.SYNTAX,
	    	ParsingLevel.ADV_SYNTAX
		);
	    
	    performDefaults();
		
		return null;
	}
	
	@Override
	protected void performDefaults() {
		super.performDefaults();
		ParsingLevel pl = ParsingLevel.loadFromProject(project, ParsingLevel.SYNTAX);
		parsingLevel.select(pl);
	}
	
	@Override
	public boolean performOk() {
		boolean succ = false;
		if (project != null) {
			succ = ParsingLevel.saveToProject(project, parsingLevel.getSelected());
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
