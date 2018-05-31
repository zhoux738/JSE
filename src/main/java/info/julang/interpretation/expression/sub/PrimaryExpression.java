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
import info.julang.external.exceptions.JSEError;
import info.julang.interpretation.RuntimeCheckException;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.context.ContextType;
import info.julang.interpretation.expression.ExpressionBase;
import info.julang.interpretation.expression.IExpression;
import info.julang.interpretation.expression.KnownOperators;
import info.julang.interpretation.expression.Operand;
import info.julang.interpretation.expression.operand.NameOperand;
import info.julang.interpretation.expression.operand.ValueOperand;
import info.julang.langspec.ast.JulianLexer;
import info.julang.langspec.ast.JulianParser.E_primaryContext;
import info.julang.langspec.ast.JulianParser.ExpressionContext;
import info.julang.langspec.ast.JulianParser.PrimaryContext;
import info.julang.memory.value.ObjectValue;
import info.julang.parser.ANTLRHelper;
import info.julang.parser.AstInfo;
import info.julang.typesystem.jclass.jufc.System.Util.JRegex;
import info.julang.typesystem.jclass.jufc.System.Util.RegexSanitizer;

import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * The lowest level of expression tree where token sequences are converted to operands.
 * 
 * @author Ming Zhou
 */
public class PrimaryExpression extends ExpressionBase {

	public PrimaryExpression(ThreadRuntime rt, AstInfo<ExpressionContext> ec) {
		super(rt, ec, KnownOperators.EVAL);
	}

	public Operand evaluate(Context context){
		Operand operand = null;
		E_primaryContext epc = (E_primaryContext)ec.getAST();
		PrimaryContext pc = epc.primary();
		
		// primary
	    //   :  IDENTIFIER
	    //   |  TRUE
	    //   |  FALSE
	    //   |  THIS
	    //   |  SUPER
	    //   |  INTEGER_LITERAL 
	    //   |  REAL_LITERAL 
	    //   |  CHAR_LITERAL 
	    //   |  STRING_LITERAL
	    //   |  REGEX_LITERAL
	    //   |  NULL
	    //   |  '(' expression ')'
	    //   ;
		TerminalNode node = (TerminalNode) pc.getChild(0);
		switch(node.getSymbol().getType()){
		case JulianLexer.IDENTIFIER:
			operand = Operand.createNameOperand(node.getText());
			break;
		case JulianLexer.TRUE:
			operand = Operand.createBoolOperand(true);
			break;
		case JulianLexer.FALSE:
			operand = Operand.createBoolOperand(false);
			break;
		case JulianLexer.THIS:
			ContextType ct = context.getContextType();
			if(ct == ContextType.FUNCTION || ct == ContextType.SMETHOD){
				throw new RuntimeCheckException("'this' cannot be used in script, global function or static method.");
			}
			operand = NameOperand.THIS;
			break;
		case JulianLexer.SUPER:
			if(context.getContextType() != ContextType.IMETHOD){
				throw new RuntimeCheckException("'super' can only be used in instance method.");
			}
			operand = NameOperand.SUPER;
			break;
		case JulianLexer.INTEGER_LITERAL:
			operand = Operand.createIntOperand(ANTLRHelper.parseIntLiteral(node.getText()));
			break;
		case JulianLexer.REAL_LITERAL:
			operand = Operand.createFloatOperand(ANTLRHelper.parseFloatLiteral(node.getText()));
			break;
		case JulianLexer.CHAR_LITERAL:
			operand = Operand.createCharOperand(ANTLRHelper.reEscapeAsChar(node.getText(), true));
			break;
		case JulianLexer.STRING_LITERAL:
			operand = Operand.createStringOperand(ANTLRHelper.reEscapeAsString(node.getText(), true));
			break;
		case JulianLexer.REGEX_LITERAL:
			String raw = node.getText();
			ObjectValue ov = JRegex.createRegexObjectFromRegexLiteral(raw, rt);
			operand = new ValueOperand(ov);
			break;
		case JulianLexer.NULL:
			operand = Operand.NullOperand;
			break;
		case JulianLexer.LEFT_PAREN:
			IExpression expr = getExpression(ec.create(pc.expression()));
			operand = expr.evaluate(context);
			break;
		}

		if (operand == null) {
			throw new JSEError("No operand was evaluated.");
		}
		
		return operand;
	}

}
