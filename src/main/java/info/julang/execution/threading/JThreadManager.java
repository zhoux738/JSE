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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import info.julang.execution.Argument;
import info.julang.execution.EngineRuntime;
import info.julang.execution.Executable;
import info.julang.execution.Result;
import info.julang.execution.namespace.NamespacePool;
import info.julang.execution.security.EngineLimit;
import info.julang.execution.simple.SimpleEngineRuntime;
import info.julang.execution.symboltable.VariableTable;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.external.exceptions.JSEError;
import info.julang.interpretation.InterpretedExecutable;
import info.julang.memory.StackArea;
import info.julang.memory.simple.SimpleStackArea;
import info.julang.memory.value.FuncValue;
import info.julang.memory.value.HostedValue;
import info.julang.memory.value.IFuncValue;
import info.julang.typesystem.jclass.jufc.System.Network.AsyncSocketSession;
import info.julang.util.Pair;

/**
 * A global thread manager that tracks all the threads running in the script engine.
 * <p>
 * In Julian, the starting script executes in the main thread, which is also the sole
 * foreground thread. The engine terminates when the main thread runs to completion,
 * and returns result yielded by it. All other threads, which are kicked off through
 * Julian's standard API (<code style="color:green">System.Concurrency.Thread</code>), 
 * are background threads that will not block the engine's termination. These threads 
 * will also be shut down when the engine terminates.
 * <pre>
 *            ==      +-------------+
 *            ||      | not running |
 *            ||      +-------------+
 *            ||             |
 *            ||        [createMain]
 *     ENGINE STARTING       |
 *            ||         [execute]
 *            ==             |
 *            ||      +------V------+   
 *            ||      |             |
 *            ||      |             +-----[createBackground]---+
 *     ENGINE RUNNING |             +-----[createBackground]---|--+
 *            ||      |   running   |                          |  |
 *            ||      |             |                          |  |
 *            ||      |             ((------[runBackground]----+  |
 *            ||      |             ((------[runBackground]-------+
 *            ==      +-------------+</pre>
 * All threads running within JSE should be managed by this class. If a Java API
 * calls back a method on a new JVM thread, such as the case with asynchronous IO, 
 * we must not execute the logic, should it involve any Julian scripting, inline.
 * Instead, fetch an IO thread from this manager and post work items to it. See 
 * {@link #fetchIOThread(ThreadRuntime, boolean)} for more information.
 * <p>
 * 
 * @author Ming Zhou
 */
// IMPLEMENTATION NOTES
//
// This class is implemented on top of a ThreadPoolExecutor. Whether this is the 
// right approach is up to debate. The ThreadPoolExecutor queues up incoming 
// work items and only starts dispatching new thread beyond the core number when 
// the queue is full. This means under certain circumstances the engine may not 
// schedule a new thread requested by the user, and if all the running threads 
// are long running instances, the new threads will never get chance to execution. 
// For now, the core/max/queue-length are configured to reduce, but not completely
// prevent, such possibility. 
public class JThreadManager {

	private final static String MAIN_THREAD_NAME = "<Julian-Main>";
	private final static String BG_THREAD_PREFIX = "<Julian-Worker>-";
	
	private final DefaultStackFactory sfactory;

    private JThread main;
    
	// The thread id tracker
	private final SequenceNumberTracker idSequencer;
	
	// A map used to store all the threads that have been run. We need this storage because
	// ExecutorService doesn't expose one.
	private final Map<Integer, Pair<JThread, JThreadRunnable>> threads;
	
	// A list storing all the faulted threads.
	private final List<FaultedThreadRecord> faulted;
	
	// A stack of main thread objects. When the main thread is replaced, the previous one will be pushed;
	// When the replacing thread is done, the top thread will be popped and resumed.
	private Deque<JThread> mainThreads;
	
	// IO related
	private final IOThreadPool iopool;
    private AsyncSocketSession sockSession;
	
	// Internal states
	private boolean running;
	private boolean terminating;
	private final AtomicInteger runCount;
	
