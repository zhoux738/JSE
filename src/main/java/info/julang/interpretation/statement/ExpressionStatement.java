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
import info.julang.execution.threading.JThread;
import info.julang.execution.threading.JThreadAbortedException;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.interpretation.RuntimeCheckException;
import info.julang.interpretation.StatementBase;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.expression.DelegatingExpression;
import info.julang.langspec.ast.JulianParser.ExpressionContext;
import info.julang.memory.value.BoolValue;
import info.julang.memory.value.JValue;
import info.julang.parser.AstInfo;
import info.julang.typesystem.basic.BoolType;

/**
 * A statement corresponding to an expression. 
 * <p>
 * Expression statement is a checkpoint for engine termination. For roughly every 8K expressions that have 
 * been executed in the engine across all threads, or 0.5 second that has passed, a termination check 
 * is performed to ensure the thread honors KILL signal sent by the engine.
 * 
 * @author Ming Zhou
 */
public class ExpressionStatement extends StatementBase implements IHasResult {
	
	private AstInfo<ExpressionContext> ainfo;
	protected Result result;

	// Counters used to check termination
	// The access to these counters is not thread safe, but that should be OK, as we only rely on 
	// the fact that the count is increasing, while the accuracy is less important.
	private static int s_total = 1;
	private static int s_batchTotal = 0;
	private static long s_timeMark = 0;
	
	/**
	 * Create a new ExpressionStatement that corresponds to a script expression.
	 * 
	 * @param runtime
	 * @param stream
	 * @param exprAst AST for the expression
	 */
	public ExpressionStatement(ThreadRuntime runtime, AstInfo<ExpressionContext> ainfo){
		super(runtime);
		this.ainfo = ainfo;
		
		s_batchTotal++;
		if (s_batchTotal >= 0b1111111) {
			// Accumulate the counter
			s_total += s_batchTotal;
			s_batchTotal = 0;
			
			// For every 128 expressions, check time once
			long newMark = System.currentTimeMillis();
			if (newMark - s_timeMark >= 500) {
				// Check every 0.5 second
				JThread thread = runtime.getJThread();
				if(thread != null && thread.checkTermination()){
					throw new JThreadAbortedException(thread);
				}
			} else if (s_total > 0b1111111111111) {
				// Also check every 8K expressions
				JThread thread = runtime.getJThread();
				if(thread.checkTermination()){
					throw new JThreadAbortedException(thread);
				}
				
				// Reset the counter
				s_total = 1;
			}
			
			// Reset the timer
			s_timeMark = newMark;
		}
	}
	
	@Override
	public void interpret(Context context){
		ExpressionContext ec = ainfo.getAST();
		DelegatingExpression de = new DelegatingExpression(runtime, ainfo.create(ec));
		JValue val = de.getResult(context);
		result = new Result(val);
	}

	@Override
	public Result getResult(){
		return result;
	}
	
	/**
	 * A convenient method to demand the result as a boolean value. 
	 * Will throw fatal error if the value is not a boolean.
	 * 
	 * @return
	 */
	public boolean getBooleanResult(){
		if(result != null){
			JValue val = result.getReturnedValue(true);
			if (val != null && val.getType() == BoolType.getInstance()){
				BoolValue bl = (BoolValue) val;
				return bl.getBoolValue();
			}
		}
		
		throw new RuntimeCheckException("The statement didn't produce a boolean value.");
	}
}
