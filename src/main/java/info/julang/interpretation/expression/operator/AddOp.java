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

import static info.julang.langspec.Operators.ADD;
import info.julang.external.interfaces.JValueKind;
import info.julang.interpretation.IllegalOperandsException;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.expression.Operand;
import info.julang.interpretation.expression.Operator;
import info.julang.memory.value.JValue;
import info.julang.memory.value.RefValue;
import info.julang.memory.value.operable.JAddable;

public class AddOp extends Operator {

	public AddOp() {
		super("+", 2, ADD.precedence, ADD.associativity);
	}

	@Override
	protected Operand doApply(Context context, Operand[] operands) {
		JValue lval = getValue(context, operands[0]);
		JValue rval = getValue(context, operands[1]);

		Operand res = Operand.createOperand(add(context, lval, rval));
		
		return res;
	}

	private JValue add(Context context, JValue lval, JValue rval){
		if(lval.getKind() == JValueKind.REFERENCE){
			lval = RefValue.tryDereference(lval);
		}
		
		if(rval.getKind() == JValueKind.REFERENCE){
			rval = RefValue.tryDereference(rval);
		}
		
		if(lval instanceof JAddable && rval instanceof JAddable){
			JAddable ladder = (JAddable) lval;
			JValue val = ladder.add(context.getFrame(), rval);
			if(val != null){
				return val;
			}
		}
		
		if (lval == RefValue.NULL || rval == RefValue.NULL) {
			throw new IllegalOperandsException(
				"The operator '" + this.toString() + "' cannot apply on null value(s).");
		} else {
			throw new IllegalOperandsException(
				"The operator '" + this.toString() + "' cannot apply on operands of type " + 
				lval.getType() + " and " + rval.getType());			
		}
	}

}
