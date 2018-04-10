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

package info.julang.modulesystem.prescanning.lazy;

import info.julang.interpretation.BadSyntaxException;
import info.julang.langspec.ast.JulianLexer;
import info.julang.modulesystem.prescanning.IllegalModuleFileException;
import info.julang.modulesystem.prescanning.LazyClassDeclInfo;
import info.julang.modulesystem.prescanning.RawClassInfo;
import info.julang.modulesystem.prescanning.RawScriptInfo;
import info.julang.scanner.ITokenStream;
import info.julang.scanner.ReadingUtility;

import org.antlr.v4.runtime.Token;

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
public class LazyClassStatement implements LazyPrescanStatement {

	// Starts right before the first class keyword.
	@Override
	public void prescan(ITokenStream stream, RawScriptInfo info) {
		while(true){
			LazyClassDeclInfo declInfo = StreamBasedSyntaxHelper.parseClassDeclaration(stream, info);
			if(declInfo != null){
				Token tok = stream.next();
				if(tok.getType() == JulianLexer.LEFT_CURLY){
					// Locate the ending '}'
					ReadingUtility.locatePairedToken(
						stream, 
						JulianLexer.LEFT_CURLY, 
						JulianLexer.RIGHT_CURLY, 
						true, // startsWithOpenPair, 
						false // stopBeforeMatch
					);
				} else {
					throw new BadSyntaxException("Illegal class declaration syntax.");
				}
				
				boolean result = info.addClass(new RawClassInfo(declInfo.getName(), declInfo));
				if(!result){
					throw new IllegalModuleFileException(
						info, stream, "Duplicated class declaration with class name \"" + declInfo.getName() + "\".");
					// TODO - the location of stream is not right.
				}
			} else {
				break;
			}
		}
	}
}
