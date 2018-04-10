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
import info.julang.interpretation.errorhandling.KnownJSException;
import info.julang.typesystem.JType;

public class IllegalAssignmentException extends JSERuntimeException {

	private static final long serialVersionUID = 7855988062306321545L;

	public IllegalAssignmentException(JType typeOfAssigner, JType typeOfAssignee) {
		super(composeMessage(
				typeOfAssigner.getName(), 
				typeOfAssignee.getName()));
	}
	
	public IllegalAssignmentException(String message) {
		super(message);
	}
	
	private static String composeMessage(String s1, String s2){
		StringBuilder sb = new StringBuilder();
		sb.append("Cannot assign a value of type ");
		sb.append(s1);
		sb.append(" to a value of type ");
		sb.append(s2);
		
		return sb.toString();
	}
	
	@Override
	public KnownJSException getKnownJSException() {
		return KnownJSException.IllegalAssignment;
	}

}
