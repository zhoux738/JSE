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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.velocity.VelocityContext;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import info.julang.eng.mvnplugin.GlobalLogger;
import info.julang.eng.mvnplugin.ScriptInfoBag;
import info.julang.eng.mvnplugin.SystemModuleProcessor;
import info.julang.eng.mvnplugin.docgen.DocModel.Constructor;
import info.julang.eng.mvnplugin.docgen.DocModel.EnumEntry;
import info.julang.eng.mvnplugin.docgen.DocModel.Field;
import info.julang.eng.mvnplugin.docgen.DocModel.InterfaceType;
import info.julang.eng.mvnplugin.docgen.DocModel.Method;
import info.julang.eng.mvnplugin.docgen.DocModel.Type;
import info.julang.eng.mvnplugin.docgen.DocModel.TypeDescription;
import info.julang.eng.mvnplugin.docgen.DocModel.TypeRef;
import info.julang.eng.mvnplugin.docgen.ModuleContext.TypeDocProcessor;
import info.julang.eng.mvnplugin.htmlgen.ApiIndexModel;
import info.julang.eng.mvnplugin.htmlgen.MarkDown2HtmlConverter;
import info.julang.eng.mvnplugin.htmlgen.WebsiteGenerator;
import info.julang.eng.mvnplugin.htmlgen.WebsiteResources;
import info.julang.eng.mvnplugin.mdgen.MarkdownConverter;
import info.julang.eng.mvnplugin.mdgen.TutorialInfo;
import info.julang.eng.mvnplugin.mdgen.TutorialInfo.ChapterInfo;
import info.julang.eng.mvnplugin.mdgen.TutorialInfo.IChapterInfo;
import info.julang.execution.namespace.NamespacePool;
import info.julang.execution.simple.SimpleEngineRuntime;
import info.julang.execution.symboltable.TypeTable;
import info.julang.execution.symboltable.VariableTable;
import info.julang.interpretation.IllegalLiteralException;
import info.julang.interpretation.syntax.ClassDeclInfo;
import info.julang.interpretation.syntax.ClassSubtype;
import info.julang.interpretation.syntax.CtorDeclInfo;
import info.julang.interpretation.syntax.FieldDeclInfo;
import info.julang.interpretation.syntax.MemberDeclInfo;
import info.julang.interpretation.syntax.MethodDeclInfo;
import info.julang.interpretation.syntax.MethodDeclInfo.TypeAndName;
import info.julang.interpretation.syntax.ParsedTypeName;
import info.julang.interpretation.syntax.SyntaxHelper;
import info.julang.langspec.ast.JulianParser.Attribute_definitionContext;
import info.julang.langspec.ast.JulianParser.Class_definitionContext;
import info.julang.langspec.ast.JulianParser.Class_member_declarationContext;
import info.julang.langspec.ast.JulianParser.Enum_definitionContext;
import info.julang.langspec.ast.JulianParser.Enum_member_declaration_bodyContext;
import info.julang.langspec.ast.JulianParser.Enum_member_declaration_initializerContext;
import info.julang.langspec.ast.JulianParser.Enum_member_declarationsContext;
import info.julang.langspec.ast.JulianParser.Field_declarationContext;
import info.julang.langspec.ast.JulianParser.Interface_definitionContext;
import info.julang.langspec.ast.JulianParser.Interface_member_declarationContext;
import info.julang.langspec.ast.JulianParser.Last_enum_member_declarationContext;
import info.julang.langspec.ast.JulianParser.Ordinary_enum_member_declarationContext;
import info.julang.langspec.ast.JulianParser.ProgramContext;
import info.julang.memory.HeapArea;
import info.julang.memory.simple.SimpleHeapArea;
import info.julang.modulesystem.ModuleManager;
import info.julang.modulesystem.RequirementInfo;
import info.julang.modulesystem.naming.FQName;
import info.julang.modulesystem.prescanning.RawClassInfo;
import info.julang.modulesystem.prescanning.RawScriptInfo;
import info.julang.parser.ANTLRHelper;
import info.julang.parser.AstInfo;
import info.julang.parser.LazyAstInfo;
import info.julang.typesystem.jclass.Accessibility;
import info.julang.typesystem.jclass.jufc.FoundationClassParser;
import info.julang.typesystem.loading.depresolving.HardDependencyResolver;
import info.julang.typesystem.loading.depresolving.IDependencyResolver;

/**
 * Generate JSON files containing Julian documentation for each system type. The generated files
 * are grouped per module.
 * <p>
 * These files are to be further processed by other tools to generate user-friendly documentation format.
 * 
 * @author Ming Zhou
 */
public class DocCollector extends SystemModuleProcessor<ScriptInfoBag> implements ISerializationHelper, IHtmlDocMerger {
	
	/** &lt;repo&gt;/docs/api/raw */
	private File apiJsonRoot;
	/** &lt;repo&gt;/docs/api/markdown */
	private File apiMdRoot;
	/** &lt;repo&gt;/docs/release/website/html/api */
	private File apiHtmlRoot;
	/** &lt;repo&gt;/docs/docs/tutorials */
	private File tutRoot;
	/** &lt;repo&gt;/release/website/html/tutorial */
	private File tutHtmlRoot;
	
