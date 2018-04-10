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

package info.julang.typesystem.jclass.jufc.System.Concurrency;

import info.julang.execution.Argument;
import info.julang.execution.Executable;
import info.julang.execution.namespace.NamespacePool;
import info.julang.execution.threading.BadThreadStateException;
import info.julang.execution.threading.JThread;
import info.julang.execution.threading.JThreadPriority;
import info.julang.execution.threading.JThreadRunnable;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.hosting.HostedMethodProviderFactory;
import info.julang.hosting.SimpleHostedMethodProvider;
import info.julang.hosting.execution.CtorNativeExecutor;
import info.julang.hosting.execution.InstanceNativeExecutor;
import info.julang.hosting.execution.StaticNativeExecutor;
import info.julang.memory.StackArea;
import info.julang.memory.value.FuncValue;
import info.julang.memory.value.HostedValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.RefValue;
import info.julang.memory.value.StringValue;
import info.julang.memory.value.VoidValue;
import info.julang.typesystem.jclass.builtin.JFunctionType;

/**
 * The platform binding of <code><font color="green">System.Concurrency.Thread</font></code>.
 * 
 * @author Ming Zhou
 */
public class ScriptThread {

	public static class ScriptThreadPriority {
		public final static String FullTypeName = "System.Concurrency.ThreadPriority";		
	}
	
	public static enum ScriptThreadState {
		
		READY, 
		RUNNING, 
		PENDING, 
		DONE;
		
		public final static String FullTypeName = "System.Concurrency.ThreadState";	
	}
	
	public final static String FullTypeName = "System.Concurrency.Thread";
	
	//----------------- IRegisteredMethodProvider -----------------//
	
	public static HostedMethodProviderFactory Factory = new HostedMethodProviderFactory(FullTypeName){

		@Override
		protected void implementProvider(SimpleHostedMethodProvider provider) {
			provider
				.add("getCurrent", new GetCurrentExecutor())
				.add("init", new InitExecutor())
				.add("getName", new GetNameExecutor())
				.add("start", new StartExecutor())
				.add("join", new JoinExecutor())
				.add("sleep", new SleepExecutor())
				.add("getState", new GetStateExecutor())
				.add("interrupt", new InterruptExecutor())
				.add("checkInterruption", new CheckInterruptionExecutor());
		}
	};
	
	//----------------- native executors -----------------//
	
	private static class GetCurrentExecutor extends StaticNativeExecutor<ScriptThread> {

		@Override
		protected JValue apply(ThreadRuntime rt, Argument[] args) throws Exception {
			// The Julian thread object was stored when thread.init(...) => threadManager.createBackground(...)
			// was called, in this same source file.
			return rt.getJThread().getScriptThreadObject();
		}
	}
	
	private static class InitExecutor extends CtorNativeExecutor<ScriptThread> {

		@Override
		protected void initialize(ThreadRuntime rt, HostedValue hvalue, ScriptThread thread, Argument[] args) throws Exception {		
			FuncValue fv = (FuncValue) RefValue.dereference(hvalue.getMemberValue("exeThread"));
			JFunctionType funTyp = (JFunctionType)fv.getType();
			String name = getString(args, 0);
			int pri = getEnumAsOrdinal(args, 1);			
			
			thread.init(rt, hvalue, funTyp, name, pri);
			
			setOverwrittenReturnValue(VoidValue.DEFAULT);
		}
		
	}
	
