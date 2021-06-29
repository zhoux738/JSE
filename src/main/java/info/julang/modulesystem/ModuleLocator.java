/*
MIT License

Copyright (c) 2017 Ming Zhou

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

package info.julang.modulesystem;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

import info.julang.external.interfaces.IExtModuleManager;
import info.julang.modulesystem.naming.FQName;
import info.julang.modulesystem.naming.NFQName;
import info.julang.modulesystem.naming.QName;

/**
 * The module locator is a utility class for locating module files from given paths. We take a minimalist 
 * approach to design this class: it is basically language agnostic.
 * <p>
 * Generally, when the {@link ModuleManager} is asked to load a module, it will resort to this class to locate 
 * all the script files which belong to that module. The locator scans the paths set by 
 * {@link ModuleLocator#addModulePath(String)} to find those script files. A script file is considered part 
 * of the module A.B.C if the following criteria are met:
 * <ul>
 * <li>The file is found under A/B/C where folder A is located <i>directly</i> under any of module path.</li>
 * <li>And the file has module declaration at its very first line, in form of "<code><b>module</b> A.B.C;</code>"</li>
 * </ul>
 * However, this class won't handle the check for the 2nd criterion, as per our minimalist design principle.
 * <p>
 * 
 * @author Ming Zhou
 */
public class ModuleLocator {
	
	private List<String> paths = new ArrayList<String>();
	
	/**
	 * Add a new path to module path repository.
	 * 
	 * @param path
	 */
	public void addModulePath(String path) {
		paths.add(path);
	}
	
	/**
	 * Clear all paths from registry.
	 * 
	 * @param path
	 */
	public void clearModulePath() {
		paths.clear();
	}
	
	/**
	 * For a given module name, find all the script files from the repository 
	 * which meet the conditions (See {@link ModuleLocator here}).
	 * 
	 * @param moduleName
	 * @return
	 */
	public ModuleLocationInfo findModuleFiles(String moduleName){
		ModuleLocationInfo info = new ModuleLocationInfo(moduleName);
		
		if(info.isEmbedded()){
			return info;
		}
		
		for(int i = 0; i < paths.size(); i++){
			String path = paths.get(i);
			boolean isFromDefault =
				i == 0 && path.endsWith(File.separator + IExtModuleManager.DefaultModuleDirectoryName);
			findModuleFilesFromPath(path, info, isFromDefault);
		}
		
		if (!info.isFound()){
			info.setSearchedModulePaths(paths);
		}
		
		return info;
	}

	private void findModuleFilesFromPath(String spath, ModuleLocationInfo info, boolean isFromDefaultModulePath) {
		// Criteria I: 
		// The file is found under A/B/C where folder A is located directly under any of module path.
		
		// First ignore illegal repository path
		if(!checkDirectory(spath)){
			return;
		}
		
		String name = info.getName();
		QName qname = new FQName(name);
		
		while(qname != NFQName.END){
			spath = spath + "/" + qname.getTopLevel();
			qname = qname.getSubQName();
		}
		
		// If there is not a directory whose structure matches the given module name, abort.
		if(!checkDirectory(spath)){
			return;
		}
		
		// Otherwise, we've found the module directory. Let's add files into module info.
		File file = new File(spath);
		File[] files = file.listFiles(new FileFilter(){
			@Override
			public boolean accept(File file) {
				if(!file.getName().endsWith(".jul")){
					return false;
				}
				
				return true;
			}
		});
		
		for(File f : files){
			// Criteria II: 
			// The file has module declaration at its very first line, 
			// in the form of "module;" (default module path only) or "module A.B.C;"
			//
			// (Defer this check to caller's end)
			
			info.addScriptFile(f.getAbsolutePath(), isFromDefaultModulePath);
		}
	}
	
	private boolean checkDirectory(String spath){
		File file = new File(spath);
		
		if(!file.exists() || !file.isDirectory()){
			return false;
		}
		
		return true;
	}

}
