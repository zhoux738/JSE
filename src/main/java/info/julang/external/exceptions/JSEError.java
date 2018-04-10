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

package info.julang.external.exceptions;

/**
 * A JSE Error is an unexpected exception which indicates a bug in script engine.
 *
 * @author Ming Zhou
 */
public class JSEError extends RuntimeException {

	private static final long serialVersionUID = -4196498452227505699L;
	
	public JSEError(Exception internal){
		super(JSEErrorString, internal);
	}

	public JSEError(String msg) {
		super(buildErrorString(msg, null));
	}
	
	public JSEError(String msg, Exception internal) {
		super(buildErrorString(msg, null), internal);
	}
	
	public JSEError(String msg, Class<?> sourceClass) {
		super(buildErrorString(msg, sourceClass));
	}

	private static final String ErrorString = "A Julian Scripting Engine error occurs. Report bug to ";
	
	private static final String Mailbox = "zhoux738@umn.edu";
	
	private static final String JSEErrorString = ErrorString + Mailbox;
	
	private static String buildErrorString(String msg, Class<? extends Object> sourceClass){
		StringBuilder sb = new StringBuilder();
		boolean hasMsg = msg != null && !"".equals(msg);
		sb.append(JSEErrorString);
		sb.append(".");
		sb.append(System.lineSeparator());
		sb.append("(");
		if(sourceClass != null){
			sb.append(sourceClass.getPackage().getName());
			sb.append(".");
			String className = sourceClass.getSimpleName();
			if(!"".equals(className)){
				sb.append(sourceClass.getSimpleName());
			} else {
				sb.append("anonymous class");
			}
			if(hasMsg){
				sb.append(": ");
			}
		}
		if(hasMsg){
			sb.append(msg);
		}
		sb.append(")");
		
		return sb.toString();
	}

}
