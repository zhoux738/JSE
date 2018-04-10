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

import static info.julang.langspec.Operators.DIVIDE;

import info.julang.execution.threading.ThreadRuntime;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.errorhandling.JSExceptionFactory;
import info.julang.interpretation.errorhandling.JulianScriptException;
import info.julang.interpretation.errorhandling.KnownJSException;
import info.julang.interpretation.expression.Operand;
import info.julang.interpretation.expression.Operator;
import info.julang.memory.value.ByteValue;
import info.julang.memory.value.FloatValue;
import info.julang.memory.value.IntValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.TempValueFactory;

public class DivideOp extends Operator {
	
	private ThreadRuntime rt;
	
	public DivideOp(ThreadRuntime rt) {
		super("/", 2, DIVIDE.precedence, DIVIDE.associativity);
		this.rt = rt;
	}

	@Override
	protected Operand doApply(Context context, Operand[] operands) {
		JValue lval = getValue(context, operands[0]);
		JValue rval = getValue(context, operands[1]);

		Operand res = Operand.createOperand(divide(context, lval, rval));
		
		return res;
	}

	private JValue divide(Context context, JValue lval, JValue rval){
		switch(lval.getKind()){
		case INTEGER:
			switch(rval.getKind()){
			case INTEGER:
				int left = ((IntValue) lval).getIntValue();
				int right = ((IntValue) rval).getIntValue();
				checkZero(right, context);
				return TempValueFactory.createTempIntValue(left / right);
			case BYTE:
				int ileft = ((IntValue) lval).getIntValue();
				byte bright = ((ByteValue) rval).getByteValue();
				checkZero(bright, context);
				return TempValueFactory.createTempIntValue(ileft / bright);
			case FLOAT:
				ileft = ((IntValue) lval).getIntValue();
				float fright = ((FloatValue) rval).getFloatValue();
				checkZero(fright, context);
				return TempValueFactory.createTempFloatValue(ileft / fright);
			default:
			}
			break;
		case BYTE:
			switch(rval.getKind()){
			case INTEGER:
				byte left = ((ByteValue) lval).getByteValue();
				int right = ((IntValue) rval).getIntValue();
				checkZero(right, context);
				return TempValueFactory.createTempIntValue(left / right);
			case BYTE:
				left = ((ByteValue) lval).getByteValue();
				byte bright = ((ByteValue) rval).getByteValue();
				checkZero(bright, context);
				return TempValueFactory.createTempIntValue(left / bright); // byte divides byte = integer
			case FLOAT:
				left = ((ByteValue) lval).getByteValue();
				float fright = ((FloatValue) rval).getFloatValue();
				checkZero(fright, context);
				return TempValueFactory.createTempFloatValue(left / fright);
			default:
			}
			break;
		case FLOAT:
			switch(rval.getKind()){
			case INTEGER:
				float left = ((FloatValue) lval).getFloatValue();
				int right = ((IntValue) rval).getIntValue();
				checkZero(right, context);
				return TempValueFactory.createTempFloatValue(left / right);
			case BYTE:
				float fleft = ((FloatValue) lval).getFloatValue();
				byte bright = ((ByteValue) rval).getByteValue();
				checkZero(bright, context);
				return TempValueFactory.createTempFloatValue(fleft / bright);
			case FLOAT:
				fleft = ((FloatValue) lval).getFloatValue();
				float fright = ((FloatValue) rval).getFloatValue();
				checkZero(fright, context);
				return TempValueFactory.createTempFloatValue(fleft / fright);
			default:
			}	
			break;	
		default:
		}

		throwIllegalOperandsException(lval, rval);
		
		// Should never come here
		return null;
	}
	
	private void checkZero(float val, Context context){
		if(val == 0){
			JulianScriptException jse = 
				JSExceptionFactory.createException(KnownJSException.DivByZero, rt, context);
			throw jse;
		}
	}

}
