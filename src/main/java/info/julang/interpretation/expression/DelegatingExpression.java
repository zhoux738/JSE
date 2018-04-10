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

package info.julang.interpretation.expression;

import info.julang.execution.threading.ThreadRuntime;
import info.julang.interpretation.context.Context;
import info.julang.langspec.ast.JulianParser.ExpressionContext;
import info.julang.memory.value.JValue;
import info.julang.parser.AstInfo;

/**
 * An expression that simply creates a more proper expression to run. 
 * 
 * @author Ming Zhou
 */
public class DelegatingExpression extends ExpressionBase {
	
	public DelegatingExpression(ThreadRuntime rt, AstInfo<ExpressionContext> ec) {
		super(rt, ec, null);
	}

	public Operand evaluate(Context context){
		IExpression expr = getExpression(ec);
		return expr.evaluate(context);
	}
	
	/**
	 * Evaluate the expression and return the value.
	 * 
	 * @param context
	 * @return a value derived from the returned operand as result of evaluation.
	 */
	public JValue getResult(Context context){
		// Replicated code as evaluate() to save stack usage
		IExpression expr = getExpression(ec);
		Operand od = expr.evaluate(context);
		return getValue(context, od);
	}

}
