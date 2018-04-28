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
package info.julang.memory.value.indexable;

import info.julang.execution.threading.ThreadRuntime;
import info.julang.hosting.interop.JSEObjectWrapper;
import info.julang.memory.value.IntValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.ObjectValue;
import info.julang.memory.value.operable.InitArgs;
import info.julang.typesystem.AnyType;
import info.julang.typesystem.JType;

/**
 * An indexable backed by a user-defined indexable script object, which implements 
 * <font color="green"><code>System.Util.IIndexable</code></font>.
 * 
 * @author Ming Zhou
 */
public class ObjectIndexable implements IIndexable {

	public static final String Method_getter = "at(var)";
	public static final String Method_setter = "at(var, var)";
	public static final String Method_size = "size()";
	
	private ObjectValue ov;
	private String fullClassName;
	private SysUtilIndexableWrapper wrapper;
	
	public ObjectIndexable(ObjectValue ov, String fullClassName){
		this.ov = ov;
		this.fullClassName = fullClassName;
	}

	@Override
	public void initialize(ThreadRuntime rt, InitArgs args){
		wrapper = new SysUtilIndexableWrapper(rt, fullClassName);
	}

	@Override
	public int getLength() {
		return wrapper.size();
	}

	@Override
	public JValue getByIndex(JValue index) throws UnsupportedIndexTypeException {
		return wrapper.get(index);
	}

	@Override
	public JValue setByIndex(JValue index, JValue value) throws UnsupportedIndexTypeException {
		wrapper.set(index, value);
		return value;
	}
	
	private class SysUtilIndexableWrapper extends JSEObjectWrapper {
		
		private SysUtilIndexableWrapper(ThreadRuntime rt, String fullClassName){
			super(fullClassName, rt, ov, false);
			AnyType atyp = AnyType.getInstance();
			this.registerMethod(Method_getter, "at", false, new JType[]{ atyp });
			this.registerMethod(Method_setter, "at", false, new JType[]{ atyp, atyp });
			this.registerMethod(Method_size, "size", false, new JType[]{ });
		}
		
		private JValue get(JValue index){
			JValue res = this.runMethod(Method_getter, index);
			return res;
		}

		private void set(JValue index, JValue value) {
			this.runMethod(Method_setter, index, value);
		}
		
		private int size() {
			IntValue res = (IntValue)this.runMethod(Method_size);
			return res.getIntValue();
		}
	}
}
