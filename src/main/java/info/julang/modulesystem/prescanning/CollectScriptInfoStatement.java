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

import org.antlr.v4.runtime.Token;

import info.julang.interpretation.BadSyntaxException;
import info.julang.langspec.ast.JulianLexer;
import info.julang.langspec.ast.JulianParser.DeclarationsContext;
import info.julang.langspec.ast.JulianParser.Import_statementContext;
import info.julang.langspec.ast.JulianParser.Include_statementContext;
import info.julang.langspec.ast.JulianParser.Module_definitionContext;
import info.julang.langspec.ast.JulianParser.PreambleContext;
import info.julang.langspec.ast.JulianParser.ProgramContext;
import info.julang.langspec.ast.JulianParser.Type_declarationContext;
import info.julang.modulesystem.ModuleInfo;
import info.julang.modulesystem.prescanning.lazy.LazyClassStatement;
import info.julang.modulesystem.prescanning.lazy.LazyImportStatement;
import info.julang.modulesystem.prescanning.lazy.LazyModuleStatement;
import info.julang.modulesystem.prescanning.lazy.ModuleNameReader;
import info.julang.parser.LazyAstInfo;
import info.julang.scanner.ITokenStream;
import info.julang.scanner.ITokenStream.StreamPosition;

/**
 * An orchestrating statement that pre-scans a script and collects module information.
 * <p>
 * In general, it follows with:
 * <ul>
 * <li>Confirming the module name is right. (<code><b>module</b> a.b.c;</code>)</li>
 * <li>Collecting all the imported modules. (<code><b>import</b> d.e.f;</code>)</li>
 * <li>Collecting declared classes. (<code><b>class</b> MyClass { ... }</code>)</li>
 * </ul>
 * <p>
 * The prescanning happens in two flavors. A full prescanning causes the complete parsing
 * of the input file and assembly of AST, which can be used directly during interpretation.
 * The partial prescanning only collects information which are necessary for type detection
 * (i.e. what types are defined under this module), but doesn't assemble the AST. The AST
 * will be lazily assembled upon any type defined in this file being loaded into runtime.
 * <p>
 * This difference, however, is totally transparent to other parts of the engine. 
 * 
 * @author Ming Zhou
 */
public class CollectScriptInfoStatement implements PrescanStatement {
	
	private boolean fullyLoadNow;
	private boolean allowImplicitModuleName;
	
	/**
	 * Create a CollectScriptInfoStatement that can take either full or partial (lazy) loading path.
	 * 
	 * @param fullyLoadNow Whether to parse everything out (types etc.) and build ASTs for the entire file.
	 * The known usage: global script; analytical loading for IDE.
	 * @param allowImplicitModuleName Whether to allow the first statement to be simply "module;"
	 */
	public CollectScriptInfoStatement(boolean fullyLoadNow, boolean allowImplicitModuleName){
		this.fullyLoadNow = fullyLoadNow;
		this.allowImplicitModuleName = allowImplicitModuleName;
	}
	
	@Override
	public void prescan(RawScriptInfo info) {
		LazyAstInfo ainfo = info.getAstInfo();
		if (fullyLoadNow){
			if (ainfo.getBadSyntaxException() == null) {
				ProgramContext pctxt = ainfo.getAST();
				fullyLoad(pctxt, ainfo, info);
			}
		} else {
			// If this is lazy loading, try not parse first. It's possible that we can still collect all the module info
			// from the script despite having some grammar errors inside method definition.
			if (ainfo.getBadSyntaxException(false) == null) {
				ITokenStream stream = ainfo.getTokenStream();
				lazilyLoad(stream, ainfo, info);
			}
		}
	}

