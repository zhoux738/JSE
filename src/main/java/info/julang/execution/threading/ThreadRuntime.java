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
import info.julang.memory.StackArea;

/**
 * ThreadRuntime represents the local runtime context of a thread. The context provides 
 * interface, among others, for getting access to thread's local memory areas. From 
 * thread runtime one can also query the engine's global state.
 * <p>
 * <b>!!! CAUTION !!! CAUTION !!! CAUTION !!!</b>
 * <p>
 * This interface is <b>NOT</b> designed to be thread-safe. Always access to it from the 
 * original managed thread with which the instance was associated from the very beginning.
 * If creating a new thread outside the manager, replicate the instance with a {@link 
 * SystemInitiatedThreadRuntime}.
 * 
 * @author Ming Zhou
 */
public interface ThreadRuntime extends EngineRuntime, IThreadLocalStorage {

	/**
	 * Get stack memory area for this thread.
	 */
	StackArea getStackMemory();
	
	/**
	 * Get thread stack (which contains both stack memory area and variable table) for this thread.
	 */
	ThreadStack getThreadStack();
	
	/**
	 * Get the {@link JThread thread object}.
	 */
	JThread getJThread();
	
}
