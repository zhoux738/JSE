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

import info.julang.external.exceptions.JSEError;
import info.julang.interpretation.errorhandling.IHasLocationInfo;
import info.julang.interpretation.syntax.AttributeDeclInfo;
import info.julang.interpretation.syntax.ClassDeclInfo;
import info.julang.scanner.ITokenStream;

import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.ParserRuleContext;

/**
 * Among data models used by module system (raw/declaration info for script, module, class, etc.), this is
 * the only one that's not shared between direct and lazy loading. As its name suggests, it is only used by
 * the latter.
 * <p>
 * When a class is loaded lazily, its containing info object would reside in runtime without really getting
 * parsed. Only by the time either its AST or attributes are demanded, will the stream get parsed and AST
 * built. All of these calls would come from type loader, which gets triggered when the type becomes used in 
 * the script code.
 * 
 * @author Ming Zhou
 */
public class LazyClassDeclInfo extends ClassDeclInfo {
	
	private RawScriptInfo minfo;
	
	public LazyClassDeclInfo(RawScriptInfo minfo){
		this.minfo = minfo;
	}
	
	@Override
	public ParserRuleContext getAST(){
		if (declTree == null){
			load();
		}
		
		return declTree;
	}

	@Override
	public List<AttributeDeclInfo> getAttributes(){
		if (declTree == null){
			load();
		}
		
		return attributes;
	}
	
	private void load() {
		Map<String, ClassDeclInfo> map = minfo.loadAllTypes();
		String name = getName();
		ClassDeclInfo loaded = map.remove(name);
		if (loaded == null) {
			throw new JSEError("Cannot load type '" + name + "' from module '" + minfo.getModuleName() + "'.");
		}
		this.declTree = loaded.getAST();
		this.attributes = loaded.getAttributes();
	}

	public void setLocationInfo(final String scriptFilePath, ITokenStream stream) {
		final int line = stream.peek().getLine();
		IHasLocationInfo info = new IHasLocationInfo(){
			@Override
			public String getFileName() {
				return scriptFilePath;
			}
			@Override
			public int getLineNumber() {
				return line;
			}
		};
		
		super.setLocationInfo(info);
	}
}
