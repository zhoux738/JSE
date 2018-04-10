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

import info.julang.external.interfaces.IExtResult;
import info.julang.interpretation.errorhandling.JulianScriptException;
import info.julang.memory.value.JValue;
import info.julang.memory.value.ValueUtilities;
import info.julang.memory.value.VoidValue;

/**
 * The result of execution, including the value returned, as well as other state information
 *
 * @author Ming Zhou
 */
public class Result implements IExtResult {

	public final static Result Void = new Result(VoidValue.DEFAULT);
	
	private JValue value;
	
	private JulianScriptException exception;

	public Result(JValue value) {
		this.value = value;
	}
	
	public Result(JulianScriptException jse) {
		this.exception = jse;
	}
	
	/**
	 * Get the thrown exception.
	 * @return null if the result is a success.
	 */
	public JulianScriptException getException(){
		return exception;
	}

	/**
	 * Replicate the returned value in this result to replace the original one.
	 */
	public void replicateValue() {
		if (value != VoidValue.DEFAULT){
			value = ValueUtilities.replicateValue(value, null, value.getMemoryArea());
		}
	}

	//------------------ IExtResult ------------------//
	
	@Override
	public boolean isSuccess(){
		return exception == null;
	}

	@Override
	public JValue getReturnedValue(boolean deref) {
		return exception != null ? 
			null : ((deref && value != null) ? value.deref() : value);
	}
	
	@Override
	public String getExceptionOutput() {
		return exception != null ? exception.getStandardExceptionOutput(0, false) : "";
	}
	
	@Override
	public String getExceptionFileName(){
		return exception != null ? exception.getFileName() : "<unknown>";
	}
	
	@Override
	public int getExceptionLineNumber(){
		return exception != null ? exception.getLineNumber() : -1;
	}
}
