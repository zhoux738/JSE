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

import info.julang.execution.Argument;
import info.julang.execution.EngineRuntime;
import info.julang.execution.Executable;
import info.julang.execution.Result;
import info.julang.execution.namespace.NamespacePool;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.external.exceptions.JSEError;
import info.julang.memory.StackArea;
import info.julang.memory.simple.SimpleStackArea;
import info.julang.memory.value.HostedValue;
import info.julang.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A global thread manager that tracks all the threads running in the script engine.
 * <p>
 * In Julian, the starting script executes in the main thread, which is also the sole
 * foreground thread. The engine terminates when the main thread runs to completion,
 * and returns result yielded by it. All other threads, which are kicked off through
 * Julian's standard API (<code><font color="green">System.Concurrency.Thread</font></code>), 
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
 *            ||      |             <-------[runBackground]----+  |
 *            ||      |             <-------[runBackground]-------+
 *            ==      +-------------+</pre>
 * The ultimate design goal is that all threads running within JSE should be managed 
 * by this class. This, however, is not the status quo as of 0.2.0, where threads may 
 * be initiated from JuFC platform end, such as a system hook invoked by asynchronous 
 * API. To ensure that such practice doesn't break the engine, always create a new 
 * thread runtime to be used in JSE-unmanaged threads.
 * <p> 
 * @author Ming Zhou
 */
public class JThreadManager {

	private final static String MAIN_THREAD_NAME = "<Julian-Main>";
	private final static String BG_THREAD_PREFIX = "<Julian-Worker>-";
	
	private final DefaultStackFactory sfactory = new DefaultStackFactory();
	
	// The thread id generator
	private final AtomicInteger idSequencer = new AtomicInteger(-1);
	
	// A map used to store all the threads that have been run. We need this storage because
	// ExecutorService doesn't expose one.
	private final Map<Integer, Pair<JThread, JThreadRunnable>> threads = 
		new ConcurrentHashMap<Integer, Pair<JThread, JThreadRunnable>>();
	
	// A list storing all the faulted threads.
	private List<FaultedThreadRecord> faulted = 
		Collections.synchronizedList(new ArrayList<FaultedThreadRecord>());

	private JThread main;
	
	// Internal states
	private boolean running;
	private boolean terminating;
	private final AtomicInteger runCount = new AtomicInteger(0);
	
	// The following are for Julian's concurrency API, so only to be lazily initialized
	private ExecutorService executor;
	
	/**
	 * Create a background thread but not run it.
	 * 
	 * @param name if null or empty, will assign a unique name.
	 * @param engineRt engine runtime
	 * @param exec the executable
	 * @param nsPool namespace pool
	 * @param threadObjInJulian script thread object
	 * @param pri the thread priority
	 * @return
	 */
	public JThread createBackground(
		String name, 
		EngineRuntime engineRt, 
		Executable exec, 
		NamespacePool nsPool, 
		HostedValue threadObjInJulian, 
		JThreadPriority pri){
		int id = idSequencer.incrementAndGet();
		if (name == null || "".equals(name)){
			synchronized (this) {
				name = BG_THREAD_PREFIX + id;
			}
		}
		
		JThreadProperties props = new JThreadProperties();
		props.setDaemon(true);
		props.setPripority(pri);
		props.setRunCount(runCount.get());
		
		JThread jt = JThread.createNewThread(
			id, name, sfactory, engineRt, exec, nsPool, props);
		jt.setScriptThreadObject(threadObjInJulian);
		
		return jt;
	}
	
	/**
	 * Run a {@link JThread} as a background thread on the platform.
	 * 
	 * @param thread
	 * @return the {@link JThreadRunnable runnable} created from this {@link JThread}, which can be waited on.
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
				initializeExecutorService();
			}
			
			// Pass "this" along in order to call JThread.exeThread() properly.
			HostedValue thisObj = thread.getScriptThreadObject();
			JThreadRunnable runnable = thread.getRunnable(
				this, new Argument[]{ new Argument("this", thisObj) });
			executor.execute(runnable);

			return runnable;
		}
	}
	
	/**
	 * Create and set main thread with name = "<Julian-Main>". Cannot be called if the engine is running.
	 * 
	 * @param engineRt
	 * @param exec
	 * @return
	 */
	public JThread createMain(EngineRuntime engineRt, Executable exec){
		synchronized (this) {
			assertNotRunning("Cannot create main thread when the engine is running.");

			JThreadProperties props = new JThreadProperties();
			props.setDaemon(false);
			props.setPripority(JThreadPriority.NORMAL);
			
			JThread t = JThread.createNewThread(
				idSequencer.incrementAndGet(), MAIN_THREAD_NAME, sfactory, engineRt, exec, null, props);
			main = t;
		}
	
		return main;
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
			JThreadRunnable runnable = main.getRunnable(this, args);
			
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
		} finally {
			// 1) Lock down the manager. No more threads can be run.
			terminating = true;
			
			synchronized (this) {
				if (executor != null){
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
	
	/**
	 * Get a list of faulted threads in the last run.
	 * 
	 * @return
	 */
	public List<FaultedThreadRecord> getFaultedThreads(){
		return new ArrayList<FaultedThreadRecord>(faulted);
	}
	
	// To reduce locking overhead, always call this with a check condition
	private synchronized void initializeExecutorService(){
		if (executor == null) {
			BlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(1);
			
			int procNum = Math.max(Runtime.getRuntime().availableProcessors(), 1);
			executor = new JSEThreadPoolExecutor(procNum, queue);				
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
	
	//------------------ Customized ThreadPoolExecutor ------------------//
	
	private static class JSEThreadPoolExecutor extends ThreadPoolExecutor {

		private JSEThreadPoolExecutor(
			int minThreads, BlockingQueue<Runnable> queue) {
			super(
				minThreads,				//	minimal number of threads
				Integer.MAX_VALUE,  	//  maximum number of threads
				1, 						//  keep-alive time before excess idle thread (beyond minimal) is recycled
				TimeUnit.MICROSECONDS,	//  unit of the parameter above
				queue,					//  a queue used to keep submitted tasks when all the threads are occupied
				new DefaultThreadFactory());
			                            //  the factory to produce thread from the given runnable. See DefaultThreadFactory in this file.
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
