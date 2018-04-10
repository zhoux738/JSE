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

import static info.julang.langspec.Operators.COMPLEMENT;
import static info.julang.langspec.Operators.DEC;
import static info.julang.langspec.Operators.INC;
import static info.julang.langspec.Operators.MINUS;
import static info.julang.langspec.Operators.PLUS;

import info.julang.interpretation.IllegalOperandsException;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.expression.KnownOperators;
import info.julang.interpretation.expression.Operand;
import info.julang.interpretation.expression.Operator;
import info.julang.interpretation.expression.operand.OperandKind;
import info.julang.interpretation.expression.operand.ValueOperand;
import info.julang.memory.value.BoolValue;
import info.julang.memory.value.ByteValue;
import info.julang.memory.value.IntValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.TempValueFactory;
import info.julang.typesystem.basic.BasicType;
import info.julang.typesystem.basic.NumberKind;

public abstract class OnSelf1Op extends Operator {
	
	public OnSelf1Op(String text, int precedence, Associativity associativity) {
		super(text, 1, precedence, associativity);
	}

	@Override
	protected Operand doApply(Context context, Operand[] operands) {
		checkType(context, operands[0]);
		Operand One = Operand.createOperand(TempValueFactory.createTempIntValue(1));
		Operand[] args = new Operand[]{operands[0], One};
		Operand res = calculateFinalResult(context, args); 
		
		return res;
	}

	protected abstract Operand calculateFinalResult(Context context, Operand[] operands);
	
	//------------- Implementation for Increment(++), and Decrement(--) operators -------------//

	private void checkType(Context context, Operand operand) {
		OperandKind okind = operand.getKind();
		switch(okind){
		case IMEMBER:
		case INDEX:
		case NAME:
		case SMEMBER:
			break;
		default:
			throw new IllegalOperandsException(
				"Operator \"" + this.toString() + "\" can be only applied on a left (assignable) value.");
		}
		
		JValue val = getValue(context, operand);
		if(!val.isBasic() || ((BasicType)val.getType()).getNumberKind() != NumberKind.WHOLE){
			throw new IllegalOperandsException(
				"Operator \"" + this.toString() + 
				"\" can be only applied on an integer operand. It is being applied on an operand of type " + 
				val.getType());
		}
	}
	
	/**
	 * Post-increment operator
	 */
	public static class IncrementOp extends OnSelf1Op {

		public IncrementOp() {
			super("++", INC.precedence, INC.associativity);
		}

		@Override
		protected Operand calculateFinalResult(Context context, Operand[] operands) {
			JValue val = getValue(context, operands[0]);
			Operand ret = new ValueOperand(val);
			KnownOperators.ADD_SELF.apply(context, operands);
			return ret;
		}
	}
	
	/**
	 * Post-decrement operator
	 */
	public static class DecrementOp extends OnSelf1Op {

		public DecrementOp() {
			super("--", DEC.precedence, DEC.associativity);
		}

		@Override
		protected Operand calculateFinalResult(Context context, Operand[] operands) {
			JValue val = getValue(context, operands[0]);
			Operand ret = new ValueOperand(val);
			KnownOperators.SUB_SELF.apply(context, operands);
			return ret;
		}
	}
	
	/**
	 * Minus (to negate the sign) operator
	 */
	public static class MinusOp extends Operator {

		public MinusOp() {
			super("-", 1, MINUS.precedence, MINUS.associativity);
		}

		@Override
		protected Operand doApply(Context context, Operand[] operands) {
			Operand od = operands[0];
			JValue val = getValue(context, od);
			if(!val.isBasic() || ((BasicType)val.getType()).getNumberKind() == NumberKind.NONE){
				throw new IllegalOperandsException(
					"Operator \"" + this.toString() + 
					"\" can be only applied on a number operand. It is being applied on an operand of type " + 
					val.getType());
			}

			Operand one = Operand.createOperand(TempValueFactory.createTempIntValue(-1));
			Operand[] ods = new Operand[] { od, one };
			Operand ret = KnownOperators.MULTIPLY.apply(context, ods);
			return ret;
		}
	}

	/**
	 * Plus (to keep the sign) operator
	 */
	public static class PlusOp extends Operator {

		public PlusOp() {
			super("+", 1, PLUS.precedence, PLUS.associativity);
		}

		@Override
		protected Operand doApply(Context context, Operand[] operands) {
			JValue val = getValue(context, operands[0]);
			if(!val.isBasic() || ((BasicType)val.getType()).getNumberKind() == NumberKind.NONE){
				throw new IllegalOperandsException(
					"Operator \"" + this.toString() + 
					"\" can be only applied on a number operand. It is being applied on an operand of type " + 
					val.getType());
			}
			
			return operands[0];
		}
	}
	
	/**
	 * Complement operator - revert all bits
	 */
	public static class ComplementOp extends Operator {
		
		public ComplementOp() {
			super("~", 1, COMPLEMENT.precedence, COMPLEMENT.associativity);
		}

		@Override
		protected Operand doApply(Context context, Operand[] operands) {
			JValue val = getValue(context, operands[0]);
			JValue res = null;
			switch(val.getKind()){
			case INTEGER:
				res = TempValueFactory.createTempIntValue(~((IntValue)val).getIntValue());
				break;
			case BYTE:
				res = TempValueFactory.createTempIntValue(~((ByteValue)val).getByteValue());
				break;
			case BOOLEAN:
				res = TempValueFactory.createTempBoolValue(!((BoolValue)val).getBoolValue());
				break;
			default:
				break;
			}
			
			if (res == null) {
				throw new IllegalOperandsException(
					"Operator \"" + this.toString() + 
					"\" can be only applied on a number operand. It is being applied on an operand of type " + 
					val.getType());
			}
			
			Operand result = Operand.createOperand(res);
			return result;
		}
	}
}