	private ModuleContext mc;
	private File metadata;
	private File properties;
	private TutorialInfo tut;
	private BuiltInPrimitiveDocParser parser0; // info.julang.typesystem
	private BuiltInPrimitiveDocParser parser1; // info.julang.typesystem.basic
	private BuiltInClassDocParser parser2; // info.julang.typesystem.jclass.jufc.System[.*]
	
	public DocCollector(File srcDirectory, File projDirectory, Log logger){
		super(new File(srcDirectory.getAbsolutePath() + FoundationClassParser.JuFCRoot), logger);
		// JSON
		apiJsonRoot = new File(projDirectory.getAbsolutePath() + "/docs/api/raw");
		// Markdown
		apiMdRoot = new File(projDirectory.getAbsolutePath() + "/docs/api/markdown");
		apiHtmlRoot = new File(projDirectory.getAbsolutePath() + "/release/update-server/website/api");
		// HTML
		tutRoot = new File(projDirectory.getAbsolutePath() + "/docs/tutorials");
		tutHtmlRoot = new File(projDirectory.getAbsolutePath() + "/release/update-server/website/tutorial");

		properties = new File(projDirectory, "build.properties");
		metadata = new File(apiJsonRoot, "metadata.json");
		mc = new ModuleContext();
		
		// AnyType, VoidType
		parser0 = new BuiltInPrimitiveDocParser(srcDirectory, this, mc, logger){
			@Override
			protected boolean shouldSkip(File file) {
				return !file.getName().endsWith("Type.java");
			}
			
			@Override
			protected String getRelativeSrcDir( ) {
				return "info/julang/typesystem";
			}
		};
		parser1 = new BuiltInPrimitiveDocParser(srcDirectory, this, mc, logger);
		parser2 = new BuiltInClassDocParser(srcDirectory, this, mc, logger);
	}

	@Override
	protected ScriptInfoBag makeScriptInfoBag(RawScriptInfo rsi, File root) {
		return new ScriptInfoBag(rsi, root);
	}
	
	/**
	 * Collect all type's doc and serialize them per pattern
	 * 
	 * @param typeFilter to filter types/chapters to be serialized. This argument doesn't affect the scope of type scanning. 
	 * All the JuFC and built-in types will be scanned and extracted no matter what. This value only controls
	 * whether to serialize the doc from the extracted information. 
	 * @param markdown If true, also serialize the doc to Markdown format, if the pattern matches.
	 * @throws MojoExecutionException
	 */
	public void collectAll(String typeFilter, SerializationType stype, boolean cleanFirst) throws MojoExecutionException {
		if (cleanFirst) {
			try {
				clean();
			} catch (IOException e) {
				throw new MojoExecutionException("Couldn't clean up doc directories.", e);
			}
		}
		
		apiJsonRoot.mkdirs();
		
		Pattern pat = createPattern(typeFilter);
		TypeTable tt = initTypeSystem(); 

		// I. raw data in JSON format
		// Primitive types
		parser0.collectBuiltIn(pat, tt);
		parser1.collectBuiltIn(pat, tt);
		
		// Built-in types
		parser2.collectBuiltIn(pat, tt);
		
		// JuFC types
		collectJuFC(pat);
		
		// Meta-data
		emitMetadata();
		
		// II. user-facing formats
		if (stype != SerializationType.RAW) {
			prepTutorial(stype);
			WebsiteGenerator wg = null;
			if (stype == SerializationType.WEBSITE) {
				wg = new WebsiteGenerator(
					this, mc, tut, apiHtmlRoot.getParentFile());
				apiIndex = wg.getApiIndexModel();
				tutIndex = wg.getTutorialIndexModel();
			}
			
			genUserDocs(pat, stype);
			tut.serialize(pat, mc, tutIndex);
			
			if (wg != null) {
				wg.genWebsite(apiIndex, tutIndex);
			}
		}
	}
	
	private ApiIndexModel apiIndex;
	private ApiIndexModel.TutorialIndexModel tutIndex;

	/**
	 * Read tutorial files.
	 */
	private void prepTutorial(SerializationType stype) {
		tut = new TutorialInfo(tutRoot, tutHtmlRoot, stype, this);
		tut.initialize();
	}
	
	/**
	 * Generate MD files for each matching type.
	 */
	private void genUserDocs(Pattern pat, final SerializationType type) {
		mc.foreach(new TypeDocProcessor(){
			@Override
			public void process(String key, Type doc) {
				if (doc == null) {
					GlobalLogger.get().warn("Couldn't generate MD for " + key + " since the type is null.");
				}
				
				if (doc.visibility == Accessibility.PUBLIC) {
					GlobalLogger.get().info("Generating MD for " + key + " ...");
					try {
						File modRoot = DocCollector.this.getModuleRoot(doc.getModuleName(), type);
						DocCollector.this.serialize(modRoot, doc, type);
					} catch (MojoExecutionException e) {
						throw new DocGenException("Unable to generate Markdown file for type: " + key, e);
					}
				}
			}
		}, pat);
	}

