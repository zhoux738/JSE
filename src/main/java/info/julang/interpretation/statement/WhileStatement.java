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

package info.julang.interpretation.statement;

import info.julang.execution.threading.ThreadRuntime;
import info.julang.interpretation.ExitCause;
import info.julang.interpretation.context.Context;
import info.julang.langspec.ast.JulianParser.Compound_statementContext;
import info.julang.langspec.ast.JulianParser.While_statementContext;
import info.julang.parser.AstInfo;

/**
 * The while statement in Julian language.
 * <p/>
 * The while statement can take two forms:<pre><code> while(|bool-expr|){
 *   ... (statements)
 * }
 * 
 * </code>or<code>
 * 
 * while(|bool-expr|)
 *   expr
 *  </code></pre>
 *
 * @author Ming Zhou
 */
public class WhileStatement extends MultiBlockStatementBase {
	
	/**
	 * Type: While_statementContext
	 */
	private AstInfo<While_statementContext> ainfo;
	
	/**
	 * Create a new while statement. The PC must be pointed at right <b>after</b> 'while' keyword.
	 * 
	 * @param runtime
	 * @param stream
	 * @param option
	 */
	public WhileStatement(ThreadRuntime runtime, AstInfo<While_statementContext> ast, StatementOption option) {
		super(runtime, option);
		this.ainfo = ast;
	}

	@Override
	public void interpret(Context context) {
		// 4.5. While
		// while_statement 
		//	  : WHILE LEFT_PAREN expression RIGHT_PAREN compound_statement
		//	  ;
		While_statementContext ast = ainfo.getAST();
		Compound_statementContext body = ast.compound_statement();
		
		while(true){
			ExpressionStatement bes = new ExpressionStatement(runtime, ainfo.create(ast.expression()));
			bes.interpret(context);
			boolean result = bes.getBooleanResult();
			
			if(result){
				// condition met
				boolean shouldBreak = performLoopBody(context, ainfo.create(body));
				
				if(shouldBreak){
					break;
				} else {
					// otherwise, iterate for another round
					continue;
				}
			} else {
				exitCause = ExitCause.THROUGH;
				break;
			}	
		}
	}
}