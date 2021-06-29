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

import info.julang.execution.Result;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.interpretation.ExitCause;
import info.julang.interpretation.StatementBase;
import info.julang.interpretation.context.Context;
import info.julang.langspec.ast.JulianParser.ExecutableContext;
import info.julang.langspec.ast.JulianParser.StatementContext;
import info.julang.langspec.ast.JulianParser.Statement_listContext;
import info.julang.parser.AstInfo;

import java.util.List;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;

/**
 * A block statement corresponds to a code block, ended by any of the specified tokens passed along
 * with {@link BlockStatement#BlockStatement(ThreadRuntime, AstInfo, StatementOption) the constructor}. 
 * 
 * @author Ming Zhou
 */
public class BlockStatement extends StatementBase implements IHasResult, IHasExitCause {
	
	private StatementOption option;
	
	private AstInfo<ExecutableContext> exec;
	
	private StatementsExecutor se;
	
	/**
	 * Create a new BlockStatement. A block statement represents a code block.
	 * The code is provided by <b>scanner</b>, and is ended by any of <b>endingTokens</b>.
	 * <p>
	 * Ending tokens will also be consumed by this statement.
	 * <p>
	 * This block doesn't allow the following statement:
	 * <ul>
	 * <li>Continue statement (<code>continue</code>)</li>
	 * <li>Break statement (<code>break</code>)</li>
	 * <li>Function definition (<code>int fun(...)</code>)</li>
	 * <li>Class definition (<code>class { ... }</code>)</li>
	 * </ul>
	 * If any of these is needed, use {@link #BlockStatement(ThreadRuntime, TokenStream, Token[], StatementOption) the alternative constructor} instead.
	 * <p>
	 * @param runtime
	 * @param exec AST for the executable body
	 * @param stream
	 * @param endingTokens
	 */
	public BlockStatement(ThreadRuntime runtime, AstInfo<ExecutableContext> exec, StatementOption option){
		super(runtime);
		this.option = option;
		this.exec = exec;
	}
	
	@Override
	public void interpret(Context context) {
		// Process each statement
		ExecutableContext ec = exec.getAST();
		if (ec != null){
			Statement_listContext slist = ec.statement_list();
			if (slist != null){
				List<StatementContext> stmtCntxs = slist.statement();
				se = new StatementsExecutor(runtime, exec.create(slist), option);
				se.interpretStatments(stmtCntxs, context);
			}
		}
	}
	
	@Override
	public Result getResult(){
		return se != null ? se.getResult() : Result.Void;
	}

	@Override
	public ExitCause getExitCause(){
		return se != null ? se.getExitCause() : ExitCause.UNDEFINED;
	}
}
