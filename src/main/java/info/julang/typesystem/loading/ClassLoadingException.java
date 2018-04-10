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

package info.julang.typesystem.loading;

import info.julang.JSERuntimeException;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.errorhandling.IHasLocationInfo;
import info.julang.interpretation.errorhandling.JSExceptionFactory;
import info.julang.interpretation.errorhandling.JSExceptionUtility;
import info.julang.interpretation.errorhandling.JulianScriptException;
import info.julang.interpretation.errorhandling.KnownJSException;
import info.julang.interpretation.internal.NewVarExecutor;
import info.julang.langspec.CharacterEscaping;
import info.julang.memory.value.ObjectValue;

public class ClassLoadingException extends JSERuntimeException {

	private static final long serialVersionUID = 1155440407068738063L;

	private JSERuntimeException jseCause;
	private String msg;
	
	public ClassLoadingException(ILoadingState state) {
		super("");

		msg = "Encountered an error when loading " + state.getTypeName();
		Exception ex = state.getException();
		if (ex instanceof JSERuntimeException){
			// If the cause is also a JSE, we convert it to a JSE cause. Otherwise, simply concatenate the message.
			jseCause = (JSERuntimeException)ex;
		} else if (ex != null){
			msg += ": " + ex.getMessage();
		}
	}
	
	public ClassLoadingException(Exception cause) {
		this("Encountered an error when loading types: " + cause.getMessage());
	}
	
	public ClassLoadingException(String msg) {
		super("");
		this.msg = msg;
	}
	
	public JulianScriptException toJSE(ThreadRuntime rt, Context context) {
		if (jseCause != null) {
			JulianScriptException jse = jseCause.toJSE(rt, context);

			if (jseCause instanceof IHasLocationInfo){
				IHasLocationInfo loInfo = (IHasLocationInfo)jseCause;
				JSExceptionUtility.setSourceInfo(jse, loInfo);
			}
			
			ObjectValue val = jse.getExceptionValue();
			NewVarExecutor nve = new NewVarExecutor();
			String varId = "tmpcauz";
			context.getVarTable().enterScope();
			nve.newVar(context, varId, val, null);
			try{
				return JSExceptionFactory.createException(
					KnownJSException.ClassLoading, 
					rt, 
					context, 
					CharacterEscaping.encodeAsStringLiteral(
						"(", 
						msg, 
						", " + varId + ");") // pass cause as the 2nd argument
					);		
			} finally {
				context.getVarTable().exitScope();
			}
		} else {
			return super.toJSE(rt, context);
		}
	}

	// Override the following mainly for the non-taken branch in toJSE method (when jseCause is null)
	
	@Override
	public KnownJSException getKnownJSException() {
		return KnownJSException.ClassLoading;
	}
	
	@Override
	protected String getScriptMessage(){
		return msg;
	}

}
