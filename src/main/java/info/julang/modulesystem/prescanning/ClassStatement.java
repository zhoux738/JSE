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

import info.julang.interpretation.syntax.ClassDeclInfo;
import info.julang.interpretation.syntax.SyntaxHelper;
import info.julang.langspec.ast.JulianParser.Type_declarationContext;
import info.julang.parser.AstInfo;

/**
 * Read the script file and discover class declarations, collecting information about these classes 
 * to store in {@link RawScriptInfo}.
 * <p>
 * A class declaration in Julian has the form of <pre><code>class A : C, I1, I2, ... {
 *   ... ...
 *}</code></pre>where C is a class name and In is interface name. This class won't care too much 
 * about the semantics though.
 * <p>
 * @author Ming Zhou
 */
public class ClassStatement implements PrescanStatement {

	private AstInfo<Type_declarationContext> ainfo;
	
	public ClassStatement(AstInfo<Type_declarationContext> ainfo){
		this.ainfo = ainfo;
	}
	
	// Starts right before the first class keyword.
	@Override
	public void prescan(RawScriptInfo info) {
		ClassDeclInfo declInfo = SyntaxHelper.parseClassDeclaration(ainfo, info.getModuleName());
		RawClassInfo rci = new RawClassInfo(declInfo.getName(), declInfo);
		boolean result = info.addClass(rci);
		if(!result){
			throw new IllegalModuleFileException(
				info, 
				declInfo.getAST(),
				"Duplicated class declaration with class name \"" + declInfo.getName() + "\".");
		}
	}
}
