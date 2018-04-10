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

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import info.julang.execution.threading.ThreadRuntime;
import info.julang.external.exceptions.JSEError;
import info.julang.interpretation.expression.GeneralExpression;
import info.julang.interpretation.expression.KnownOperators;
import info.julang.interpretation.expression.operator.RuntimeAwareOnSelf2Op;
import info.julang.langspec.ast.JulianLexer;
import info.julang.langspec.ast.JulianParser.Assignment_operatorContext;
import info.julang.langspec.ast.JulianParser.E_assignContext;
import info.julang.langspec.ast.JulianParser.ExpressionContext;
import info.julang.parser.AstInfo;

// | <assoc=right> expression assignment_operator expression                # e_assign        // 70
public class AssignExpression extends GeneralExpression {

	private List<ExpressionContext> subexprs;
	
	public AssignExpression(ThreadRuntime rt, AstInfo<ExpressionContext> ec) {
		super(rt, ec);
		
		E_assignContext aec = (E_assignContext)(ec.getAST());
		Assignment_operatorContext opc = aec.assignment_operator();
		
		ParseTree pt = opc.getChild(0);
		RuntimeException rex = null;
		
	    //assignment_operator 
	    //  : ASSIGN                    // '=';
	    //  | PLUS_SELF                 // '+=';
	    //  | MINUS_SELF                // '-=';
	    //  | MULTIPLY_SELF             // '*=';
	    //  | DIVIDE_SELF               // '/=';
	    //  | MODULO_SELF               // '%=';
	    //  | BITWISE_AND_SELF          // '&=';
	    //  | BITWISE_OR_SELF           // '|=';
	    //  | BITWISE_COMPLEMENT_SELF   // '^=';
	    //  | BITWISE_LEFT_SHIFT_SELF   // '<<=';
	    //  ;
		
		try {
			TerminalNode node = (TerminalNode)pt;
			switch(node.getSymbol().getType()){
			case JulianLexer.ASSIGN:
				op = KnownOperators.ASSIGN;
				break;
			case JulianLexer.PLUS_SELF:
				op = KnownOperators.ADD_SELF;
				break;
			case JulianLexer.MINUS_SELF:
				op = KnownOperators.SUB_SELF;
				break;
			case JulianLexer.MULTIPLY_SELF:
				op = KnownOperators.MULTIPLY_SELF;
				break;
			case JulianLexer.DIVIDE_SELF:
				op = RuntimeAwareOnSelf2Op.makeDivideSelf(rt);
				break;
			case JulianLexer.MODULO_SELF:
				op = RuntimeAwareOnSelf2Op.makeModuloSelf(rt);
				break;
			case JulianLexer.BITWISE_AND_SELF:
				op = KnownOperators.BITWISE_AND_SELF;
				break;
			case JulianLexer.BITWISE_OR_SELF:
				op = KnownOperators.BITWISE_OR_SELF;
				break;
			case JulianLexer.BITWISE_XOR_SELF:
				op = KnownOperators.BITWISE_XOR_SELF;
				break;
			case JulianLexer.BITWISE_RIGHT_SHIFT_SELF:
				op = KnownOperators.BITWISE_RSHIFT_SELF;
				break;
			case JulianLexer.BITWISE_LEFT_SHIFT_SELF:
				op = KnownOperators.BITWISE_LSHIFT_SELF;
				break;
			default:
			}
		} finally {
			if (rex != null){
				throw rex;
			} else if (op == null){
				throw new JSEError("Unrecognized operator: " + pt.getText());
			}
		}
		
		subexprs = aec.expression();
	}

	@Override
	protected List<ExpressionContext> getSubExpressions(AstInfo<ExpressionContext> ec) {
		return subexprs;
	}

}
