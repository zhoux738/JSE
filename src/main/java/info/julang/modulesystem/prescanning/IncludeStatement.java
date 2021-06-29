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

import java.io.File;
import java.io.IOException;

import info.julang.interpretation.BadSyntaxException;
import info.julang.langspec.ast.JulianParser.Include_statementContext;
import info.julang.modulesystem.IncludedFile;
import info.julang.modulesystem.scripts.InternalScriptLoader;
import info.julang.parser.ANTLRHelper;
import info.julang.parser.AstInfo;
import info.julang.util.OSTool;

/**
 * Read the script file and parse the continuous "<code>include ...;</code>" statements.
 * <p>
 * Examples:<pre>
 * <code>import "path/to/file1";</code>
 * <code>import "path\\to\\file2";</code></pre>
 * <p>
 * Both slashes are recognized since they are to be canonicalized anyway.
 * 
 * @author Ming Zhou
 */
public class IncludeStatement implements PrescanStatement {

	private AstInfo<Include_statementContext> ainfo;
	
	public IncludeStatement(AstInfo<Include_statementContext> ainfo){
		this.ainfo = ainfo;
	}
	
	// Starts right after the include keyword.
	@Override
	public void prescan(RawScriptInfo info) {
		Include_statementContext includeContext = ainfo.getAST();
		String rawStr = includeContext.STRING_LITERAL().getText();
		
		String str;
		try {
			str = ANTLRHelper.reEscapeAsString(rawStr, true);
		} catch (BadSyntaxException bse) {
			throw new IllegalModuleFileException(info, includeContext, "Included path cannot be parsed: " + rawStr);
		}
		
		if (InternalScriptLoader.hasScript(str)) {
			info.addInclusion(new IncludedFile(IncludedFile.ResolutionStrategy.EXTERNAL_THEN_BUILTIN, str, ainfo));
		} else {			
//			File file = new File(str);
//			String path;
//			try {
//				path = OSTool.canonicalizePath(file.getCanonicalPath());
//			} catch (IOException e) {
//				throw new IllegalModuleFileException(info, includeContext, "Included path cannot be resolved: " + path);
//			}
			
			info.addInclusion(new IncludedFile(IncludedFile.ResolutionStrategy.EXTERNAL_ONLY, str, ainfo));
		}
	}

}
