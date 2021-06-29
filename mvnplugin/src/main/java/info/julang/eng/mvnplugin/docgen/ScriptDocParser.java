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
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.Token;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import info.julang.eng.mvnplugin.docgen.DocModel.Function;
import info.julang.eng.mvnplugin.docgen.DocModel.Script;
import info.julang.eng.mvnplugin.docgen.DocModel.TypeDescription;
import info.julang.interpretation.syntax.ParsedTypeName;
import info.julang.interpretation.syntax.SyntaxHelper;
import info.julang.langspec.ast.JulianLexer;
import info.julang.langspec.ast.JulianParser.Declaration_statementContext;
import info.julang.langspec.ast.JulianParser.ExecutableContext;
import info.julang.langspec.ast.JulianParser.Function_declaratorContext;
import info.julang.langspec.ast.JulianParser.Function_parameterContext;
import info.julang.langspec.ast.JulianParser.Function_parameter_listContext;
import info.julang.langspec.ast.JulianParser.Function_signatureContext;
import info.julang.langspec.ast.JulianParser.Function_signature_mainContext;
import info.julang.langspec.ast.JulianParser.Include_statementContext;
import info.julang.langspec.ast.JulianParser.PreambleContext;
import info.julang.langspec.ast.JulianParser.ProgramContext;
import info.julang.langspec.ast.JulianParser.StatementContext;
import info.julang.langspec.ast.JulianParser.Statement_listContext;
import info.julang.langspec.ast.JulianParser.TypeContext;
import info.julang.modulesystem.scripts.InternalScriptLoader;
import info.julang.parser.ANTLRHelper;
import info.julang.parser.ANTLRParser;
import info.julang.util.Pair;

/**
 * Doc model producer for built-in scripts.
 * 
 * @author Ming Zhou
 */
public class ScriptDocParser {

	private final File scriptsRoot;
	private final Log logger;
	private final ISerializationHelper serializer;
	private final ModuleContext mc;
	
	public ScriptDocParser(File srcDirectory, ISerializationHelper serializer, ModuleContext mc, Log logger){
		scriptsRoot = new File(srcDirectory.getAbsolutePath() + InternalScriptLoader.ScriptsRoot);
		this.logger = logger;
		this.serializer = serializer;
		this.mc = mc;
	}
	
	public void collectAll(Pattern pat) throws MojoExecutionException {
		// 1. From scriptsRoot, read each Doc to create ANTLR parser. Put result in an object.
		File[] files = scriptsRoot.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".jul");
			}
		});
		
		List<BuiltinScriptRawInfo> scriptRawInfoList = new ArrayList<BuiltinScriptRawInfo>(files.length);
		for (File file : files) {
			String fname = file.getName();
			try (FileInputStream ins = new FileInputStream(file)){
				// logger.info("Collecting info for built-in script file: " + fname);
				
				ANTLRParser parser = new ANTLRParser(fname, ins, true);
				parser.parse(true, true);
				
				BuiltinScriptRawInfo info = new BuiltinScriptRawInfo(fname, parser);
				scriptRawInfoList.add(info);
			} catch (IOException e) {
				logger.error("Couldn't read script file: " + fname);
				logger.error(e);
			}
		}
		
		// 2. Now for each function we discovered in step 1, 
		//    create a Function model (DocModel.Function) with all the pertinent data set into it.
		int funcCount = 0;
		List<DocModel.Script> scripts = new ArrayList<DocModel.Script>();
		Map<String, Pair<DocModel.Script, BuiltinScriptRawInfo>> scriptMap = new HashMap<>();
		for (BuiltinScriptRawInfo scriptRawInfo : scriptRawInfoList) {
			String sname = scriptRawInfo.getName();
			DocModel.Script script = new DocModel.Script(sname);
			scriptMap.put(sname, new Pair<>(script, scriptRawInfo));
			Collection<GlobalFuncRawInfo> funcRawInfoColl = scriptRawInfo.getAllFunctions();
			
			for (GlobalFuncRawInfo funcRawInfo : funcRawInfoColl) {
				Function func = new Function(funcRawInfo.name);
				
				// Params
				List<FuncParamRawInfo> tans = funcRawInfo.params;
				if (tans != null) {
					for (FuncParamRawInfo paramInfo : tans) {
						ParsedTypeName ptn = SyntaxHelper.parseTypeName(paramInfo.type);
						TypeInfo resolved = mc.getTypeInfo(ptn, null);
						if (resolved == null) {
							throw new MojoExecutionException(
								"Cannot resolve type '" + paramInfo.type.getText() + "' for argument '" + paramInfo.name 
								+ "' in function '" + func.name + "' from script file: " + script.name);
						}
						
						TypeDescription td = new TypeDescription(paramInfo.name, "", resolved.getFullName());
						func.params.add(td);
					}
				}
				
				// Return
				func.returnType = new TypeDescription("", "", funcRawInfo.returnType.getText());
				
				// Summary
				func.processRawDoc(funcRawInfo.rawDoc);
				
				script.functions.add(func);
				funcCount++;
			}
			
			script.processRawDoc(scriptRawInfo.getScriptDoc());
			
			if (pat.matcher(script.name).matches()) {
	    		File modRoot = serializer.getModuleRoot(script.getDocFolderName(), SerializationType.RAW);
	    		serializer.serialize(modRoot, script, SerializationType.RAW);
			}
			
			scripts.add(script);
		}
		
		// Add includes
		for (Pair<Script, BuiltinScriptRawInfo> entry : scriptMap.values()) {
			Script includer = entry.getFirst();
			List<Script> list = new ArrayList<>();
			BuiltinScriptRawInfo rawInfo = entry.getSecond();
			Collection<String> incls = rawInfo.getAllIncludes();
			for (String incl : incls) {
				Pair<Script, BuiltinScriptRawInfo> included = scriptMap.get(incl);
				if (included != null) {
					Script includee = included.getFirst();
					list.add(includee);
				}
			}
			
			includer.includes = list;
		}

		logger.info("Collected a total of " + scripts.size() + " script files, containing " + funcCount + " functions.");
		
		mc.setScriptDoc(scripts);
	}
}

