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

package info.julang.execution;


/**
 * The possible state of {@link IScriptEngine}. The life cycle can be illustrated as following:
 * <pre>
 *                 +-------------+
 *                 | NOT_STARTED |
 *                 +-------------+
 *                        |(call {@link run()})
 *           +----------))|((----------+
 *      (call|            V            |
 *     run())|     +-------------+     |
 *           |     |   RUNNING   |     |(call run())
 *           |     +-------------+     |                                
 *           |         /     \         |    
 *           |        /       \ (exception thrown from run())
 *           |   +---+         +---+   |
 *           |   |(run()           |   |
 *           |   V completes)      V   |
 *      +-------------+       +-------------+
 *      |   SUCCESS   |       |   FAULTED   |
 *      +-------------+       +-------------+  
 * </pre>
 * Note this state is different from, but related to, the state of 
 * {@link info.julang.execution.threading.JThread JThread}, which is represented by 
 * {@link info.julang.typesystem.jclass.jufc.System.Concurrency.ScriptThread.ScriptThreadState ScriptThreadState} and
 * exposed through Julian API. In particular, the state of main thread running in Julian engine
 * is mirrored to the engine's state throughout the execution.
 * 
 * @author Ming Zhou
 */
public enum State {
	
	/**
	 * The engine ran to successful completion.
	 */
	SUCCESS,
	
	/**
	 * The engine finished in error.
	 */
	FAULTED,
	
	/**
	 * The engine is running.
	 */
	RUNNING,
	
	/**
	 * The engine has not been started yet.
	 */
	NOT_STARTED
	
}
