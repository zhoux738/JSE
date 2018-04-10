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

package info.julang.interpretation.expression.operator;

import static info.julang.langspec.Operators.AND;
import static info.julang.langspec.Operators.NEGATE;
import info.julang.external.interfaces.JValueKind;
import info.julang.interpretation.IllegalOperandsException;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.expression.Operand;
import info.julang.interpretation.expression.Operator;
import info.julang.memory.value.BoolValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.TempValueFactory;

public abstract class LogicalOp extends Operator {
	
	public LogicalOp(String text, int arity, int precedence, Associativity associativity) {
		super(text, arity, precedence, associativity);
	}

	@Override
	protected Operand doApply(Context context, Operand[] operands) {
		Operand res = null;
		
		if(arity == 1){
			JValue lval = getValue(context, operands[0]);
			
			BoolValue lb = convertToBoolValue(lval);
			
			res = Operand.createOperand(doLogic1Op(lb));
		} else if(arity == 2){
			JValue lval = getValue(context, operands[0]);
			JValue rval = getValue(context, operands[1]);
			
			BoolValue lb = convertToBoolValue(lval);
			BoolValue rb = convertToBoolValue(rval);
			
			res = Operand.createOperand(doLogic2Op(lb, rb));
		}
		
		return res;
	}

	private BoolValue convertToBoolValue(JValue val) {
		if(val.getKind() != JValueKind.BOOLEAN){
			throw new IllegalOperandsException(
				"Operator \"" + this.toString() + 
				"\" must be of boolean type but it is type of " + 
				val.getType().toString());
		}
		
		return (BoolValue)val;
	}

	protected JValue doLogic2Op(BoolValue lval, BoolValue rval){
		return null;
	}
	
	protected JValue doLogic1Op(BoolValue lval){
		return null;
	}
	
	//------------- Implementation for And(&&), Or(||) and Not(!) operators -------------//
	
	public static class AndOp extends LogicalOp {
		
		public AndOp() {
			super("&&", 2, AND.precedence, AND.associativity);
		}

		@Override
		protected JValue doLogic2Op(BoolValue lval, BoolValue rval) {
			boolean result = lval.getBoolValue() && rval.getBoolValue();
			return TempValueFactory.createTempBoolValue(result);
		}
		
	}
	
	public static class OrOp extends LogicalOp {
		
		public OrOp() {
			super("||", 2, AND.precedence, AND.associativity);
		}

		@Override
		protected JValue doLogic2Op(BoolValue lval, BoolValue rval) {
			boolean result = lval.getBoolValue() || rval.getBoolValue();
			return TempValueFactory.createTempBoolValue(result);
		}
		
	}
	
	public static class NegateOp extends LogicalOp {
		
		public NegateOp() {
			super("!", 1, NEGATE.precedence, NEGATE.associativity);
		}

		@Override
		protected JValue doLogic1Op(BoolValue lval) {
			boolean result = !lval.getBoolValue();
			return TempValueFactory.createTempBoolValue(result);
		}
		
	}
}
