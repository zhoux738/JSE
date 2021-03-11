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

import info.julang.execution.threading.ThreadRuntime;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.memory.value.IFuncValue;

/**
 * An executable represents a script function that can be invoked by engine.
 *
 * @author Ming Zhou
 */
public interface Executable {

	/**
	 * Execute in the context of given thread runtime. 
	 * 
	 * @param runtime thread runtime.
	 * @param func the function value to invoke against.
	 * @param args the provided arguments. It is intentionally undefined whether the values in these arguments 
	 * are stored or not. The implementation of this method thus must assume that they are not stored.
	 * @return the result of execution.
	 */
	Result execute(ThreadRuntime runtime, IFuncValue func, Argument[] args) throws EngineInvocationError;
	
}
