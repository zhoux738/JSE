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

package info.julang.hosting.mapped.exec;

import info.julang.JSERuntimeException;
import info.julang.execution.symboltable.ITypeTable;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.errorhandling.JSExceptionFactory;
import info.julang.interpretation.errorhandling.JulianScriptException;
import info.julang.interpretation.errorhandling.KnownJSException;
import info.julang.langspec.CharacterEscaping;

/**
 * An exception that represents the original platform exception (Java exception). 
 * <p>
 * When a mapped Java class throws, the original Java exception will be converted to this script engine
 * exception, with its class name, message and part of stack trace copied over. JSE error handling is
 * able to process this exception like other ordinary exceptions, only with slight difference in 
 * formatting its stack during error report.
 * 
 * @author Ming Zhou
 */
public class PlatformOriginalException extends JSERuntimeException {

	private static final long serialVersionUID = 1467240442227628137L;

	private String declClass;
	private int deductibleStackDepth;
	
	public PlatformOriginalException(Throwable ex, String declClass, int deductibleStackDepth) {
		super(makeMsg(ex), ex);
		this.declClass = declClass;
		this.deductibleStackDepth = deductibleStackDepth;
	}

	private static String makeMsg(Throwable inner) {
		return "An exception is thrown from the hosting platfrom. Exception: " + inner.getMessage();
	}

	@Override
	public KnownJSException getKnownJSException() {
		return KnownJSException.PlatformOriginal;
	}
	
	@Override
	public JulianScriptException toJSE(ThreadRuntime rt, Context context) {
		Throwable t = this.getCause();
		JulianScriptException jse = toJSE0(rt, context, t, true);
		return jse;
	}
	
	private JulianScriptException toJSE0(ThreadRuntime rt, Context context, Throwable t, boolean firstOne) {
		String cname = t.getClass().getName();
		String msg = t.getMessage();
		if (msg == null) {
			msg = "";
		}
		KnownJSException kjse = getKnownJSException();
		JulianScriptException jse = JSExceptionFactory.createException(
			kjse, 
			rt, 
			context, 
			// PlatformOriginalException(string className, string msg) : super("(" + className + ") " + msg) {} 
			"(" + CharacterEscaping.encodeAsStringLiteral(null, cname, null) + 
			"," + CharacterEscaping.encodeAsStringLiteral(null, msg, null) + ");"
			);
		
		jse.setRawFormat(true);
		
		ITypeTable tt = rt.getTypeTable();
		StackTraceElement[] stes = t.getStackTrace();
		int i = 0;
		
		if (firstOne) {
			// Filter the trace so that we only show the parts relevant to user's code. The 
			// reflection invocation and script engine internals must be not exposed.
			String dcname = declClass;
			int max = stes.length - deductibleStackDepth;
			for(; i < 10 && i < max; i++){
				String sm = stes[i].toString();
				jse.addRawStackTrace(tt, sm);
			}
			
			int deduction = 0;
			for(int j = i - 1; j >= 0; j--){
				String sm = stes[j].toString();
				if (!sm.startsWith(dcname)) {
					deduction++;
				} else {
					break;
				}
			}
			
			int newDepth = Math.max(i - deduction, 0);
			jse.setStackTraceDepth(newDepth);
			
			int remains = stes.length - deduction - 10;
			if (i == 10 && remains > 0) {
				// Since we filter by class name, it's possible we end up with newDepth == 0 (especially 
				// during class loading), effectively cutting the entire stack and leaving a single entry 
				// here which reads like "at ... (N more)". This is by design as of 0.2.0.
				jse.addRawStackTrace(tt, "... (" + remains + " more)");
			}
		} else {
			// From the second cause, only show the 1st stack element, and do not show the exact number of trace count.
			// This is more due to technology restriction than a design decision. To present a clean stack to users, we
			// must know where to cut the original stack, but that can be hard depending on what accuracy we aim at.
			// For now, simplify this by just showing the 1st entry, which is guaranteed to be user-code.
			if (stes.length > 0) {
				String sm = stes[0].toString();
				jse.addRawStackTrace(tt, sm);
			}
			
			if (stes.length > 1) {
				jse.addRawStackTrace(tt, "... (more)");
			}
		}
		
		// Add cause
		Throwable t1 = t.getCause();
		if (t1 != null){
			JulianScriptException jse1 = toJSE0(rt, context, t1, false);
			jse.setJSECause(jse1);
		}
		
		return jse;
	}
	
}
