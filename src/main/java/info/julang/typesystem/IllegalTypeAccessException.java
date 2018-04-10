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

package info.julang.typesystem;

import info.julang.interpretation.RuntimeCheckException;
import info.julang.interpretation.errorhandling.KnownJSException;

/**
 * The exception thrown when trying to reference a type that is not accessible from the current context.
 * 
 * @author Ming Zhou
 */
public class IllegalTypeAccessException extends RuntimeCheckException {

	private static final long serialVersionUID = 3893744690552205997L;

	/**
	 * Create a new IllegalTypeAccessException with type name.
	 * 
	 * @param tName
	 */
	public IllegalTypeAccessException(String tName) {
		super(createMsg(tName));
	}

	private static String createMsg(String tName) {
		return "Cannot refer to an internal type: " + tName + ".";
	}

	@Override
	public KnownJSException getKnownJSException() {
		return KnownJSException.IllegalTypeAccess;
	}

}
