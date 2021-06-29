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

import java.util.List;

import info.julang.execution.threading.ThreadRuntime;
import info.julang.interpretation.ExitCause;
import info.julang.interpretation.context.Context;
import info.julang.langspec.ast.JulianParser.Compound_statementContext;
import info.julang.langspec.ast.JulianParser.If_statementContext;
import info.julang.parser.AstInfo;

/**
 * The if statement in Julian language.
 * <p>
 * The if statement is comprised of multiple parts:<pre><code> if(|bool-expr|)
 *   |clause|
 * else if
 *   |clause|
 * ... ...
 * else 
 *   |clause|
 *  </code></pre>
 * Note that:
 * <ul>
 *   <li>|clause| can be a single expression or a script block enclosed by {}</li>
 *   <li>there can be any number of else if clauses</li>
 *   <li>there can be zero or one else clause</li>
 * </ul>
 *
 * @author Ming Zhou
 */
public class IfStatement extends MultiBlockStatementBase {
	
	/**
	 * Type: If_statementContext<pre><code>// 4.3. If
	 *if_statement 
	 *    : IF LEFT_PAREN expression RIGHT_PAREN loop_body (ELSE loop_body)? 
	 *    ;<code></pre>
	 */
	private AstInfo<If_statementContext> ainfo;
	
	/**
	 * Create a new if statement. The PC must be pointed at right <b>after</b> 'if' keyword.
	 * 
	 * @param runtime
	 * @param stream
	 * @param option
	 */
	public IfStatement(ThreadRuntime runtime, AstInfo<If_statementContext> ainfo, StatementOption option) {
		super(runtime, option);
		this.ainfo = ainfo;
	}

	@Override
	public void interpret(Context context) {
		// if_statement 
		//    : IF LEFT_PAREN expression RIGHT_PAREN loop_body (ELSE loop_body)? 
		//    ;<code></pre>
		If_statementContext ast = ainfo.getAST();
		ExpressionStatement bes = new ExpressionStatement(runtime, ainfo.create(ast.expression()));
		bes.interpret(context);
		boolean result = bes.getBooleanResult();
		
		List<Compound_statementContext> blocks = ast.compound_statement();
		if(result){
			Compound_statementContext ifBlock = blocks.get(0);
			StatementOption skipOption = StatementOption.createInheritedOption(option);
			performSection(context, ainfo.create(ifBlock), skipOption, false);
		} else if (blocks.size() == 2){ // else branch exists
			Compound_statementContext elseBlock = blocks.get(1);
			StatementOption skipOption = StatementOption.createInheritedOption(option);
			performSection(context, ainfo.create(elseBlock), skipOption, false);
		} else {
			exitCause = ExitCause.THROUGH;
		}
	}
}