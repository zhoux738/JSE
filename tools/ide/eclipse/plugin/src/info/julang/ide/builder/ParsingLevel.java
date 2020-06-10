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

package info.julang.ide.builder;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import info.julang.ide.properties.JulianScriptProperties;

/**
 * The level at which a Julian project is built.
 * 
 * This is a mostly transitional feature, allowing us to incrementally evolve the IDE down the road.
 * 
 * @author Ming Zhou
 */
public enum ParsingLevel {
	
	LEXICAL("Syntax highlighting only"),
	SYNTAX("Basic parsing"),
	ADV_SYNTAX("Advanced parsing (experimental)");

	private String text;
	
	private ParsingLevel(String text) {
		this.text = text;
	}
	
	@Override
	public String toString() {
		return text;
	}
	
	public static ParsingLevel loadFromProject(IProject project, ParsingLevel defaultTo) {
		ParsingLevel pl = defaultTo;
		
		if (project != null) {
			try {
				String str = project.getPersistentProperty(JulianScriptProperties.PARSING_LEVEL_PROPERTY);
				if (str != null) {
					pl = ParsingLevel.valueOf(str);
				}
			} catch (Exception e) {
				// Give up
			}
		}
		
		return pl;
	}
	
	public static boolean saveToProject(IProject project, ParsingLevel level) {
		if (project == null) {
			return false;
		}
		
		try {
			project.setPersistentProperty(
				JulianScriptProperties.PARSING_LEVEL_PROPERTY,
				level.name());
			return true;
		} catch (CoreException e) {
			// TODO - log?
		}

		return false;
	}
}
