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
import info.julang.langspec.ast.JulianParser.Do_statementContext;
import info.julang.parser.AstInfo;

/**
 * The do-while statement in Julian language.
 * <p>
 * The do-while statement has the form: <pre><code> do{
 *   ... (statements)
 * } while(|bool-expr|)</code></pre>
 *
 * @author Ming Zhou
 */
public class DoWhileStatement extends MultiBlockStatementBase {
	
	/**
	 * Type: Do_statementContext
	 */
	private AstInfo<Do_statementContext> ainfo;
	
	/**
	 * Create a new while statement. The PC must be pointed at right <b>after</b> 'while' keyword.
	 * 
	 * @param runtime
	 * @param stream
	 * @param option
	 */
	public DoWhileStatement(ThreadRuntime runtime, AstInfo<Do_statementContext> ainfo, StatementOption option) {
		super(runtime, option);
		this.ainfo = ainfo;
	}

	@Override
	public void interpret(Context context) {
		// 4.6. Do
		// do_statement 
		//	   : DO compound_statement WHILE LEFT_PAREN expression RIGHT_PAREN SEMICOLON
		//     ;
		Do_statementContext ast = ainfo.getAST();
		Compound_statementContext body = ast.compound_statement();
		
		while(true){
			boolean shouldBreak = performLoopBody(context, ainfo.create(body));
			
			if(shouldBreak){
				// breaks from loop body 
				break;
			} else {
				// evaluate expression to determine continuation
				ExpressionStatement bes = new ExpressionStatement(runtime, ainfo.create(ast.expression()));
				bes.interpret(context);
				boolean result = bes.getBooleanResult();
				if(!result){
					exitCause = ExitCause.THROUGH;
					break;
				}
			}		
		}
	}
}