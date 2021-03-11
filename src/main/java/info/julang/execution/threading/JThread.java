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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import info.julang.execution.Argument;
import info.julang.execution.EngineRuntime;
import info.julang.execution.Executable;
import info.julang.execution.Result;
import info.julang.execution.StandardIO;
import info.julang.execution.namespace.NamespacePool;
import info.julang.execution.symboltable.ITypeTable;
import info.julang.execution.symboltable.IVariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.external.exceptions.JSEError;
import info.julang.interpretation.errorhandling.JulianScriptException;
import info.julang.memory.MemoryArea;
import info.julang.memory.StackArea;
import info.julang.memory.value.HostedValue;
import info.julang.memory.value.IFuncValue;
import info.julang.modulesystem.IModuleManager;
import info.julang.typesystem.JType;
import info.julang.typesystem.jclass.jufc.SystemTypeUtility;
import info.julang.typesystem.jclass.jufc.System.Concurrency.ScriptThread;
import info.julang.typesystem.loading.InternalTypeResolver;

/**
 * A JThread represents a thread running in script engine. It may or may not be backed by a native thread.
 * <p/>
 * A thread has its own stack and variable table. 
 * 
 * @author Ming Zhou
 */
public class JThread {

	// Thread immutable info
	private int id;
	private String name;
	private JThreadProperties props;
	private IFuncValue func;
	private Executable exec;
	
	// Thread runtime objects
	private boolean started;
	private boolean faulted;
	private ThreadStack tstack;
	private EngineRuntime engineRt;	
	private ThreadRuntime threadRt;
	private JThreadRunnable runnable; // Only initialized when the thread starts running.
	
	// Synchronization object
	private Object lock = new Object();
	
	// Thread signals
	private AtomicBoolean _flag_interrupted = new AtomicBoolean(false);
	private AtomicBoolean _flag_terminating = new AtomicBoolean(false);
		
	// Script interop: the thread object exposed in Julian (System.Concurrency.Thread)
	private HostedValue threadObjectInJulian;
	
	protected JThread(){
		
	}
	
	public static JThread createNewThread(
		int id, String name, StackAreaFactory stackFactory, EngineRuntime engineRt, IFuncValue func, Executable exec, NamespacePool nsPool, JThreadProperties props){
		JThread thread = new JThread();
		thread.id = id;
		thread.name = name;
		thread.tstack = new ThreadStack(stackFactory, engineRt.getGlobalVariableTable());
		if (nsPool != null) {
			thread.tstack.setNamespacePool(nsPool);
		}
		thread.func = func;
		thread.exec = exec;
		thread.engineRt = engineRt;
		thread.props = props;
		
		thread.threadRt = thread.new JThreadRuntime();
		
		return thread;
	}
	
	/**
	 * The method to run this thread. Always call this through the runnable object returned by 
	 * {@ #getRunnable(Argument[])}, which implements the standard {@link java.lang.Runnable 
	 * runnable} object that will call back this method when running in a Java thread. 
	 * 
	 * @param args
	 * @return
	 * @throws EngineInvocationError
	 */
	Result run(Argument[] args) throws EngineInvocationError {
		synchronized(lock){
			if(started){
				throw new EngineInvocationError("Cannot start a thread that is running.");
			} else {
				// legitimacy check
				if (runnable == null){
					throw new EngineInvocationError("Trying to start the thread in an illegal way. Aborting.");
				}
				
				started = true;
			}
		}

		// execute the thread in blocking mode
		try {
			Result result = exec.execute(threadRt, func, args);
			if(!result.isSuccess()){
				faulted = true;
			}
			return result;
		} catch (JulianScriptException jse) {	// 1) user error
			faulted = true;
			throw jse;
		} catch (JSEError error) { 				// 2) engine error (bug)
			faulted = true;
			throw new EngineInvocationError(
				"A fatal error occurs when running a thread (" + name + ") in Julian script engine.", error);
		}
	}
	
	/**
	 * Get a Runnable instance that can be run or scheduled in Java's threading API.
	 * <p/>
	 * One and only one {@link JThreadRunnable runnable} object will be ever generated from this JThread.
	 * 
	 * @param manager
	 * @param args
	 * @return {@link JThreadRunnable instance} associated with this JThread.
	 */
	public JThreadRunnable getRunnable(JThreadManager manager, Argument[] args) {
		if (runnable == null) {
			synchronized (lock) {
				if (runnable == null) {
					runnable = new JThreadRunnable(manager, this, args);
				}
			}
		}
		
		return runnable;
	}
		
	public ThreadRuntime getThreadRuntime(){
		return threadRt;
	}
	
	//----------------- Thread information -----------------//
	