	// The following are for Julian's concurrency API, so only to be lazily initialized
	private JSEThreadPoolExecutor executor;
	
	public JThreadManager(){
	    sfactory = new DefaultStackFactory();
	    idSequencer = new SequenceNumberTracker();
	    threads = new ConcurrentHashMap<Integer, Pair<JThread, JThreadRunnable>>();
	    faulted = Collections.synchronizedList(new ArrayList<FaultedThreadRecord>());
	    iopool = new IOThreadPool();
	    runCount = new AtomicInteger(0);
	}
	
	//-------------------------- main thread stacking --------------------------//

	public synchronized JThread resumePreviousMain() {
		if (mainThreads == null || mainThreads.size() == 0) {
			throw new JSEError("Cannot resume a previous main thread as there is no such one.");
		}
		
		return (main = mainThreads.pop());
	}
	
	public synchronized JThread getPreviousMain() {
		if (mainThreads == null || mainThreads.size() == 0) {
			throw new JSEError("Cannot get a previous main thread as there is no such one.");
		}
		
		return mainThreads.peek();
	}
	
	public synchronized JThread getCurrentMain() {	
		if (main == null) {
			throw new JSEError("Cannot get the first main thread when the engine is not running.");
		}
		
		return main;
	}
	
	public synchronized JThread getFirstMain() {
		if (mainThreads != null && mainThreads.size() > 0) {
			return mainThreads.getLast();
		} else {
			return getCurrentMain();
		}
	}
	
	/**
	 * Get all of the main thread objects in the order of evaluation.
	 */
	public synchronized JThread[] getAllMains() {
		JThread[] threads = new JThread[main != null ? 1 + (mainThreads != null ? mainThreads.size() : 0) : 0];
		int i = threads.length;
		if (main != null) {
			threads[--i] = main;
		}
		
		if (mainThreads != null) {
			for (JThread mt : mainThreads) {
				threads[--i] = mt;
			}
		}
		
		return threads;
	}
	
	/**
	 * Replicate the main thread with another one, inheriting all the thread-identifiable info such as id and name, 
	 * sharing all runtime info except for global variable table, which is completely replaced with a new and empty one.
	 * 
	 * @param engineRt
	 * @param exec
	 * @return
	 */
	public JThread replaceMain(EngineRuntime engineRt, InterpretedExecutable exec) {
		if (main == null) {
			throw new JSEError("Cannot replace main thread when the engine is not running.");
		}
		
		// Replicate EngineRuntime with new global variable table
		SimpleEngineRuntime newEngRt = new SimpleEngineRuntime(
			engineRt.getHeap(),
			new VariableTable(null), // new GVT
			engineRt.getTypeTable(),
			engineRt.getModuleManager(),
			engineRt.getThreadManager());
		newEngRt.setStandardIO(engineRt.getStandardIO());
		
		synchronized (this) {
			JThread currMain = main;
			main = JThread.replicateThread(main, sfactory, exec, newEngRt);
			
			if (mainThreads == null) {
				mainThreads = new LinkedList<JThread>();
			}
			
			// Store the previous main thread.
			mainThreads.push(currMain);
		}
		
		return main;
	}
	
	//-------------------------- IO threading --------------------------//
	
	/**
	 * Fetch an IO thread from the pool.
	 * 
	 * @param rt
	 * @param pooled If false, will create a new IO thread.
	 * @return A handle for IO thread. Always treat the thread as unpooled and send complete() after the use. A pooled thread will ignore the signal. 
	 */
    public IOThreadHandle fetchIOThread(ThreadRuntime rt, boolean pooled) {
        return iopool.fetch(rt, !pooled);
    }
    
    /**
     * Get the globally shared async socket session. This session is used by all sockets to
     * perform asynchronous operations driven by a single polling thread. The post-IO works
     * will be posted to a limited IO thread pool. So the total thread usage is 1 + K (K is 
     * a very small number, usually equal to the count of CPU cores)
     * 
     * @param rt
     * @return
     */
    public synchronized AsyncSocketSession getAsyncSocketSession(ThreadRuntime rt) {
    	if (sockSession == null) {
    		sockSession = new AsyncSocketSession(rt);
    	}
    	
    	return sockSession;
    }
    
