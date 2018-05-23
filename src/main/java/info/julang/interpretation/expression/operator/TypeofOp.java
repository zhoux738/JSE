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

import static info.julang.langspec.Operators.TYPEOF;

import info.julang.execution.threading.ThreadRuntime;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.expression.Operand;
import info.julang.interpretation.expression.Operator;
import info.julang.interpretation.expression.operand.ValueOperand;
import info.julang.memory.value.ObjectValue;
import info.julang.memory.value.RefValue;
import info.julang.memory.value.TypeValue;
import info.julang.typesystem.JType;
import info.julang.typesystem.jclass.Accessibility;
import info.julang.typesystem.jclass.ICompoundType;

/**
 * typeof operator is used to get the type metadata object for the given type.
 * <pre><code>System.Type typ = typeof(MyClass);</code></pre>
 * <p>
 * As a precondition the type to be demanded of its metadata will first be loaded if it hasn't. If the type
 * cannot be resolved and the loading failed this operator will fail also. This means the operator has the
 * effect of loading and initializing the specified type.
 * 
 * @author Ming Zhou
 */
public class TypeofOp extends Operator {

	private ThreadRuntime rt;
	
	public TypeofOp(ThreadRuntime rt) {
		super("typeof( )", 1, TYPEOF.precedence, TYPEOF.associativity);
		this.rt = rt;
	}

	@Override
	protected Operand doApply(Context context, Operand[] operands) {
		JType type = getType(context, operands[0]);
		
		Operand res = getTypeObject(context, type);
		
		return res;
	}

	private Operand getTypeObject(Context context, JType type) {	
		if (type.isObject()){
			Accessibility.checkTypeVisibility((ICompoundType)type, context.getContainingType(), true);
		}
		
		TypeValue tv = context.getTypTable().getValue(type.getName());
		ObjectValue ov = tv.getScriptTypeObject(rt);
		RefValue rv = new RefValue(context.getFrame(), ov);
		
		return new ValueOperand(rv);
	}
}
