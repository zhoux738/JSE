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

import info.julang.modulesystem.RequirementInfo;
import info.julang.modulesystem.prescanning.RawScriptInfo;
import info.julang.scanner.ITokenStream;

/**
 * Read the script file and parse the continuous "<code>import ...;</code>" statements.
 * <p>
 * There are two possible syntaxes for import statement, <p>
 * either<pre>
 * <code>import a.b.c;</code></pre>
 * or <pre>
 * <code>import a.b.c as xyz;</code></pre>
 * 
 * @author Ming Zhou
 */
public class LazyImportStatement implements LazyPrescanStatement {

	private ModuleNameReader reader;
	
	public LazyImportStatement(ModuleNameReader reader){
		this.reader = reader;
	}
	
	// Starts right after the import keyword.
	@Override
	public void prescan(ITokenStream stream, RawScriptInfo info) {		
		StringBuilder fullName = new StringBuilder();
		String alias = reader.readModuleName(stream, fullName, false);
		
		String name = fullName.toString();
		if(!"System".equals(name)){
			info.addRequirement(new RequirementInfo(name, alias));		
		}
	}

}