	//-------------------------- background thread management --------------------------//
	
	/**
	 * Create a background thread but not run it.
	 * 
	 * @param name if null or empty, will assign a unique name.
	 * @param engineRt engine runtime
	 * @param func function value
	 * @param exec the executable
	 * @param nsPool namespace pool
	 * @param threadObjInJulian script thread object
	 * @param pri the thread priority
	 * @return
	 */
	public JThread createBackground(
		String name, 
		EngineRuntime engineRt, 
		IFuncValue func, 
		Executable exec, 
		NamespacePool nsPool, 
		HostedValue threadObjInJulian, 
		boolean isIOThread,
		JThreadPriority pri){
	    Pair<Integer, String> idName = getThreadIdName(name);
		int id = idName.getFirst();
        name = idName.getSecond();
		
		JThreadProperties props = new JThreadProperties();
		props.setDaemon(true);
		props.setPriority(pri);
		props.setRunCount(runCount.get());
		props.setIOThread(isIOThread);
		
		JThread jt = JThread.createNewThread(
			id, name, sfactory, engineRt, func, exec, nsPool, props);
		jt.setScriptThreadObject(threadObjInJulian);
		
		return jt;
	}
	
	/**
	 * Run a {@link JThread} as a background thread on the platform.
	 * 
	 * @param thread
	 * @return the {@link JThreadRunnable runnable} created from this {@link JThread}, which can be waited on.
	 * @throws info.julang.execution.security.RuntimeQuotaException if by running this thread it would break the max thread limit.
	 */
	public JThreadRunnable runBackground(JThread thread){
		if (thread == main) {
			throw new JSEError("Cannot run main thread as background thread.");
		}
		
		if (terminating) {
			throw new JThreadAbortedException(thread);
		}
		
		synchronized (this) {
			if(!running || thread.getRunCount() != runCount.get()){
				// too late, the executor has run to completion.
				throw new JThreadAbortedException(thread);
			}
			
			if (executor == null) {
				int maxThreads = thread.getThreadRuntime().getModuleManager().getEnginePolicyEnforcer().getLimit(EngineLimit.MAX_THREADS);
				initializeExecutorService(maxThreads);
			}
			
			JThreadRunnable runnable = executor.execute(thread);

			return runnable;
		}
	}

	//-------------------------- main thread management --------------------------//
	
	/**
	 * Create and set main thread with name = "&lt;Julian-Main&gt;". Cannot be called if the engine is running.
	 * 
	 * @param engineRt
	 * @param exec
	 * @return The object representing the main thread running in Julian engine. It has not been started.
	 */
	public JThread createMain(EngineRuntime engineRt, InterpretedExecutable exec){
		synchronized (this) {
			assertNotRunning("Cannot create main thread when the engine is running.");

			JThreadProperties props = new JThreadProperties();
			props.setDaemon(false);
			props.setPriority(JThreadPriority.NORMAL);

		    Pair<Integer, String> idName = getThreadIdName(MAIN_THREAD_NAME);
		    int id = idName.getFirst();
		    String name = idName.getSecond();
		        
			JThread t = JThread.createNewThread(
			    id, name, sfactory, engineRt, FuncValue.DUMMY, exec, null, props);
			
			main = t;
		}
	
		return main;
	}
	
	/**
	 * Run a thread inline.
	 */
	public Result runThreadInline(JThread thread, Argument[] args) throws EngineInvocationError {
		// Run the main thread inline.
		JThreadRunnable runnable = thread.getRunnable(this, args);
		
		runnable.run();
		if (runnable.isSuccess()){
			return runnable.getResult();
		} else {
			Exception e = runnable.getException();
			if (e instanceof RuntimeException){
				// This includes JulianRuntimeException and JSERuntimeException
				RuntimeException re = (RuntimeException) e;
				throw re;
			} else if (e instanceof EngineInvocationError){
				EngineInvocationError eie = (EngineInvocationError) e;
				throw eie;
			} else {
				throw new EngineInvocationError("Unknown exception caught in Julian engine.", e);
			}
		}
	}
	
