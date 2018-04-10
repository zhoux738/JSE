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

package info.julang.typesystem.conversion;

import info.julang.JSERuntimeException;
import info.julang.interpretation.errorhandling.KnownJSException;
import info.julang.typesystem.JType;

public class TypeIncompatibleException extends JSERuntimeException {

	private static final long serialVersionUID = 1904000385641918660L;

	public TypeIncompatibleException(JType actualType, JType declType) {
		this(actualType, declType, false);
	}
	
	public TypeIncompatibleException(JType actualType, JType declType, boolean isCastable) {
		super(makeMessage(actualType, declType, isCastable));
	}

	private static String makeMessage(JType actualType, JType declType, boolean isCastable) {
		return "A value has a type not compitable with the declared type. " + 
				(isCastable ? "An explicit casting operation is required." : "") + 
				"Runtime type: " + actualType.getName() + 
				"; Declared type: " + declType.getName() + ".";
	}
	
	@Override
	public KnownJSException getKnownJSException() {
		return KnownJSException.TypeIncompatible;
	}

}
