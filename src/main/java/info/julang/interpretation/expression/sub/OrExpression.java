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
import info.julang.interpretation.expression.KnownOperators;
import info.julang.langspec.ast.JulianParser.E_orContext;
import info.julang.langspec.ast.JulianParser.ExpressionContext;
import info.julang.parser.AstInfo;

// | expression OR expression                                               # e_or            // 61
public class OrExpression extends LogicalExpression {

	public OrExpression(ThreadRuntime rt, AstInfo<ExpressionContext> ec) {
		super(rt, ec, KnownOperators.OR, true);
	}
	
	@Override
	protected ExpressionContext getLeftExpression(AstInfo<ExpressionContext> ec) {
		return ((E_orContext)ec.getAST()).expression(0);
	}

	@Override
	protected ExpressionContext getRightExpression(AstInfo<ExpressionContext> ec) {
		return ((E_orContext)ec.getAST()).expression(1);
	}

}
