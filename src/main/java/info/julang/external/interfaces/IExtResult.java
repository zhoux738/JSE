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

package info.julang.external.interfaces;

public interface IExtResult {

	/**
	 * Whether the execution is successful.
	 * 
	 * @return true if the execution is successful.  
	 */
	public boolean isSuccess();
	
	/**
	 * Get a string that contains standard exception information, including exception type, message, stack trace 
	 * and those of each cause in the chain. 
	 * 
	 * @return a standardized string containing exception information.
	 */
	public String getExceptionOutput();
	
	/**
	 * Get the full path-file name in which this exception was originally thrown. 
	 * 
	 * @return the full path-file name in which this exception was originally thrown. 
	 */
	public String getExceptionFileName();
	
	/**
	 * Get the line number on the file at which this exception was originally thrown. 
	 * 
	 * @return the line number on the file at which this exception was originally thrown. 
	 */
	public int getExceptionLineNumber();

	/**
	 * Get the returned value of execution. Note this value is stored in the current frame (the callee has returned).
	 * 
	 * @param deref true to dereference the value before returning it.
	 * @return can be null if exception is thrown.
	 */
	public IExtValue getReturnedValue(boolean deref);
}
