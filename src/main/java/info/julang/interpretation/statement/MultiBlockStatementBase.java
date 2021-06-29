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
import info.julang.interpretation.errorhandling.JulianScriptException;
import info.julang.langspec.ast.JulianParser.BlockContext;
import info.julang.langspec.ast.JulianParser.Compound_statementContext;
import info.julang.langspec.ast.JulianParser.StatementContext;
import info.julang.langspec.ast.JulianParser.Statement_listContext;
import info.julang.parser.AstInfo;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.TokenStream;

/**
 * The multi-block statement base implements shared features of several statements which involve using sub-block,
 * including <code>if-else</code>, <code>for</code>, <code>while</code>, and <code>try</code>.
 * <p>
 * The class implements {@link IHasResult}. But it by default returns {@link Result#Void}. This is because
 * in general these statements do not yield a result by itself. But if it does, such as the case where a <code>return</code>
 * statement is used in a <code>for</code> loop, {@link IHasResult#getResult()} should keep returning the appropriate result.
 * 
 * @author Ming Zhou
 */
public abstract class MultiBlockStatementBase extends StatementBase implements IHasExitCause, IHasResult {

	protected ExitCause exitCause = ExitCause.UNDEFINED;
	
	protected Result result = Result.Void;
	
	protected StatementOption option;
	
	/**
	 * Create a new loop statement. The PC must be pointed at right <b>after</b> the keyword leading the loop structure.
	 * 
	 * @param runtime
	 * @param stream
	 * @param option
	 */
	public MultiBlockStatementBase(ThreadRuntime runtime, StatementOption option) {
		super(runtime);
		this.option = option;
	}
	
	@Override
	public ExitCause getExitCause(){
		return exitCause;
	}
	
	@Override
	public Result getResult(){
		return result;
	}
	
	/**
	 * Execute the loop body, which may be aborted by break statement.
	 * <p>
	 * The sub-class should call, but not override, this method.
	 * 
	 * @param context
	 * @param csc AST of loop body
	 * @return true if the caller should break; false to continue.
	 * @throws TokenStreamRelocationException
	 */
	protected boolean performLoopBody(Context context, AstInfo<Compound_statementContext> ainfo) {
		return performSection(context, ainfo, null, true);
	}
	
	/**
	 * Execute a section, which may be enclosed by { and }.
	 * <p>
	 * The sub-class should call, but not override, this method.
	 * 
	 * @param context
	 * @param csc AST of loop body
	 * @param option 
	 * @param consumeJumpOut true to convert {@link ExitCause#BROKEN} or {@link ExitCause#CONTINUED} to {@link ExitCause#THROUGH}. 
	 * This is desired when executing a section within a loop statement. 
	 * @return true if the caller should break; false to continue.
	 * @throws TokenStreamRelocationException
	 */
	protected boolean performSection(
		Context context, AstInfo<Compound_statementContext> ainfo, StatementOption option, boolean consumeJumpOut) {
		Compound_statementContext csc = ainfo.getAST();
		List<StatementContext> stmts = null;
		BlockContext block = csc.block();
		boolean isBlock = true;
		if (block != null) {
			Statement_listContext slist = block.statement_list();
			if (slist != null) {
				stmts = slist.statement();
			}
		} else {
			// If this is a single statement, synthesize the statement list
			// statement_list -> statement(+) -> compound_statement(1)
			StatementContext scont = new StatementContext(null, 0);
			scont.addChild(csc);
			
			stmts = new ArrayList<StatementContext>();
			stmts.add(scont);
			
			isBlock = false;
		}
		
		return performStatementList(context, ainfo, stmts, option, isBlock, consumeJumpOut, true);
	}
	
	/**
	 * Execute a section which is enclosed by { and }.
	 * <p>
	 * The sub-class should call, but not override, this method.
	 * 
	 * @param context
	 * @param csc AST of loop body
	 * @param option 
	 * @param consumeJumpOut true to convert {@link ExitCause#BROKEN} or {@link ExitCause#CONTINUED} to {@link ExitCause#THROUGH}. 
	 * This is desired when executing a section within a loop statement.
	 * @throws TokenStreamRelocationException
	 */
	protected void performBlock(Context context, AstInfo<BlockContext> ainfo, StatementOption option, boolean overwriteResult) {
		BlockContext bc = ainfo.getAST();
		Statement_listContext sast = bc.statement_list();
		List<StatementContext> stmts = sast != null ? sast.statement() : new ArrayList<StatementContext>(); 
		// We may want to optimize this a little - no need to call if the block is empty
		
		performStatementList(context, ainfo, stmts, option, true, false, overwriteResult);
	}
	
