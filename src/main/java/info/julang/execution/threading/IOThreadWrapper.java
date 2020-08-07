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

package info.julang.execution.threading;

import info.julang.execution.EngineRuntime;
import info.julang.execution.Executable;
import info.julang.execution.namespace.NamespacePool;
import info.julang.hosting.interop.JSEObjectWrapper;
import info.julang.memory.MemoryArea;
import info.julang.memory.value.FuncValue;
import info.julang.memory.value.ObjectValue;
import info.julang.typesystem.JType;
import info.julang.typesystem.VoidType;
import info.julang.typesystem.jclass.JParameter;
import info.julang.typesystem.jclass.builtin.JFunctionType;

/**
 * Interop object targeting <font color="green"><code>System.Concurrency.IOThreadWrapper</code></font>, 
 * which wraps an IO-continuation thread that continues polling from a job queue until signaled with completion.
 * 
 * @author Ming Zhou
 */
public class IOThreadWrapper extends JSEObjectWrapper implements IOThreadHandle {
	
	private boolean done;
	
	public static final String FullName = "System.Concurrency.IOThreadWrapper";
	
	public static final String Method_post = "post(Function)";
	public static final String Method_start = "start()";
	public static final String Method_complete = "complete()";
    public static final String Method_abort = "abort()";
	
	public static final String MethodName_createIOThread = "_createIOThread";

	public IOThreadWrapper(ThreadRuntime rt, ObjectValue ov, boolean completable) {
		// useIndependentThreadRuntime = true: must use a new thread runtime since we are going to call post() and complete() from JVM's worker thread.
		super(FullName, rt, ov, true); 
		
		this.registerMethod(Method_post,     "post",     false, new JType[]{ JFunctionType.getInstance() });
		this.registerMethod(Method_start,    "start",    false, new JType[]{ });
		this.registerMethod(Method_complete, "complete", false, new JType[]{ });
        this.registerMethod(Method_abort,    "abort",    false, new JType[]{ });
		
		this.done = !completable;
	}

	/**
	 * Post an executable to this IO thread.
	 * <p>
	 * The IO thread has an event loop which keeps polling from a blocking queue to get the next piece
	 * of work, wrapped in a Function. This method creates a Function value wrapping the given Executable,
	 * and passes it to the target queue.
	 * 
	 * @param ert engine runtime
	 * @param exec
	 */
	public void post(EngineRuntime ert, Executable exec){
		JFunctionType funcTyp = new JFunctionType("<IO-continuation>", new JParameter[0], VoidType.getInstance(), new NamespacePool(), exec);
		MemoryArea memory = ert.getHeap();
		FuncValue fv = FuncValue.createGlobalFuncValue(memory, funcTyp, true);
		
		this.runMethod(Method_post, fv);
	}
	
	/**
	 * Starts the IO thread, polling work items from queue infinitely until a termination signal is received.
	 */
	public synchronized void start(){
		this.runMethod(Method_start);
	}
	
	/**
	 * Mark completion for this IO thread. This can only have effect if the thread was created with completable = true.
	 */
	public synchronized void complete(){
		if (done) {
			return;
		}
		
		this.runMethod(Method_complete);
		done = true;
	}
	
	/**
	 * Only to be called by engine internals.
	 */
    synchronized void abort(){
        this.runMethod(Method_abort);
    }
}
