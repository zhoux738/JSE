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

package info.julang.interpretation;

import info.julang.JSERuntimeException;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.errorhandling.JulianScriptException;
import info.julang.interpretation.errorhandling.KnownJSException;

public class ReflectedInvocationException extends JSERuntimeException {

	private static final long serialVersionUID = -3720735253547093519L;
	
	private JulianScriptException cause;
	
	/**
	 * Create a new ReflectedInvocationException.
	 * 
	 * @param msg
	 */
	public ReflectedInvocationException(String msg) {
		this(msg, null);
	}
	
	/**
	 * Create a new ReflectedInvocationException with an inner cause.
	 * 
	 * @param msg
	 * @param inner
	 */
	public ReflectedInvocationException(String msg, JulianScriptException inner) {
		super(msg);
		this.cause = inner;
	}

	@Override
	public KnownJSException getKnownJSException() {
		return KnownJSException.ReflectedInvocation;
	}
	
	@Override
	public JulianScriptException toJSE(ThreadRuntime rt, Context context) {
		JulianScriptException jse = super.toJSE(rt, context);
		
		// If we have an inner exception, set it as the cause
		if (cause != null) {
			jse.setJSECause(cause);
			jse.setInvokedByPlatform();
		}
		
		return jse;
	}
}
