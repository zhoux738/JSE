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

import info.julang.external.exceptions.EngineInvocationError;
import info.julang.external.interfaces.IExtScriptEngine;
import info.julang.interpretation.errorhandling.JulianScriptException;

/**
 * The script engine interface.
 * <p/>
 * This interface defines the basic behavior that a script engine can perform, such as invoking it and querying the state.
 * 
 * @author Ming Zhou
 */
public interface IScriptEngine extends IExtScriptEngine {

	/**
	 * Run the script engine. The script is provided by {@link ScriptProvider}.
	 * <p/>
	 * The engine will consult with the engine context while executing. 
	 * Arguments must be set into the context before this method is called. To get the context, call {@link #getContext()}.
	 * <p/> 
	 * Based on implementation, this method can be either blocking or non-blocking. To query its state, call {@link #getState()}.
	 * @param provider
	 * @return
	 * @throws EngineInvocationError 
	 */
	void run(ScriptProvider provider) throws EngineInvocationError;
	
	/**
	 * Run the script engine with specified arguments. The script is provided by {@link ScriptProvider}.
	 * <p/>
	 * Arguments passed in here will overwrite existing ones found in the engine's context, 
	 * which can be retrieved by calling {@link #getContext()}.
	 * <p/>
	 * Based on implementation, this method can be either blocking or non-blocking. To query its state, call {@link #getState()}.
	 * @param provider
	 * @param arguments
	 * @throws EngineInvocationError
	 */
	void run(ScriptProvider provider, String[] arguments) throws EngineInvocationError;
	
	/**
	 * Return the context of this engine, which is expected, but not guaranteed, to refer to the same context passed into {@link #run}. 
	 * Can be null if {@link State state} returns {@link State#NOT_STARTED}.
	 * @return
	 */
	EngineContext getContext();
	
	/**
	 * Get the current {@link State state} of this ScriptEngine.
	 * @return 
	 */
	State getState();
	
	/**
	 * Set up exception handler, which gets called back when a {@link JulianScriptException} is thrown and not handled by user.
	 * @param hanlder
	 */
	void setExceptionHandler(ScriptExceptionHandler hanlder);
	
	/**
	 * Get the last {@link Result result} of this ScriptEngine.
	 * @return null if it failed last time or not invoked once.
	 */
	Result getResult();
	
}