	/** Emit a metadata JSON file that contains versioning info. */
	private void emitMetadata() throws MojoExecutionException {
		try {
			Properties prop = new Properties();
			prop.load(new FileInputStream(properties));
			
			DocModel.Metadata md = new DocModel.Metadata(); 
			md.version = prop.getProperty(ModuleContext.MD_VERSION);
			
			mc.addMetatdata(ModuleContext.MD_VERSION, md.version);
			mc.addMetatdata(ModuleContext.OFFICIAL_WEBSITE, prop.getProperty(ModuleContext.OFFICIAL_WEBSITE));
			
			String result = serialize0(md);
			try (FileWriter fw = new FileWriter(metadata)){
				fw.write(result);
				fw.flush();
			}
		} catch (IOException e) {
			throw new MojoExecutionException("Couldn't emit metadata file.", e);
		}
	}

	/** Collect documentations from JuFC types written in Julian. */
	private void collectJuFC(Pattern pat) throws MojoExecutionException {
		// Load all JuFC scripts
		List<ScriptInfoBag> scripts = new ArrayList<ScriptInfoBag>();
		loadAll(rootDir, scripts);
		
		// Build a cross-reference context
		for (ScriptInfoBag sib : scripts) {
			mc.addTypesFromScript(sib);
		}
		
		List<DocumentedClassInfo> postponed = new ArrayList<DocumentedClassInfo>();
		for (ScriptInfoBag sib : scripts) {
			ProgramContext pc = sib.getRawScriptInfo().getAstInfo().getAST();
			AstInfo<ProgramContext> pcAst = AstInfo.succ(pc, sib.getFileName());
			
			RawScriptInfo rsi = sib.getRawScriptInfo();
			NamespacePool ns = createNamespace(rsi);
			LazyAstInfo ainfo = rsi.getAstInfo();
			String modName = rsi.getModuleName();
			
			List<RawClassInfo> cinfos = rsi.getClasses();
			for (RawClassInfo cinfo : cinfos) {
				ClassDeclInfo cdi = cinfo.getDeclInfo();
				String sname = cdi.getName();
				
				ParserRuleContext prc = cdi.getAST();
				
				DocModel.Type typ = null;
				switch(cdi.getSubtype()){
				case CLASS:
					DocModel.ClassType ct = docGenClass(
						ainfo, pcAst, cdi, (Class_definitionContext)prc, mc, ns, modName, sname);
					if (ct != null) { 
						if (ct.isInherited) {
							DocumentedClassInfo dci = new DocumentedClassInfo(modName, cinfo);
							dci.setDoc(ct);
							postponed.add(dci);
						} else {
							typ = ct;
						}
					}
					break;
				case INTERFACE:
					DocModel.InterfaceType it = docGenInterface(
						ainfo, pcAst, cdi, (Interface_definitionContext)prc, mc, ns, modName, sname);
					if (it != null){
						if (it.isInherited) {
							DocumentedClassInfo dci = new DocumentedClassInfo(modName, cinfo);
							dci.setDoc(it);
							postponed.add(dci);
						} else {
							typ = it;
						}
					}
					break;
				case ENUM:
					typ = docGenEnum(ainfo, pcAst, cdi, (Enum_definitionContext)prc, mc, ns, modName, sname);
					break;
				case ATTRIBUTE:
					typ = docGenAttribute(ainfo, pcAst, cdi, (Attribute_definitionContext)prc, mc, ns, modName, sname);
					break;
				default:
					throw new MojoExecutionException("Unsupported class subtype: " + cdi.getSubtype().name());
				}
				
				// Serialize the type and generate file
				if (typ != null) {
					typ.setNamespacePool(ns);
					typ.setModuleName(modName);
					mc.setDoc(modName, typ);
					
					String fname = modName + "." + sname;
					if (pat.matcher(fname).matches() && typ.visibility == Accessibility.PUBLIC){
						File modRoot = getModuleRoot(modName, SerializationType.RAW);
						serialize(modRoot, typ, SerializationType.RAW);
					}
				}
			}
		}
		
		// If a type inherits its documentation from its base type, it's put into the postponed list, which
		// must be sorted in an order as per dependency.
		
		IDependencyResolver resolver = new HardDependencyResolver();
		@SuppressWarnings("unchecked")
		List<DocumentedClassInfo> sorted = (List<DocumentedClassInfo>)(Object)resolver.resolve(postponed);

		for (DocumentedClassInfo dci : sorted) {
			DocModel.Type doc = dci.getDoc();
			DocModel.InterfaceType idoc = (DocModel.InterfaceType)doc;
			for (DocModel.Method method : idoc.methods){
				if (method.isInherited) {
					fillInheritedDoc(method, idoc);
				}
			}

			String modName = dci.getModuleName();
			idoc.setModuleName(modName);
			mc.setDoc(modName, idoc);
			
			String fname = modName + "." + idoc.name;
			if (pat.matcher(fname).matches()){
				File modRoot = getModuleRoot(modName, SerializationType.RAW);
				serialize(modRoot, idoc, SerializationType.RAW);
			}
		}
	}

	/** Find the method from a base type that can provide inherited documentation. */
	private void fillInheritedDoc(Method method, InterfaceType doc) {
		switch(doc.subtype){
		case CLASS:
			DocModel.ClassType ct = (DocModel.ClassType)doc;
			TypeRef tref = ct.parent;
			if (tref != null) {
				if (tryFillInheritedDoc(method, tref)) {
					return;
				}
			}
			// Fall through
		case INTERFACE:
			for (TypeRef tr : doc.interfaces){
				if (tr != null) {
					if (tryFillInheritedDoc(method, tr)) {
						return;
					}
				}
			}
			break;
		default:
			throw new DocGenException(
				"The type " + doc.name + 
				" is not a class or interface, therefore it should not contain any inherited method documentation.");
		}
	}

