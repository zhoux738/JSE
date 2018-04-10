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

import java.util.List;

import info.julang.execution.threading.ThreadRuntime;
import info.julang.interpretation.expression.GeneralExpression;
import info.julang.interpretation.expression.KnownOperators;
import info.julang.langspec.ast.JulianParser.E_bitwise_andContext;
import info.julang.langspec.ast.JulianParser.ExpressionContext;
import info.julang.parser.AstInfo;

// | expression BITWISE_AND expression                                      # e_bitwise_and   // 55
public class BitwiseAndExpression extends GeneralExpression {

	public BitwiseAndExpression(ThreadRuntime rt, AstInfo<ExpressionContext> ec) {
		super(rt, KnownOperators.BITWISE_AND, ec);
	}

	@Override
	protected List<ExpressionContext> getSubExpressions(AstInfo<ExpressionContext> ec) {
		return ((E_bitwise_andContext)ec.getAST()).expression();
	}

}
