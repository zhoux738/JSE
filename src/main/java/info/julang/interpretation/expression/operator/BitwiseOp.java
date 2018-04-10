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

import static info.julang.langspec.Operators.BAND;
import static info.julang.langspec.Operators.BLSHIFT;
import static info.julang.langspec.Operators.BOR;
import static info.julang.langspec.Operators.BXOR;
import info.julang.external.interfaces.JValueKind;
import info.julang.interpretation.IllegalOperandsException;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.expression.Operand;
import info.julang.interpretation.expression.Operator;
import info.julang.memory.value.ByteValue;
import info.julang.memory.value.IntValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.TempValueFactory;

/*
	BITWISE_AND, // &
	BITWISE_OR, // |
	BITWISE_COMPLEMENT, // ^
	BITWISE_LSHIFT, // <<
	BITWISE_RSHIFT, // >>
*/
public abstract class BitwiseOp extends Operator {
	
	public BitwiseOp(String text, int arity, int precedence, Associativity associativity) {
		super(text, arity, precedence, associativity);
	}

	@Override
	protected Operand doApply(Context context, Operand[] operands) {
		JValue lval = getValue(context, operands[0]);
		JValue rval = getValue(context, operands[1]);
		
		boolean isBytes = lval.getKind() == JValueKind.BYTE && rval.getKind() == JValueKind.BYTE;
		
		int lb = convertToRawIntValue(lval);
		int rb = convertToRawIntValue(rval);
		
		int ires = doBitwise2Op(lb, rb);
		JValue result = null;
		if(isBytes){
			result = TempValueFactory.createTempByteValue((byte)ires);
		} else {
			result = TempValueFactory.createTempIntValue(ires);
		}
		
		return Operand.createOperand(result);
	}

	private int convertToRawIntValue(JValue val) {
		switch(val.getKind()){
		case INTEGER:
			return ((IntValue)val).getIntValue();
		case BYTE:
			return ((ByteValue)val).getByteValue();
		default:
		}
		
		throw new IllegalOperandsException(
			"Operator \"" + this.toString() + 
			"\" must be of int type but it is type of " + 
			val.getType().toString());
	}

	protected abstract int doBitwise2Op(int lval, int rval);
	
	//------------- Implementation of bitwise operators -------------//
	
	public static class BandOp extends BitwiseOp {
		
		public BandOp() {
			super("&", 2, BAND.precedence, BAND.associativity);
		}

		@Override
		protected int doBitwise2Op(int lval, int rval){
			return lval & rval;
		}
	}
	
	public static class BorOp extends BitwiseOp {
		
		public BorOp() {
			super("|", 2, BOR.precedence, BOR.associativity);
		}

		@Override
		protected int doBitwise2Op(int lval, int rval){
			return lval | rval;
		}
	}
	
	public static class BxorOp extends BitwiseOp {
		
		public BxorOp() {
			super("^", 2, BXOR.precedence, BXOR.associativity);
		}

		@Override
		protected int doBitwise2Op(int lval, int rval){
			return lval ^ rval;
		}
	}
	
	public static class BLShiftOp extends BitwiseOp {
		
		public BLShiftOp() {
			super("<<", 2, BLSHIFT.precedence, BLSHIFT.associativity);
		}

		@Override
		protected int doBitwise2Op(int lval, int rval){
			return lval << rval;
		}
	}
	
	public static class BRShiftOp extends BitwiseOp {
		
		public BRShiftOp() {
			super(">>", 2, BLSHIFT.precedence, BLSHIFT.associativity);
		}

		@Override
		protected int doBitwise2Op(int lval, int rval){
			return lval >> rval;
		}
	}
}
