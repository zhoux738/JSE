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

package info.julang.interpretation.statement;

import info.julang.interpretation.ExitCause;

/**
 * The interface marking that an exit cause can be demanded.
 *  
 * @author Ming Zhou
 */
public interface IHasExitCause {

	/**
	 * Get the cause for exit. If the statement has yet to exit, this will always return {@link ExitCause#UNDEFINED}.
	 * Otherwise, it will returns any of the following:
	 * <p>
	 *  <ul>{@link ExitCause#THROUGH THROUGH}: the statement ran to end.</ul> 
	 *  <ul>{@link ExitCause#BROKEN BROKEN}: the statement is jumped out by break.</ul> 
	 *  <ul>{@link ExitCause#CONTINUED CONTINUED}: the statement is jumped out by continue.</ul> 
	 *  <ul>{@link ExitCause#FAULTED FAULTED}: the statement is aborted due to exception.</ul> 
	 * @return
	 */
	ExitCause getExitCause();
	
}
