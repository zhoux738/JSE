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

import static info.julang.langspec.Operators.MODULO;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.interpretation.IllegalOperandsException;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.errorhandling.JSExceptionFactory;
import info.julang.interpretation.errorhandling.JulianScriptException;
import info.julang.interpretation.errorhandling.KnownJSException;
import info.julang.interpretation.expression.Operand;
import info.julang.interpretation.expression.Operator;
import info.julang.memory.value.ByteValue;
import info.julang.memory.value.IntValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.TempValueFactory;

public class ModuloOp extends Operator {
	
	private ThreadRuntime rt;
	
	public ModuloOp(ThreadRuntime rt) {
		super("%", 2, MODULO.precedence, MODULO.associativity);
		this.rt = rt;
	}

	@Override
	protected Operand doApply(Context context, Operand[] operands) {
		JValue lval = getValue(context, operands[0]);
		JValue rval = getValue(context, operands[1]);

		Operand res = Operand.createOperand(modulo(context, lval, rval));
		
		return res;
	}

	private JValue modulo(Context context, JValue lval, JValue rval){
		switch(lval.getKind()){
		case INTEGER:
			switch(rval.getKind()){
			case INTEGER:
				int left = ((IntValue) lval).getIntValue();
				int right = ((IntValue) rval).getIntValue();
				if(right == 0){
					JulianScriptException jse = 
						JSExceptionFactory.createException(KnownJSException.DivByZero, rt, context);
					throw jse;
				}
				return TempValueFactory.createTempIntValue(left % right);
			case BYTE:
				left = ((IntValue) lval).getIntValue();
				int bright = ((ByteValue) rval).getByteValue();
				if(bright == 0){
					JulianScriptException jse = 
						JSExceptionFactory.createException(KnownJSException.DivByZero, rt, context);
					throw jse;
				}
				return TempValueFactory.createTempIntValue(left % bright);			
			default:
			}
			break;
		case BYTE:
			switch(rval.getKind()){
			case INTEGER:
				byte bleft = ((ByteValue) lval).getByteValue();
				int right = ((IntValue) rval).getIntValue();
				if(right == 0){
					JulianScriptException jse = 
						JSExceptionFactory.createException(KnownJSException.DivByZero, rt, context);
					throw jse;
				}
				return TempValueFactory.createTempIntValue(bleft % right);
			case BYTE:
				bleft = ((ByteValue) lval).getByteValue();
				byte bright = ((ByteValue) rval).getByteValue();
				if(bright == 0){
					JulianScriptException jse = 
						JSExceptionFactory.createException(KnownJSException.DivByZero, rt, context);
					throw jse;
				}
				return TempValueFactory.createTempIntValue(bleft % bright);			
			default:
			}
			break;
		default:
		}
		
		throwException(lval, rval);

		// Should never come here
		return null;
	}
	
	private void throwException(JValue lval, JValue rval){
		throw new IllegalOperandsException(
			"The operator '" + this.toString() + "' cannot apply on operands of type " + 
			lval.getType() + " and " + rval.getType());
	}

}
