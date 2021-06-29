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

import info.julang.execution.Argument;
import info.julang.execution.threading.JThread;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.hosting.HostedMethodProviderFactory;
import info.julang.hosting.SimpleHostedMethodProvider;
import info.julang.hosting.execution.CtorNativeExecutor;
import info.julang.hosting.execution.InstanceNativeExecutor;
import info.julang.memory.value.ArrayIndexOutOfRangeException;
import info.julang.memory.value.BoolValue;
import info.julang.memory.value.HostedValue;
import info.julang.memory.value.IntValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.TempValueFactory;
import info.julang.memory.value.VoidValue;
import info.julang.memory.value.indexable.JConcurrentModificationException;
import info.julang.memory.value.operable.ValueComparator;

import java.util.ArrayList;
import java.util.List;

/**
 * The native implementation of <code style="color:green">System.Collection.List</code>.
 * 
 * @author Ming Zhou
 */
public class JList {
	
	public final static String FullTypeName = "System.Collection.List";
	
	//----------------- IRegisteredMethodProvider -----------------//

	public static HostedMethodProviderFactory Factory = new HostedMethodProviderFactory(FullTypeName){

		@Override
		protected void implementProvider(SimpleHostedMethodProvider provider) {
			provider
				.add("ctor", new InitExecutor())
				.add("add", new AddExecutor())
				.add("get", new GetExecutor())
				.add("put", new PutExecutor())
				.add("sort", new SortExecutor())
				.add("remove", new RemoveExecutor())
				.add("size", new SizeExecutor());
		}
		
	};
	
	//----------------- native executors -----------------//
	
	private static class InitExecutor extends CtorNativeExecutor<JList> {

		@Override
		protected void initialize(ThreadRuntime rt, HostedValue hvalue, JList jlist, Argument[] args) throws Exception {
			jlist.init(hvalue);
		}
		
	}
	
	private static class RemoveExecutor extends InstanceNativeExecutor<JList> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, JList jlist, Argument[] args) throws Exception {
			int index = getInt(args, 0);
			JValue jv = jlist.remove(rt.getJThread(), index);
			return jv;
		}
		
	}
	
	private static class AddExecutor extends InstanceNativeExecutor<JList> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, JList jlist, Argument[] args) throws Exception {
			JValue jv = args[0].getValue();
			jlist.add(rt.getJThread(), jv);
			return VoidValue.DEFAULT;
		}
		
	}
	
	private static class GetExecutor extends InstanceNativeExecutor<JList> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, JList jlist, Argument[] args) throws Exception {
			int index = getInt(args, 0);
			JValue jv = jlist.get(index);
			return jv;
		}
		
	}
	
	private static class PutExecutor extends InstanceNativeExecutor<JList> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, JList jlist, Argument[] args) throws Exception {
			int index = getInt(args, 0);
			JValue v = args[1].getValue();
			jlist.put(rt.getJThread(), index, v);
			return VoidValue.DEFAULT;
		}
		
	}
	private static class SizeExecutor extends InstanceNativeExecutor<JList> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, JList jlist, Argument[] args) throws Exception {
			IntValue iv = TempValueFactory.createTempIntValue(jlist.size());
			return iv;
		}
		
	}
	
	private static class SortExecutor extends InstanceNativeExecutor<JList> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, JList jlist, Argument[] args) throws Exception {
			boolean desc = ((BoolValue)(args[0].getValue().deref())).getBoolValue();
			jlist.sort(rt, desc);
			return VoidValue.DEFAULT;
		}
		
	}

	//----------------- implementation at native end -----------------//
	
	private List<JValue> list;
	private HostedValue hvalue;
	
	public void init(HostedValue hvalue){
		this.list = new ArrayList<JValue>();
		this.hvalue = hvalue;
	}
	
	public synchronized void add(JThread jt, JValue e){
		assertWriteLock(jt);
		
		list.add(e);
	}
	
	public synchronized JValue get(int i){
		if(i < 0 || i >= list.size()){
			throw new ArrayIndexOutOfRangeException(i, list.size() - 1);
		}
		JValue result = list.get(i);
		return result == null ? TempValueFactory.createTempNullRefValue() : result;
	}
	
	public synchronized JValue put(JThread jt, int i, JValue val){
		assertWriteLock(jt);
		
		if(i < 0 || i >= list.size()){
			throw new ArrayIndexOutOfRangeException(i, list.size() - 1);
		}
		list.add(i, val);
		list.remove(i+1);
		return val;
	}
	
	public synchronized JValue remove(JThread jt, int i){
		assertWriteLock(jt);
		
		if(i < 0 || i >= list.size()){
			return TempValueFactory.createTempNullRefValue();
		}
		JValue result = list.remove(i);
		return result == null ? TempValueFactory.createTempNullRefValue() : result;
	}
	
	public int size(){
		return list.size();
	}
	
	public void sort(ThreadRuntime rt, final boolean desc){
		final ListWrapper lw = new ListWrapper(rt, hvalue);
		ListValueComparator comp = new ListValueComparator(lw, desc);
		list.sort(comp);
	}
	
	private class ListValueComparator extends ValueComparator {

		private ListWrapper lw;
		
		public ListValueComparator(ListWrapper lw, boolean desc) {
			super(desc);
			this.lw = lw;
		}

		@Override
		protected int compareObjectValues(JValue v1, JValue v2) {
			return lw.compare(v1, v2);
		}
		
	}

	//---------------- thread safety ----------------//
	
	private JThread thread;
	
	/**
	 * Let the specified thread have the exclusive write access to the list.
	 * 
	 * @param rt the runtime whose thread is to lock down the list.
	 */
	public void applyWriteLock(ThreadRuntime rt) {
		// If the list is already locked (either by us or someone else), just return.
		if(thread == null){
			synchronized(this){
				if(thread == null){
					// System.out.println("list write lock secured by " + rt.getJThread().getName());
					this.thread = rt.getJThread();
				}
			}
		}
	}
	
	/**
	 * Release the exclusive write access to the specified thread (only if this thread
	 * had successfully called {@link #applyWriteLock(ThreadRuntime)}).
	 * 
	 * @param rt the runtime whose thread has been locking down the list.
	 */
	public void releaseWriteLock(ThreadRuntime rt) {
		// If the list is not locked or not locked by us, just return.
		JThread t = rt.getJThread();
		if(thread == t){
			synchronized(this){
				if(thread == t){
					// System.out.println("list write lock released by " + rt.getJThread().getName());
					this.thread = null;
				}
			}
		}
	}
	
	private void assertWriteLock(JThread t) {
		if(thread != null && thread != t){
			throw new JConcurrentModificationException(FullTypeName);
		}
	}
	
}
