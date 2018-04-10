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

package info.julang.hosting.execution;

import info.julang.execution.Argument;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.memory.value.HostedValue;
import info.julang.memory.value.JValue;

/**
 * A native executor for instance method.
 * 
 * @author Ming Zhou
 */
public abstract class InstanceNativeExecutor<T> extends NativeExecutorBase {
	
	@Override
	public JValue execute(ThreadRuntime rt, HostedValue hvalue, Argument[] args) throws Exception {
		@SuppressWarnings("unchecked")
		T thisVal = (T) hvalue.getHostedObject();
		
		try {
			JValue val = apply(rt, thisVal, args);
			return val;	
		} catch (Exception e){
			Exception e2 = handleException(e);
			throw e2;
		}
	}

	protected abstract JValue apply(ThreadRuntime rt, T thisVal, Argument[] args) throws Exception;
	
	/**
	 * The subclass may inspect and transform this exception
	 * @param e
	 */
	protected Exception handleException(Exception e){
		// Do nothing by default
		return e;
	}

}
