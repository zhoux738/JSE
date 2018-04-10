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

import static info.julang.langspec.Operators.MULTIPLY;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.expression.Operand;
import info.julang.interpretation.expression.Operator;
import info.julang.memory.value.ByteValue;
import info.julang.memory.value.FloatValue;
import info.julang.memory.value.IntValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.TempValueFactory;

public class MultiplyOp extends Operator {

	public MultiplyOp() {
		super("*", 2, MULTIPLY.precedence, MULTIPLY.associativity);
	}

	@Override
	protected Operand doApply(Context context, Operand[] operands) {
		JValue lval = getValue(context, operands[0]);
		JValue rval = getValue(context, operands[1]);

		Operand res = Operand.createOperand(multiply(lval, rval));
		
		return res;
	}

	private JValue multiply(JValue lval, JValue rval){
		switch(lval.getKind()){
		case INTEGER:
			switch(rval.getKind()){
			case INTEGER:
				int iproduct = ((IntValue) lval).getIntValue() * ((IntValue) rval).getIntValue();
				return TempValueFactory.createTempIntValue(iproduct);
			case BYTE:
				iproduct = ((IntValue) lval).getIntValue() * ((ByteValue) rval).getByteValue();
				return TempValueFactory.createTempIntValue(iproduct);
			case FLOAT:
				float fproduct = ((IntValue) lval).getIntValue() * ((FloatValue) rval).getFloatValue();
				return TempValueFactory.createTempFloatValue(fproduct);	
			default:
			}
			break;
		case BYTE:
			switch(rval.getKind()){
			case INTEGER:
				int iproduct = ((ByteValue) lval).getByteValue() * ((IntValue) rval).getIntValue();
				return TempValueFactory.createTempIntValue(iproduct);
			case BYTE:
				iproduct = ((ByteValue) lval).getByteValue() * ((ByteValue) rval).getByteValue();
				return TempValueFactory.createTempIntValue(iproduct);
			case FLOAT:
				float fproduct = ((ByteValue) lval).getByteValue() * ((FloatValue) rval).getFloatValue();
				return TempValueFactory.createTempFloatValue(fproduct);	
			default:
			}
			break;
		case FLOAT:
			switch(rval.getKind()){
			case INTEGER:
				float fproduct = ((FloatValue) lval).getFloatValue() * ((IntValue) rval).getIntValue();
				return TempValueFactory.createTempFloatValue(fproduct);
			case BYTE:
				fproduct = ((FloatValue) lval).getFloatValue() * ((ByteValue) rval).getByteValue();
				return TempValueFactory.createTempFloatValue(fproduct);
			case FLOAT:
				fproduct = ((FloatValue) lval).getFloatValue() * ((FloatValue) rval).getFloatValue();
				return TempValueFactory.createTempFloatValue(fproduct);	
			default:
			}
			break;
		default:
		} 
		
		throwIllegalOperandsException(lval, rval);

		// Should never come here
		return null;
	}

}