	/** 
	 * Populate the summary, return, params from the the base method, unless the doc is already provided for that part.
	 *  
	 * @return true if the doc is found and used to populate this method.
	 */
	private boolean tryFillInheritedDoc(Method method, TypeRef tr) {
		DocModel.Type doc = mc.getDoc(tr);
		if (doc == null) {
			return false;
		}
		
		// Let's bet this works. If not it crashes the plugin anyway.
		DocModel.InterfaceType idoc = (DocModel.InterfaceType)doc;
		for (Method m : idoc.methods){
			if (method.equals(m)){ // Found the method.
				if (method.summary == null || "".equals(method.summary)) {
					method.summary = "(INHERITED DOC)<br><br>" + m.summary;
				}
				
				TypeDescription td = method.returnType;
				if (td != null && (td.summary == null || "".equals(td.summary))) {
					td.summary = m.returnType.summary;
				}

				int i = 0;
				for (TypeDescription ptd : method.params){
					if (ptd != null && (ptd.summary == null || "".equals(ptd.summary))) {
						ptd.summary = m.params.get(i).summary;
					}
					
					i++;
				}
				
				return true;
			}
		}
		
		return false;
	}
	
	/** 
	 *  Create a regex pattern from the filtering string. The filtering string supports wildcard (*), 
	 *  line-start (^), line-end ($), and can be quoted  by either ' or ".
	 *  
	 *  @throws MojoExecutionException when the pattern is not recognized.
	 */
	private Pattern createPattern(String raw) throws MojoExecutionException {
		if ("".equals(raw) || raw == null){
			raw = "*";
		}
		
		StringBuilder sb = new StringBuilder("(?i)"); // case insensitive
		int len = raw.length();
		for(int i = 0; i < len; i++) {
			char c = raw.charAt(i);
			switch(c){
			case '\'':
				if (i == 0) {
					len--;
					char c2 = len >= 0 ? raw.charAt(len) : '\0';
					if (c2 != '\''){
						throw new MojoExecutionException(
							"Cannot recognize pattern: " + raw + ". The quotes (') are not matched.");
					}
				} else {
					throw new MojoExecutionException("Cannot recognize pattern: " + raw + 
						". '\'' is not allowed anywhere but the outermost quotes.");
				}
				break;
			case '"':
				if (i == 0) {
					len--;
					char c2 = len >= 0 ? raw.charAt(len) : '\0';
					if (c2 != '"'){
						throw new MojoExecutionException(
							"Cannot recognize pattern: " + raw + ". The quotes (\") are not matched.");
					}
				} else {
					throw new MojoExecutionException("Cannot recognize pattern: " + raw + 
						". '\"' is not allowed anywhere but the outermost quotes.");
				}
				break;
			case '^':
				if (i == 0) {
					sb.append("^"); // Allow ^ only at the beginning.
				} else {
					throw new MojoExecutionException("Cannot recognize pattern: " + raw + 
						". '^' is only allowed at the beginning.");
				}
				break;
			case '$':
				if (i == len - 1) {
					sb.append("$"); // Allow $ only at the end.
				} else {
					throw new MojoExecutionException("Cannot recognize pattern: " + raw + 
						". '$' is only allowed at the end.");
				}
				break;
			case '.':
				sb.append("\\."); // Interpret '.' as literal, since this is the module/type separator.
				break;
			case '*':
				sb.append(".*"); // Any number of characters.
				break;
			default:
				if (Character.isLetter(c) || Character.isDigit(c) || c == '_') {
					sb.append(c); // Preserve other legal identifier characters.
				} else {
					throw new MojoExecutionException(
						"Cannot recognize pattern: " + raw + 
						". Other than legal Julian identifier characters and namespace separator '.', " + 
						" only '*' is allowed as a wildcard character.");
				}
				break;
			}
		}
		
		Pattern pat = Pattern.compile(sb.toString());
		return pat;
	}
	
	/** Initialize type system for Julian. This is needed to resolve built-in types such as String. */
	private TypeTable initTypeSystem(){
		HeapArea heap = new SimpleHeapArea();
		TypeTable tt = new TypeTable(heap);
		ModuleManager mm = new ModuleManager();
		VariableTable gvt = new VariableTable(null);
		SimpleEngineRuntime sert = new SimpleEngineRuntime(heap, gvt, tt, mm);
		tt.initialize(sert);
		return tt;
	}
	
	/** Create a namespace pool for the current script's import information. This is needed for type resolution. */
	private NamespacePool createNamespace(RawScriptInfo rsi){
		NamespacePool ns = new NamespacePool();
		ns.addNamespace(rsi.getModuleName());
		List<RequirementInfo> list = rsi.getRequirements();
		for(RequirementInfo r : list) {
			ns.addNamespaceFromRequirementInfo(r);
		}
		
		return ns;
	}
	
	/** Get documentation block. Returns <code>&#047;**&#047;</code> if the doc is absent. */
	private String extractRawDoc(LazyAstInfo ainfo, ParserRuleContext prc){
		String typdoc = ainfo.getDoc(prc);
		return typdoc == null ? "/**/" : typdoc;
	}
	
