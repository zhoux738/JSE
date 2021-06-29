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

import info.julang.execution.Argument;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.external.exceptions.JSEError;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.internal.NewObjExecutor;
import info.julang.interpretation.syntax.ParsedTypeName;
import info.julang.memory.value.HostedValue;
import info.julang.memory.value.ObjectValue;
import info.julang.memory.value.TypeValue;
import info.julang.modulesystem.prescanning.IRawScriptInfo;
import info.julang.modulesystem.prescanning.RawClassInfo;
import info.julang.parser.LazyAstInfo;
import info.julang.typesystem.jclass.JClassConstructorMember;
import info.julang.typesystem.jclass.JClassType;
import info.julang.typesystem.jclass.jufc.System.Reflection.ScriptModule;

/**
 * This class contains all the known info about a module, including its name, the script files belonging to
 * this module, it requirements, and all the classed defined in the module.
 * 
 * @author Ming Zhou
 */
public class ModuleInfo {
	
	class DuplicateClassInfoException extends Exception {

		private static final long serialVersionUID = -8865975483359618636L;
		
		ClassInfo oldClass;
		
		ClassInfo newClass;
		
		private DuplicateClassInfoException(ClassInfo oldClass, ClassInfo newClass) {
			this.oldClass = oldClass;
			this.newClass = newClass;
		}
	}

	public static final String DEFAULT_MODULE_NAME = "<default>";
	
	public static final String IMPLICIT_MODULE_NAME = "<implicit>";
	
	protected List<ScriptInfo> scripts;
	
	protected List<ClassInfo> classes;
	
	protected List<ModuleInfo> requirements;
	
	private String name;
	
	protected ModuleInfo(String name){
		this.name = name;
		requirements = new ArrayList<ModuleInfo>();
	}
	
	public String getName(){
		return name;
	}

	public ScriptInfo getFirstScript() {
		return scripts.get(0);
	}
	
	public List<ScriptInfo> getScripts() {
		return scripts;
	}

	public List<ClassInfo> getClasses() {
		return classes;
	}

	public List<ModuleInfo> getRequirements() {
		return requirements;
	}
	
	/**
	 * Add from the given module info. If a type already exist in the current one, throw.
	 * <p>
	 * Only used when loading a loose script to be the default module, merging with the existing default module.
	 * 
	 * @param src the module info to be added from.
	 */
	void addFrom(ModuleInfo src) throws DuplicateClassInfoException {
		combineFrom(src, false);
	}
	
	/**
	 * Merge from the given module info. The current one should take precedence.
	 * <p>
	 * Only used when loading a loose script to be the default module, merging with the existing default module.
	 * 
	 * @param src the module info to be merged from, but will not overwrite the
	 * current one whenever there is a conflict.
	 */
	void mergeFrom(ModuleInfo src) {
		try {
			combineFrom(src, true);
		} catch (DuplicateClassInfoException e) {
			// Won't happen.
			throw new JSEError("Thrown DuplicateClassInfoException while it shouldn't.");
		}
	}
	
	private void combineFrom(ModuleInfo src, boolean merge) throws DuplicateClassInfoException {
		// (No need to merge scripts info - we only store one script for the default module)
		// this.scripts.addAll(src.scripts);
		
		this.classes = mergeLists(src.classes, this.classes, merge);
		this.requirements = mergeLists(src.requirements, this.requirements, true);
		
		// NOTE: we don't set new requirement list to each ScriptInfo instance. 
		// This means classed defined in a previous line won't get bind to new
		// namespace, if any is introduced later. As of 0.2.0, this is by design.
		//
		// However, we must reset requirement info for the very 1st ScriptInfo,
		// which is used to populate namespace ahead of global script execution.
		//
		// Note this will overwrite the requirements of the previous first script.
		// This so far is not very much a concern since the requirements are only
		// used once when the script was executed. And that has already happened.
		if (merge) {
			ScriptInfo currScript = this.getFirstScript();
			currScript.setRequirements(
				mergeLists(src.getFirstScript().getRequirements(), currScript.getRequirements(), true));
		}
	}
	
	/**
	 * Merge two lists. <code>src</code> list would overwrite any element in the <code>dst</code> list if
	 * it equals() to the element.
	 */
	private <T> List<T> mergeLists(List<T> dst, List<T> src, boolean ignoreConflict) throws DuplicateClassInfoException {
		Set<T> set = new HashSet<T>();
		set.addAll(src);
		
		if (ignoreConflict) {
			set.addAll(dst); // When adding an element from dst, if the element already exists in set, no-op. 
		} else {
			for (T t : dst) {
				if (set.contains(t)) {
					for (T s : src) {
						if (s.equals(t)) {
							throw new DuplicateClassInfoException((ClassInfo)s, (ClassInfo)t);
						}
					}
				} else {
					set.add(t);
				}
			}
		}
		
		
		List<T> list = new ArrayList<T>();
		list.addAll(set);
		return list;
	}

