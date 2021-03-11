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

package info.julang.typesystem.jclass.builtin;

import info.julang.interpretation.RuntimeCheckException;
import info.julang.interpretation.errorhandling.KnownJSException;
import info.julang.typesystem.JType;
import info.julang.typesystem.jclass.JParameter;

/**
 * Attempt at binding a Function object with 'this' and other arguments failed due to various reasons.
 * 
 * @author Ming Zhou
 */
public class IllegalBindingException extends RuntimeCheckException {

	private static final long serialVersionUID = 7698837681817205293L;
	
	private IllegalBindingException(String msg) {
		super(msg);
	}

	static IllegalBindingException cannotBindHostedFunction() {
		return new IllegalBindingException("Binding a hosted function is disallowed.");
	}

	static IllegalBindingException cannotBindThisWithIncompatibleType(JType srcTyp, JType tgtTyp) {
		String msg = "Failed to bind a value of type '" + srcTyp.getName()
			+ "' to 'this' which is expected to receive a type of '"
			+ tgtTyp.getName() + "'.";
		return new IllegalBindingException(msg);
	}
	
	static IllegalBindingException bindIsNotAllowed(JFunctionType funcType) {
		String msg = "Binding a function of " + funcType.getFunctionKind() + " is disallowed.";
		return new IllegalBindingException(msg);
	}

	static IllegalBindingException cannotBindArgument(int index, JType argType, JParameter param) {
		String msg = "Failed to bind the parameter '" + param.getName()
			+ "' of type " + param.getType().getName() 
			+ " at index " + index 
			+ " with the given argument of type " + argType.getName() + ".";
		return new IllegalBindingException(msg);
	}
	
	@Override
	public KnownJSException getKnownJSException() {
		return KnownJSException.IllegalBindingException;
	}
}