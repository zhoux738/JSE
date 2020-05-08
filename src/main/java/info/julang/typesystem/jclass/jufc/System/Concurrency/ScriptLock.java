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
import info.julang.execution.threading.BadThreadStateException;
import info.julang.execution.threading.JThread;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.hosting.HostedMethodProviderFactory;
import info.julang.hosting.SimpleHostedMethodProvider;
import info.julang.hosting.execution.CtorNativeExecutor;
import info.julang.hosting.execution.InstanceNativeExecutor;
import info.julang.memory.value.HostedValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.VoidValue;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * The platform binding of <code><font color="green">System.Concurrency.Lock</font></code>.
 * 
 * @author Ming Zhou
 */
public class ScriptLock {

	public final static String FullTypeName = "System.Concurrency.Lock";
	public final static String MethodName_Lock = "lock";
	public final static String MethodName_Unlock = "unlock";
	private static long HalfMilliSecInNano = TimeUnit.MILLISECONDS.toNanos(1) >> 1;
	
	public static HostedMethodProviderFactory Factory = new HostedMethodProviderFactory(FullTypeName){

		@Override
		protected void implementProvider(SimpleHostedMethodProvider provider) {
			provider
				.add("ctor", new InitExecutor())
				.add(MethodName_Lock, new LockExecutor())
				.add(MethodName_Unlock, new UnlockExecutor())
				.add("wait", new WaitExecutor())
				.add("waitFor", new WaitForExecutor())
				.add("notify", new NotifyExecutor());
		}
		
	};
	
	private static class InitExecutor extends CtorNativeExecutor<ScriptLock> {

		@Override
		protected void initialize(ThreadRuntime rt, HostedValue hvalue, ScriptLock lock, Argument[] args) throws Exception {		
			lock.init();
		}
		
	}
	
	private static class LockExecutor extends InstanceNativeExecutor<ScriptLock> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, ScriptLock lock, Argument[] args) throws Exception {	
			lock.lock();
			return VoidValue.DEFAULT;
		}
		
	}
	
	private static class UnlockExecutor extends InstanceNativeExecutor<ScriptLock> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, ScriptLock lock, Argument[] args) throws Exception {	
			lock.unlock();
			return VoidValue.DEFAULT;
		}
		
	}
	
	private static class WaitExecutor extends InstanceNativeExecutor<ScriptLock> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, ScriptLock lock, Argument[] args) throws Exception {	
			boolean res = lock.waitOnCond(rt);
			return createBool(rt, res);
		}
		
	}
	
	private static class WaitForExecutor extends InstanceNativeExecutor<ScriptLock> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, ScriptLock lock, Argument[] args) throws Exception {	
			int milliSec = getInt(args, 0);
			int res = lock.waitOnCond(rt, milliSec);
			return createInt(rt, res);
		}
		
	}
	
	private static class NotifyExecutor extends InstanceNativeExecutor<ScriptLock> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, ScriptLock lock, Argument[] args) throws Exception {	
			lock.notifyOnCond(rt);
			return VoidValue.DEFAULT;
		}
		
	}
	
	//----------------- implementation at native end -----------------//
	
	private ReentrantLock lock;
	private Condition cond;
	// private String lckName;
	
	private void init(){
		lock = new ReentrantLock();
		// To debug this, pass along hvalue to this ctor
		// lckName = "System.Concurrency.Lock@" + hvalue.hashCode();
	}

	private void lock() {
		lock.lock();
	}

	private void unlock() {
		try {
			lock.unlock();
		} catch (IllegalMonitorStateException e) {
			throw new BadThreadStateException("Cannot call unlock() because the lock is not owned by the current thread.");
		}
	}
	
//	private static int SEQ = 0;
//	private int seq = SEQ++;
	private boolean waitOnCond(ThreadRuntime rt) {
		initCond();
		JThread thread = rt.getJThread();
		
		try {
			// If we are already interrupted, do not bother.
			if(thread.checkInterruption(false)){
				thread.checkInterruption(true);
				return true;
			}

//System.out.println("====> Waiting on " + lckName);
			// Otherwise, starting waiting without setting flag
			cond.await();
//System.out.println("====> Reacquired " + lckName);
		} catch (InterruptedException e) {
			rt.getJThread().checkInterruption(true);
//System.out.println("====> Interrupted " + lckName);
			return true;
		} catch (IllegalMonitorStateException e) {
			// If awaiting was illegal, throw. Note that we didn't change interruption state.
			throw new BadThreadStateException("Cannot call wait() because the lock is not owned by the current thread.");
		}
		
		return false;
	}
	
	private int waitOnCond(ThreadRuntime rt, int milliSec) {
		initCond();
		JThread thread = rt.getJThread();
		
		try {
			// If we are already interrupted, do not bother.
			if(thread.checkInterruption(false)){
				thread.checkInterruption(true);
				return -1;
			}
			
			// Otherwise, starting waiting without setting flag
			if (milliSec <= 0){
				return 0;
			} else {
				long nanos = TimeUnit.MILLISECONDS.toNanos(milliSec);
				long remaining = cond.awaitNanos(nanos);
				if (remaining <= 0){ // [Javadoc on awaitNanos] The method returns ... a value less than or equal to zero if it timed out
					return 0;
				} else if (remaining < HalfMilliSecInNano){
					// if the remaining time is less than 0.5 millisec, consider it has timed out. This is because our API only has a granularity of 1 millisec.
					return 0;
				} else {
					return Math.max(milliSec - (int)TimeUnit.NANOSECONDS.toMillis(remaining), 1);
				}
			}
		} catch (InterruptedException e) {
			rt.getJThread().checkInterruption(true);
			return -1;
		} catch (IllegalMonitorStateException e) {
			// If awaiting was illegal, throw. Note that we didn't change interruption state.
			throw new BadThreadStateException("Cannot call wait(int) because the lock is not owned by the current thread.");
		}
	}
	
	private void notifyOnCond(ThreadRuntime rt) {
		initCond();
		
		try {
			// Notify everyone
			cond.signalAll();
//System.out.println("====> Notified " + lckName);
		} catch (IllegalMonitorStateException e) {
			// If awaiting was illegal, throw. Note that we didn't change interruption state.
			throw new BadThreadStateException("Cannot call notify() because the lock is not owned by the current thread.");
		}
	}
	
	private void initCond(){
		if(cond == null){
			synchronized (this) {
				if(cond == null){
					cond = lock.newCondition();
				}
			}
		}
	}
}
