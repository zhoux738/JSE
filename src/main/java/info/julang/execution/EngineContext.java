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

import info.julang.external.interfaces.IExtEngineContext;
import info.julang.interpretation.errorhandling.JulianScriptException;

/**
 * Engine context is the main channel connecting engine runtime and the outside world.
 * <p>
 * The hosting environment is able to create an engine context and passes it to script 
 * engine, which runs some executable (provided by a separate class {@link ScriptProvider} 
 * based on the given context.
 * <p>
 * The hosting environment can query bindings from the context. It is, however, up to the 
 * implementation of EngineContext to decide which and how a state (variable) is translated 
 * to a binding to be exposed to the external world.
 * <p>
 * One should note the differences between this interface and {@link EngineRuntime}. The 
 * latter concerns only engine internals, providing all the runtime information as strictly 
 * defined by Julian Script Engine specification. This interface, on the other hand, should 
 * be considered as a media through which the hosting environment and the script engine 
 * exchange information.
 * <p>
 * To enforce in-JVM isolation, most methods are defined in {@link IExtEngineContext} that 
 * can be safely called from outside script engine.
 * 
 * @author Ming Zhou
 */
public interface EngineContext extends IExtEngineContext {
	
	/**
	 * Get the exception unhandled by script.
	 * 
	 * @return null if no exception was thrown
	 */
	JulianScriptException getException();
}
