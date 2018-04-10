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

import info.julang.JSERuntimeException;
import info.julang.interpretation.errorhandling.IHasLocationInfo;
import info.julang.interpretation.errorhandling.ILocationInfoAware;
import info.julang.interpretation.errorhandling.KnownJSException;

/**
 * A runtime check failed.
 * <p>
 * This exception is for interpretation-based language only. If Julian were to be compiled before execution we 
 * would not use this exception since all the invalid cases would have been covered by compile-time check.
 * <p>
 * This exception is location-aware.
 * 
 * @author Ming Zhou
 */
public class RuntimeCheckException extends JSERuntimeException implements IHasLocationInfo, ILocationInfoAware {

	private static final long serialVersionUID = 5810636477388171467L;

	private String fileName = "";
	private int lineNo = -1;	
	
	public RuntimeCheckException(String msg) {
		super(msg);
	}
	
	public RuntimeCheckException(String msg, IHasLocationInfo ainfo) {
		super(msg);
		setLocationInfo(ainfo);
	}
	
	@Override
	public KnownJSException getKnownJSException() {
		return KnownJSException.RuntimeCheck;
	}

	@Override
	public String getFileName() {
		return fileName;
	}

	@Override
	public int getLineNumber() {
		return lineNo;
	}

	@Override
	public void setLocationInfo(IHasLocationInfo ainfo) {
		// Set location info only if we don't have one yet. This is because it's more likely 
		// the calls that happen earlier contains the more accurate location info.
		if (fileName == ""){
			fileName = ainfo.getFileName();
		}
		if (lineNo == -1){
			lineNo = ainfo.getLineNumber();
		}
	}
}
