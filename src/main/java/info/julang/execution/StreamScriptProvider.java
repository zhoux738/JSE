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

package info.julang.execution;

import java.io.InputStream;

import info.julang.external.exceptions.ScriptNotFoundException;
import info.julang.interpretation.GlobalScriptExecutable;
import info.julang.interpretation.InterpretedExecutable;
import info.julang.parser.ANTLRParser;
import info.julang.parser.LazyAstInfo;

/**
 * The stream script provider provides an executable from an input stream.
 * 
 * @author Ming Zhou
 */
public class StreamScriptProvider implements ScriptProvider {

	private InputStream stream;
	private String fileName;
	
	public StreamScriptProvider(InputStream stream, String fileName){
		this.stream = stream;
		this.fileName = fileName;
	}
	
	@Override
	public GlobalScriptExecutable getExecutable(boolean allowReentry) throws ScriptNotFoundException {
		ANTLRParser ap = new ANTLRParser(fileName, stream, false);
		LazyAstInfo lainfo = ap.scan(false);
		ap.parse(true, false);
		return new GlobalScriptExecutable(lainfo, allowReentry, false);
	}

	@Override
	public String getDefaultModulePath() {
		return null;
	}
}
