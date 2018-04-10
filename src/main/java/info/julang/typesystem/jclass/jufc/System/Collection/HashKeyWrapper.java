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

package info.julang.typesystem.jclass.jufc.System.Collection;

import info.julang.execution.threading.ThreadRuntime;
import info.julang.external.interfaces.JValueKind;
import info.julang.hosting.interop.JSEObjectWrapper;
import info.julang.memory.value.BoolValue;
import info.julang.memory.value.IntValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.ObjectValue;
import info.julang.typesystem.AnyType;
import info.julang.typesystem.JType;

/**
 * An interop wrapper over System.Collection.HashKey. This class is used as the key for
 * Java's HashMap class which serves as the underlying implementation of <code>
 * <font color="green">System.Collection.Map</font></code>. Both {@link #hashCode()} 
 * and {@link #equals(Object)} are redirected to the JSE code.
 * 
 * @author Ming Zhou
 */
public class HashKeyWrapper extends JSEObjectWrapper {

	public static final String FullName = "System.Collection.HashKey";
	public static final String Method_getKey = "getKey()";
	public static final String Method_getHashCode = "getHashCode()";
	public static final String Method_equals_var = "equals(var)";
	
	/**
	 * Create a wrapper over a System.Collection.HashKey object.
	 */
	public HashKeyWrapper(ThreadRuntime rt, ObjectValue ov){
		super(FullName, rt, ov, false);
		this.registerMethod(Method_getKey,      "getKey",      false, new JType[]{ });
		this.registerMethod(Method_getHashCode,	"getHashCode", false, new JType[]{ });
		this.registerMethod(Method_equals_var,  "equals",      false, new JType[]{ AnyType.getInstance() });
	}
	
	/**
	 * Get the original map key as passed in Julian.
	 */
	JValue getKey(){
		return this.runMethod(Method_getKey);
	}

	private int getHashCode(){
		JValue jval = this.runMethod(Method_getHashCode).deref();
		if (jval.getKind() == JValueKind.INTEGER){
			IntValue iv = (IntValue)jval;
			return iv.getIntValue();
		} else {
			// If the key is not an int (either converted from Object or is a genuine int), call 
			// Java's hashCode(), which is overridden by basic values of non-int types.
			return jval.hashCode();
		}
	}
	
	private boolean equalsTo(JValue val){
		JValue jval = this.runMethod(Method_equals_var, val).deref();
		BoolValue bv = (BoolValue)jval;
		return bv.getBoolValue();
	}
	
	//---------------------- Object ----------------------//
	
	@Override
	public int hashCode(){
		return getHashCode();
	}
	
	/**
	 * Through the HashKey wrapper, eventually call Julian's object.equals() on the raw key against another HashKey instance.
	 * <pre><code>
	 * internal bool equals(var another){
	 *   ... ...
	 *   HashKey hk = (HashKey)another;
	 *   return key.equals(hk.key);
	 * }
	 * </code></pre>
	 * @param val an instance of <code><font color="green">System.Collection.HashKey</font></code>
	 */
	@Override
	public boolean equals(Object another){
		JValue jval = ((HashKeyWrapper)another).getObjectValue(); // The casting is guaranteed by the internal usage, otherwise it is a bug.
		return equalsTo(jval);
	}
}
