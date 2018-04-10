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

package info.julang.interpretation;

import org.antlr.v4.runtime.Token;

import info.julang.JSERuntimeException;
import info.julang.interpretation.errorhandling.IHasLocationInfo;
import info.julang.interpretation.errorhandling.KnownJSException;

/**
 * Exception indicating that the syntax in script is incorrect.
 * <p>
 * This exception is location-aware.
 * 
 * @author Ming Zhou
 */
public class BadSyntaxException extends JSERuntimeException implements IHasLocationInfo {
	
	private static final long serialVersionUID = 3053670970808771430L;
	
	private String fileName = "";
	private int lineNo = -1;
	
	public BadSyntaxException(String msg) {
		super(msg);
	}
	
	public BadSyntaxException(String msg, String fileName, Token atok) {
		super(msg);
		this.lineNo = atok.getLine();
		this.fileName = fileName;
	}
	
	public BadSyntaxException(String msg, IHasLocationInfo linfo) {
		super(msg);
		this.fileName = linfo.getFileName();
		this.lineNo = linfo.getLineNumber();
	}
	
	@Override
	public KnownJSException getKnownJSException() {
		return KnownJSException.BadSyntax;
	}
	
	//--------------- IHasLocationInfo ---------------//

	@Override
	public String getFileName() {
		return fileName;
	}

	@Override
	public int getLineNumber() {
		return lineNo;
	}
}
