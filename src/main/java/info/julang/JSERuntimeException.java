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

package info.julang;

import info.julang.execution.threading.ThreadRuntime;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.errorhandling.JSExceptionFactory;
import info.julang.interpretation.errorhandling.JulianScriptException;
import info.julang.interpretation.errorhandling.KnownJSException;
import info.julang.langspec.CharacterEscaping;

/**
 * A JSE Runtime Exception is the kind of exception caused by incorrect script or bad engine state. 
 * It indicates of a user/environment error.
 * 
 * @author Ming Zhou
 */
public abstract class JSERuntimeException extends RuntimeException {
	
	private static final long serialVersionUID = 8276279283469667107L;

	public JSERuntimeException(String msg, Throwable inner){
		super(msg, inner);
	}
	
	public JSERuntimeException(String msg){
		super(msg);
	}
	
	/**
	 * Convert this exception to a {@link JulianScriptException JSE} instance. Most of time we
	 * should pop up a JSE instead of the original JSERuntimeException so that the stack trace
	 * of script engine can be preserved and presented before users.
	 * <p>
	 * If not overridden, will create an instance of JulianScriptException of type returned 
	 * by {@link #getKnownJSException()}, using a default constructor which takes {@link #getMessage()}
	 * as the sole argument. Note the message from Java object will be used without escaping, so the
	 * subclass must create the message carefully such it any Julian meta-chars must be properly escaped.
	 * 
	 * @param rt The thread runtime
	 * @param context The execution context
	 * @return An instance of JulianScriptException that wraps Julian's <code style="color:green">System.Exception</code>.
	 */
	public JulianScriptException toJSE(ThreadRuntime rt, Context context) {
		KnownJSException kjse = getKnownJSException();
		return kjse != null ? toJSE0(rt, context, kjse) : null;
	}
	
	/**
	 * Get the type of corresponding exception in Julian script.
	 * 
	 * @return null if this exception is not convertible to script exception.
	 */
	public abstract KnownJSException getKnownJSException();
	
	private JulianScriptException toJSE0(ThreadRuntime rt, Context context, KnownJSException kjs){
		return JSExceptionFactory.createException(
				kjs, 
				rt, 
				context, 
				CharacterEscaping.encodeAsStringLiteral("(", getScriptMessage(), ");")
				);
	}
	
	/**
	 * Get a message used to pass to script exception's constructor.
	 * <p>
	 * If the constructor requires more than a single string or different param types, 
	 * override {@link #toJSE} instead.
	 * 
	 * @return null if the constructor is nullary (has 0 parameters).
	 */
	protected String getScriptMessage(){
		return getMessage();
	}
}
