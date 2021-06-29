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

import info.julang.execution.threading.ThreadRuntime;
import info.julang.execution.threading.ThreadRuntimeHelper;
import info.julang.memory.value.HostedValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.ObjectValue;
import info.julang.parser.LazyAstInfo;
import info.julang.typesystem.jclass.jufc.System.Reflection.ScriptScript;

/**
 * The information about a script info, which is to be stored in a {@link ModuleInfo}.
 * 
 * @author Ming Zhou
 */
public class ScriptInfo {

	private String fullPath;
	
	private List<RequirementInfo> reqs;

	private List<IncludedFile> incs;
	
	private LazyAstInfo ainfo;
	
	private ModuleInfo modInfo;
	
	public ScriptInfo(
		ModuleInfo modInfo,
		String fullPath,
		List<RequirementInfo> reqs,
		LazyAstInfo ainfo,
		List<IncludedFile> includes) {
		
		this.modInfo = modInfo;
		this.fullPath = fullPath;
		this.reqs = reqs;
		this.ainfo = ainfo;
		
		if (includes != null) {
		    int size = includes.size();
		    if (size > 0) {
				incs = new ArrayList<IncludedFile>(size);
				Set<String> set = new HashSet<>(size);
				
				for (IncludedFile inf : includes) {
					String path = inf.getFullPath();
					if (!set.contains(path)) {
						set.add(path);
						incs.add(inf);
					}
				}
		    }
		}
	}

	public ModuleInfo getModuleInfo() {
		return modInfo;
	}
	
	public String getFullPath() {
		return fullPath;
	}

	public List<RequirementInfo> getRequirements() {
		return reqs;
	}
	
	public LazyAstInfo getAstInfo() {
		return ainfo;
	}
	
	/**
	 * Distinct script files included via include statement, 
	 * in then order they appear the the top of this script file.
	 * 
	 * @return Null if there isn't any.
	 */
	public List<IncludedFile> getIncludedFiles() {
		return incs;
	}
	
	void setRequirements(List<RequirementInfo> reqs) {
		this.reqs = reqs;
	}
	
	//--------------------- storage of System.Reflection.Script instance ---------------------//

	private ObjectValue scriptObject;
	
	/**
	 * Get <code style="color:green">System.Reflection.Script</code> object for this type. 
	 * This object will be created the first time this method is called.
	 * 
	 * @param runtime
	 */
	public ObjectValue getScriptScriptObject(ThreadRuntime runtime) {
		if (scriptObject == null) {
			synchronized(ScriptScript.class){
				if (scriptObject == null) {
					scriptObject = ThreadRuntimeHelper.instantiateSystemType(
						runtime, ScriptScript.FQCLASSNAME, new JValue[0]);
					HostedValue hv = (HostedValue)scriptObject;
					ScriptScript ss = (ScriptScript)hv.getHostedObject();
					ss.setScriptInfo(this);
				}
			}
		}
		
		return scriptObject;
	}
}
