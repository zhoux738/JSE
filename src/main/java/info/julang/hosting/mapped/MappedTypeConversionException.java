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

package info.julang.hosting.mapped;

import info.julang.external.exceptions.JSEException;
import info.julang.interpretation.errorhandling.DefaultLocationInfo;
import info.julang.interpretation.errorhandling.IHasLocationInfo;

public class MappedTypeConversionException extends JSEException implements IHasLocationInfo {

	private static final long serialVersionUID = 6028402455327274412L;

	private IHasLocationInfo loc;
	
	public MappedTypeConversionException(String msg){
		this(msg, DefaultLocationInfo.INSTANCE);
	}
	
	public MappedTypeConversionException(String msg, IHasLocationInfo loc){
		super(msg);
		this.loc = loc;
	}
	
	public IHasLocationInfo getLocationInfo(){
		return loc;
	}

	@Override
	public String getFileName() {
		return loc.getFileName();
	}

	@Override
	public int getLineNumber() {
		return loc.getLineNumber();
	}
	
}