	private static class GetNameExecutor extends InstanceNativeExecutor<ScriptThread> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, ScriptThread thread, Argument[] args) throws Exception {	
			return thread.getName(rt);
		}
		
	}
	
	private static class StartExecutor extends InstanceNativeExecutor<ScriptThread> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, ScriptThread thread, Argument[] args) throws Exception {	
			thread.start(rt);
			return VoidValue.DEFAULT;
		}
		
	}
	
	private static class SleepExecutor extends StaticNativeExecutor<ScriptThread> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, Argument[] args) throws Exception {	
			int totalMilli = getInt(args, 0);
			boolean interrupted = sleep(rt, totalMilli);
			return createBool(rt, interrupted);
		}
		
	}
	
	private static class JoinExecutor extends InstanceNativeExecutor<ScriptThread> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, ScriptThread thread, Argument[] args) throws Exception {	
			thread.join(rt);
			return VoidValue.DEFAULT;
		}
		
	}
	
	private static class GetStateExecutor extends InstanceNativeExecutor<ScriptThread> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, ScriptThread thread, Argument[] args) throws Exception {	
			ScriptThreadState state = thread.getState();
			return createEnumValue(ScriptThreadState.FullTypeName, rt, state.toString());
		}
		
	}

	private static class InterruptExecutor extends InstanceNativeExecutor<ScriptThread> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, ScriptThread thread, Argument[] args) throws Exception {	
			thread.interrupt(rt);
			return VoidValue.DEFAULT;
		}
		
	}
	
	private static class CheckInterruptionExecutor extends StaticNativeExecutor<ScriptThread> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, Argument[] args) throws Exception {	
			boolean interrupted = checkInterruption(rt);
			return createBool(rt, interrupted);
		}
		
	}

	//----------------- implementation at native end -----------------//

	private JThread thread;
	private JThreadRunnable runnable;
	
	public ScriptThread(){
		
	}
	
	/**
	 * Used only for non-user defined threads, such as main thread.
	 * 
	 * @param thread
	 * @param runnable
	 */
	public ScriptThread(JThread thread, JThreadRunnable runnable){
		this.thread = thread;
		this.runnable = runnable;
	}
	
	private void init(ThreadRuntime rt, HostedValue hvalue, JFunctionType funTyp, String name, int pri){
		Executable exec = funTyp.getExecutable();
		NamespacePool np = funTyp.getNamespacePool();
		
		JThreadPriority priority = JThreadPriority.values()[pri];
		// When we create the new thread through thread manager, we associate the new thread with this 
		// ScriptThread object. ScriptThread (wrapped in Julian type "System.Concurrency.Thread") and 
		// JThread are two objects referring to the same thread, with the former used in the script 
		// and the latter in the engine internals.
		thread =  rt.getThreadManager().createBackground(name, rt, exec, np, hvalue, priority);
	}
	
	private JValue getName(ThreadRuntime rt) {
		// Create a name value in the current frame populated by the thread's name.
		StackArea sa = rt.getStackMemory();
		StringValue sv = new StringValue(sa, thread.getName());
		return sv;
	}
	
	private void start(ThreadRuntime rt){
		// CHECK:		
		// 1) cannot be called from the current thread
		canOnlyBeCalledFromADifferentThread(rt, "start()");
		
		if(runnable == null){
			synchronized(this){
				if(runnable == null){
					runnable = rt.getThreadManager().runBackground(thread);
					return;
				}
			}
		}
		
		// 2) cannot be called more than once
		throw new BadThreadStateException("start() cannot be called more than once.");
	}
	
	private void join(ThreadRuntime rt){
		// CHECK:
		// 1) cannot be called from the current thread
		canOnlyBeCalledFromADifferentThread(rt, "join()");
		
		// If called before the thread is start()ed, finish immediately.
		if(runnable != null){
			runnable.waitForCompletion();
		}
	}
	
	private static boolean sleep(ThreadRuntime rt, int totalMillis) {		
		try {
			// If we are already interrupted, do not bother.
			if(rt.getJThread().checkInterruption(true)){
				return true;
			}
			
			Thread.sleep(totalMillis);
		} catch (InterruptedException e) {
			rt.getJThread().checkInterruption(true);
			return true;
		}
		
		return false;
	}
	
	private ScriptThreadState getState() {
		return runnable != null ? runnable.getState() : ScriptThreadState.READY;
	}
	
	private void interrupt(ThreadRuntime rt){
		// If called before the thread is start()ed, no effect.
		if(runnable != null){
			// If called after the thread has run() to completion, no effect.
			if(runnable.getState() == ScriptThreadState.DONE){
				return;
			}
			
			thread.signalInterruption();
		}
	}
	
	private static boolean checkInterruption(ThreadRuntime rt){
		return rt.getJThread().checkInterruption(true);
	}
	
	//------------------- Internal checking methods -------------------//
	
//	private void canOnlyBeCalledFromTheCurrentThread(ThreadRuntime rt, String name){
//		if (rt.getJThread() != thread){
//			throw new BadThreadStateException(name + " can only be called from the current thread.");
//		}
//	}
	
	private void canOnlyBeCalledFromADifferentThread(ThreadRuntime rt, String name){
		if (rt.getJThread() == thread){
			throw new BadThreadStateException(name + " can only be called from a different thread.");
		}
	}
}
