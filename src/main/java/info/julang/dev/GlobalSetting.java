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

package info.julang.dev;

import info.julang.interpretation.errorhandling.KnownJSException;

import java.util.HashSet;
import java.util.Set;

public final class GlobalSetting {

	public static final String PKG_PREFIX = "info.julang.";
	
	private static Set<KnownJSException> exceptionsToSkip;
	
	/**
	 * True to not print out scripts being scanned.
	 */
	public static final boolean NoEcho = true; // true // false
	
	/**
	 * True to dump the exception.
	 */
	public static final boolean DumpException = false; // true // false
	
	/**
	 * False to disable the default error report by ANTLR.
	 */
	public static final boolean EnableANTLRDefaultErrorReport = false; // true // false
	
	//////////// Set the following to false during massive refactoring ////////////
	/**
	 * True to capture all exceptions as part of script's error handling. 
	 * False to throw them out from inside the engine.
	 */
	public static final boolean EnableJSE = true; // true // false

	/**
	 * False to disable all multithreading tests.
	 */
	public static final boolean EnableMultiThreadingTests = true; // true // false
	
	/**
	 * Should catch the specified type of exception.
	 * 
	 * @param kjse
	 * @return
	 */
	public static boolean skipCatch(KnownJSException kjse){
		if(!EnableJSE || kjse == null){
			return true;
		}
		
		if(exceptionsToSkip == null){
			exceptionsToSkip = new HashSet<KnownJSException>();
			
			//exceptionsToSkip.add(KnownJSException.IllegalAssignment);
		}
		
		return exceptionsToSkip.contains(kjse);
	}
}
