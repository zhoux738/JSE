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
import info.julang.hosting.HostedMethodProviderFactory;
import info.julang.hosting.SimpleHostedMethodProvider;
import info.julang.hosting.execution.CtorNativeExecutor;
import info.julang.hosting.execution.InstanceNativeExecutor;
import info.julang.memory.value.HostedValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.RefValue;
import info.julang.memory.value.TempValueFactory;
import info.julang.modulesystem.ScriptInfo;
import info.julang.util.OSTool;

/**
 * The native implementation of <code><font color="green">System.Reflection.Script</font></code>.
 * 
 * @author Ming Zhou
 */
public class ScriptScript {
	
	public static final String FQCLASSNAME = "System.Reflection.Script";
	
	//----------------- IRegisteredMethodProvider -----------------//
	
	public static HostedMethodProviderFactory Factory = new HostedMethodProviderFactory(FQCLASSNAME){

		@Override
		protected void implementProvider(SimpleHostedMethodProvider provider) {
			provider
				.add("ctor", new InitExecutor())
				.add("getPath", new GetPathExecutor())
				.add("getModule", new GetModuleExecutor());
		}
		
	};
	
	private ScriptInfo si;
	
	public void setScriptInfo(ScriptInfo si){
		this.si = si;
	}
	
	//----------------- native executors -----------------//
	
	private static class InitExecutor extends CtorNativeExecutor<ScriptScript> {

		@Override
		protected void initialize(ThreadRuntime rt, HostedValue hvalue, ScriptScript ss, Argument[] args) throws Exception {
			// NO-OP
		}
		
	}
	
	private static class GetPathExecutor extends InstanceNativeExecutor<ScriptScript> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, ScriptScript ss, Argument[] args) throws Exception {
			JValue res = null;
			if (ss != null) {
				String path = ss.getPath();
				if (path == null) {
					res = RefValue.NULL;
				} else {
					path = OSTool.canonicalizePath(path);
					res = TempValueFactory.createTempStringValue(path);
				}
			}
			
			return res;
		}
		
	}
	
	private static class GetModuleExecutor extends InstanceNativeExecutor<ScriptScript> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, ScriptScript ss, Argument[] args) throws Exception {
			JValue res = ss.getModule(rt);
			return res;
		}
		
	}

	public String getPath() {
		return si.getFullPath();
	}

	public JValue getModule(ThreadRuntime rt) {
		return si.getModuleInfo().getOrCreateScriptObject(rt);
	}
}
