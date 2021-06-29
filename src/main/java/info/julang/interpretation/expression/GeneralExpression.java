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

import java.util.List;

import info.julang.JSERuntimeException;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.external.exceptions.JSEError;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.errorhandling.ILocationInfoAware;
import info.julang.langspec.ast.JulianParser.ExpressionContext;
import info.julang.parser.AstInfo;

/**
 * A generalized expression that can handle most cases. It leaves the part of extracting sub-nodes from the given AST to 
 * the more concrete classes but handles everything else. 
 * 
 * @author Ming Zhou
 */
public abstract class GeneralExpression extends ExpressionBase {
	
	public GeneralExpression(ThreadRuntime rt, Operator op, AstInfo<ExpressionContext> ec) {
		super(rt, ec, op);
	}
	
	public GeneralExpression(ThreadRuntime rt, AstInfo<ExpressionContext> ec) {
		super(rt, ec, null);
	}

	public Operand evaluate(Context context){
		Operand[] operands = evalOperands(context, ec);
		return op.apply(context, operands);
	}

	/**
	 * Given an expression tree, evaluate its nodes and return results in form of {@link Operand Operands}.
	 * <p>
	 * This is a rather generalized approach. For expressions which have special requirements, this whole 
	 * method must be overridden.
		
	 * @param context
	 * @param ec
	 * @return The operands corresponding to each sub-expression of the tree.
	 */
	public Operand[] evalOperands(Context context, AstInfo<ExpressionContext> ec){
		try{
			int arity = op.getArity();
			
			List<ExpressionContext> list = getSubExpressions(ec);
			
			if (arity != list.size()){ // This should be a bug
				throw new JSEError("Expression doesn't contain sufficient number of operands.");
			}
			
			Operand[] ods = new Operand[arity];
			for(int i = 0; i<arity; i++){
				ExpressionContext ect = list.get(i);
				IExpression exp = getExpression(ec.create(ect));
				Operand od = exp.evaluate(context);
				ods[i] = od;
			}
			
			return ods;
		} catch (JSERuntimeException jse) {
			if (jse instanceof ILocationInfoAware) {
				ILocationInfoAware ilw = (ILocationInfoAware) jse;
				ilw.setLocationInfo(getLocation());
			}
			
			throw jse;
		}
	}
	
	protected abstract List<ExpressionContext> getSubExpressions(AstInfo<ExpressionContext> ec);
	
}
