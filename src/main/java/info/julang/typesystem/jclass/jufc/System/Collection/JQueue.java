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

import java.util.Deque;
import java.util.LinkedList;

import info.julang.execution.Argument;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.hosting.HostedMethodProviderFactory;
import info.julang.hosting.SimpleHostedMethodProvider;
import info.julang.hosting.execution.CtorNativeExecutor;
import info.julang.hosting.execution.InstanceNativeExecutor;
import info.julang.memory.value.HostedValue;
import info.julang.memory.value.IntValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.RefValue;
import info.julang.memory.value.TempValueFactory;
import info.julang.memory.value.VoidValue;

public class JQueue {

	/*
	 * Use as a single directional queue:
	 * 
	 *   enqueue => Deque.add()
	 *   dequeue => Deque.poll()
	 * 
	 * Use as a stack:
	 * 
	 *   push => Deque.push()
	 *   pop  => Deque.pop()
	 *   peek => Deque.peek()
	 * 
	 */
	
	public final static String FullTypeName = "System.Collection.Queue";
	
	//----------------- IRegisteredMethodProvider -----------------//

	public static HostedMethodProviderFactory Factory = new HostedMethodProviderFactory(FullTypeName){

		@Override
		protected void implementProvider(SimpleHostedMethodProvider provider) {
			provider
				.add("ctor", new InitExecutor())
				.add("enqueue", new EnqueueExecutor())
				.add("dequeue", new DequeueExecutor())
				.add("size", new SizeExecutor());
		}
		
	};
	
	//----------------- native executors -----------------//
	
	private static class InitExecutor extends CtorNativeExecutor<JQueue> {

		@Override
		protected void initialize(ThreadRuntime rt, HostedValue hvalue, JQueue jq, Argument[] args) throws Exception {
			jq.init();
		}
		
	}
	
	private static class DequeueExecutor extends InstanceNativeExecutor<JQueue> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, JQueue jq, Argument[] args) throws Exception {
			JValue jv = jq.dequeue();
			return jv != null ? jv : RefValue.NULL;
		}
		
	}
	
	private static class EnqueueExecutor extends InstanceNativeExecutor<JQueue> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, JQueue jq, Argument[] args) throws Exception {
			JValue jv = args[0].getValue();
			jq.enqueue(jv);
			return VoidValue.DEFAULT;
		}
		
	}

	private static class SizeExecutor extends InstanceNativeExecutor<JQueue> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, JQueue jq, Argument[] args) throws Exception {
			IntValue iv = TempValueFactory.createTempIntValue(jq.size());
			return iv;
		}
		
	}
	
	//----------------- implementation at native end -----------------//
	
	private Deque<JValue> queue;
	
	public void init(){
		this.queue = new LinkedList<JValue>();
	}
	
	public synchronized JValue dequeue(){
		return queue.poll();
	}
	
	public synchronized void enqueue(JValue e){
		queue.add(e);
	}
	
	public int size(){
		return queue.size();
	}
	
}
