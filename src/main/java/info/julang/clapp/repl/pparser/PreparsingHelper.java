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

import info.julang.util.Pair;

public final class PreparsingHelper {

	/**
	 * Remove the trailing blanks, then trailing back-slashes (\).
	 *  
	 * @param s the string to be processed.
	 * @return As an example, <code>" some \ text \\ "</code> would become <code>"some \ text"</code>
	 */
	public static Pair<String, Boolean> removeTrailingBackslashes(String s){
    	// The input can end with one or more \ concatenated together, 
		// but they must be separated from the rest by at least one blank char.
    	//    
    	// text  \      legal
    	// text \\\     legal
    	// text\        illegal
    	// text\\\      illegal
    	// text \ \     legal, but will likely cause syntax error
    	
        int ending = s.length() - 1;
        for(int i = ending; i >= 0 ; i--) {
        	char c = s.charAt(i);
        	if (c == ' ' || c == '\t' || c == '\r' || c == '\n') {
        		ending--;
        	} else {
        		break;
        	}
        }
        
        int index = -1;
        for(int i = ending; i >= 0 ; i--) {
        	char c = s.charAt(i);
        	if (c == '\\') {
        		// Keep check the preceding char
        		index = i;
        	} else if (c == ' ' || c == '\t') {
        		// Breaking on blank
        		break;
        	} else {
        		// A preceding char is not '\'
        		index = -1;
        		break;
        	}
        }

    	boolean hasBackSlash = false;
        if (index >= 0) {
    		s = s.substring(0, index);
    		hasBackSlash = true;
        } else {
    		s = s.substring(0, ending + 1);
        }
        
        return new Pair<String, Boolean>(s, hasBackSlash);
	}
}
