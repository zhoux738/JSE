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

package info.julang.memory.value;

import info.julang.JSERuntimeException;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.errorhandling.JSExceptionFactory;
import info.julang.interpretation.errorhandling.JulianScriptException;
import info.julang.interpretation.errorhandling.KnownJSException;

public class ArrayIndexOutOfRangeException extends JSERuntimeException {

	private static final long serialVersionUID = 7932685544299188392L;

	private int index;
	
	private int max;
	
	public ArrayIndexOutOfRangeException(int index, int max) {
		super(composeMessage(index, max));
		this.index = index;
		this.max = max;
	}
	
	private static String composeMessage(int index, int max){
		StringBuilder sb = new StringBuilder();
		sb.append("Array index out of the bound. Range: [0 - ");
		sb.append(max);
		sb.append("] Index: ");
		sb.append(index);
		
		return sb.toString();
	}

	@Override
	public KnownJSException getKnownJSException() {
		return KnownJSException.ArrayOutOfRange;
	}
	
	@Override
	public JulianScriptException toJSE(ThreadRuntime rt, Context context) {
		// The constructor argument list. 1st arg: the accessing index, 2nd arg: the max index allowed.
		// Example: (5,4);
		String source = "(" +  index + "," + max + ");";
		return JSExceptionFactory.createException(
				getKnownJSException(), 
				rt, 
				context, 
				source);
	}

}
