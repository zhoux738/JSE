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

package info.julang.eng.mvnplugin.docgen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import info.julang.eng.mvnplugin.ScriptInfoBag;
import info.julang.eng.mvnplugin.docgen.DocModel.PrimitiveType;
import info.julang.eng.mvnplugin.docgen.DocModel.Type;
import info.julang.execution.namespace.NamespacePool;
import info.julang.interpretation.syntax.ClassSubtype;
import info.julang.interpretation.syntax.ParsedTypeName;
import info.julang.modulesystem.prescanning.RawClassInfo;
import info.julang.modulesystem.prescanning.RawScriptInfo;
import info.julang.typesystem.JType;
import info.julang.typesystem.jclass.builtin.JStringType;
import info.julang.util.OneOrMoreList;

/**
 * An environmental object that contains information about all the System modules, types contained within, etc.
 * <p>
 * This class serves as an alternative combination of module manager, type resolver and other context objects
 * that would be used during real engine execution. In probably far future, when we start supporting static 
 * analysis for the language (which would be required by any decent IDE implementation), we may want to use a 
 * more evolved language service in place of this.
 * 
 * @author Ming Zhou
 */
public class ModuleContext {

	public final static String MD_VERSION = "jse.version";
	public static final String OFFICIAL_WEBSITE = "official.website";
	
	public static interface TopLevelDocProcessor {

		/**
		 * Process a given top-level doc model.
		 * @param key Fully qualified name of this type. For primitive types language alias is used.
		 * @param doc The doc model.
		 */
		void process(String key, DocModel.Documented doc);
		
	}
	
	private Map<String, DocumentedClassInfo> typesByFN;
	private Map<String, DocModel.Type> builtInTyps;
	private Map<String, String> metadata;
	private List<DocModel.Script> scripts;
	
	ModuleContext(){
		typesByFN = new HashMap<String, DocumentedClassInfo>();
		builtInTyps = new HashMap<String, DocModel.Type>();
		metadata = new HashMap<String, String>();
		scripts = new ArrayList<DocModel.Script>();
	}
	
	/**
	 * Add types defined in a script to context.
	 * 
	 * @param script
	 */
	void addTypesFromScript(ScriptInfoBag sib){
		RawScriptInfo rsi = sib.getRawScriptInfo();
		String modName = rsi.getModuleName();
		
		List<RawClassInfo> cinfos = rsi.getClasses();
		for (RawClassInfo cinfo : cinfos) {
			String name = cinfo.getName();
			String fname = modName + "." + name;
			typesByFN.put(fname, new DocumentedClassInfo(modName, cinfo, null));
		}
	}
	
	/**
	 * Iterate over all the types and call back processor.
	 * 
	 * @param proc
	 * @param pat if null, no filter will be applied
	 */
	public void foreach(TopLevelDocProcessor proc, Pattern pat){
		for (Entry<String, Type> entry: builtInTyps.entrySet()){
			String fname = entry.getKey();
			if (pat == null || pat.matcher(fname).matches()){
				proc.process(fname, entry.getValue());
			}
		}
		
		for (Entry<String, DocumentedClassInfo> entry: typesByFN.entrySet()){
			String fname = entry.getKey();
			if (pat == null || pat.matcher(fname).matches()){
				proc.process(fname, entry.getValue().getDoc());
			}
		}
		
		for (DocModel.Script script : scripts) {
			String fname = script.name;
			if (pat == null || pat.matcher(fname).matches()){
				proc.process(fname, script);
			}
		}
	}
	
	/**
	 * Set the given type documentation to the corresponding type.
	 */
	void setDoc(String modName, DocModel.Type doc){
		String fname = modName + "." + doc.name;
		DocumentedClassInfo dci = typesByFN.get(fname);
		if (dci != null) {
			dci.setDoc(doc);
		} else {
			throw new DocGenException("The type " + fname + " cannot be resolved.");
		}
	}
	
	/**
	 * Set the given type.
	 * 
	 * @param doc must be a built-in type.
	 */
	void setBuildInDoc(DocModel.Type doc){
		String fname = doc.name;
		if (!doc.isBuiltIn()){
			throw new DocGenException(
				"The type \'" + fname + 
				"\' is not a built-in type and thus cannot be added as built-in type into module context.");
		}
		
		String key = fname.toLowerCase();
		builtInTyps.put(key, doc);
		
		if (doc instanceof PrimitiveType){
			PrimitiveType pt = (PrimitiveType)doc;
			String key2 = pt.alias;
			if (!key2.equals(key)) {
				builtInTyps.put(key2, doc);
			}
		}
	}
	
