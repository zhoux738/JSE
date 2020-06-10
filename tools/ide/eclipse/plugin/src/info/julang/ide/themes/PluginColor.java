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

import org.eclipse.swt.graphics.RGB;

/**
 * An enum of all the colors used by this plugin.
 * 
 * The id refers to the identifier when registered with the platform by theme extension point.
 * See plugin.xml => org.eclipse.e4.ui.css.swt.theme and org.eclipse.ui.themes extensions.
 * 
 * @author Ming Zhou
 */
public enum PluginColor {

	// Colors registered with the platform.
	KEYWORD("julian.editor.keyword_color", 153, 51,  102),
	COMMENT("julian.editor.comment_color", 102, 153, 102),
	LITERAL("julian.editor.literal_color", 51,  0,   255),
	REGEX  ("julian.editor.regex_color",   0,   206, 209),

	// Internal colors. No registration
	LIGHT_GRAY(null,  222, 222, 222);
	
	;
	
	private PluginColor(String id, int R, int G, int B) {
		this.id = id;
		this.defaultColor = new RGB(R, G, B);
	}
	
	private String id;
	private RGB defaultColor;
	
	public String getId() {
		return id;
	}
	
	public RGB getDefaultColor() {
		return defaultColor;
	}
}
