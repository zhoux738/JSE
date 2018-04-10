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

import info.julang.execution.threading.ThreadRuntime;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.expression.Operand;
import info.julang.interpretation.expression.Operator;
import info.julang.langspec.Operators;

public abstract class RuntimeAwareOnSelf2Op extends OnSelf2Op {

	public static RuntimeAwareOnSelf2Op makeDivideSelf(ThreadRuntime rt){
		return new DivToSelfOp(rt);
	}
	
	public static RuntimeAwareOnSelf2Op makeModuloSelf(ThreadRuntime rt){
		return new ModToSelfOp(rt);
	}
	
	private Operator op;
	
	protected RuntimeAwareOnSelf2Op(ThreadRuntime rt, String text, int precedence, Associativity associativity) {
		super(text, precedence, associativity);
		op = getOp(rt);
	}

	@Override
	protected Operand calculateIntermediateResult(Context context, Operand[] operands) {
		return op.apply(context, operands);
	}

	protected abstract Operator getOp(ThreadRuntime rt);
	
	private static class DivToSelfOp extends RuntimeAwareOnSelf2Op {

		protected DivToSelfOp(ThreadRuntime rt) {
			super(rt, "/=", Operators.DIVIDESELF.precedence, Operators.DIVIDESELF.associativity);
		}

		@Override
		protected Operator getOp(ThreadRuntime rt) {
			return new DivideOp(rt);
		}
		
	}
	
	private static class ModToSelfOp extends RuntimeAwareOnSelf2Op {

		protected ModToSelfOp(ThreadRuntime rt) {
			super(rt, "%=", Operators.MODULOSELF.precedence, Operators.MODULOSELF.associativity);
		}

		@Override
		protected Operator getOp(ThreadRuntime rt) {
			return new ModuloOp(rt);
		}
		
	}
}
