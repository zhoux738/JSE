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
import info.julang.interpretation.expression.operator.DotOp;
import info.julang.langspec.ast.JulianParser.E_dotContext;
import info.julang.langspec.ast.JulianParser.ExpressionContext;
import info.julang.parser.AstInfo;

// | expression LEFT_BRACKET expression RIGHT_BRACKET                       # e_indexer       // 10
public class DotExpression extends ExpressionBase {
	
	public DotExpression(ThreadRuntime rt, AstInfo<ExpressionContext> ec) {
		super(rt, ec, null);
		op = new DotOp(rt);
	}

	@Override
	public Operand evaluate(Context context) {
		E_dotContext edc = (E_dotContext)(ec.getAST());
		ExpressionContext _ec = edc.expression();
		
		Operand[] operands = new Operand[2];
		IExpression expr = getExpression(ec.create(_ec));
		operands[0] = expr.evaluate(context);

		Operand nod = Operand.createNameOperand(edc.IDENTIFIER().getText());
		operands[1] = nod;
		
		Operand ores = op.apply(context, operands);
		return ores;
	}

}
