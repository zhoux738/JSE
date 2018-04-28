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
import info.julang.memory.value.HostedValue;
import info.julang.memory.value.ObjectValue;
import info.julang.memory.value.operable.InitArgs;
import info.julang.typesystem.JType;
import info.julang.typesystem.jclass.jufc.System.Collection.JList;

/**
 * An iterable backed by a user-defined script object which implements 
 * <font color="green"><code>System.Util.IIterable</code></font>.
 * 
 * @author Ming Zhou
 */
public class ObjectIterable {

	public static final String Method_getIterator = "getIterator()";
	
	private ObjectValue ov;
	private String fullClassName;
	private SysUtilIterableWrapper wrapper;
	
	public ObjectIterable(ObjectValue ov, String fullClassName){
		this.ov = ov;
		this.fullClassName = fullClassName;
	}

	public void initialize(ThreadRuntime rt, InitArgs args){
		wrapper = new SysUtilIterableWrapper(rt, fullClassName);
	}

	public ObjectIterator getIterator() {
		return JList.FullTypeName.equals(this.fullClassName) ? new DeferredListIterator() : new DeferredObjectIterator();
	}
	
	private class SysUtilIterableWrapper extends JSEObjectWrapper {
		
		private SysUtilIterableWrapper(ThreadRuntime rt, String fullClassName){
			super(fullClassName, rt, ov, false);
			this.registerMethod(Method_getIterator, "getIterator", false, new JType[]{ });
		}
		
		private ObjectValue getIterator(){
			ObjectValue res = (ObjectValue)this.runMethod(Method_getIterator).deref();
			return res;
		}
	}
	
	private class DeferredObjectIterator extends ObjectIterator {

		public DeferredObjectIterator() {
			super(null, null); // Cannot initialize here, since we don't know the IIterable value yet.
		}
		
		@Override
		public void initialize(ThreadRuntime rt, InitArgs args){
			// 1) Initialize IIterable
			ObjectIterable.this.initialize(rt, args);
			
			// 2) Get IIterable instance
			ObjectValue ov = ObjectIterable.this.wrapper.getIterator();
			String fname = ov.getType().getName();
			
			// 3) Initialize IIterator
			super.initialize(ov, fname);
			super.initialize(rt, args);
			
			// Now next() and hasNext() are ready to be called.
		}
	}
	
	// Special treatment for <font color="green"><code>System.Collection.List</code></font>.
	private class DeferredListIterator extends DeferredObjectIterator {
		
		private JList list;
		private ThreadRuntime rt;
		
		public DeferredListIterator(){
			super();
		}

		@Override
		public void initialize(ThreadRuntime rt, InitArgs args){
			super.initialize(rt, args);

			this.rt = rt;
			HostedValue hv = (HostedValue)ov;
			list = (JList)hv.getHostedObject();
			
			if (args.shouldApplyLock()) {
				list.applyWriteLock(rt);
			}
		}

		@Override
		public void dispose() {
			list.releaseWriteLock(rt);
		}
	}
}
