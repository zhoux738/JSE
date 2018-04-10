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

import static info.julang.langspec.Operators.SELECT;
import info.julang.external.interfaces.JValueKind;
import info.julang.interpretation.RuntimeCheckException;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.expression.Operand;
import info.julang.interpretation.expression.Operator;
import info.julang.memory.value.BoolValue;
import info.julang.memory.value.JValue;

/**
 * The ':' in ternary conditional operator '?:'. 
 * ':' evaluates the conditions and return the value from a matched branch.
 * 
 * @author Ming Zhou
 */
public class SelectOp extends Operator {

	public SelectOp() {
		super(":", 3, SELECT.precedence, SELECT.associativity);
	}

	@Override
	protected Operand doApply(Context context, Operand[] operands) {
		JValue cval = getValue(context, operands[0]);
		if(cval.getKind() != JValueKind.BOOLEAN){
			throw new RuntimeCheckException("The condition part of a conditional expression doesn't evaluate to a boolean value.");
		} else {
			BoolValue bv = (BoolValue) cval;
			int i = bv.getBoolValue() ? 1 : 2;
			JValue val = getValue(context, operands[i]);
			Operand res = Operand.createOperand(val);
			return res;
		}
	}
	
}
