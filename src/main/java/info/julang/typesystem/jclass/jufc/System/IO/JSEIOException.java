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

package info.julang.typesystem.jclass.jufc.System.IO;

import info.julang.JSERuntimeException;
import info.julang.interpretation.errorhandling.KnownJSException;

/**
 * A wrapper exception to carry along the underlying platform IO exception through Julian stack.
 * 
 * @author Ming Zhou
 */
public class JSEIOException extends JSERuntimeException {

	private static final long serialVersionUID = 8402656672926257854L;

	public JSEIOException(String message) {
		super("IO error: " + message);
	}
	
	public JSEIOException(Exception inner) {
		super("IO error", inner);
	}

	@Override
	public KnownJSException getKnownJSException() {
		return KnownJSException.IO;
	}

	protected String getScriptMessage(){
		Throwable t = getCause();
		if (t != null){
			return getMessage() + ": " + getCause().getMessage();
		} else {
			return getMessage();
		}
	}
}
