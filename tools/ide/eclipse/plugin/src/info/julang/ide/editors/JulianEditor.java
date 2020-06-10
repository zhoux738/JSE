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

package info.julang.ide.editors;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;

import info.julang.ide.themes.ColorManagerFactory;
import info.julang.ide.themes.IColorManager;
import info.julang.ide.themes.SharedColorManager;
import info.julang.ide.themes.ThemeChangeListener;

public class JulianEditor extends TextEditor {

	private IColorManager colorManager;
	private JulianConfiguration config;
	private EditorThemeChangeListener listener;

	private class EditorThemeChangeListener implements IPropertyChangeListener {
		
		@Override
		public void propertyChange(PropertyChangeEvent p) {
			if (IWorkbenchPreferenceConstants.CURRENT_THEME_ID.equals(p.getProperty())){
				Object val = p.getNewValue();
				if (val instanceof String) {
					String sval = (String)val;
					if (sval.startsWith(ThemeChangeListener.JULIAN_THEME_ID_PREFIX)) {
						IThemeManager mgr = PlatformUI.getWorkbench().getThemeManager();
						ITheme theme = mgr.getCurrentTheme();
						SharedColorManager.Instance.setColorRegistry(theme.getColorRegistry());
					}
				}
			}
		}
	}
	
	public JulianEditor() {
		super();

		colorManager = ColorManagerFactory.getColorManager();
		
		setSourceViewerConfiguration(
			config = new JulianConfiguration(colorManager));
		
		setDocumentProvider(new JulianDocumentProvider());
		
		PlatformUI.getPreferenceStore().addPropertyChangeListener(
			listener = new EditorThemeChangeListener());
	}
	
	@Override
	public void dispose() {
		try {
			config.dispose();
		} catch (Exception e) {
			// Continue to dispose the next resource
			// LOG ("Failed at disposing of config");
		}
		
		try {
			colorManager.dispose();
		} catch (Exception e) {
			// Continue to dispose the next resource
			// LOG ("Failed at disposing of colorManager");
		}
		
		try {
			PlatformUI.getPreferenceStore().removePropertyChangeListener(listener);
		} catch (Exception e) {
			// Continue to dispose the next resource
			// LOG ("Failed at removing listeners from PlatformUI's PreferenceStore()");
		}
		
		super.dispose();
	}

}
