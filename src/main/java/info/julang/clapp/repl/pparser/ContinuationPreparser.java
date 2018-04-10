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
import info.julang.util.Pair;

/**
 * The continuation pre-parser processes by these rules:  
 * <p>
 * 1. Read and stash each line. If it's ended with one or more '\' at the end, ignore it.<br>
 * 2. If a line is comprised sole of 0+ ';' and EOL, finishes reading. Transform to {@link RegularPreparser}; return the accumulated string.
 * 
 * @author Ming Zhou
 */
public class ContinuationPreparser implements IPreparser {

	public final static ContinuationPreparser INSTANCE = new ContinuationPreparser();
	
	private ContinuationPreparser(){}
	
    private StringBuilder sb;
    
	public String parse(IPreparserReplaceable console, String s) throws REPLParsingException {
    	Pair<String, Boolean> pair = PreparsingHelper.removeTrailingBackslashes(s);
    	String st = pair.getFirst();
    	boolean removed = pair.getSecond();

		sb.append(st);
		
		if (!removed) {
			// If comprising solely of ';', the continuation is concluded
			while (st.endsWith(";")){
				st = st.substring(0, st.length() - 1);
			}
			
			if ("".equals(st)){
				String res = sb.toString();
				sb = null;
				console.setPreparser(RegularPreparser.INSTANCE);
				return res;
			}
		}
		
		sb.append(System.lineSeparator());
		return null;
	}

	// the argument is guaranteed to be trimmed and ended with at least one '\'
	public boolean reset(String st) {
		// Remove the trailing back slashes (\).
		do {
			st = st.substring(0, st.length() - 1);
		} while (st.endsWith("\\"));
		
		sb = new StringBuilder(st);
		sb.append(System.lineSeparator());
		
		return true;
	}
	
	public void prompt(IPrinter console) {
		console.print(PROMPT_CONT);
	}
}
