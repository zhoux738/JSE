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

import info.julang.interpretation.context.Context;
import info.julang.interpretation.expression.KnownOperators;
import info.julang.interpretation.expression.Operand;
import info.julang.interpretation.expression.Operator;
import info.julang.langspec.Operators;

public abstract class OnSelf2Op extends Operator {
	
	protected OnSelf2Op(String text, int precedence, Associativity associativity) {
		super(text, 2, precedence, associativity);
	}

	@Override
	protected Operand doApply(Context context, Operand[] operands) {
		Operand result = calculateIntermediateResult(context, operands);
		Operand[] args = new Operand[]{operands[0], result};
		Operand res = KnownOperators.ASSIGN.apply(context, args);
		
		return res;
	}

	protected abstract Operand calculateIntermediateResult(Context context, Operand[] operands);
	
	/**
	 * Operator += (add-to-self)
	 * 
	 * @author Ming Zhou
	 */
	public static class AddToSelfOp extends OnSelf2Op {
		
		public AddToSelfOp() {
			super("+=", Operators.ADDSELF.precedence, Operators.ADDSELF.associativity);
		}

		@Override
		protected Operand calculateIntermediateResult(Context context, Operand[] operands) {
			return KnownOperators.ADD.apply(context, operands);
		}
		
	}
	
	/**
	 * Operator -= (sub-to-self)
	 * 
	 * @author Ming Zhou
	 */
	public static class SubToSelfOp extends OnSelf2Op {
		
		public SubToSelfOp() {
			super("-=", Operators.SUBSELF.precedence, Operators.SUBSELF.associativity);
		}

		@Override
		protected Operand calculateIntermediateResult(Context context, Operand[] operands) {
			return KnownOperators.SUB.apply(context, operands);
		}
		
	}
	
	/**
	 * Operator *= (multiply-to-self)
	 * 
	 * @author Ming Zhou
	 */
	public static class MulToSelfOp extends OnSelf2Op {
		
		public MulToSelfOp() {
			super("*=", Operators.MULTIPLYSELF.precedence, Operators.MULTIPLYSELF.associativity);
		}

		@Override
		protected Operand calculateIntermediateResult(Context context, Operand[] operands) {
			return KnownOperators.MULTIPLY.apply(context, operands);
		}
		
	}
	
	/**
	 * Operator &amp;= (bitwise-and-to-self)
	 * 
	 * @author Ming Zhou
	 */
	public static class BitwiseAndToSelfOp extends OnSelf2Op {
		
		public BitwiseAndToSelfOp() {
			super("&=", Operators.BADDSELF.precedence, Operators.BADDSELF.associativity);
		}

		@Override
		protected Operand calculateIntermediateResult(Context context, Operand[] operands) {
			return KnownOperators.BITWISE_AND.apply(context, operands);
		}
		
	}
	
	/**
	 * Operator |= (bitwise-or-to-self)
	 * 
	 * @author Ming Zhou
	 */
	public static class BitwiseOrToSelfOp extends OnSelf2Op {
		
		public BitwiseOrToSelfOp() {
			super("|=", Operators.BORSELF.precedence, Operators.BORSELF.associativity);
		}

		@Override
		protected Operand calculateIntermediateResult(Context context, Operand[] operands) {
			return KnownOperators.BITWISE_OR.apply(context, operands);
		}
		
	}
	
	/**
	 * Operator |= (bitwise-lshift-to-self)
	 * 
	 * @author Ming Zhou
	 */
	public static class BitwiseLShiftToSelfOp extends OnSelf2Op {
		
		public BitwiseLShiftToSelfOp() {
			super("<<=", Operators.BLSHIFTSELF.precedence, Operators.BLSHIFTSELF.associativity);
		}

		@Override
		protected Operand calculateIntermediateResult(Context context, Operand[] operands) {
			return KnownOperators.BITWISE_LSHIFT.apply(context, operands);
		}
		
	}
	
	/**
	 * Operator &gt;&gt;= (bitwise-rshift-to-self)
	 * 
	 * @author Ming Zhou
	 */
	public static class BitwiseRShiftToSelfOp extends OnSelf2Op {
		
		public BitwiseRShiftToSelfOp() {
			super(">>=", Operators.BRSHIFTSELF.precedence, Operators.BRSHIFTSELF.associativity);
		}

		@Override
		protected Operand calculateIntermediateResult(Context context, Operand[] operands) {
			return KnownOperators.BITWISE_RSHIFT.apply(context, operands);
		}
		
	}
	
	/**
	 * Operator ^= (bitwise-xor-to-self)
	 * 
	 * @author Ming Zhou
	 */
	public static class BitwiseXorToSelfOp extends OnSelf2Op {
		
		public BitwiseXorToSelfOp() {
			super("^=", Operators.BXORSELF.precedence, Operators.BXORSELF.associativity);
		}

		@Override
		protected Operand calculateIntermediateResult(Context context, Operand[] operands) {
			return KnownOperators.BITWISE_XOR.apply(context, operands);
		}
		
	}

}