	/** Fill in the base information about the type. Returns true if successful; false if this type should be skipped for doc extraction. */
	private boolean fillBasics(
		ClassDeclInfo declInfo, Type typ, String sname, LazyAstInfo ainfo, ParserRuleContext prc, NamespacePool ns){
		typ.name = sname;
		
		// Fill summary and references
		String typdoc = extractRawDoc(ainfo, prc);
		typ.processRawDoc(typdoc);
		
		// Modifiers
		switch(declInfo.getAccessibility()){
		case PUBLIC:
			typ.visibility = declInfo.getAccessibility();
			break;
		default:
			// return false;
		}
		
		if (declInfo.isStatic() && typ.subtype == ClassSubtype.CLASS) {
			((DocModel.ClassType)typ).isStatic = true;
		}
		
		// Parent and interfaces
		switch(typ.subtype){
		case CLASS:
		case INTERFACE:
			List<ParsedTypeName> ptns = declInfo.getParentTypes();
			if (ptns != null){
				for (ParsedTypeName ptn : ptns) {
					TypeInfo baseType = mc.getTypeInfo(ptn, ns);
					switch(baseType.getSubType()){
					case CLASS:
						// class type => parent class or extension class
						if (baseType.isStatic()) {
							if (declInfo.isStatic()) {
								// static class can have static parent
								((DocModel.ClassType)typ).parent = new TypeRef(baseType);
							} else {
								// Extension
								((DocModel.InterfaceType)typ).extensions.add(new TypeRef(baseType));
							}
						} else {
							if (declInfo.isStatic()) {
								// static class cannot inherit from non-static class
								logger.warn(
									"Found a static class '" + declInfo.getFQName().toString() + 
									"' that inherits from a non-static class '" + baseType.getFullName() + "'.");
							} else {
								// regular inheritance
								((DocModel.ClassType)typ).parent = new TypeRef(baseType);
							}			
						}
						break;
					case INTERFACE:
						// interface type => interfaces					
						((DocModel.InterfaceType)typ).interfaces.add(new TypeRef(baseType));
						break;
					default:
						break;
					}
				}
			}
			break;
		case ENUM:
		case ATTRIBUTE:
			break;
		default:
			return false;
		}
		
		return true;
	}

	//------------------------ Class ------------------------//
	
	/** Return null if no doc should be generated for this class. */
	private DocModel.ClassType docGenClass(
		LazyAstInfo ainfo,				// For getting doc using parser associated with this type
		AstInfo<ProgramContext> pcAst, 	// The whole AST to be further analyzed
		ClassDeclInfo cdi, 				// The semantic info regarding the type
		Class_definitionContext prc, 	// The partial AST concerning only this type
		ModuleContext mc, 				// The module/type context derived from the entire JuFC environment
		NamespacePool ns, 				// A namespace pool created from this script
		String mname, 					// Module name
		String sname) {		 			// Simple name
		
		DocModel.ClassType typ = new DocModel.ClassType();
		if (!fillBasics(cdi, typ, sname, ainfo, prc, ns)){
			return null;
		}
		
		// Members
		List<Class_member_declarationContext> mems = prc.class_body().class_member_declaration();
		List<MemberDeclInfo> members = SyntaxHelper.parseClassMemberDeclarations(mems, pcAst, new FQName(mname + "." + sname), true);
		for (MemberDeclInfo mdi : members) {
			switch (mdi.getAccessibility()) {
			case PROTECTED:
			case PUBLIC:
				break;
			default:
				continue; // Do not proceed if it doesn't meet required visibility
			}
			
			switch(mdi.getMemberType()){
			case CONSTRUCTOR:
				CtorDeclInfo ctorDecl = (CtorDeclInfo)mdi;
				Constructor ctor = new Constructor(sname);
				
				// Params
				List<TypeAndName> tans = ctorDecl.getParameters();
				if (tans != null) {
					for(TypeAndName tan : tans){
						TypeInfo resolved = mc.getTypeInfo(tan.getTypeName(), ns);
						TypeDescription td = new TypeDescription(tan.getParamName(), "", resolved.getFullName());
						ctor.params.add(td);
					}
				}
				
				// Properties
				ctor.isStatic = ctorDecl.isStatic();
				ctor.visibility = ctorDecl.getAccessibility();
				
				// Summary; Params (additional info from doc section); Throws
				// This must happen at the end, since we are only to update parameter info, instead of adding it.
				String doc = extractRawDoc(ainfo, mdi.getAST());
				ctor.processRawDoc(doc);
				
				if (!ctor.isDocDisabled()){
					typ.ctors.add(ctor);
				}
				break;
			case METHOD:
				MethodDeclInfo methodDecl = (MethodDeclInfo)mdi;
				Method method = new Method();
				
				// Params
				tans = methodDecl.getParameters();
				if (tans != null) {
					for(TypeAndName tan : tans){
						TypeInfo resolved = mc.getTypeInfo(tan.getTypeName(), ns);
						TypeDescription td = new TypeDescription(tan.getParamName(), "", resolved.getFullName()); //tan.getTypeName().toString());
						method.params.add(td);
					}
				}
				
				// Properties
				method.name = methodDecl.getName();
				method.isStatic = methodDecl.isStatic();
				method.isAbstract = methodDecl.isAbstract();
				method.visibility = methodDecl.getAccessibility();
				
				// Return
				ParsedTypeName retName = methodDecl.getReturnTypeName();
				method.returnType = new TypeDescription("", "", retName.toString());
				
				// Summary; Params (additional info from doc section); Throws
				// This must happen at the end, since we are only to update parameter info, instead of adding it.
				doc = extractRawDoc(ainfo, mdi.getAST());
				method.processRawDoc(doc);
				
				if (!method.isDocDisabled()){
					typ.methods.add(method);
					if(method.isInherited) {
						typ.isInherited = true;
					}
				}
				break;
			case FIELD:
				FieldDeclInfo fdecl = (FieldDeclInfo)mdi;
				Field field = new Field();
				
				// Properties
				field.name = fdecl.getName();
				field.isStatic = fdecl.isStatic();
				field.isConst = fdecl.isConst();
				field.visibility = fdecl.getAccessibility();
				field.type = TypeRef.makeFromFullName(fdecl.getTypeName().toString());
				
				// Summary
				doc = extractRawDoc(ainfo, mdi.getAST());
				field.processRawDoc(doc);
				
				if (!field.isDocDisabled()){
					typ.fields.add(field);
				}
				break;
			default:
				// Do not show others
				break;
			}
		}
		
		return typ;
	}

