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

package info.julang.clapp;

import info.julang.external.exceptions.JSEException;

/**
 * An interface abstracting the way to create and execute a script engine.
 * <p>
 * The type of engine instance used by this interface is rather raw, since we need to
 * deal with both JSR-223 engine type and our own type with more friendly API. The
 * implementation, however, can assume that the instance created by <code>getEngineInstance()</code>
 * will be used in other callbacks exposed by the interface, therefore a downcasting is
 * always safe.
 * 
 * @author Ming Zhou
 */
public interface IExecutionHelper {

	/**
	 * Create a new engine instance.
	 * 
	 * @param env The cmdline environment.
	 * @return The created engine instance.
	 */
	Object getEngineInstance(CLEnvironment env);

	/**
	 * Add module paths to runtime context.
	 * 
	 * @param engine The script engine.
	 * @param args The arguments used when invoking the engine.
	 * @param paths The module paths.
	 */
	void addModulePaths(Object engine, ExecutionArguments args, String[] paths);
	
	/**
	 * Invoke the engine against the given context.
	 * 
	 * @param engine The script engine.
	 * @param args The arguments used when invoking the engine.
	 * @return result from execution
	 * @throws JSEException a wrapper exception with its inner exception being the root cause.
	 */
	Object invokeEngine(Object engine, ExecutionArguments args) throws JSEException;

	/**
	 * Add variable bindings to the engine.
	 * 
	 * @param engine The script engine.
	 * @param namedBindings an array of name~binding pairs
	 */
	void addBindings(Object engine, NamedBinding[] namedBindings);

}
