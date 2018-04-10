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

import java.util.List;

/**
 * The JSE runtime exception containing multiple inner exceptions.
 *
 * @author Ming Zhou
 */
public class MultipleJSERuntimeException extends JSERuntimeException {

	private static final long serialVersionUID = -6691475404258152974L;

	private List<Exception> exes;
	
	public MultipleJSERuntimeException(List<Exception> exes) {
		super(createMsg(exes));
		this.exes = exes;
	}

	private static String createMsg(List<Exception> exes) {
		StringBuilder sb = new StringBuilder();
		for(Exception e : exes){
			sb.append(e.getMessage());
			sb.append("\n");
		}
		return sb.toString();
	}

	public List<Exception> getAllExceptions() {
		return exes;
	}

	@Override
	public JulianScriptException toJSE(ThreadRuntime rt, Context context) {
		// Special case: if there is only one exception which is JSERuntimeException, 
		// throw that out instead of an aggregated one.
		if(exes.size() == 1){
			Exception e = exes.get(0);
			if(e instanceof JSERuntimeException){
				JSERuntimeException jse = (JSERuntimeException) e;
				return jse.toJSE(rt, context);
			}
		}
		
		StringBuilder sb = new StringBuilder("Multiple exceptions:");
		int index = 1;
		for(Exception ex : exes){
			sb.append(System.lineSeparator());
			sb.append("[");
			sb.append(index);
			sb.append("]");
			if(ex instanceof JSERuntimeException){
				sb.append(System.lineSeparator());
				JulianScriptException jse = ((JSERuntimeException) ex).toJSE(rt, context);
				sb.append(jse.getStandardExceptionOutput(4, false));
			} else {
				sb.append(" (engine error)");
				sb.append(System.lineSeparator());
				sb.append(ex.getMessage());
			}
			index++;
		}
		return JSExceptionFactory.createException(
			KnownJSException.Exception, rt, context, 
			CharacterEscaping.encodeAsStringLiteral("(", sb.toString(), ");"));
	}

	// Irrelevant, as toJSE(rt, context) is overriden.
	@Override
	public KnownJSException getKnownJSException() {
		return KnownJSException.Exception;
	}

}