	//------------------------ Interface ------------------------//

	/** Return null if no doc should generated for this interface. */
	private DocModel.InterfaceType docGenInterface(
		LazyAstInfo ainfo,				// For getting doc using parser associated with this type
		AstInfo<ProgramContext> pcAst, 	// The whole AST to be further analyzed
		ClassDeclInfo cdi, 				// The semantic info regarding the type
		Interface_definitionContext prc, 	// The partial AST concerning only this type
		ModuleContext mc, 				// The module/type context derived from the entire JuFC environment
		NamespacePool ns, 				// A namespace pool created from this script
		String mname, 					// Module name
		String sname) {		 			// Simple name
		
		DocModel.InterfaceType typ = new DocModel.InterfaceType();
		if (!fillBasics(cdi, typ, sname, ainfo, prc, ns)){
			return null;
		}

		// Synthesize class member declarations from interface member declarations
		List<Interface_member_declarationContext> infMemDecls = prc.interface_body().interface_member_declaration();
		List<Class_member_declarationContext> cmems = new ArrayList<Class_member_declarationContext>();
		for (Interface_member_declarationContext infMemDecl : infMemDecls){
			Class_member_declarationContext cmd = new Class_member_declarationContext(null, 0); // ctor args are not important here.
			List<ParseTree> children = infMemDecl.children;
			for (ParseTree pt : children) {
				cmd.addChild((RuleContext)pt);
			}
			cmems.add(cmd);
		}
		
		List<MemberDeclInfo> members = SyntaxHelper.parseClassMemberDeclarations(
			cmems, pcAst, new FQName(mname + "." + sname), true);
		
		for (MemberDeclInfo mdi : members) {
			switch (mdi.getAccessibility()) {
			case PUBLIC:
				break;
			default:
				continue; // Do not proceed if it doesn't meet required visibility
			}
			
			switch(mdi.getMemberType()){
			case METHOD:
				MethodDeclInfo methodDecl = (MethodDeclInfo)mdi;
				Method method = new Method();
				
				// Params
				List<TypeAndName> tans = methodDecl.getParameters();
				if (tans != null) {
					for(TypeAndName tan : tans){
						//TypeInfo resolved = mc.getTypeInfo(tan.getTypeName(), ns);
						TypeInfo resolved = mc.getTypeInfo(tan.getTypeName(), ns);
						TypeDescription td = new TypeDescription(tan.getParamName(), "", resolved.getFullName()); //tan.getTypeName().toString());
						method.params.add(td);
					}
				}
				
				// Properties
				method.name = methodDecl.getName();
				method.isStatic = methodDecl.isStatic();
				method.isAbstract = methodDecl.isAbstract();
				method.visibility = methodDecl.getAccessibility();
				
				// Return
				//TypeInfo resolved = mc.getTypeInfo(methodDecl.getReturnTypeName(), ns);
				method.returnType = new TypeDescription("", "", methodDecl.getReturnTypeName().toString());
				
				// Summary; Params (additional info from doc section); Throws
				// This must happen at the end, since we are only to update parameter info, instead of adding it.
				String doc = extractRawDoc(ainfo, mdi.getAST());
				method.processRawDoc(doc);
				
				if (!method.isDocDisabled()){
					typ.methods.add(method);
					if(method.isInherited) {
						typ.isInherited = true;
					}
				}
				break;
			default:
				// Do not show others
				break;
			}
		}
		
		return typ;
	}
	
	//------------------------ Enum ------------------------//
	
