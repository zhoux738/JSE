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
import info.julang.interpretation.expression.GeneralExpression;
import info.julang.interpretation.expression.KnownOperators;
import info.julang.interpretation.expression.Operand;
import info.julang.langspec.ast.JulianParser.E_primaryContext;
import info.julang.langspec.ast.JulianParser.E_unaryContext;
import info.julang.langspec.ast.JulianParser.ExpressionContext;
import info.julang.langspec.ast.JulianParser.PrimaryContext;
import info.julang.parser.ANTLRHelper;
import info.julang.parser.AstInfo;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.tree.TerminalNode;

// | ( PLUS | MINUS | NEGATION ) expression                                 # e_unary         // 20 (no pre INCREMENT | DECREMENT )
public class UnaryExpression extends GeneralExpression {

	private List<ExpressionContext> subexprs;
	private Operand computedOperand;
	
	public UnaryExpression(ThreadRuntime rt, AstInfo<ExpressionContext> ec) {
		super(rt, ec);
		
		E_unaryContext eic = (E_unaryContext)ec.getAST();
		if (eic.NEGATION() != null){
			op = KnownOperators.NOT;
		} else if (eic.MINUS() != null){
			op = KnownOperators.MINUS;
		} else if (eic.COMPLEMENT() != null){
			op = KnownOperators.COMPLEMENT;
		} else if (eic.PLUS() != null){
			op = KnownOperators.PLUS;
		}
		
		ExpressionContext sec = eic.expression();
		
		// If this is a negative operator (-), check if it's followed by a number literal. If so, we must process it right now.
		// This is because the normal logic for '-' operator is to apply a multiplication between -1 and the other value. For 
		// example, if that value is 2147483648, what the programmer really meant is -2147483648. Parsing 2147483648, however, 
		// would cause error since integer's range is [-2147483648, 2147483647].
		if (op == KnownOperators.MINUS){
			if (sec instanceof E_primaryContext){
				PrimaryContext prc = ((E_primaryContext)sec).primary();
				TerminalNode node = prc.INTEGER_LITERAL();
				if (node != null) {
					// integer literal
					computedOperand = Operand.createIntOperand(ANTLRHelper.parseIntLiteral("-" + node.getText()));
				} else {
					node = prc.REAL_LITERAL();
					if (node != null) {
						// float literal
						computedOperand = Operand.createFloatOperand(ANTLRHelper.parseFloatLiteral("-" + node.getText()));
					}
				}
			}
		}

		subexprs = new ArrayList<ExpressionContext>();
		subexprs.add(sec);
	}
	
	@Override
	public Operand evaluate(Context context){
		if (computedOperand != null) {
			return computedOperand;
		} else {
			return super.evaluate(context);
		}
	}

	@Override
	protected List<ExpressionContext> getSubExpressions(AstInfo<ExpressionContext> ec) {
		return subexprs;
	}

}
