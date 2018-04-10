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

import info.julang.external.exceptions.ScriptNotFoundException;
import info.julang.interpretation.GlobalScriptExecutable;
import info.julang.parser.ANTLRParser;
import info.julang.parser.LazyAstInfo;

import java.io.ByteArrayInputStream;

/**
 * The string script provider provides an executable from a string.
 * 
 * @author Ming Zhou
 */
public class StringScriptProvider implements ScriptProvider {

	private String script;
	private boolean interactiveMode;
	
	public StringScriptProvider(String script, boolean interactiveMode){
		this.script = script;
		this.interactiveMode = interactiveMode;
	}
	
	@Override
	public Executable getExecutable(boolean allowReentry) throws ScriptNotFoundException {
		ByteArrayInputStream bais = new ByteArrayInputStream(script.getBytes());
		ANTLRParser ap = new ANTLRParser("<memory>", bais, false);
		LazyAstInfo lainfo = ap.scan(false);
		ap.parse(true, false);
		return new GlobalScriptExecutable(lainfo, allowReentry, interactiveMode);
	}
	
}