	/** Return null if no doc should generated for this enum. */
	private DocModel.EnumType docGenEnum(
		LazyAstInfo ainfo,				// For getting doc using parser associated with this type
		AstInfo<ProgramContext> pcAst, 	// The whole AST to be further analyzed
		ClassDeclInfo cdi, 				// The semantic info regarding the type
		Enum_definitionContext prc, 	// The partial AST concerning only this type
		ModuleContext mc, 				// The module/type context derived from the entire JuFC environment
		NamespacePool ns, 				// A namespace pool created from this script
		String mname, 					// Module name
		String sname) {		 			// Simple name
		
		DocModel.EnumType typ = new DocModel.EnumType();
		if (!fillBasics(cdi, typ, sname, ainfo, prc, ns)){
			return null;
		}
		
		// Members
		Enum_member_declarationsContext enc = prc.enum_body().enum_member_declarations();
		
		if (enc != null) {
			int ordIndex = -1;
			boolean firstTime = true;
			List<Ordinary_enum_member_declarationContext> members = enc.ordinary_enum_member_declaration();
			if (members != null){
				for(Ordinary_enum_member_declarationContext mem : members){
					// Name and ordinal value
					Enum_member_declaration_bodyContext mbody = mem.enum_member_declaration_body();
					ordIndex = addEnumEntry(ainfo, typ, mbody, ordIndex, firstTime);
					firstTime = false;
				}
			}
			
			Last_enum_member_declarationContext lmem = enc.last_enum_member_declaration();
			Enum_member_declaration_bodyContext mbody = lmem.enum_member_declaration_body();
			addEnumEntry(ainfo, typ, mbody, ordIndex, firstTime);
		}
		
		return typ;
	}
	
	private int addEnumEntry(
		LazyAstInfo ainfo, DocModel.EnumType typ, Enum_member_declaration_bodyContext mbody, int ordIndex, boolean firstTime){
		// Name and ordinal value
		EnumEntry entry = parseEnumEntry(mbody, ordIndex, firstTime);
		entry.name = mbody.IDENTIFIER().toString();
				
		// Fill summary
		String doc = extractRawDoc(ainfo, mbody);
		entry.processRawDoc(doc);
		
		typ.entries.add(entry);
		
		return entry.ordinal;
	}
	
	private EnumEntry parseEnumEntry(Enum_member_declaration_bodyContext mbody, int ordIndex, boolean firstTime) {
		String svalue = mbody.IDENTIFIER().getText();
		Enum_member_declaration_initializerContext init = mbody.enum_member_declaration_initializer();
		int ivalue = 0;
		if (init != null){
			try {
				ivalue = ANTLRHelper.parseIntLiteral(init.INTEGER_LITERAL().getText());
			} catch (IllegalLiteralException e) {
				throw new DocGenException("Cannot parse enum value. ", e);
			}
		} else {
			ivalue = firstTime ? 0 : ordIndex + 1;
		}
		
		EnumEntry ev = new EnumEntry();
		ev.name = svalue;
		ev.ordinal = ivalue;
		
		return ev;
	}
	
	//------------------------ Attribute ------------------------//
	
	/** Return null if no doc should generated for this attribute. */
	private DocModel.AttributeType docGenAttribute(
		LazyAstInfo ainfo,				// For getting doc using parser associated with this type
		AstInfo<ProgramContext> pcAst, 	// The whole AST to be further analyzed
		ClassDeclInfo cdi, 				// The semantic info regarding the type
		Attribute_definitionContext prc,// The partial AST concerning only this type
		ModuleContext mc, 				// The module/type context derived from the entire JuFC environment
		NamespacePool ns, 				// A namespace pool created from this script
		String mname, 					// Module name
		String sname) {		 			// Simple name
		
		DocModel.AttributeType typ = new DocModel.AttributeType();
		if (!fillBasics(cdi, typ, sname, ainfo, prc, ns)){
			return null;
		}

		// Synthesize attribute member declarations from class field declarations
		List<Field_declarationContext> fdcs = prc.attribute_body().field_declaration();
		List<Class_member_declarationContext> cmems = new ArrayList<Class_member_declarationContext>();
		for (Field_declarationContext fdc : fdcs){
			Class_member_declarationContext cmd = new Class_member_declarationContext(null, 0); // ctor args are not important here.
			cmd.addChild(fdc);
			cmems.add(cmd);
		}
		
		List<MemberDeclInfo> members = SyntaxHelper.parseClassMemberDeclarations(
			cmems, pcAst, new FQName(mname + "." + sname), true);
		
		for (MemberDeclInfo mdi : members) {
			switch (mdi.getAccessibility()) {
			case PUBLIC:
				break;
			default:
				continue; // Do not proceed if it doesn't meet required visibility
			}
			
			switch(mdi.getMemberType()){
			case FIELD:
				FieldDeclInfo fdecl = (FieldDeclInfo)mdi;
				Field field = new Field();
				
				// Properties
				field.name = fdecl.getName();
				field.isStatic = fdecl.isStatic();
				field.visibility = fdecl.getAccessibility();
				field.type = TypeRef.makeFromFullName(fdecl.getTypeName().toString());
						
				// Summary
				String doc = extractRawDoc(ainfo, mdi.getAST());
				field.processRawDoc(doc);
				
				if (!field.isDocDisabled()){
					typ.fields.add(field);
				}
				break;
			default:
				// Do not show others
				break;
			}
		}
		
		return typ;
	}
	
	//----------------- ISerializationHelper -----------------//
	
	@Override
	public File getModuleRoot(String modName, SerializationType styp){
		String path = null;
		switch(styp){
			case WEBSITE:
			path = apiHtmlRoot.getAbsolutePath();
			break;
		case MARKDOWN:
			path = apiMdRoot.getAbsolutePath();
			break;
		case RAW:
			path = apiJsonRoot.getAbsolutePath();
			break;
		default:
			break;
		}
		
		if (path != null) {
			File modRoot = new File(path + "/" + modName);
			if (!modRoot.exists()){
				modRoot.mkdirs();
			}
			
			return modRoot;
		} else {
			throw new DocGenException("Unknown serialization type: " + styp.name());
		}
	}

