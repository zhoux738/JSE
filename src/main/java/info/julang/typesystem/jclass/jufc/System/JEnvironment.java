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

import java.io.File;

import info.julang.execution.Argument;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.execution.threading.ThreadRuntimeHelper;
import info.julang.hosting.HostedMethodProviderFactory;
import info.julang.hosting.SimpleHostedMethodProvider;
import info.julang.hosting.execution.StaticNativeExecutor;
import info.julang.interpretation.IStackFrameInfo;
import info.julang.memory.value.JValue;
import info.julang.memory.value.ObjectValue;
import info.julang.memory.value.TempValueFactory;
import info.julang.modulesystem.ClassInfo;
import info.julang.modulesystem.ModuleInfo;
import info.julang.modulesystem.ModuleManager;
import info.julang.modulesystem.ScriptInfo;
import info.julang.typesystem.JType;
import info.julang.typesystem.jclass.jufc.System.Reflection.ScriptScript;

/**
 * The native implementation of <font color="green">System.Environment</font>.
 * 
 * @author Ming Zhou
 */
public class JEnvironment {
	
	public static final String FQCLASSNAME = "System.Environment";
	
	//----------------- IRegisteredMethodProvider -----------------//
	
	public static HostedMethodProviderFactory Factory = new HostedMethodProviderFactory(FQCLASSNAME){

		@Override
		protected void implementProvider(SimpleHostedMethodProvider provider) {
			provider
				.add("getScript", new GetScriptExecutor())
				.add("getLBS", new GetLBSExecutor())
				.add("getPS", new GetPSExecutor())
				.add("getS", new GetSExecutor());
		}
		
	};
	
	//----------------- native executors -----------------//
	
	private static class GetScriptExecutor extends StaticNativeExecutor<JEnvironment> {

		@Override
		protected JValue apply(ThreadRuntime rt, Argument[] args) throws Exception {
			ObjectValue ov = getScript(rt);
			return ov;
		}
	}
	
	private static class GetLBSExecutor extends StaticNativeExecutor<JEnvironment> {

		@Override
		protected JValue apply(ThreadRuntime rt, Argument[] args) throws Exception {
			return TempValueFactory.createTempStringValue(System.lineSeparator());
		}
	}
	
	private static class GetPSExecutor extends StaticNativeExecutor<JEnvironment> {

		@Override
		protected JValue apply(ThreadRuntime rt, Argument[] args) throws Exception {
			return TempValueFactory.createTempStringValue(File.pathSeparator);
		}
	}
	
	private static class GetSExecutor extends StaticNativeExecutor<JEnvironment> {

		@Override
		protected JValue apply(ThreadRuntime rt, Argument[] args) throws Exception {
			return TempValueFactory.createTempStringValue(File.separator);
		}
	}

	//----------------- implementation at native end -----------------//
	
	private static ObjectValue getScript(ThreadRuntime rt){
		IStackFrameInfo info = rt.getThreadStack().getFrameInfoFromTop(0);
		JType typ = info.getContainingType();
		ScriptInfo si = null;
		if (typ != null) {
			ClassInfo cinfo = rt.getModuleManager().getClassesByFQName(typ.getName());
			si = cinfo.getScriptInfo();
		} else if (info.isFromLooseScript()) {
			ModuleManager mm = (ModuleManager)rt.getModuleManager();
			// This is not really to load the module. We are on assumption that 1) at this moment
			// the default module has been loaded and 2) the module manager cached the result.
			ModuleInfo mi = mm.loadModule(rt.getJThread(), ModuleInfo.DEFAULT_MODULE_NAME);
			si = mi.getFirstScript();
		}
		
		ObjectValue ov = null;
		if (si != null) {
			ov = si.getScriptScriptObject(rt);
		} else {
			ov = ThreadRuntimeHelper.instantiateSystemType(rt, ScriptScript.FQCLASSNAME, new JValue[0]);
		}

		return ov;
	}
	
}
