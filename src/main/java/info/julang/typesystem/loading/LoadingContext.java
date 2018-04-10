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

package info.julang.typesystem.loading;

import info.julang.execution.InContextTypeResolver;
import info.julang.execution.namespace.NamespacePool;
import info.julang.execution.symboltable.ITypeTable;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.syntax.AttributeDeclInfo;
import info.julang.interpretation.syntax.ClassDeclInfo;
import info.julang.langspec.ast.JulianParser.ProgramContext;
import info.julang.modulesystem.ClassInfo;
import info.julang.modulesystem.IModuleManager;
import info.julang.parser.AstInfo;
import info.julang.typesystem.UnknownTypeException;
import info.julang.typesystem.jclass.ICompoundTypeBuilder;

import java.util.List;

/**
 * The runtime context used when loading a type. It is primarily derived from
 * {@link Context the user context} which, among other things, provides an
 * independent token stream for the type being loaded.
 * 
 * @author Ming Zhou
 */
public class LoadingContext {

	private ICompoundTypeBuilder builder;
	
	private ITypeTable tt;
	
	private NamespacePool nsPool;
	
	private IModuleManager mm;
	
	private AstInfo<ProgramContext> ainfo;
	
	private ClassDeclInfo declInfo;
	
	private Context context;
	
	private ILoadingState state;
	
	private List<AttributeDeclInfo> typeAttributes;
	
	LoadingContext(Context context, String typeName, ILoadingState state){
		this.context = context;
		this.state = state;
		this.builder = state.getBuilder();
		tt = context.getTypTable();
		mm = context.getModManager();
		
		ClassInfo cinfo = mm.getClassesByFQName(typeName);
		if(cinfo == null){
			throw new UnknownTypeException(typeName);
		} else {
			NamespacePool nsPool = new NamespacePool();
			nsPool.addNamespaceFromScriptInfo(cinfo.getScriptInfo());
			this.nsPool = nsPool;
			
			String modName = cinfo.getScriptInfo().getModuleInfo().getName();
			builder.setModuleName(modName);
		}
		
		declInfo = cinfo.getClassDeclInfo();
		ainfo = cinfo.getScriptInfo().getAstInfo();
		builder.setLocationInfo(ainfo);
		typeAttributes = declInfo.getAttributes();
	}
	
	ICompoundTypeBuilder getTypeBuilder() {
		return builder;
	}

	ITypeTable getTypeTable() {
		return tt;
	}
	
	InternalTypeResolver getTypeResolver(){
		return ((InContextTypeResolver)context.getTypeResolver()).getInternalTypeResolver();
	}

	NamespacePool getNamespacePool() {
		return nsPool;
	}

	IModuleManager getModuleManager() {
		return mm;
	}

	AstInfo<ProgramContext> getAstInfo() {
		return ainfo;
	}
	
	List<AttributeDeclInfo> getClassTypeAttributes(){
		return typeAttributes;
	}
	
	/**
	 * Get runtime context
	 * @return
	 */
	Context getContext() {
		return context;
	}
	
	ClassDeclInfo getClassDeclInfo() {
		return declInfo;
	}
	
	void addDependency(String typeName){
		state.addDependency(typeName);
	}
}
