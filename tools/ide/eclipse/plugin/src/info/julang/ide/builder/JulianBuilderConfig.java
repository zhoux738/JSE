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

public class JulianBuilderConfig {

	private ParsingLevel lvl;
	private boolean processPragma;
	
	public JulianBuilderConfig(ParsingLevel lvl, boolean processPragma) {
		this.lvl = lvl;
		this.processPragma = processPragma;
	}

	public ParsingLevel getParsingLevel() {
		return lvl;
	}
	
	public boolean shouldProcessPragma() {
		return processPragma;
	}
	
	public static JulianBuilderConfig loadFromProject(IProject project) {
		ParsingLevel pl = ParsingLevel.loadFromProject(project, ParsingLevel.SYNTAX);
		boolean processPragma = false;
		if (project != null) {
			try {
				String str = project.getPersistentProperty(JulianScriptProperties.PROCESS_PRAGMA_PROPERTY);
				if (str != null) {
					processPragma = Boolean.valueOf(str);
				}
			} catch (Exception e) {
				// Give up
			}
		}
		
		return new JulianBuilderConfig(pl, processPragma);
	}
	
	public boolean saveToProject(IProject project) {
		if (project == null) {
			return false;
		}
		
		try {
			ParsingLevel.saveToProject(project, this.lvl);
			project.setPersistentProperty(
				JulianScriptProperties.PROCESS_PRAGMA_PROPERTY,
				Boolean.toString(this.processPragma));
			return true;
		} catch (CoreException e) {
			// TODO - log?
		}

		return false;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((lvl == null) ? 0 : lvl.hashCode());
		result = prime * result + (processPragma ? 1231 : 1237);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JulianBuilderConfig other = (JulianBuilderConfig) obj;
		if (lvl != other.lvl)
			return false;
		if (processPragma != other.processPragma)
			return false;
		return true;
	}
}
