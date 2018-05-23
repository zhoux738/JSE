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

package info.julang.typesystem.jclass.jufc.System.Reflection;

import info.julang.execution.Argument;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.execution.threading.ThreadRuntimeHelper;
import info.julang.hosting.HostedMethodProviderFactory;
import info.julang.hosting.SimpleHostedMethodProvider;
import info.julang.hosting.execution.CtorNativeExecutor;
import info.julang.hosting.execution.InstanceNativeExecutor;
import info.julang.memory.value.HostedValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.ObjectValue;
import info.julang.memory.value.TempValueFactory;
import info.julang.typesystem.jclass.JParameter;

/**
 * The native implementation of <code><font color="green">System.Reflection.Parameter</font></code>.
 * 
 * @author Ming Zhou
 */
public class ScriptParam {
		
	public static final String FQCLASSNAME = "System.Reflection.Parameter";
	
	//----------------- IRegisteredMethodProvider -----------------//
	
	public static HostedMethodProviderFactory Factory = new HostedMethodProviderFactory(FQCLASSNAME){

		@Override
		protected void implementProvider(SimpleHostedMethodProvider provider) {
			provider
				.add("ctor", new InitExecutor())
				.add("getName", new GetNameExecutor())
				.add("getType", new GetTypeExecutor());
		}
		
	};
	
	private JParameter jparam;
	
	public void setParam(JParameter jparam){
		this.jparam = jparam;
	}
	
	//----------------- native executors -----------------//
	
	private static class InitExecutor extends CtorNativeExecutor<ScriptParam> {

		@Override
		protected void initialize(ThreadRuntime rt, HostedValue hvalue, ScriptParam jmap, Argument[] args) throws Exception {
			// NO-OP
		}
		
	}
	private static class GetNameExecutor extends InstanceNativeExecutor<ScriptParam> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, ScriptParam st, Argument[] args) throws Exception {
			String name = st.getName();
			JValue res = TempValueFactory.createTempStringValue(name);
			return res;
		}
		
	}
	
	private static class GetTypeExecutor extends InstanceNativeExecutor<ScriptParam> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, ScriptParam st, Argument[] args) throws Exception {
			ObjectValue ov = st.getType(rt);
			JValue res = TempValueFactory.createTempRefValue(ov);
			return res;
		}
		
	}
	
	//----------- implementation at native end -----------//

	public String getName() {
		return jparam.getName();
	}

	public ObjectValue getType(ThreadRuntime rt) {
		ObjectValue ov = ThreadRuntimeHelper.getScriptTypeObject(rt, jparam.getType());
		return ov;
	}
}