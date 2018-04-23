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
import info.julang.hosting.interop.JSEObjectWrapper;
import info.julang.memory.value.IntValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.ObjectValue;
import info.julang.typesystem.AnyType;
import info.julang.typesystem.JType;

/**
 * An interop wrapper over System.Collection.List. Used only for calling a static method to compare elements.
 * 
 * @author Ming Zhou
 */
public class ListWrapper extends JSEObjectWrapper {

	public static final String FullName = "System.Collection.List";
	public static final String Method_compareObjs = "_compareObjs(var, var)";
	
	/**
	 * Create a wrapper over a System.Collection.HashKey object.
	 */
	public ListWrapper(ThreadRuntime rt, ObjectValue ov){
		super(FullName, rt, ov, false);
		this.registerMethod(Method_compareObjs, "_compareObjs", true, new JType[]{ AnyType.getInstance(), AnyType.getInstance() });
	}
	
	/**
	 * Compare two values
	 */
	int compare(JValue v1, JValue v2){
		IntValue iv = (IntValue)this.runMethod(Method_compareObjs, v1, v2);
		return iv.getIntValue(); // If the method didn't return int, it would have caused runtime check error
	}
}
