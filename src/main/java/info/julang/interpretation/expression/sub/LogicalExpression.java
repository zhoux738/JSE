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

package info.julang.interpretation.expression.sub;

import info.julang.execution.threading.ThreadRuntime;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.expression.ExpressionBase;
import info.julang.interpretation.expression.IExpression;
import info.julang.interpretation.expression.Operand;
import info.julang.interpretation.expression.Operator;
import info.julang.langspec.ast.JulianParser.ExpressionContext;
import info.julang.parser.AstInfo;

public abstract class LogicalExpression extends ExpressionBase {

	private boolean shorcutOnTrue;
	
	protected LogicalExpression(ThreadRuntime rt, AstInfo<ExpressionContext> ec, Operator op, boolean shorcutOnTrueOrFalse) {
		super(rt, ec, op);
		this.shorcutOnTrue = shorcutOnTrueOrFalse;
	}

	@Override
	public Operand evaluate(Context context){
		// Evaluate the left expression
		ExpressionContext left = getLeftExpression(ec);
		IExpression exp = getExpression(ec.create(left));
		Operand od = exp.evaluate(context);

		// See if we can shortcut
		boolean lval = asBoolean(context, od);
		if (lval && shorcutOnTrue){ // for ||, if the left value is true, shortcut
			return od;
		} else if (!lval && !shorcutOnTrue){ // for &&, if the left value is false, shortcut
			return od;
		}
		
		// Continue to evaluate the right expression
		Operand[] ods = new Operand[2];
		ods[0] = od;

		ExpressionContext right = getRightExpression(ec);
		exp = getExpression(ec.create(right));
		ods[1] = exp.evaluate(context);
		
		// Apply the operator on the two parts
		return op.apply(context, ods);
	}

	protected abstract ExpressionContext getLeftExpression(AstInfo<ExpressionContext> ec);
	protected abstract ExpressionContext getRightExpression(AstInfo<ExpressionContext> ec);
}
