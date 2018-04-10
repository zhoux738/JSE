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

import info.julang.langspec.ast.JulianParser.Import_statementContext;
import info.julang.modulesystem.RequirementInfo;
import info.julang.parser.AstInfo;

import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * Read the script file and parse the continuous "<code>import ...;</code>" statements.
 * <p/>
 * There are two possible syntaxes for import statement, <p/>
 * either<pre>
 * <code>import a.b.c;</code></pre>
 * or <pre>
 * <code>import a.b.c as xyz;</code></pre>
 * 
 * @author Ming Zhou
 */
public class ImportStatement implements PrescanStatement {

	private AstInfo<Import_statementContext> ainfo;
	
	public ImportStatement(AstInfo<Import_statementContext> ainfo){
		this.ainfo = ainfo;
	}
	
	// Starts right after the import keyword.
	@Override
	public void prescan(RawScriptInfo info) {
		Import_statementContext importContext = ainfo.getAST();
		String fname = importContext.composite_id().getText();
		String alias = null;
		TerminalNode aliasNode = importContext.IDENTIFIER();
		if (aliasNode != null) {
			alias = aliasNode.getText();
		}
		
		if(!"System".equals(fname)){
			info.addRequirement(new RequirementInfo(fname, alias));		
		}
	}

}
