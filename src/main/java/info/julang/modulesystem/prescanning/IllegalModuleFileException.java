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

package info.julang.modulesystem.prescanning;

import org.antlr.v4.runtime.ParserRuleContext;

import info.julang.JSERuntimeException;
import info.julang.interpretation.errorhandling.IHasLocationInfo;
import info.julang.interpretation.errorhandling.KnownJSException;
import info.julang.scanner.ITokenStream;

public class IllegalModuleFileException extends JSERuntimeException implements IHasLocationInfo {

	private static final long serialVersionUID = 8845209782260396763L;

	private String fileName;
	private int line;
	
	public IllegalModuleFileException(RawScriptInfo info, ParserRuleContext prc, String msg) {
		super(makeMessage(info, msg));
		fileName = info.getScriptFilePath();
		line = prc.getStart().getLine();
	}
	
	public IllegalModuleFileException(RawScriptInfo info, ITokenStream ts, String msg) {
		super(makeMessage(info, msg));
		fileName = info.getScriptFilePath();
		line = ts.peek().getLine();
	}
	
	public IllegalModuleFileException(RawScriptInfo info, String fileName, int lineNo, String msg) {
		super(makeMessage(info, msg));
		this.fileName = fileName;
		line = lineNo;
	}

	private static String makeMessage(RawScriptInfo info, String msg) {
		return "Encountered an illegal module file: " + msg;
	}
	
	@Override
	public KnownJSException getKnownJSException() {
		return KnownJSException.IllegalModule;
	}

	@Override
	public String getFileName() {
		return fileName;
	}

	@Override
	public int getLineNumber() {
		return line;
	}

}
