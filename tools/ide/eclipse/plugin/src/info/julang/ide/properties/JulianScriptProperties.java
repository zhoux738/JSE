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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;

import info.julang.ide.Constants;

/**
 * This class only handles persisted properties. All value types must be serializable.
 * 
 * @author Ming Zhou
 */
public final class JulianScriptProperties {
	
	private JulianScriptProperties() { }
	
	//---------------- ENABLED_FOR_PARSING: whether a file/project is enabled for parsing ----------------//
	
	static final QualifiedName ENABLED_BOOL_PROPERTY = new QualifiedName(Constants.PLUGIN_ID, "ENABLED_FOR_PARSING");
	static final String ENABLED_BOOL_VALUE_DISABLED = "false";
	static final String ENABLED_BOOL_VALUE_DEFAULT = "";
	
	public static boolean isEnabledForParsing(IResource resource) {
		try {
			String enabledStr = resource.getPersistentProperty(ENABLED_BOOL_PROPERTY);
			
			if (ENABLED_BOOL_VALUE_DISABLED.equals(enabledStr)) {
				return false;
			}
		} catch (CoreException e) {
			// Log?
		}
		
		// Default to true
		return true;
	}

	//---------------- MODULE_PATHS: the paths to search for modules. project-level. ----------------//
	public static final QualifiedName MODULE_PATHS_PATHARRAY_PROPERTY = new QualifiedName(Constants.PLUGIN_ID, "MODULE_PATHS");
	
	public static final QualifiedName PARSING_LEVEL_PROPERTY = new QualifiedName(Constants.PLUGIN_ID, "PARSING_LEVEL");

	public static final QualifiedName PROCESS_PRAGMA_PROPERTY = new QualifiedName(Constants.PLUGIN_ID, "PROCESS_PRAGMA");
}