	/**
	 * Execute main thread and returns the result.
	 * 
	 * @return
	 */
	public Result runMain(Argument[] args) throws EngineInvocationError {
		synchronized (this) {
			assertNotRunning("Cannot execute main thread while the engine is running.");
			
			threads.clear();
			faulted.clear();
			
			running = true;
		}
		
		try {
			// Run the main thread inline.
			return runThreadInline(main, args);
		} finally {
			// 1) Lock down the manager. No more threads can be run.
			terminating = true;
			
			synchronized (this) {
				if (executor != null){
				    // Send complete signal to all IO-continuation threads.
				    iopool.terminate();
				    
					for(Entry<Integer, Pair<JThread, JThreadRunnable>> entry : threads.entrySet()){
						JThread t = entry.getValue().getFirst();
						
						// 2) Send terminate signal to all threads to force a termination.
						t.signalTermination();
						
						// 3) Send interrupt signal to all threads to break out wait/sleep.
						//    (Since we have sent terminate signal, all system-level wait() 
						//    calls will be aborted too)
						t.signalInterruption();
					}
					
					// THIS IS NOT NECESSARY SINCE WE HAVE DONE ALL IT CAN POSSIBLY DO
					// 4) Call service shutdown. Note this is merely making some best efforts.
					// executor.shutdownNow();
					
					// 5) Now let's drain the executor for a short while.
					// This will not warrant that all threads terminate. In fact, threads which are
					// trying to schedule new threads through runBackground() are deadlocking with 
					// us right now. By waiting for a while, we effectively let all such threads
					// arriving at the sync region, so that we can safely update the running count.
					try {
						executor.awaitTermination(10, TimeUnit.MILLISECONDS);
					} catch (InterruptedException e) {
						// Ignore.
					}
				}
				
				// Re-throw the first unhandled exception caught in a background thread.
				Exception ex = null;
				for(FaultedThreadRecord record : faulted){
					if(!record.isMain() && record.isFatal()){
						ex = record.getException();
						break;
					}
				}
				
				// Reset internal state to allow re-entrance.
				main = null;
				running = false;
				terminating = false;
				executor = null;
				
				// Update the running count.
				runCount.incrementAndGet();
				
				if(ex != null){
					throw new EngineInvocationError("Unknown exception caught in Julian engine.", ex);
				}
			}
		}
	}
	
	//-------------------------- thread state and info --------------------------//
	
	/**
	 * Check if the managed threads are running. This method returns false if the engine has not started,
	 * or true if the engine has started tearing down.
	 */
	public boolean isRunning(){
		return running && !terminating;
	}
	
	/**
	 * Get a list of faulted threads in the last run.
	 * 
	 * @return
	 */
	public List<FaultedThreadRecord> getFaultedThreads(){
		return new ArrayList<FaultedThreadRecord>(faulted);
	}
    
    private Pair<Integer, String> getThreadIdName(String name){
        int id = idSequencer.obtain();
        if (name == null || "".equals(name)){
            name = BG_THREAD_PREFIX + id;
        }
        
        return new Pair<>(id, name);
    }
	
	// To reduce locking overhead, always call this with a check condition
	private synchronized void initializeExecutorService(int threadLimit){
		if (executor == null) {
			// The length is set to 1, the smallest number possible. This is to let
			// the executor to dispatch the new threads as soon as possible.
			BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(1);

			int min = Math.min(Math.max(Runtime.getRuntime().availableProcessors() * 8, 1), 1024);
			if (threadLimit > 0) {
				// If thread limit is set, set the max thread number at twice the limit. We are not hitting there anyway.
				threadLimit = Math.min(min, threadLimit);
				executor = new JSEThreadPoolExecutor(true, threadLimit, threadLimit * 2, queue);	
			} else {
				// Otherwise, have each processor maintain a minimum of 8 threads.
				executor = new JSEThreadPoolExecutor(false, min, Integer.MAX_VALUE, queue);		
			}		
		}
	}
	
