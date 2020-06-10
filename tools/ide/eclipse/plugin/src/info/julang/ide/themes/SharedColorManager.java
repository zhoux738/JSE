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

package info.julang.ide.themes;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.swt.graphics.Color;

/**
 * A globally shared color manager that solely relies on the system-wide theme manager to get colors.
 * 
 * @author Ming Zhou
 */
public class SharedColorManager implements IColorManager {

	private ColorRegistry colorReg;
	
	public final static SharedColorManager Instance = new SharedColorManager();
	
	private SharedColorManager(){
		list = new ArrayList<ThemeChangeListener>();
	}
	
	public void setColorRegistry(ColorRegistry reg) {
		if (this.colorReg != reg) {
			this.colorReg = reg;
			for (ThemeChangeListener l : list) {
				l.onThemeChange();
			}
		}
	}
	
	public Color getColor(PluginColor pcolor) {
		Color color = null;
		String id = pcolor.getId();
		
		// First try from color registry
		if (colorReg != null && id != null) {
			color = colorReg.get(id);
		}
		
		return color;
	}
	
	public void dispose() {
		// Nothing to do
	}

	public void addOnThemeChangeListener(ThemeChangeListener l) {
		list.add(l);
	}
	
	public void removeOnThemeChangeListener(ThemeChangeListener l) {
		list.remove(l);
	}
	
	// For internal use only
	boolean isValid() {
		return this.colorReg != null;
	}
	
	private List<ThemeChangeListener> list;
}
