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

package info.julang.execution.namespace;

import info.julang.interpretation.syntax.ParsedTypeName;
import info.julang.modulesystem.RequirementInfo;
import info.julang.modulesystem.ScriptInfo;
import info.julang.modulesystem.naming.FQName;
import info.julang.util.OneOrMoreList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A namespace pool is a collection of namespaces which can be referred to by the current code.
 * <p/>
 * The namespace pool is associated with execution context. More specifically there are two cases:
 * <p/>
 * (1) The global context. The global script, and code in global function are in global context. Its 
 * namespaces are contributed by import statements appearing at the very start of scripts.
 * <p/>
 * (2) The method context. The code in a method make references to class context. Its namespaces are
 * contributed by the import statement appearing at the very start of the module script that contains
 * the definition of this class.
 * 
 * @author Ming Zhou
 */
public class NamespacePool {

	private List<String> namespaces;
	
	private Map<String, String> aliases;
	
	private Map<String, OneOrMoreList<String>> bindings;
	
	public NamespacePool(){
		namespaces = new ArrayList<String>();
		aliases = new HashMap<String, String>();
		bindings = new HashMap<String, OneOrMoreList<String>>();
	}
	
	public void addNamespace(String ns){
		namespaces.add(ns);
	}
	
	public List<String> getNamespaces(){
		return namespaces;
	}
	
	public void addNamespaceFromRequirementInfo(RequirementInfo req){
		namespaces.add(req.getName());
		String alias = req.getAlias();
		if(alias != null && !alias.isEmpty()){
			aliases.put(alias, req.getName());
		}
	}
	
	public void addNamespaceFromScriptInfo(ScriptInfo script){
		addNamespace(script.getModuleInfo().getName());
		List<RequirementInfo> reqs = script.getRequirements();
		for(RequirementInfo req : reqs){
			addNamespaceFromRequirementInfo(req);
		}
	}
	
	/**
	 * Bind a simple name with a full name. This is called during interpretation when a name match is determined.
	 * 
	 * @param name
	 * @param fullName
	 */
	public void addBinding(String name, String fullName){
		bindings.put(name, new OneOrMoreList<String>(fullName));
	}
	
	/**
	 * For a given namespace alias and simple name, return a full name.
	 * 
	 * @param alias
	 * @param name
	 * @return
	 */
	public String getFullNameByAlias(String alias, String name){
		String ns = aliases.get(alias);
		if(ns != null){
			return concatName(ns, name);
		} else {
			return null;
		}
	}
	
	/**
	 * For a given simple name, return a list of all the possible full names.
	 * 
	 * @param name
	 * @return
	 */
	public OneOrMoreList<String> getAllPossibleFullNames(ParsedTypeName tname){
		FQName qname = tname.getFQName();
		String name = qname.toString();
		OneOrMoreList<String> result = bindings.get(name);
		if(result!=null){
			return result;
		} else {
			int sections = tname.getSections();
			if(sections == 2){
				// If there are two sections, it is very likely led by an alias 
				String dealiasedName = getFullNameByAlias(qname.getTopLevel(), qname.getSubQName().toString());
				if(dealiasedName!=null){
					addBinding(name, dealiasedName);
					return new OneOrMoreList<String>(dealiasedName);
				}
			} else if(sections > 2){
				// If there are three or more sections, it must be a full name (as long as we don't support inner class).
				return new OneOrMoreList<String>(name);
			}
			return new OneOrMoreList<String>(makeFullNames(name));
		}
	}

	private List<String> makeFullNames(String name) {
		List<String> names = new ArrayList<String>();

		// try with all the imported namespaces.
		for(String ns : namespaces){
			names.add(concatName(ns, name));
		}
		// and don't exclude the possibility that it is full name already.
		names.add(name);
		
		return names;
	}
	
	private String concatName(String s1, String s2){
		return s1 + "." + s2;
	}
}
