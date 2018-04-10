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

import static info.julang.langspec.Operators.IS;
import info.julang.external.exceptions.JSEError;
import info.julang.external.interfaces.JValueKind;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.expression.Operand;
import info.julang.interpretation.expression.Operator;
import info.julang.interpretation.expression.operand.ValueOperand;
import info.julang.memory.value.JValue;
import info.julang.memory.value.RefValue;
import info.julang.memory.value.TempValueFactory;
import info.julang.typesystem.AnyType;
import info.julang.typesystem.JType;
import info.julang.typesystem.JTypeKind;
import info.julang.typesystem.jclass.ICompoundType;
import info.julang.typesystem.jclass.builtin.JObjectType;

/**
 * Is operator is used to check if a value is of type A.
 * <pre><code>if(a is A){...}</code></pre>
 * <p/>
 * The operation returns a boolean value. It returns true if any of the following conditions is met:
 *   <li>a's type is A;</li>
 *   <li>any of a's ancestral type is A</li>
 * <br/><br/>
 * 
 * @author Ming Zhou
 */
public class IsOp extends Operator {

	public IsOp() {
		super("is", 2, IS.precedence, IS.associativity);
	}

	@Override
	protected Operand doApply(Context context, Operand[] operands) {
		JType type = getType(context, operands[1]);
		
		Operand res = is(context, operands[0], type);
		
		return res;
	}

	private Operand is(Context context, Operand operand, JType toType) {
		if (toType == AnyType.getInstance()) {
			return new ValueOperand(TempValueFactory.createTempBoolValue(true));
		}
		
		JValue val = getValue(context, operand);
		JType typ = null;
		if(val.getKind() == JValueKind.REFERENCE){
			RefValue rv = (RefValue) val;
			typ = rv.getRuntimeType();
		} else {
			typ = val.getType();		
			if(typ.isBasic()){
				return new ValueOperand(TempValueFactory.createTempBoolValue(typ == toType));
			}
		}
		
		if (typ == null){
			if (val.deref() == RefValue.NULL){
				return new ValueOperand(TempValueFactory.createTempBoolValue(false));
			} else {
				throw new JSEError("A value doesn't have any type.");
			}
		} else {
			if(typ.getKind() == JTypeKind.CLASS && toType.getKind() == JTypeKind.CLASS){
				ICompoundType jct1 = (ICompoundType) typ;
				ICompoundType jct2 = (ICompoundType) toType;
				return new ValueOperand(
					TempValueFactory.createTempBoolValue(jct1.isDerivedFrom(jct2, true)));
			} else {
				return new ValueOperand(TempValueFactory.createTempBoolValue(typ == toType));
			}
		}
		// throw new JSEError("Type checking operator (is) doesn't support type \"" + toType.getName() + "\".");
	}

}
