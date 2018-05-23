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
import info.julang.interpretation.expression.operand.TypeOperand;
import info.julang.interpretation.syntax.ParsedTypeName;
import info.julang.interpretation.syntax.SyntaxHelper;
import info.julang.langspec.ast.JulianParser.ExpressionContext;
import info.julang.langspec.ast.JulianParser.TypeContext;
import info.julang.parser.AstInfo;
import info.julang.typesystem.JType;

/**
 * Shared based for expressions which involve a type and a sub-expression.
 * 
 * @author Ming Zhou
 */
public abstract class TypeExpressionBase extends ExpressionBase {

	private TypeContext tc;
	private int indexOfValue;
	
	/**
	 * @param rt runtime
	 * @param op the operator
	 * @param tc the AST for type
	 * @param ec the AST for expression. Can be null if indexOfValue == -1
	 * @param indexOfValue the index of operand that represents value. Can be -1, 0 and 1. If -1, there is no value operand in this expression.
	 */
	protected TypeExpressionBase(ThreadRuntime rt, Operator op, TypeContext tc, AstInfo<ExpressionContext> ec, int indexOfValue) {
		super(rt, ec, op);
		this.tc = tc;
		this.indexOfValue = indexOfValue;
	}

	@Override
	public Operand evaluate(Context context) {
		boolean indexAndType = indexOfValue >= 0;
		Operand[] operands = new Operand[indexAndType ? 2 : 1];
		if (indexAndType) {
			IExpression expr = getExpression(ec);
			operands[indexOfValue] = expr.evaluate(context);
		}

		ParsedTypeName ptn = SyntaxHelper.parseTypeName(tc);
		JType type = context.getTypeResolver().resolveType(ptn);
		operands[indexAndType ? 1 - indexOfValue : 0] = new TypeOperand(type);
		
		Operand ores = op.apply(context, operands);
		return ores;
	}

}
