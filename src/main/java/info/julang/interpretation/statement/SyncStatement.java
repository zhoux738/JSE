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

import info.julang.execution.Argument;
import info.julang.execution.Result;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.external.exceptions.JSEError;
import info.julang.interpretation.RuntimeCheckException;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.internal.FuncCallExecutor;
import info.julang.langspec.ast.JulianParser.BlockContext;
import info.julang.langspec.ast.JulianParser.ExpressionContext;
import info.julang.langspec.ast.JulianParser.Sync_statementContext;
import info.julang.memory.value.FuncValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.ObjectValue;
import info.julang.memory.value.RefValue;
import info.julang.parser.AstInfo;
import info.julang.typesystem.jclass.JClassMethodMember;
import info.julang.typesystem.jclass.JClassType;
import info.julang.typesystem.jclass.jufc.System.Concurrency.ScriptLock;

/**
 * The sync statement in Julian language.
 * <p/>
 * The sync statement has form:<pre><code> sync (expr) {
 *   ... ...
 * }</code></pre>
 * Note that:
 * <p/>
 *   <li><code>expr</code> evaluates to a value of type <code><font color="green">System.Concurrency.Lock</font></code></li>
 *   <li>the critical region must be enclosed in <code>{ }</code>. One-liner statement is not allowed.</li>
 * <p/>
 * The sync statement is a syntax sugar built on top of <code><font color="green">System.Concurrency.Lock</font></code>.
 * The same functionality can be achieved by calling <code>Lock.lock()</code> at the beginning and <code>Lock.unlock()</code>
 * at the end in a <code>finally</code> block.
 * 
 * @author Ming Zhou
 */
public class SyncStatement extends MultiBlockStatementBase {
	
	private AstInfo<Sync_statementContext> ainfo;
	
	/**
	 * Create a new try statement. The PC must be pointed at right <b>after</b> 'sync' keyword.
	 * 
	 * @param runtime
	 * @param stream
	 * @param option
	 */
	public SyncStatement(ThreadRuntime runtime, AstInfo<Sync_statementContext> ainfo, StatementOption option) {
		super(runtime, option);
		this.ainfo = ainfo;
	}
	
	@Override
	public void interpret(Context context) {
		// Evaluate the lock
		Sync_statementContext ast = ainfo.getAST();
		AstInfo<ExpressionContext> ai = ainfo.create(ast.expression());
		ExpressionStatement es = new ExpressionStatement(runtime, ai);
		es.interpret(context);
		Result res = es.getResult();
		if(!res.isSuccess()){
			throw res.getException();
		} else {
			JValue rv = res.getReturnedValue(false);
			ObjectValue ov = RefValue.dereference(rv);
			
			JClassType typ = (JClassType) context.getTypTable().getType(ScriptLock.FullTypeName);
			if(ov.getType() != typ){
				throw new RuntimeCheckException(
					"A sync statement must start with an expression that evaluates to an object of type '" + 
					ScriptLock.FullTypeName + "'. But saw a '" + ov.getType().getName() + "'.", ai);
			} else if(typ == null){
				throw new JSEError(ScriptLock.FullTypeName + " is not loaded.", SyncStatement.class);
			}
			
			BlockContext bc = ast.block();
			interpretCriticalArea(context, typ, ov, bc);
		}
	}

	private void interpretCriticalArea(Context context, JClassType typ, ObjectValue ov, BlockContext bc){
		FuncCallExecutor exe = new FuncCallExecutor(runtime);
		
		Argument[] args = Argument.CreateThisOnlyArguments(ov);
		
		// Lock
		callMethod(context, exe, typ, ScriptLock.MethodName_Lock, args);
		try {
			// Execute the critical region
			performBlock(context, ainfo.create(bc), option, true);
		} finally {
			// Unlock, regardless of exceptions (even if it's a bug)
			callMethod(context, exe, typ, ScriptLock.MethodName_Unlock, args);
		}
	}
	
	private void callMethod(Context context, FuncCallExecutor exe, JClassType typ, String methodName, Argument[] args){
		JClassMethodMember jmm = (JClassMethodMember) typ.getInstanceMemberByName(methodName);
		exe.invokeFunction(FuncValue.DUMMY, jmm.getMethodType(), methodName, args);
	}
}