	public int getId(){
		return id;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isFaulted(){
		return faulted;
	}
	
	public JThreadPriority getPriority() {
		return props.getPripority();
	}
	
	public boolean isDaemon() {
		return props.isDaemon();
	}
	
	public boolean isMain() {
		return props.isMain();
	}
	
	public int getRunCount() {
		return props.getRunCount();
	}
	
	public boolean isIOThread() {
		return props.isIOThread();
	}
	
	//----------------- Thread signaling -----------------//
	
	/**
	 * Check if the thread is interrupted. And reset the interruption flag if <code>reset</code> is true.
	 * 
	 * @param reset true to reset the interruption flag.
	 * @return true if interrupted
	 */
	public boolean checkInterruption(boolean reset){
		// If resetting, make sure we also reset the platform thread's state.
		// Note this must be done here instead of in the expression below to avoid expression short-cutting.
		boolean ptState = reset ? Thread.interrupted() : false;
		return reset ? 
				_flag_interrupted.getAndSet(false) || ptState
				: 
				_flag_interrupted.get();
	}
	
	/**
	 * Signal interruption to this thread.
	 */
	public void signalInterruption(){
		// 1) set local flag
		_flag_interrupted.set(true);
		
		// 2) interrupt the underlying thread to break out of sleep and waiting state.
		if(runnable != null){
			Thread pt = runnable.getPlatformThread();
			if(pt != null){
				pt.interrupt();
			}
		}
	}
	
	/**
	 * Check if the thread is asked to terminate.
	 * @return
	 */
	public boolean checkTermination(){
		return _flag_terminating.get();
	}
	
	/**
	 * Signal termination to this thread. This operation is irreversible.
	 */
	public void signalTermination(){
		_flag_terminating.set(true);
	}
	
	//----------------- Julian/Java interop -----------------//
	
	/**
	 * Set the System.Concurrency.Thread object representing this thread through Julian's concurrency API.
	 * <p>
	 * At the point this is called, the hosted object has not been set, so we cannot check if the object
	 * contains the right platform object (ScriptThread.class). Use caution when calling this method.
	 * @param threadObjectInJulian
	 */
	synchronized void setScriptThreadObject(HostedValue threadObjectInJulian){
		if(this.threadObjectInJulian == null){
			this.threadObjectInJulian = threadObjectInJulian;
		} else {
			throw new JSEError(
				"Cannot bind a thread (" + 
				name + 
				") + with a Julian thread object. The current thread is already bound.");
		}
	}
	
	/**
	 * Get the System.Concurrency.Thread object representing this thread through Julian's concurrency API.
	 * This method will fault if the thread has not fully initialized.
	 * 
	 * @return a {@link HostedValue} which wraps an object of {@link ScriptThread}.
	 */
	public synchronized HostedValue getScriptThreadObject() {		
		if(threadObjectInJulian == null){
			if (props.isMain()){				
				// If this is the main thread, lazy-initialize the object.
				JType typ = SystemTypeUtility.ensureTypeBeLoaded(threadRt, ScriptThread.FullTypeName);
				
				ScriptThread st = new ScriptThread(this, this.runnable);
				HostedValue hv = new HostedValue(threadRt.getHeap(), typ);
				hv.setHostedObject(st);
				
				threadObjectInJulian = hv;
			} else {
				throw new JSEError(
					"The current thread (" + 
					name + 
					") + doesn't have an associated Julian thread object.");
			}
		}
		
		return threadObjectInJulian;
	}
	
	/**
	 * Safely call wait() on the monitor.
	 * <p/>
	 * This method must be used when awaiting a monitor while ignoring the interruption
	 * signal sent from scripts (through <code><font color="green">
	 * System.Concurrency.Thread.interrupt()</font></code>). All the system usage of wait()
	 * must be called through this wrapper.
	 * 
	 * @param monitor
	 * @param condition a condition to determine if we should quite waiting if interrupted.
	 */
	public void safeWait(Object monitor, MonitorInterruptCondition condition){
		while(true){
			try {
				monitor.wait();
				// Return unconditionally if we are notified
				return;
			} catch (InterruptedException e) {
				if (condition != null && condition.shouldInterrupt()) {
					// Condition met
					return;
				} else if (checkTermination()) {
					// Terminate now
					throw new JThreadAbortedException(this);
				}
				
				// Otherwise, re-enter waiting state.
			}
		}
	}
	
	/**
	 * An interface defining the condition on which {@link JThread#safeWait(Object)} should quit.
	 */
	public static interface MonitorInterruptCondition {
		
		/**
		 * @return true to quite waiting.
		 */
		boolean shouldInterrupt();
		
	}
	
	/**
	 * The internal thread runtime implementation.
	 * 
	 * @author Ming Zhou
	 */
	private class JThreadRuntime implements ThreadRuntime {

		private JThreadRuntime(){
			
		}
		
		@Override
		public JThread getJThread(){
			return JThread.this;
		}
		
		@Override
		public MemoryArea getHeap() {
			return engineRt.getHeap();
		}

		@Override
		public ITypeTable getTypeTable() {
			return engineRt.getTypeTable();
		}
		
		@Override
		public IVariableTable getGlobalVariableTable() {
			return engineRt.getGlobalVariableTable();
		}

		@Override
		public StackArea getStackMemory() {
			return JThread.this.tstack.getStackArea();
		}

		@Override
		public ThreadStack getThreadStack() {
			return JThread.this.tstack;
		}

		@Override
		public IModuleManager getModuleManager() {
			return engineRt.getModuleManager();
		}

		@Override
		public InternalTypeResolver getTypeResolver() {
			return engineRt.getTypeResolver();
		}

		@Override
		public JThreadManager getThreadManager() {
			return engineRt.getThreadManager();
		}

		@Override
		public StandardIO getStandardIO() {
			return engineRt.getStandardIO();
		}

        @Override
        public Object putLocal(String key, IThreadLocalObjectFactory factory) {
            if (tls == null || !tls.containsKey(key)) {
                synchronized(JThread.this.lock){
                    if (tls == null){
                        tls = new HashMap<String, Object>();   
                    }
                    
                    if (!tls.containsKey(key)){
                        tls.put(key, factory.create());  
                    }
                }
            }
            
            return tls.get(key);
        }

        @Override
        public Object getLocal(String key) {
            if (tls == null || !tls.containsKey(key)) {
                synchronized(JThread.this.lock){
                    if (tls != null){
                        return tls.get(key);  
                    }
                }
            }
            
            return null;
        }
        
        private Map<String, Object> tls;
	}
}
