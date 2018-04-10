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
import info.julang.interpretation.StatementBase;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.errorhandling.JSExceptionUtility;
import info.julang.interpretation.errorhandling.JulianScriptException;
import info.julang.langspec.ast.JulianParser.ExpressionContext;
import info.julang.langspec.ast.JulianParser.Throw_statementContext;
import info.julang.memory.value.JValue;
import info.julang.parser.AstInfo;

/**
 * The throw statement in Julian language.
 * <p/>
 * The throw statement has format:<pre><code> throw |expr| ;</code></pre>
 * 
 * Note that |expr| must return a value which derives from, or is of, 
 * <font color="green">System.Exception</font> type.</li>
 * <p/>
 * 
 * @author Ming Zhou
 */
public class ThrowStatement extends StatementBase {
	
	private AstInfo<Throw_statementContext> ainfo;
	
	public ThrowStatement(ThreadRuntime runtime, AstInfo<Throw_statementContext> ainfo) {
		super(runtime);
		this.ainfo = ainfo;
	}

	@Override
	public void interpret(Context context) {
		Throw_statementContext ast = ainfo.getAST();
		
		AstInfo<ExpressionContext> aec = ainfo.create(ast.expression());
		ExpressionStatement expr = new ExpressionStatement(runtime, aec);
		expr.interpret(context);
		
		Result res = expr.getResult();
		JValue jval = res.getReturnedValue(false);
		
		JulianScriptException jse = JSExceptionUtility.initializeAsScriptException(context, jval, ainfo);
		
		throw jse;
	}
}