	protected StatementOption makeBreakableOption() {
		StatementOption blockOption = StatementOption.createInheritedOption(option);
		blockOption.setAllowBreak(true);
		blockOption.setAllowContinue(true);
		return blockOption;
	}
	
	/**
	 * Run through a list of statements. Set exit cause and result at the end.
	 * 
	 * @param context
	 * @param stmts
	 * @param option
	 * @param isBlock true if the list comes from a block (enclosed by '{' and '}'); false a single statement.
	 * @param consumeJumpOut true to convert {@link ExitCause#BROKEN} or {@link ExitCause#CONTINUED} to {@link ExitCause#THROUGH}.
	 * @param overwriteResult true to overwrite the previous result. Will always overwrite the previous result if the statements
	 * exited with {@link ExitCause#RETURNED} cause.
	 * @return true if the caller should break; false to continue.
	 */
	private boolean performStatementList(
		Context context, 
		AstInfo<? extends ParserRuleContext> ainfo, 
		List<StatementContext> stmts, 
		StatementOption option, 
		boolean isBlock, 
		boolean consumeJumpOut,
		boolean overwriteResult) {
		if (stmts == null) {
			stmts = new ArrayList<StatementContext>();
		}
		if (option == null){
			option = StatementOption.createInheritedOption(this.option);
			option.setAllowBreak(true);
			option.setAllowContinue(true);
		}
		
		BlockEvalResult ber = interpretBlock(context, ainfo, stmts, isBlock, option);
		
		// if the body is exited with break,
		boolean exit = false;
		ExitCause cause = ber.getExitCause();
		switch (cause){
		case BROKEN:
			if (consumeJumpOut) {
				exitCause = ExitCause.THROUGH;
			} else {
				exitCause = cause;
			}
			exit = true;
			break;
		case CONTINUED:
			if (consumeJumpOut) {
				exitCause = ExitCause.THROUGH;
			} else {
				exitCause = cause;
			}
			exit = false;
			break;
		case RETURNED:
			// if the body is returned, keep returning.
			overwriteResult = true;
			exitCause = ExitCause.RETURNED;
			exit = true;
			break;
		default:
		}

		if (overwriteResult){
			this.result = ber.getResult();
		}
		
		return exit;
	}

	/**
	 * Interpret or skip (if shouldSkip is true) a block (enclosed by '{' and '}' or a single statement 
	 * typically ended with ';').
	 * 
	 * @param context
	 * @param stmts a list of statements contained in this block, or the single statement.
	 * @param isBlock if true, the list comes from a block, even if the size is 0 or 1; otherwise the 
	 * statement is a single (which typically ends in ';') and the list's size is exactly one.
	 * @param blockOption the statement option for the block. 
	 * If null, will create an option inheriting from the given one passed along to the 
	 * {@link #LoopStatementBase(ThreadRuntime, TokenStream, StatementOption) constructor}.
	 * @return the result of block interpretation, providing exit cause and value result.
	 */
	private BlockEvalResult interpretBlock(
		Context context, 
		AstInfo<? extends ParserRuleContext> ainfo, 
		List<StatementContext> stmts, 
		boolean isBlock, 
		StatementOption blockOption) {
		if(blockOption==null){
			blockOption = StatementOption.createInheritedOption(option);
		}
		
		StatementsExecutor stmtsExec = new StatementsExecutor(runtime, ainfo, blockOption);

		if(isBlock){
			// enter scope
			context.getVarTable().enterScope();
			
			try {
				stmtsExec.interpretStatments(stmts, context);
				// leave scope
				context.getVarTable().exitScope();
			} catch (JulianScriptException jse){
				context.getVarTable().exitScope();
				throw jse;
			}
		} else {
			stmtsExec.interpretStatments(stmts, context);
		}
		
		return new BlockEvalResult(stmtsExec);
	}
	
	public static class BlockEvalResult implements IHasResult, IHasExitCause {
		private StatementsExecutor se;
		
		public BlockEvalResult(StatementsExecutor se) {
			this.se = se;
		}

		@Override
		public Result getResult() {
			return se.getResult();
		}

		@Override
		public ExitCause getExitCause() {
			return se.getExitCause();
		}
	}
}
