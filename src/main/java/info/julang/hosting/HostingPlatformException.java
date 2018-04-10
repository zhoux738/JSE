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

package info.julang.hosting;

import info.julang.JSERuntimeException;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.hosting.mapped.exec.PlatformOriginalException;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.errorhandling.JulianScriptException;
import info.julang.interpretation.errorhandling.KnownJSException;
import info.julang.modulesystem.naming.FQName;

public class HostingPlatformException extends JSERuntimeException {

	private static final long serialVersionUID = 4584474815379232672L;

	private PlatformExceptionInfo exInfo;
	
	/**
	 * Create a new HostingPlatformException with full details about the root cause. Used by mapped API.
	 * 
	 * @param inner
	 */
	public HostingPlatformException(PlatformExceptionInfo exInfo) {
		super(makeMsg(exInfo.getCause()));
		this.exInfo = exInfo;
	}
	
	/**
	 * Create a new HostingPlatformException with both a brief root cause. Used by bridged API.
	 * 
	 * @param inner
	 * @param className
	 * @param methodName
	 */
	public HostingPlatformException(Exception inner, FQName className, String methodName) {
		super(makeMsg(inner, className, methodName));
	}
	
	/**
	 * Create a new HostingPlatformException with customized message and JSE stack info.
	 * 
	 * @param msg
	 * @param className
	 * @param methodName
	 */
	public HostingPlatformException(String msg, FQName className, String methodName) {
		super(makeCustomizedMsg(msg, className, methodName));
	}

	private static String makeMsg(Throwable inner) {
		return "An exception is thrown from the hosting platfrom. Exception: " + inner != null ? inner.getMessage() : "";
	}

	private static String makeMsg(Exception inner, FQName className, String methodName) {
		return "An exception is thrown from a method implemented by hosting language. Method: " + 
				className.toString() + "." + methodName + ", Exception: " + inner.getMessage();
	}
	
	private static String makeCustomizedMsg(String msg, FQName className, String methodName) {
		return "A problem is encountered when calling a method implemented by hosting language. Method: " + 
				className.toString() + "." + methodName + ", Exception: " + msg;
	}

	@Override
	public KnownJSException getKnownJSException() {
		return KnownJSException.HostingPlatform;
	}
	
	@Override
	public JulianScriptException toJSE(ThreadRuntime rt, Context context) {
		JulianScriptException jse = super.toJSE(rt, context);
		
		// If we have an inner exception from the platform, set it as the cause
		if (exInfo != null) {
			PlatformOriginalException poe = new PlatformOriginalException(exInfo.getCause(), exInfo.getContainingClass(), exInfo.getDeductibleStackDepth());
			JulianScriptException inner = poe.toJSE(rt, context);
			
			jse.setJSECause(inner);
		}
		
		return jse;
	}
}
