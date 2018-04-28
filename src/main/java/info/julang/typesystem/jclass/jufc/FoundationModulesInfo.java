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

package info.julang.typesystem.jclass.jufc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class FoundationModulesInfo {

	private static boolean initialized = false;
	
	/**
	 * A map between module name and a list of Julian script files contained in this module.
	 */
	private static HashMap<String, String[]> allModuleClasses;
	
	// Add all embedded .jul files here.
	private synchronized static void initialize(){
		if(initialized){
			return;
		}
		
		allModuleClasses = new HashMap<String, String[]>();
		allModuleClasses.put("System", new String[]{
			// Exceptions
			"Exception",
			// Hosted method attributes
			"HOLI",
			"PlatformObject",
			"Console",
			// Annotation
			"Annotation",
			// Utilities
			"Time",
			"Process"
		});
		allModuleClasses.put("System.IO", new String[]{
			// File System
			"Item",
			"File",
			"Directory",
			// Stream API
			"Stream",
			"FileStream",
			// Exceptions
			"Exception"
		});
		allModuleClasses.put("System.Lang", new String[]{
			// Exceptions
			"Exception"
		});
		allModuleClasses.put("System.Util", new String[]{
			"Interfaces",
			"Math"
		});
		allModuleClasses.put("System.Collection", new String[]{
			// Containers
			"Container",
			"List",
			"Map",
			"Queue",
			// Exceptions
			"Exception"
		});
		allModuleClasses.put("System.Concurrency", new String[]{
			"Thread",
			"Lock",
			"Promise"
		});
		
		initialized = true;
	}
	
	/**
	 * For a given module name, tell if it is a Julian Foundation Module. 
	 * <p/>
	 * If a module is a Julian Foundation Module, it means: 
	 * (1) this module is contained in the Julian runtime as part of deliverable; 
	 * (2) its namespace is reserved and cannot be used by other parties.
	 * 
	 * @param modName
	 * @return
	 */
	public static boolean isFoundationModule(String modName){
		initialize();
		return allModuleClasses.containsKey(modName);
	}
	
	/**
	 * For a given module name, return the paths of all the constituting scripts.
	 * <p/>
	 * The path contains module segment. For example, if modName = "System", the 
	 * return path will be started with "System/".
	 * 
	 * @param modName
	 * @return
	 */
	public static List<String> getScriptPathsByModule(String modName){
		initialize();
		String[] classes = allModuleClasses.get(modName);
		modName = modName.replace('.', '/');
		int len = classes.length;
		List<String> ret = new ArrayList<String>(len);
		for(int i=0;i<len;i++){
			ret.add(modName + "/" + classes[i] + ".jul");
		}
		return ret;
	}
}
