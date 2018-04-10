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

package info.julang.execution.namespace;

import info.julang.JSERuntimeException;
import info.julang.interpretation.errorhandling.KnownJSException;

/**
 * The exception thrown when two or more types can be resolved from the current context 
 * given the imported modules.
 * <p/>
 * For example, in a Julian file, two modules, A and B, are imported. And both A and B contain
 * a class called MyClass. If only the simple name of MyClass is used in this file, it will
 * cause a namespace conflict because both A.MyClass and B.MyClass are resolvable. 
 *
 * @author Ming Zhou
 */
public class NamespaceConflictException extends JSERuntimeException {

	private static final long serialVersionUID = -6046472974157357364L;

	public NamespaceConflictException(String typName1, String typName2) {
		super(createMsg(typName1, typName2));
	}

	private static String createMsg(String typName1, String typName2) {
		return "Namespace conflict: " + typName1 + " and " + typName2 + ".";
	}

	@Override
	public KnownJSException getKnownJSException() {
		return KnownJSException.NamespaceConflict;
	}
}