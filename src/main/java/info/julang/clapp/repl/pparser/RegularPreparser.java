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

package info.julang.clapp.repl.pparser;

import info.julang.clapp.repl.IPreparser;
import info.julang.clapp.repl.IPreparserReplaceable;
import info.julang.clapp.repl.IPrinter;
import info.julang.clapp.repl.REPLParsingException;

/**
 * A regular pre-parser processes by these rules:  
 * <p>
 * 1. If the line starts with '.', treat it as meta-command and hand over to meta-command processor;<br>
 * 2. If the line ends with '{' or '(', check if we are entering a block. If so, transform to {@link BlockPreparser};<br>
 * 3. If the line ends with an isolated '\', transform to {@link ContinuationPreparser};<br>
 * 4. Otherwise, if the line doesn't end with '}' or ';', complement with a ';'. Return the resultant string.
 * 
 * @author Ming Zhou
 */
public class RegularPreparser implements IPreparser {

	public final static RegularPreparser INSTANCE = new RegularPreparser();
	
	private RegularPreparser(){}
	
	public String parse(IPreparserReplaceable console, String s) throws REPLParsingException {
		String st = s.trim();
		if (st.endsWith("{")) {
			BlockPreparser tdp = BlockPreparser.INSTANCE;
			if (tdp.reset(st)){
				// If TypeDeclPreparser accepted the input, return now. The rest of input will
				// be handled by TypeDeclPreparser.
				console.setPreparser(tdp);
				return null;
			}
		} else if (st.endsWith("\\")){
			ContinuationPreparser ctp = ContinuationPreparser.INSTANCE;
			if (ctp.reset(st)){
				// If ContinuationPreparser accepted the input, return now. The rest of input will
				// be handled by ContinuationPreparser.
				console.setPreparser(ctp);
				return null;
			}	
		}

		if (!s.endsWith("}") && !s.endsWith(";")){
			s += ";";
		}
		
		return s;
	}

	public boolean reset(String s) {
		// Do nothing
		return true;
	}

	public void prompt(IPrinter console) {
		console.print(PROMPT_START);
	}
}