	/**
	 * Set doc models for built-in scripts.
	 * 
	 * @param scripts
	 */
	void setScriptDoc(List<DocModel.Script> scripts) {
		this.scripts = scripts;
	}
	
	/**
	 * Get documentation for the given type reference. Returns null if the doc for this type has not been set.
	 */
	DocModel.Type getDoc(DocModel.TypeRef tref){
		String fname = tref.getFullName();
		DocumentedClassInfo dci = typesByFN.get(fname);	
		if (dci != null) {
			return dci.getDoc();
		} else {
			throw new DocGenException("The type " + fname + " cannot be resolved.");
		}
	}
	
	/**
	 * Try to get a built-in script matching the given name.
	 * 
	 * @param name
	 * @return null if not found.
	 */
	public ScriptInfo getScriptInfo(String name) {
		for (DocModel.Script s : this.scripts) {
			if (name.equals(s.name)) {
				return new ScriptInfo(s);
			}
		}
		
		return null;
	}
	
	/**
	 * For a simple name against a given namespace pool, try to get the full type info 
	 * 
	 * @param simpleName
	 * @param ns
	 * @return null if not found.
	 */
	public TypeInfo getTypeInfo(String simpleName, int dimension, NamespacePool ns){
		// 1) check built-in
		switch(simpleName){
		// primitive
		case "int": 
		case "byte": 
		case "float": 
		case "bool": 
		case "char": 
		case "void":
			return new TypeInfo("", simpleName, dimension, null, builtInTyps.get(simpleName), null);
		// compound
		case "Function": 
		case "Object":
		case "Array": 
		case "Enum":
		case "Attribute":
		case "Any":
		case "Dynamic":
		case "string": 
			return new TypeInfo("", simpleName, dimension, ClassSubtype.CLASS, builtInTyps.get(simpleName.toLowerCase()), null);
		// aliases
		case "String":
		case "Byte":
		case "Float":
		case "Bool":
		case "Char":
			return getTypeInfo(simpleName.toLowerCase(), dimension, ns);
		case "Integer":
			return getTypeInfo("int", dimension, ns);
		case "var":
		case "any":
			return getTypeInfo("Any", dimension, ns);
		}
		
		// 2) check defined
		OneOrMoreList<String> names;
		if (ns == null) {
			names = new OneOrMoreList<String>(simpleName);
		} else {
			names = ns.getAllPossibleFullNames(ParsedTypeName.makeFromFullName(simpleName));
		}
		
		for(String name : names){
			DocumentedClassInfo rci = typesByFN.get(name);
			if(rci != null){
				ClassSubtype subt = rci.getDeclInfo().getSubtype();
				int index = name.lastIndexOf('.');
				String modName = name.substring(0, index);
				return new TypeInfo(modName, simpleName, dimension, subt, rci.getDoc(), rci.getDeclInfo());
			}
		}
		
		return null;
	}

	/**
	 * For a parsed type name against a given namespace pool, try to get the full type info 
	 * 
	 * @param ptn
	 * @param ns
	 * @return null if not found.
	 */
	TypeInfo getTypeInfo(ParsedTypeName ptn, NamespacePool ns) {
		String tname = null;
		JType bt = ptn.getBasicType();
		if (bt != null){
			switch(bt.getKind()){
			case BOOLEAN:
				tname = "bool";
				break;
			case BYTE:
				tname = "byte";
				break;
			case CHAR:
				tname = "char";
				break;
			case FLOAT:
				tname = "float";
				break;
			case INTEGER:
				tname = "int";
				break;
			case VOID:
				tname = "void";
				break;
			case CLASS:
				if (bt == JStringType.getInstance()){
					tname = "string";
				}
				break;
			case ANY:
				tname = "any";
				break;
			default:
				throw new DocGenException("Do not recognize " + bt.getName());	
			}
		} else if (ptn == ParsedTypeName.ANY) {
			tname = "any";
		} else {
			tname = ptn.getFQNameInString();
		}
		
		return getTypeInfo(tname, ptn.getDimensionNumber(), ns);
	}

	public void addMetatdata(String key, String data) {
		metadata.put(key, data);
	}
	
	public String getMetatdata(String key) {
		return metadata.get(key);
	}	
}