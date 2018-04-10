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

/**
 * The cause for which a statement finishes interpretation.
 * 
 * @author Ming Zhou
 */
public enum ExitCause {
	
	/** The statement has yet to exit */
	UNDEFINED,
	
	/** Ran to end successfully */
	THROUGH,
	
	/** Exited by return statement */
	RETURNED,
	
	/** Exited by break statement */
	BROKEN,
	
	/** Exited by continue statement */
	CONTINUED,
	
	/** Failed to run to the end due to exception/error */
	FAULTED;
	
	/**
	 * Check if the cause is by jump statements (break/continue)
	 * @param cause
	 * @return true if it jumped out by break/continue statements.
	 */
	public static boolean isJumpedOut(ExitCause cause){
		if(cause == ExitCause.BROKEN || cause == ExitCause.CONTINUED){
			return true;
		}
		
		return false;
	}
	
	/**
	 * Check if the cause indicates an early exit (by return, exception or jump).
	 * @param cause
	 * @return
	 */
	public static boolean isAborted(ExitCause cause){
		return cause.ordinal() > ExitCause.THROUGH.ordinal();
	}
	
}
