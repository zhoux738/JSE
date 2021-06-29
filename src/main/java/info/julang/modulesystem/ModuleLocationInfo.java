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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import info.julang.typesystem.jclass.jufc.FoundationModulesInfo;

/**
 * This class contains file path for each script contained in the specified module. Note since a module 
 * can be spread across multiple module paths, these script paths may not share the same root directory.
 * 
 * @author Ming Zhou
 */
public class ModuleLocationInfo {

	private String name;
	
	private List<String> filePaths; 
	
	private Set<String> filePathsFromCustomizedModulePaths; 
	
	private boolean embedded;
	
	private List<String> searchedModulePaths; 
	
	public ModuleLocationInfo(String name){
		this.name = name;
		this.filePaths = new ArrayList<String>();

		embedded = FoundationModulesInfo.isFoundationModule(name);
		if(embedded){
			filePaths = FoundationModulesInfo.getScriptPathsByModule(name);
		}
	}
	
	public String getName(){
		return name;
	}
	
	/**
	 * Add a file path. This will have no effect if the module is an embedded one.
	 * @param path
	 */
	void addScriptFile(String path, boolean isFromDefaultModulePath){
		if(embedded){
			return;
		}
		filePaths.add(path);
		
		if (!isFromDefaultModulePath) {
			if (filePathsFromCustomizedModulePaths == null) {
				filePathsFromCustomizedModulePaths = new HashSet<>();
			}
			
			filePathsFromCustomizedModulePaths.add(path);
		}
	}
	
	public boolean isFound(){
		return filePaths.size() > 0;
	}
	
	public boolean isFromCustomizedModulePath(String path) {
		return filePathsFromCustomizedModulePaths != null && filePathsFromCustomizedModulePaths.contains(path);
	}
	
	/**
	 * Get all the absolute script paths.
	 * 
	 * @return
	 */
	public List<String> getScriptPaths(){
		return filePaths;
	}
	
	public boolean isEmbedded(){
		return embedded;
	}
	
	public void setSearchedModulePaths(List<String> searchedModulePaths){
		this.searchedModulePaths = searchedModulePaths;
	}
	
	public List<String> getSearchedModulePaths(){
		return this.searchedModulePaths;
	}
}