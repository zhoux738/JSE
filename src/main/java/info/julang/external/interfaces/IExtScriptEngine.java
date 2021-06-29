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

package info.julang.external.interfaces;

import java.io.InputStream;
import java.io.OutputStream;

import info.julang.external.exceptions.EngineInvocationError;

public interface IExtScriptEngine {

	/**
	 * Reset the engine. Memory, types, variable table will all be wiped out.
	 * 
	 * @param pol The policy for resetting operation, defining what should be reset and what should be kept.
	 */
	void reset(ResetPolicy pol);
	
	/**
	 * Execute a script file.
	 * 
	 * @param filePathName the full path of script file.
	 * @throws EngineInvocationError if the engine encountered a fatal error.
	 */
	void runFile(String filePathName) throws EngineInvocationError;
	
	/**
	 * Execute a script snippet.
	 * 
	 * @param script the contents of script
	 * @throws EngineInvocationError if the engine encountered a fatal error.
	 */
	void runSnippet(String script) throws EngineInvocationError;
	
	/**
	 * Return the context of this engine. Can be null if 
	 * {@link info.julang.execution.State state} returns {@link 
	 * info.julang.execution.State#NOT_STARTED}.
	 * 
	 * @return The execution context.
	 */
	IExtEngineContext getContext();
	
	/**
	 * Get the last {@link info.julang.execution.Result result} of this ScriptEngine.
	 * @return null if it failed last time or not invoked once.
	 */
	IExtResult getResult();
	
	/**
	 * Abort the current running. Have no effect if the engine is not running.
	 * 
	 * @return true if a termination signal was successfully sent to the running engine.
	 * In other words if the engine is running this method will return false.
	 */
	boolean abort();
	
	/**
	 * Set a limit for the engine. In runtime if this limit is broken or to be broken 
	 * a <code style="color:green">System.UnderprivilegeException</code> will be thrown, 
	 * possibly crashing the engine.
	 * <p>
	 * By default, no limit is set.
	 * 
	 * @param name The name of this limit.
	 * @param value The value to set.
	 */
	void setLimit(String name, int value);
	
	/**
	 * Set standard IO redirection. If set, the engine will redirection input and out to the given streams.
	 * <p>
	 * All arguments are optional. If any is set to null, no redirection will happen for that stream, instead 
	 * the platform's default, namely System.in/out/err, will be used. If no redirection is intended, do not
	 * bother calling this method at all.
	 * 
	 * @param out The output stream. 
	 * @param err The error stream.
	 * @param in The input stream.
	 */
	void setRedirection(OutputStream out, OutputStream err, InputStream in);
}
