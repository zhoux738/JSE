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

import java.util.ArrayList;
import java.util.List;

import info.julang.execution.threading.ThreadRuntime;
import info.julang.interpretation.expression.GeneralExpression;
import info.julang.interpretation.expression.operator.CallFuncOp;
import info.julang.langspec.ast.JulianParser.ArgumentContext;
import info.julang.langspec.ast.JulianParser.Argument_listContext;
import info.julang.langspec.ast.JulianParser.E_function_callContext;
import info.julang.langspec.ast.JulianParser.ExpressionContext;
import info.julang.langspec.ast.JulianParser.Function_callContext;
import info.julang.parser.AstInfo;

// | expression function_call                                               # e_function_call
public class CallFuncExpression extends GeneralExpression {

	private ExpressionContext callee;
	private List<ArgumentContext> args;
	
	public CallFuncExpression(ThreadRuntime rt, AstInfo<ExpressionContext> ec) {
		super(rt, ec);
		
		E_function_callContext fec = (E_function_callContext)ec.getAST();
		callee = fec.expression();
		Function_callContext fca = fec.function_call();
		Argument_listContext alc = fca.argument_list();
		if (alc != null){
			args = alc.argument();
			op = new CallFuncOp(rt, args.size());
		} else {
			args = new ArrayList<ArgumentContext>();
			op = new CallFuncOp(rt, 0);
		}
	}

	// The sub-expressions include callee at 0th place and all the arguments in the passed order.
	@Override
	protected List<ExpressionContext> getSubExpressions(AstInfo<ExpressionContext> ec) {
		List<ExpressionContext> subexprs = new ArrayList<ExpressionContext>();
		
		// 1) Callee
		subexprs.add(callee);
		
		// 2) Arguments
		for (ArgumentContext ac : args) {
			subexprs.add(ac.expression());
		}
		
		return subexprs;
	}

}