class BuiltinScriptRawInfo {
	
	private String name;
	
	private Map<String, GlobalFuncRawInfo> map;
	
	private List<String> includes;
	
	private String scriptDoc;
	
	BuiltinScriptRawInfo(String name, ANTLRParser parser) {
		this.name = name;
		map = new HashMap<>();
		includes = new ArrayList<>();
		init(parser);
	}
	
	public String getName() {
		return name;
	}
	
	public String getScriptDoc() {
		return scriptDoc;
	}
	
	public Collection<String> getAllIncludes() {
		return includes;
	}
	
	public Collection<GlobalFuncRawInfo> getAllFunctions() {
		return map.values();
	}
	
	public GlobalFuncRawInfo getFunction(String name) {
		return map.get(name);
	}

	private void init(ANTLRParser parser) {
		// Use the first block comment as the script doc.
		List<Token> tokens = parser.getAllTokens();
		for (Token tok : tokens) {
			int typ = tok.getType();
			if (typ == JulianLexer.BLOCK_COMMENT) {
				scriptDoc = tok.getText();
				break;
			} else if (typ != JulianLexer.LINE_COMMENT 
					&& typ != JulianLexer.NEW_LINE) {
				break;
			}
		}
		
		ProgramContext pc = parser.getAstInfo().getAST();
		
		PreambleContext prc = pc.preamble();
		if (prc != null) {
			List<Include_statementContext> includes = prc.include_statement();
			if (includes != null) {
				for (Include_statementContext inc : includes) {
					String fname = ANTLRHelper.reEscapeAsString(inc.STRING_LITERAL().getText(), true);
					this.includes.add(fname);
				}
			}
		}
		
		ExecutableContext ec = pc.executable();
		if (ec != null) {
			Statement_listContext list = ec.statement_list();
			if (list != null) {
				for (StatementContext sc : list.statement()) {
					Declaration_statementContext decl = sc.declaration_statement();
					if (decl != null) {
						Function_declaratorContext funcDecl = decl.function_declarator();
						if (funcDecl != null) {
							// Return type
							TypeContext tc = decl.type();
							
							// Name
							Function_signatureContext sign = funcDecl.function_signature();
							String name = sign.IDENTIFIER().getText();
							
							// Params
							List<FuncParamRawInfo> fpInfos = new ArrayList<FuncParamRawInfo>();
							Function_signature_mainContext signMain = sign.function_signature_main();
							Function_parameter_listContext plist = signMain.function_parameter_list();
							for (Function_parameterContext fp : plist.function_parameter()) {
								FuncParamRawInfo fpInfo = new FuncParamRawInfo();
								fpInfo.name = fp.IDENTIFIER().getText();
								fpInfo.type = fp.type();
								fpInfos.add(fpInfo);
							}
							
							// Doc
							Token startTok = decl.getStart();
							String doc = parser.getDoc(startTok);
							
							GlobalFuncRawInfo funInfo = new GlobalFuncRawInfo();
							funInfo.name = name;
							funInfo.returnType = tc;
							funInfo.params = fpInfos;
							funInfo.rawDoc = doc;
							
							map.put(name, funInfo);
						}
					}
				}
			}
		}
	}
}

class GlobalFuncRawInfo {
	TypeContext returnType;
	String name;
	List<FuncParamRawInfo> params;
	String rawDoc;
}

class FuncParamRawInfo {
	String name;
	TypeContext type;
}
