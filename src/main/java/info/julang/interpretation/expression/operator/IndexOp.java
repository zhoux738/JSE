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

import static info.julang.langspec.Operators.INDEX;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.external.interfaces.JValueKind;
import info.julang.interpretation.IllegalOperandsException;
import info.julang.interpretation.JNullReferenceException;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.errorhandling.JSExceptionFactory;
import info.julang.interpretation.errorhandling.JulianScriptException;
import info.julang.interpretation.errorhandling.KnownJSException;
import info.julang.interpretation.expression.Operand;
import info.julang.interpretation.expression.Operator;
import info.julang.memory.value.JValue;
import info.julang.memory.value.RefValue;
import info.julang.memory.value.indexable.IIndexable;
import info.julang.memory.value.operable.InitArgs;

/**
 * Operator (<code>[ ]</code>) for indexing an array.
 * <pre><code>
 * a[5], a[f(x)], a[i + 7]
 * </code></pre>
 *
 * @author Ming Zhou
 */
public class IndexOp extends Operator {

	private ThreadRuntime rt;
	
	public IndexOp(ThreadRuntime rt) {
		super("[ ]", 2, INDEX.precedence, INDEX.associativity);
		this.rt = rt;
	}

	@Override
	protected Operand doApply(Context context, Operand[] operands) {
		JValue lval = getValue(context, operands[0]);
		JValue rval = getValue(context, operands[1]);

		Operand res = index(context, lval, rval);
		
		return res;
	}

	private Operand index(Context context, JValue lval, JValue rval){
		if(lval.getKind() == JValueKind.REFERENCE){
			try {
				lval = ((RefValue)lval).dereference();
			} catch (JNullReferenceException ex) {
				JulianScriptException jse = JSExceptionFactory.createException(
					KnownJSException.NullReference, rt, context);
				throw jse;
			}
		}
		
		if(lval.getKind() != JValueKind.OBJECT){
			throw new IllegalOperandsException(
				"The operator '" + this.toString() + "' can only apply on an indexable object.");
		}
		
		IIndexable lind = RefValue.dereference(lval).asIndexer();
		if (lind == null) {
			throw new IllegalOperandsException(
				"The operator '" + this.toString() + "' can only apply on an indexable object.");
		}
		
		lind.initialize(rt, new InitArgs(context, false));
		
		return Operand.createIndexOperand(lind, rval, lind);
	}
}
