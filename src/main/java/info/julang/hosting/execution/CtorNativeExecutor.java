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

import java.lang.reflect.ParameterizedType;

/**
 * A native executor for instance constructor.
 * 
 * @param <T> Must contain a parameter-less constructor. 
 * @author Ming Zhou
 */
public abstract class CtorNativeExecutor<T> extends NativeExecutorBase {
	
	private Class<T> clazz;
	
	@SuppressWarnings("unchecked")
	public CtorNativeExecutor(){
		Class<?> c1 = this.getClass();
		ParameterizedType pt = (ParameterizedType) c1.getGenericSuperclass();
		clazz = (Class<T>) pt.getActualTypeArguments()[0];
	}
	
	@Override
	public JValue execute(ThreadRuntime rt, HostedValue hvalue, Argument[] args) throws Exception {
		T platformObj = (T) clazz.newInstance();
		
		initialize(rt, hvalue, platformObj, args);
		
		hvalue.setHostedObject(platformObj);
		
		return ret != null ? ret : hvalue;	
	}

	/**
	 * Initialize the platform object backing the hosted value.
	 * 
	 * @param rt thread runtime in which the constructor is called.
	 * @param hvalue the Julian value object which contains <code>platformObject</code>. 
	 * Essentially the '<code>this</code>' object in Julian constructor.
	 * @param platformObject the platform (native) object which actually implemented <code>hvalue</code>'s type.
	 * @param args arguments to constructor.
	 * @throws Exception
	 */
	protected abstract void initialize(ThreadRuntime rt, HostedValue hvalue, T platformObject, Argument[] args) throws Exception;

	private JValue ret;
	
	protected void setOverwrittenReturnValue(JValue value){
		ret = value;
	}
}
