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

package info.julang.modulesystem.prescanning;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import info.julang.external.exceptions.JSEError;
import info.julang.interpretation.BadSyntaxException;
import info.julang.interpretation.syntax.ClassDeclInfo;
import info.julang.interpretation.syntax.SyntaxHelper;
import info.julang.langspec.ast.JulianParser.DeclarationsContext;
import info.julang.langspec.ast.JulianParser.ProgramContext;
import info.julang.langspec.ast.JulianParser.Type_declarationContext;
import info.julang.modulesystem.RequirementInfo;
import info.julang.parser.ANTLRParser;
import info.julang.parser.AstInfo;
import info.julang.parser.LazyAstInfo;
import info.julang.typesystem.jclass.jufc.FoundationClassParser;

/**
 * Contains the most preliminary info about a script: the AST parsed from this script, the classes 
 * defined inside, its dependencies, etc.
 * <p/>
 * An instance of this class is created and populated when the file is loaded into the runtime.  
 * 
 * @author Ming Zhou
 */
public class RawScriptInfo implements IRawScriptInfo {

	public class Option {
		
		private boolean allowNoModule;
		
		private boolean allowNameInconsistency;
		
		private boolean allowSystemModule;

		public boolean isAllowNoModule() {
			return allowNoModule;
		}

		public void setAllowNoModule(boolean allowNoModule) {
			this.allowNoModule = allowNoModule;
		}

		public boolean isAllowNameInconsistency() {
			return allowNameInconsistency;
		}

		public void setAllowNameInconsistency(boolean allowNameInconsistency) {
			this.allowNameInconsistency = allowNameInconsistency;
		}

		public boolean isAllowSystemModule() {
			return allowSystemModule;
		}

		public void setAllowSystemModule(boolean allowSystemModule) {
			this.allowSystemModule = allowSystemModule;
		}
		
	}
	
	private String moduleName;
	
	private String filePath;
	
	private List<RequirementInfo> requirements;
	
	private Set<String> requirementsSet;
	
	private List<RawClassInfo> classes;

	private Set<String> classesSet;
	
	private Set<String> allClassesSet;
	
	protected LazyAstInfo ainfo;
	
	private Option option;
	
	private boolean embedded;
	
	public RawScriptInfo(String moduleName, boolean isEmbedded){
		this.moduleName = moduleName;
		allClassesSet = new HashSet<String>();
		option = new Option();
		embedded = isEmbedded;
		option.setAllowSystemModule(embedded);
	}
	
	// Used only by AOT scripts
	protected RawScriptInfo(){
		
	}

	//------------------ IRawScriptInfo ------------------//
	
	public String getModuleName() {
		return moduleName;
	}

	public List<RequirementInfo> getRequirements() {
		return requirements;
	}

	public List<RawClassInfo> getClasses() {
		return classes;
	}
	
	public LazyAstInfo getAstInfo(){
		return ainfo;
	}

	public String getScriptFilePath() {
		return filePath;
	}

	//----------------------------------------------------//
	
	/**
	 * Add a new requirement.
	 * 
	 * @param reqInfo
	 * @return false if it already has the one.
	 */
	public boolean addRequirement(RequirementInfo reqInfo) {
		String name = reqInfo.getName();
		if(requirementsSet.contains(name)){
			return false;
		} else {
			requirementsSet.add(name);
			requirements.add(reqInfo);
			return true;
		}
	}
	
	/**
	 * Add a new class.
	 * 
	 * @param classInfo
	 * @return false if a class with same has been added.
	 */
	public boolean addClass(RawClassInfo classInfo){
		String name = classInfo.getName();
		if(allClassesSet.contains(name) || classesSet.contains(name)){
			return false;
		} else {
			classesSet.add(name);
			classes.add(classInfo);
			return true;
		}
	}
	
	/**
	 * Clear requirements, re-create token stream; memorize all the detected classes.
	 * 
	 * @param filePath
	 */
	public void initialize(String filePath) throws FileNotFoundException {
		if (embedded){
			FoundationClassParser p = new FoundationClassParser(filePath);
			ainfo = p.scan(true);
		} else {
			FileInputStream fis = new FileInputStream(filePath);
			ANTLRParser p = new ANTLRParser(filePath, fis, true);
			ainfo = p.scan(false);
		}
		
		reset(filePath, ainfo);
	}
	
	public void reset(String filePath, LazyAstInfo ainfo){
		this.filePath = filePath;
		requirements = new ArrayList<RequirementInfo>();
		requirementsSet = new HashSet<String>();
		
		if(classesSet!=null){
			allClassesSet.addAll(classesSet);
		}
		classes = new ArrayList<RawClassInfo>();
		classesSet = new HashSet<String>();
		
		this.ainfo = ainfo;
		
		addRequirement(new RequirementInfo("System", null));
	}

	public Option getOption() {
		return option;
	}

	public void setModuleName(String name) {
		this.moduleName = name;
	}
	
	@Override
	public String toString(){
		return this.filePath;
	}
	
	private Map<String, ClassDeclInfo> types;
	
	/**
	 * Load all types defined in this script. All the types will be cached.
	 * <p>
	 * This method is meant to be used only by {@link LazyClassDeclInfo}. The method is not
	 * thread-safe, but since TypeLoader will load types in in synchronized way, we are good
	 * here to not guard with a semaphore.
	 */
	Map<String, ClassDeclInfo> loadAllTypes(){
		if (types == null) {
			BadSyntaxException bse = ainfo.getBadSyntaxException();
			if (bse != null){
				throw bse;
			}
			
			types = new HashMap<String, ClassDeclInfo>();
			ProgramContext pc = ainfo.getAST();
			DeclarationsContext dc = pc.declarations();
			for(Type_declarationContext tdc : dc.type_declaration()){
				AstInfo<Type_declarationContext> ast = ainfo.create(tdc);
				ClassDeclInfo cdl = SyntaxHelper.parseClassDeclaration(ast, moduleName);
				types.put(cdl.getName(), cdl); // It's sufficient to differentiate types using simple name found in a single script file.
			}
		}
		
		return types;
	}
}