	// Fully load the module file and generate AST from it.
	public void fullyLoad(ProgramContext pctxt, LazyAstInfo ainfo, RawScriptInfo info){
		// 1) Module info
		boolean isLooseScript = false;
		PreambleContext preamble = pctxt.preamble();
		Module_definitionContext module = preamble.module_definition();
		if (module == null) {
			String preModName = info.getOption().getPresetModuleName();
			if(preModName == null){
				throw new IllegalModuleFileException(info, preamble, "A module file must start with module declaration.");
			} else {
				// Module name has been mandated externally.
				isLooseScript = ModuleInfo.DEFAULT_MODULE_NAME.equals(preModName);
				info.setModuleName(preModName);
			}
		} else {
			ModuleStatement ms = new ModuleStatement(
				ainfo.create(module),
				allowImplicitModuleName ? info.getModuleName() : null);
			ms.prescan(info);
		}
		
		// 2) Imported modules
		for (Import_statementContext impCntx : preamble.import_statement()) {
			ImportStatement is = new ImportStatement(ainfo.create(impCntx));
			is.prescan(info);
		}
		
		// 3) Include files
		if (isLooseScript) {
			for (Include_statementContext incCntx : preamble.include_statement()) {
				IncludeStatement is = new IncludeStatement(ainfo.create(incCntx));
				is.prescan(info);
			}
		}
		
		// (The following check is not necessary since the syntax prevents a file from having both module and include statements.
		//  however, we do also demand that include statement be only recognized when the module is default, i.e. loaded from a
		//  loose script. As of 0.1.34, the preset module name can only be <default> or <implicit>, with the latter synthesized
		//  internally by the engine, which is guaranteed to not feature any include)
		//
		// else if (preamble.include_statement().size() > 0) {
		//	throw new IllegalModuleFileException(info, preamble, "A loose file must not contain include statements.");
		// }
		
		// 4) Definitions
		DeclarationsContext decls = pctxt.declarations();
		for (Type_declarationContext typDecl : decls.type_declaration()){
			ClassStatement cs = new ClassStatement(ainfo.create(typDecl));
			cs.prescan(info);
		}
	}
	
	// Only partially load the module, collect only information necessary for type detection.
	// The AST will be assembled only when any type defined in this module file is to be loaded.
	private void lazilyLoad(ITokenStream stream, LazyAstInfo ainfo, RawScriptInfo info) {
		stream.seek(StreamPosition.START, 0);
		
		boolean hasModStmt = false;
		ModuleNameReader reader = new ModuleNameReader(info);
		Token tok = stream.peek();
		if(tok.getType() != JulianLexer.MODULE){
			String preModName = info.getOption().getPresetModuleName();
			if(preModName == null){
				throw new IllegalModuleFileException(info, stream, "A module file must start with module declaration.");
			} else {
				// Module name has been mandated externally.
				info.setModuleName(preModName);
			}
		} else {
			stream.next();
			LazyModuleStatement ms = new LazyModuleStatement(
				reader, this.allowImplicitModuleName ? info.getModuleName() : null);
			ms.prescan(stream, info);
			tok = stream.peek();
			hasModStmt = true;
		}
		
		if(tok.getType() == JulianLexer.MODULE){
			throw new IllegalModuleFileException(info, stream, "There can be only one module declaration.");
		}
		
		while((tok = stream.peek()).getType() == JulianLexer.IMPORT){
			stream.next();
			LazyImportStatement is = new LazyImportStatement(reader);
			is.prescan(stream, info);
		}
		
		if(tok.getType() == JulianLexer.MODULE){
			if(hasModStmt){
				throw new IllegalModuleFileException(info, stream, "There can be only one module declaration.");
			} else {
				throw new IllegalModuleFileException(info, stream, "Module declaration must appear at the beginning of script file.");
			}
		}

		// IMPLEMENTATION NOTES:
		// If you change the list of the tokens below, you most likely need to change StreamBasedSyntaxHelper as well.
		
		tok = stream.peek();
		switch(tok.getType()){
		case JulianLexer.PUBLIC:
		case JulianLexer.INTERNAL:
		case JulianLexer.CLASS:
		case JulianLexer.INTERFACE:
		case JulianLexer.ATTRIBUTE:
		case JulianLexer.ENUM:
		case JulianLexer.ABSTRACT:
		case JulianLexer.STATIC:
		case JulianLexer.FINAL:		
		case JulianLexer.LEFT_BRACKET: // annotation
			LazyClassStatement cs = new LazyClassStatement();
			try {
				cs.prescan(stream, info);
			} catch (BadSyntaxException e) {
				// Ignore because we allow loose code (non-class definition) in script file.
			}
		default:
			break;
		}
		
		tok = stream.peek();
		if(tok.getType() == JulianLexer.MODULE || tok.getType() == JulianLexer.IMPORT){
			throw new IllegalModuleFileException(info, stream, 
				"Module declaration/imports must appear at the beginning of script file, before class definition.");
		}
	}
}
