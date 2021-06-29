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

package info.julang.memory.value.iterable;

import info.julang.execution.threading.ThreadRuntime;
import info.julang.hosting.interop.JSEObjectWrapper;
import info.julang.memory.value.BoolValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.ObjectValue;
import info.julang.memory.value.operable.InitArgs;
import info.julang.typesystem.JType;

/**
 * An iterator backed by a user-defined script object which implements <code style="color:green">System.Util.IIterator</code>.
 * 
 * @author Ming Zhou
 */
public class ObjectIterator implements IIterator {

	public static final String Method_next = "next()";
	public static final String Method_hasNext = "hasNext()";
	
	private ObjectValue ov;
	private String fullClassName;
	private SysUtilIteratorWrapper wrapper;
	
	public ObjectIterator(ObjectValue ov, String fullClassName){
		initialize(ov, fullClassName);
	}
	
	protected void initialize(ObjectValue ov, String fullClassName){
		this.ov = ov;
		this.fullClassName = fullClassName;
	}

	@Override
	public void initialize(ThreadRuntime rt, InitArgs args){
		wrapper = new SysUtilIteratorWrapper(rt, fullClassName);
	}

	@Override
	public JValue next() {
		return wrapper.next();
	}

	@Override
	public boolean hasNext() {
		BoolValue val = (BoolValue)wrapper.hasNext();
		return val.getBoolValue();
	}
	
	private class SysUtilIteratorWrapper extends JSEObjectWrapper {
		
		private SysUtilIteratorWrapper(ThreadRuntime rt, String fullClassName){
			super(fullClassName, rt, ov, false);
			this.registerMethod(Method_next, "next", false, new JType[]{ });
			this.registerMethod(Method_hasNext, "hasNext", false, new JType[]{ });
		}
		
		private JValue next(){
			JValue res = this.runMethod(Method_next);
			return res;
		}

		private JValue hasNext() {
			JValue res = this.runMethod(Method_hasNext);
			return res;
		}
	}

	@Override
	public void dispose() {
		// NO-OP
	}
}
