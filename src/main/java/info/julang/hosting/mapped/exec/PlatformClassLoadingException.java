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

import info.julang.execution.threading.ThreadRuntime;
import info.julang.hosting.PlatformExceptionInfo;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.errorhandling.JSExceptionFactory;
import info.julang.interpretation.errorhandling.JulianScriptException;
import info.julang.interpretation.errorhandling.KnownJSException;
import info.julang.langspec.CharacterEscaping;
import info.julang.typesystem.loading.ClassLoadingException;

/**
 * A specialized class loading exception that is caused by a platform error.
 * <p>
 * When loading a mapped Java class, the chance is that the loading failed for various errors: class 
 * may not pass bytecode validation, couldn't link, failed at class initialization, etc. Like semantic
 * errors encountered in the class definition, these errors would fatally fault the entire loading 
 * efforts, but they are distinct from usual errors in that the contributing factors are not coming
 * from Julian script but binaries from a dependent platform component. Therefore we use this exception
 * to group such errors together to help script writers to better identify the root cause.
 * 
 * @author Ming Zhou
 */
public class PlatformClassLoadingException extends ClassLoadingException {

	private static final long serialVersionUID = -7360620010305431117L;
	
	private PlatformExceptionInfo exInfo;
	
	public PlatformClassLoadingException(PlatformExceptionInfo exInfo) {
		super("Encountered an error when loading platform types.");
		this.exInfo = exInfo;
	}

	@Override
	public KnownJSException getKnownJSException() {
		return KnownJSException.PlatformClassLoading;
	}
	
	@Override
	public JulianScriptException toJSE(ThreadRuntime rt, Context context) {
	    // Completely bypass the parent method
		KnownJSException kjse = getKnownJSException();
		JulianScriptException jse = JSExceptionFactory.createException(
			kjse, 
			rt, 
			context, 
			CharacterEscaping.encodeAsStringLiteral("(", exInfo.getContainingClass(), ");")
			);

		PlatformOriginalException poe = new PlatformOriginalException(
			exInfo.getCause(), exInfo.getContainingClass(), exInfo.getDeductibleStackDepth());
		JulianScriptException inner = poe.toJSE(rt, context);
		
		jse.setJSECause(inner);
		
		return jse;
	}
}
