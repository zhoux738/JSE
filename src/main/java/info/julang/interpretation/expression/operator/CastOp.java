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

import static info.julang.langspec.Operators.CAST;

import info.julang.execution.threading.ThreadRuntime;
import info.julang.external.exceptions.JSEError;
import info.julang.interpretation.JIllegalCastingException;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.expression.Operand;
import info.julang.interpretation.expression.Operator;
import info.julang.interpretation.expression.operand.ValueOperand;
import info.julang.interpretation.internal.FuncCallExecutor;
import info.julang.memory.value.BasicValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.ObjectValue;
import info.julang.memory.value.RefValue;
import info.julang.memory.value.TempValueFactory;
import info.julang.memory.value.UntypedValue;
import info.julang.memory.value.ValueUtilities;
import info.julang.memory.value.operable.JCastable;
import info.julang.typesystem.AnyType;
import info.julang.typesystem.JType;
import info.julang.typesystem.conversion.Convertibility;
import info.julang.typesystem.jclass.Accessibility;
import info.julang.typesystem.jclass.ICompoundType;
import info.julang.typesystem.jclass.JClassMethodMember;
import info.julang.typesystem.jclass.JClassType;
import info.julang.typesystem.jclass.builtin.JMethodType;
import info.julang.typesystem.jclass.builtin.JObjectType;
import info.julang.typesystem.jclass.builtin.JStringType;

/**
 * Casting operator is used to dynamically cast a value of some type A to another type B.
 * <pre><code>A a = (A) b;</code></pre>
 * <p/>
 * The operation will succeed only if any of the following conditions is met:
 *   <li>b's type is A;</li>
 *   <li>b is a class type which is derived from A;</li>
 *   <li>b is a basic type and is convertible to type A;</li>
 *   <li>A is string - this is equal to calling toString()</li> 
 * <br/>
 * 
 * @author Ming Zhou
 */
public class CastOp extends Operator {

	private ThreadRuntime rt;
	
	public CastOp(ThreadRuntime rt) {
		super("(type)", 2, CAST.precedence, CAST.associativity);
		this.rt = rt;
	}

	@Override
	protected Operand doApply(Context context, Operand[] operands) {
		JType toType = getType(context, operands[0]);
		JValue val = getValue(context, operands[1]);
		
		Operand res = cast(context, val, toType);
		
		return res;
	}

	private Operand cast(Context context, JValue val, JType toType) {
		if (toType == AnyType.getInstance()){
			UntypedValue uv = new UntypedValue(context.getFrame(), val);
			return new ValueOperand(uv);
		}
		
		if (toType.isObject()){
			Accessibility.checkTypeVisibility((ICompoundType)toType, context.getContainingType(), true);
		}
		
		if(val.isBasic()){
			// 1) basic => basic
			BasicValue bv = (BasicValue) val;
			JType fromType = bv.getType();
			Convertibility convert = fromType.getConvertibilityTo(toType);
			if(convert != Convertibility.UNCONVERTIBLE){
				JValue result = bv.replicateAs(toType, context.getFrame());
				if(result == null){
					throw new JIllegalCastingException(fromType, toType);
				}
				return new ValueOperand(result);
			} else {
				throw new JIllegalCastingException(fromType, toType);
			}
		} else {
			// 3) object => object
			ObjectValue ov = RefValue.tryDereference(val);
			if(ov == null){
				throw new JSEError("A value of kind " + val.getKind() + " cannot be cast to type " + toType.getName());
			}
			
			if(ov == RefValue.NULL){
				if(toType.isObject()){
					JValue dv = ValueUtilities.makeDefaultValue(context.getFrame(), toType, false);
					ov.assignTo(dv);
					return new ValueOperand(dv);
				} else {
					throw new JIllegalCastingException(JObjectType.getInstance(), toType);
				}
			}
			
			
			// Special case: all the objects can cast to a string
			JType fromType = ov.getType();
			if (toType == JStringType.getInstance()) {
				if (fromType.isObject()) {
					JClassType jct = ov.getClassType();
					String name = JObjectType.MethodNames.toString.name();
					JClassMethodMember jcmm = (JClassMethodMember)jct.getInstanceMemberByName(name);
					JMethodType jmt = jcmm.getMethodType();
					FuncCallExecutor fce = new FuncCallExecutor(rt);
					JValue result = fce.invokeMethodInternal(jmt, name, new JValue[0], ov);
					return new ValueOperand(result);
				}
			}
			
			Convertibility convert = fromType.getConvertibilityTo(toType);
			
			switch(convert){
			case EQUIVALENT:
				return new ValueOperand(makeTempValue(ov));
			case DOWNGRADED:
				if(toType instanceof ICompoundType){
					RefValue rv = TempValueFactory.createTempRefValue(ov, (ICompoundType)toType);
					return new ValueOperand(rv);
				} else {
					break;
				}
			case CASTABLE:
				if(ov instanceof JCastable){
					JCastable jc = (JCastable) ov;
					JValue rv = jc.to(context.getFrame(), toType);
					return new ValueOperand(makeTempValue(rv));
				} else {
					break;
				}
			default:
				// Fall through to exception
			}
			
			throw new JIllegalCastingException(fromType, toType);	
		}

	}
	
	private JValue makeTempValue(JValue v){
		return (v.isBasic() || JStringType.isStringType(v.getType())) ? 
			v : TempValueFactory.createTempRefValue((ObjectValue)v);
	}

}