	static class MutableModuleInfo extends ModuleInfo {

		MutableModuleInfo(String name) {
			super(name);
		}
		
		private List<String> requiredModuleNames;
		
		List<String> getRequiredModuleNames() {
			return requiredModuleNames;
		}
		
		void addRequiredModule(ModuleInfo info){
			requirements.add(info);
		}

		public void replaceRequirements(List<ModuleInfo> miListNew) {
			requirements = miListNew;
		}
	}
	
	public static class Builder {
		
		private MutableModuleInfo info;
		
		public Builder(String name){
			info = new MutableModuleInfo(name);
		}
		
		public MutableModuleInfo build(){
			return info;
		}
		
		public void addScript(IRawScriptInfo script){
			LazyAstInfo ainfo = script.getAstInfo();
			
			// Script file info
			if(info.scripts == null){
				info.scripts = new ArrayList<ScriptInfo>();
			}
			
			ScriptInfo scriptFile = new ScriptInfo(
				info,
				script.getScriptFilePath(), 
				script.getRequirements(),
				ainfo,
				script.getInclusions());
			
			info.scripts.add(scriptFile);
			
			// Requirements
			if(info.requiredModuleNames == null){
				info.requiredModuleNames = new ArrayList<String>();
			}
			for(RequirementInfo reqMod : script.getRequirements()){ // guaranteed to be not duplicated
				info.requiredModuleNames.add(reqMod.getName());
			}
			
			// Classes
			if(info.classes == null){
				info.classes = new ArrayList<ClassInfo>();
			}
			for(RawClassInfo rci : script.getClasses()){ // guaranteed to be not duplicated
				info.classes.add(
					new ClassInfo(
						info.getName() + "." + rci.getName(), 
						rci.getName(), 
						rci.getDeclInfo(),
						scriptFile));
			}
		}
	}
	
	//---------------------------------- Object ----------------------------------//

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		ModuleInfo other = (ModuleInfo) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	
	@Override
	public String toString(){
		return name;
	}
    
    private ObjectValue modObject;
    
    /**
     * Reset script info for this module.
     * <p>
     * Used exclusively by the mutable default module.
     * 
     * @param csi The current script info.
     * @param psi The previous script info.
     */
    void resetScriptInfo(ScriptInfo csi, ScriptInfo psi) {
		// Note that module info and script info are cross-referencing each other,
		// but we can only update them one by one. First create a new script info
		// using the existing module info, then update the script info in that module.
		List<ScriptInfo> sis = new ArrayList<>(1);
		ScriptInfo nsi = new ScriptInfo(
			csi.getModuleInfo(), 	// ModuleInfo from the current script
			psi.getFullPath(),		// File path from the previous script
			csi.getRequirements(),	// Requirements from the current script
			psi.getAstInfo(),		// AST from the previous script
			psi.getIncludedFiles());// Inclusions from the previous script (not effectively needed anymore)
		sis.add(nsi);
		
		scripts = sis;
    	modObject = null;
    }
    
    /**
     * Get <code style="color:green">System.Reflection.Module</code> object for this module. 
     * This object will be created the first time this method is called.
     * 
     * @param runtime
     */
    public ObjectValue getOrCreateScriptObject(ThreadRuntime runtime) {
        if (modObject == null) {
            synchronized(TypeValue.class){
                if (modObject == null) {
                    JClassType modClassType = (JClassType) runtime.getTypeTable().getType(ScriptModule.FQCLASSNAME);
                    if (modClassType == null) {
                        Context context = Context.createSystemLoadingContext(runtime);
                        runtime.getTypeResolver().resolveType(context, ParsedTypeName.makeFromFullName(ScriptModule.FQCLASSNAME), true);
                        modClassType = (JClassType) runtime.getTypeTable().getType(ScriptModule.FQCLASSNAME);
                    }
                    
                    JClassConstructorMember modClassCtor = modClassType.getClassConstructors()[0];
                    
                    NewObjExecutor noe = new NewObjExecutor(runtime);
                    ObjectValue ov = noe.newObjectInternal(modClassType, modClassCtor, new Argument[0]);
                    
                    ScriptModule st = new ScriptModule();
                    st.setModule(this);
                    HostedValue hv = (HostedValue)ov;
                    hv.setHostedObject(st);
                    
                    modObject = ov;
                }
            }
        }
        
        return modObject;
    }
}