	private void assertNotRunning(String message){
		if (running) {
			throw new JSEError(message);
		}
	}
	
	private class DefaultStackFactory implements StackAreaFactory {
		@Override
		public StackArea createStackArea() {
			return new SimpleStackArea();
		}	
	}
	
	//------------------ customized ThreadPoolExecutor ------------------//
	
	private class JSEThreadPoolExecutor extends ThreadPoolExecutor {
		
		private int threadLimit = EngineLimit.UNDEFINED;
		
		private JSEThreadPoolExecutor(
			boolean hasLimit, int minThreads, int maxThreads, BlockingQueue<Runnable> queue) {
			super(
				minThreads,				//	minimum number of threads
				maxThreads,  			//  maximum number of threads
				1, 						//  keep-alive time before excess idle thread (beyond minimal) is recycled
				TimeUnit.MICROSECONDS,	//  unit of the parameter above
				queue,					//  a queue used to keep submitted tasks when all the threads are occupied
				new DefaultThreadFactory());
			                            //  the factory to produce thread from the given runnable. See DefaultThreadFactory in this file.
		
			if (hasLimit) {
				threadLimit = minThreads;
			}
		}
		
		@Override
		protected void beforeExecute(Thread t, Runnable r) {
			if (r instanceof JThreadRunnable){
				JThreadRunnable jtr = (JThreadRunnable)r;
				JThread jt = jtr.getJThread();
				t.setName(jt.getName());
				t.setPriority(jt.getPriority().toJavaThreadPriority());
			}
		}
		
		JThreadRunnable execute(JThread jthread) {
			// Check the limit.
			//   - Engine limit must be preset before running, so it's fine to cache the limit's value.
			//   - Use getActiveCount() + 2 to count the main thread and the thread to execute here.
			if (threadLimit != EngineLimit.UNDEFINED) {
				jthread.getThreadRuntime().getModuleManager().getEnginePolicyEnforcer().checkLimit(
					EngineLimit.MAX_THREADS, this.getActiveCount() + 2);
			}
			
			// Pass "this" along in order to call JThread.exeThread() properly.
			HostedValue thisObj = jthread.getScriptThreadObject();
			JThreadRunnable runnable = jthread.getRunnable(
				JThreadManager.this, new Argument[]{ new Argument("this", thisObj) });
			
			if (jthread.isIOThread()) {
				// If this is an IO thread, do not run it in the executor. 
				// All the IO threads must be started immediately, but executor will not dispatch new threads before the queue is full.
				Thread t = new Thread(runnable);
				t.start();
			} else {
				this.execute(runnable);
			}
			
			return runnable;
		}
	}
	
	private static class DefaultThreadFactory implements ThreadFactory {

		@Override
		public Thread newThread(Runnable r) {
			Thread t = new Thread(r);
			t.setDaemon(true);
			return t;
		}
		
	}
	
	//------------------ thread registration ------------------//
	
	/**
	 * Register a new running thread.
	 * 
	 * @param thread
	 * @param runnable
	 */
	void addThread(JThread thread, JThreadRunnable runnable){
		threads.put(thread.getId(), new Pair<JThread, JThreadRunnable>(thread, runnable));
	}
	
	/**
	 * Unregister a completed thread.
	 * 
	 * @param threadId
	 */
	void removeThread(int threadId){		
		threads.remove(threadId);
		idSequencer.recycle(threadId);
	}
	
	/**
	 * Register an exception thrown from the specified thread.
	 * 
	 * @param threadId
	 * @param ex
	 */
	void registerError(int threadId, boolean fatal, Exception ex){
		Pair<JThread, JThreadRunnable> pair = threads.get(threadId);
		FaultedThreadRecord record = new FaultedThreadRecord(pair.getFirst(), fatal, ex);
		faulted.add(record);
	}

}
