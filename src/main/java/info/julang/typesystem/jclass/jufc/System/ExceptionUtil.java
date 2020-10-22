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

package info.julang.typesystem.jclass.jufc.System;

import info.julang.execution.Argument;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.hosting.HostedMethodProviderFactory;
import info.julang.hosting.SimpleHostedMethodProvider;
import info.julang.hosting.execution.StaticNativeExecutor;
import info.julang.interpretation.errorhandling.JulianScriptException;
import info.julang.memory.value.JValue;
import info.julang.memory.value.ObjectValue;
import info.julang.memory.value.StringValue;

/**
 * A bridge facilitating interop between JSE exception's internal structure and external presentation.
 * 
 * @author Ming Zhou
 */
public class ExceptionUtil {

	private static final String FullTypeName = "System.ExceptionUtil";
	
	//------------ IRegisteredMethodProvider -------------//

	public static HostedMethodProviderFactory Factory = new HostedMethodProviderFactory(FullTypeName){

		@Override
		protected void implementProvider(SimpleHostedMethodProvider provider) {
			provider
				.add("getStackTraceAsString", new GetStackTraceAsStringExecutor());
		}
		
	};

	//----------------- native executors -----------------//
	
	private static class GetStackTraceAsStringExecutor extends StaticNativeExecutor<ExceptionUtil> {

		GetStackTraceAsStringExecutor(){
			super();
		}
		
		@Override
		protected JValue apply(ThreadRuntime rt, Argument[] args) throws Exception {
			ObjectValue ov = (ObjectValue)args[0].getValue().deref();
			
			String val = JulianScriptException.toStandardExceptionOutput(ov);
			
			StringValue sv = new StringValue(rt.getHeap(), val);
			
			return sv;
		}
	}
}