	@Override
	public void serialize(File modRoot, Type typ, SerializationType styp) throws MojoExecutionException {
		try {
			// JSON
			switch(styp){
			case RAW:
				String result = serialize0(typ);
				try (FileWriter fw = new FileWriter(getFile(modRoot, typ, ".json"))){
					fw.write(result);
				}
				break;
			case MARKDOWN:
				try (MarkdownConverter mdconv = new MarkdownConverter(mc, tut, typ, getFile(modRoot, typ, ".md"))){
					mdconv.write();
				}
				break;
			case WEBSITE:
				// First generate MD
				File mdModRoot = this.getModuleRoot(typ.getModuleName(), SerializationType.MARKDOWN);
				File markDownFile = getFile(mdModRoot, typ, ".md");
				try (MarkdownConverter mdconv = new MarkdownConverter(mc, tut, typ, markDownFile)){
					mdconv.write();
				}
				
				// Then convert to HTML
				File htmlFile = getFile(modRoot, typ, ".html");
				MarkDown2HtmlConverter htmlConv = new MarkDown2HtmlConverter(markDownFile);
				String contents = htmlConv.convert();
				mergeDocTemplate(this.apiIndex, WebsiteResources.api, typ.getModuleName(), typ.name, contents, htmlFile, typ);
				break;
			default:
				throw new MojoExecutionException("Unknown serialization type: " + styp.name());
			}
		} catch (IOException e) {
			throw new MojoExecutionException("Failed to emit documentation for type " + typ.name, e);
		}
	}

	@Override
	public void mergeDocTemplate(ApiIndexModel index, WebsiteResources resourceType,
			String modName, String typName, String contents, File htmlFile,
			Object model)
			throws MojoExecutionException {
		VelocityContext context = new VelocityContext();
		context.put("tv_root", index.getRoot());
		context.put("tv_type", resourceType.name());
		context.put("tv_module", modName);
		context.put("tv_name", typName);
		if (resourceType == WebsiteResources.tutorial && model instanceof ChapterInfo){
			ChapterInfo chInfo = (ChapterInfo)model;
			IChapterInfo prev = chInfo.getPrev();
			IChapterInfo next = chInfo.getNext();
			if (prev != null) {
				context.put("tv_prev_name", prev.getLinkName());
			}
			if (next != null) {
				context.put("tv_next_name", next.getLinkName());
			}
		}
		context.put("tv_index", contents == null); // If no contents, this is an index page
		context.put("tv_contents", contents);
		mergeToFile("website/doc.vm", context, htmlFile);
	}
	
	private File getFile(File modRoot, Type typ, String ext) throws MojoExecutionException, IOException {
		String parentDirPath = modRoot.getAbsolutePath();
		File dir = new File(parentDirPath);
		dir.mkdirs();
		if (!dir.exists()){
			throw new MojoExecutionException("Module path doesn't exist and couldn't be created: " + parentDirPath);
		}
		
		File file = new File(parentDirPath + File.separator + typ.name + ext);
		file.createNewFile();
		if (!file.exists()){
			throw new MojoExecutionException("Module file doesn't exist and couldn't be created: " + file.getAbsolutePath());
		}
		
		return file;
	}
	
	private String serialize0(Object obj){
		// Options:
		// - Serialize only public fields and white-listed non-public fields
		// - Pretty print
		Gson gson = new GsonBuilder()
			.addSerializationExclusionStrategy(
				new ExclusionStrategy() {
					private boolean isCtor;
		            @Override
		            public boolean shouldSkipField(FieldAttributes f) {
		            	String name = f.getName();
		            	// Non-public fields we want to serialize
		            	if ("summary".equals(name)){
		            		return false;
		            	}
		            	
		            	// Public fields we don't want to serialize
		            	if (isCtor && "name".equals(name)){
		            		return true;
		            	}
		            	
		                return !f.hasModifier(Modifier.PUBLIC);
		            }
		
		            @Override
		            public boolean shouldSkipClass(Class<?> aClass) {
		            	if (DocModel.Documented.class.isAssignableFrom(aClass)){
			            	isCtor = aClass == DocModel.Constructor.class;
		            	} // If this class is not any subclass of Documented, do not change isCtor
		            	
		                return false;
		            }
		        })
			.setPrettyPrinting()
			.create();
		
		String result = gson.toJson(obj);
		
		return result;
	}

	private void clean() throws IOException {
		cleanDir(apiJsonRoot);
		cleanDir(apiMdRoot);
		cleanDir(apiHtmlRoot);
		cleanDir(new File(tutRoot, "markdown"));
	}
	
	private void cleanDir(File root) throws IOException {
		Path directory = Paths.get(root.getAbsolutePath());
		Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
		   @Override
		   public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			   String name = file.getFileName().toString();
			   if (name.endsWith(".md") || name.endsWith(".html") || name.endsWith(".json")) {
			       Files.delete(file);
			   }
			   
		       return FileVisitResult.CONTINUE;
		   }

		   @Override
		   public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
			   String[] children = dir.toFile().list();
			   if (children == null || children.length == 0) {
			       Files.delete(dir);
			   }
			   
		       return FileVisitResult.CONTINUE;
		   }
		});
	}
}