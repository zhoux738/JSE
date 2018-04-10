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
import info.julang.langspec.ast.JulianParser.E_equalContext;
import info.julang.langspec.ast.JulianParser.ExpressionContext;
import info.julang.parser.AstInfo;

// | expression ( EQUAL | NOT_EQUAL ) expression                            # e_equal         // 50
public class EqualExpression extends GeneralExpression {

	private List<ExpressionContext> subexprs;
	
	public EqualExpression(ThreadRuntime rt, AstInfo<ExpressionContext> ec) {
		super(rt, ec);
		
		E_equalContext aec = (E_equalContext)ec.getAST();
		if (aec.EQUAL() != null){
			op = KnownOperators.EQUAL;
		} else {
			op = KnownOperators.NOT_EQUAL;
		}
		
		subexprs = aec.expression();
	}

	@Override
	protected List<ExpressionContext> getSubExpressions(AstInfo<ExpressionContext> ec) {
		return subexprs;
	}

}
