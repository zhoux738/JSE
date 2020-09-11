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

import java.lang.Thread.State;

import info.julang.execution.Argument;
import info.julang.execution.Result;
import info.julang.execution.ScriptExceptionHandler;
import info.julang.execution.simple.DefaultExceptionHandler;
import info.julang.execution.threading.JThread.MonitorInterruptCondition;
import info.julang.external.exceptions.JSEError;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.errorhandling.JulianScriptException;
import info.julang.memory.JSEStackOverflowException;
import info.julang.typesystem.jclass.jufc.System.Concurrency.ScriptThread.ScriptThreadState;

/**
 * The standard {@link Runnable Java Runnable} implementation that's associated with a {@link JThread} object.
 * <p/>
 * The implementation of {@link Runnable#run()} calls {@ JThread#run(Argument[])}. At the end it captures 
 * the unhandled exception, if any.
 * 
 * @author Ming Zhou
 */
public class JThreadRunnable implements Runnable {

	private boolean finished;
	
	private JThreadManager manager;
	
	private JThread thread;
	
	private Result result;
	
	private Exception error;
	
	private Argument[] args;
	
	private Thread platformThread;
	
	private ScriptExceptionHandler handler;
	
	JThreadRunnable(JThreadManager manager, JThread thread, Argument[] args){
		this.manager = manager;
		this.thread = thread;
		this.args = args;
		
		manager.addThread(thread, this);
	}
	
	@Override
	public void run() {
		try {
			platformThread = Thread.currentThread();
			
			// Check interruption state. It's possible we have received an interrupt signal at this point.
			if(thread.checkInterruption(false)){
				// If so, re-assert the interruption on the underlying thread. This is necessary because 
				// we must ensure that once thread.start() is called, all the signals will be correctly
				// received and handled.
				platformThread.interrupt();
			}
			
			result = thread.run(args);
		} catch (JulianScriptException jse) { 
			error = jse;
		} catch (JThreadAbortedException jtae) {
			// If thread is aborted by engine termination, ignore it.
			error = null;
		} catch (Exception e) {
			error = e;
			
			// Uncomment these when debugging multi-threading issue.
			/* *
			info.julang.execution.StandardIO sio = thread.getThreadRuntime().getStandardIO();
			java.io.PrintStream stdout = sio.getOut();
			stdout.println("Thread faulted. Remove this after fixing.");
			error.printStackTrace(sio.getError());
			if(e instanceof JulianScriptException){
				JulianScriptException jse = (JulianScriptException) e;
				stdout.println("JSE message: " + jse.getExceptionMessage());
			}
			//*/
		} catch (Error er) {
			// At this point we have unwound the whole stack, so as the last resort 
			// synthesize an Exception without location info.
			if (er instanceof StackOverflowError){
				JSEStackOverflowException jsoe = new JSEStackOverflowException();
				ThreadRuntime rt = thread.getThreadRuntime();
				error = jsoe.toJSE(rt, Context.createSystemLoadingContext(rt));
			} else {
				throw er;
			}
		} finally {
			finished = true;
			boolean handled = error == null;
			
			if(!handled && !thread.isMain() && error instanceof JulianScriptException){
				try {
					if(handler == null){
						handler = new DefaultExceptionHandler(thread.getThreadRuntime().getStandardIO(), true);
					}
					
					handler.onException((JulianScriptException)error);
					manager.registerError(thread.getId(), false, error);
					
					handled = true;
				} catch (Exception ex){
					error = ex;
				}
			}
			
			synchronized(this){
				this.notifyAll();
			}
			
			if(!handled){
				// We should have caught everything. Running to here indicates of an engine bug.
				manager.registerError(thread.getId(), true, error);
			}
			
			manager.removeThread(thread.getId());
		}
	}
	
	public boolean isSuccess(){
		return error == null;
	}
	
	public Result getResult(){
		return result;
	}
	
	public Exception getException(){
		return error;
	}
	
	public JThread getJThread(){
		return thread;
	}
	
	public void waitForCompletion(){
		while(!finished) {
			synchronized(this){
				if (!finished){
//					try {
//						this.wait();
//					} catch (InterruptedException e) {
//						// Ignore
//					}
					thread.safeWait(this, condition);
				}
			}
		}
	}
	
	private MonitorInterruptCondition condition = new MonitorInterruptCondition(){

		@Override
		public boolean shouldInterrupt() {
			return finished;
		}
		
	};
	
	Thread getPlatformThread(){
		return platformThread;
	}
	
	public ScriptThreadState getState(){
		if(platformThread == null){
			return ScriptThreadState.READY;
		}
		
		if(finished){
			return ScriptThreadState.DONE;
		}
		
		// Get underlying thread's state and translate to Julian's state:		
		State state = platformThread.getState();
		switch(state){
		case BLOCKED:
		case TIMED_WAITING:
		case WAITING:
			return ScriptThreadState.PENDING;
		case NEW:
			return ScriptThreadState.READY;
		case RUNNABLE:
			return ScriptThreadState.RUNNING;
		case TERMINATED:
			return ScriptThreadState.DONE;
		}
		
		throw new JSEError("The current thread doesn't have available state information.");
	}
	
	@Override
	public String toString(){
		return thread.getName() + " [" + getState().name() + "]"; 
	}
}
