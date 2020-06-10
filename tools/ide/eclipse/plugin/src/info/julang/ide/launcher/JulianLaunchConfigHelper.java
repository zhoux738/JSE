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

package info.julang.ide.launcher;

import java.io.File;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

import info.julang.ide.util.FSUtil;

/**
 * A helper class to convert forth and back the various settings from ILaunchConfiguration, 
 * which is conveyed between the launch configuration UI and the launcher.
 * 
 * @author Ming Zhou
 */
// Implementation Notes: 
// ILaunchConfiguration is not implementable. So have to use a utility class.
public final class JulianLaunchConfigHelper {

	public static final String ATTRIBUTE_SCRIPT_FILE = "ATTRIBUTE_SCRIPT_FILE";
	public static final String ATTRIBUTE_ARGUMENTS = "ATTRIBUTE_ARGUMENTS";
	public static final String ATTRIBUTE_MODULE_PATHS = "ATTRIBUTE_MODULE_PATHS";
	public static final String ATTRIBUTE_APPEND_PROJECT_MODULE_PATHS = "ATTRIBUTE_APPEND_PROJECT_MODULE_PATHS";
	
	/**
	 * Get the script file to run.
	 */
	public static File getScriptFile(ILaunchConfiguration config) {
		try {
			String path = config.getAttribute(ATTRIBUTE_SCRIPT_FILE, "");
			if (path != null && !"".equals(path)) {
				return new File(path);
			}
		} catch (CoreException e) {
			// Ignore
		}
		
		return null;
	}
	
	/**
	 * Set the script file to run.
	 */
	public static void setScriptFile(ILaunchConfigurationWorkingCopy config, File file) {
		config.setAttribute(ATTRIBUTE_SCRIPT_FILE, file.getAbsolutePath());
	}
	
	/**
	 * Get the arguments to run the script with.
	 */
	public static String getArguments(ILaunchConfiguration config) {
		try {
			return config.getAttribute(ATTRIBUTE_ARGUMENTS, "");
		} catch (CoreException e) {
			// Ignore
			return "";
		}
	}
	
	/**
	 * Set the arguments to run the script with.
	 */
	public static void setArguments(ILaunchConfigurationWorkingCopy config, String args) {
		config.setAttribute(ATTRIBUTE_ARGUMENTS, args);
	}
	
	/**
	 * Get the module paths to run the script with.
	 */
	public static File[] getModulePaths(ILaunchConfiguration config) {
		try {
			String str = config.getAttribute(ATTRIBUTE_MODULE_PATHS, "");
			return FSUtil.fromFSPathArray(str);
		} catch (CoreException e) {
			// Ignore
		}
		
		return null;
	}
	
	/**
	 * Set the module paths to run the script with.
	 */
	public static void setModulePaths(ILaunchConfigurationWorkingCopy config, File[] modulePaths) {
		if (modulePaths == null) {
			config.removeAttribute(ATTRIBUTE_MODULE_PATHS);
		} else {
			config.setAttribute(ATTRIBUTE_MODULE_PATHS, FSUtil.toAbsoluteFSPathArray(modulePaths));
		}
	}
	
	/**
	 * Get whether to append to the project-level module paths.
	 */
	public static boolean isAppendProjectLevelModulePaths(ILaunchConfiguration config) {
		try {
			return config.getAttribute(ATTRIBUTE_APPEND_PROJECT_MODULE_PATHS, true);
		} catch (CoreException e) {
			// Ignore
		}
		
		return true;
	}
	
	/**
	 * Set whether to append to the project-level module paths.
	 */
	public static void setAppendProjectLevelModulePaths(ILaunchConfigurationWorkingCopy config, boolean shouldAppend) {
		config.setAttribute(ATTRIBUTE_APPEND_PROJECT_MODULE_PATHS, shouldAppend);
	}
}
