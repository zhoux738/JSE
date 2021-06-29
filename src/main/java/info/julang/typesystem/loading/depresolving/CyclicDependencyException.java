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

package info.julang.typesystem.loading.depresolving;

import info.julang.JSERuntimeException;
import info.julang.interpretation.errorhandling.KnownJSException;

public class CyclicDependencyException extends JSERuntimeException {

    private static final long serialVersionUID = -2717726182728855064L;

	public CyclicDependencyException(String[] names, boolean isTypeOrScript) {
		super(createMsg(names, isTypeOrScript));
	}

	private static String createMsg(String[] names, boolean isTypeOrScript) {
		StringBuilder sb = new StringBuilder("The following " );
		sb.append(isTypeOrScript ? "types" : "scripts");
		sb.append(" form a cyclic dependency with each other");
		if (!isTypeOrScript) {
			sb.append(", in the order of inclusion");
		}
		sb.append(": ");
		if (!isTypeOrScript) {
			sb.append(System.lineSeparator());
		}
		int total = names.length - 1;
		for(int i = 0; i < total; i++){
			sb.append(names[i]);
			sb.append(',');
			if (!isTypeOrScript) {
				sb.append(System.lineSeparator());
			}
		}
		
		sb.append(names[total]);
		
	    return sb.toString();
    }

	@Override
	public KnownJSException getKnownJSException() {
		return KnownJSException.CyclicDependency;
	}

}
