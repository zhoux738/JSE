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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * A local cache of colors. If the color is managed by the system, the manager won't dispose of it after use.
 * 
 * @author Ming Zhou
 */
public class ColorManager implements IColorManager {

	private Map<RGB, Color> colorMap;
	private ColorRegistry colorReg;
	
	public ColorManager(ColorRegistry reg){
		colorReg = reg;
	}
	
	public Color getColor(PluginColor pcolor) {
		Color color = null;
		String id = pcolor.getId();
		
		// First try from color registry
		if (colorReg != null && id != null) {
			color = colorReg.get(id);
		}
		
		// Then try the default color
		if (color == null) {
			if (colorMap == null) {
				colorMap = new HashMap<>();
				
				RGB rgb = pcolor.getDefaultColor();
				color = new Color(Display.getCurrent(), rgb);
				colorMap.put(rgb, color);
			}
		}
		
		return color;
	}
	
	public void addOnThemeChangeListener(ThemeChangeListener l) {
		// Do not support adaptive change.
	}
	
	public void removeOnThemeChangeListener(ThemeChangeListener l) {
		// Do not support adaptive change.
	}
	
	public void dispose() {
		if (colorMap != null) {
			colorMap.values().forEach(Color::dispose);
		}
	}
}
