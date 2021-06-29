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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import info.julang.execution.Argument;
import info.julang.execution.ArgumentUtil;
import info.julang.execution.security.PACON;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.execution.threading.ThreadRuntimeHelper;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.hosting.HostedMethodProviderFactory;
import info.julang.hosting.SimpleHostedMethodProvider;
import info.julang.hosting.execution.StaticNativeExecutor;
import info.julang.interpretation.IStackFrameInfo;
import info.julang.interpretation.JIllegalStateException;
import info.julang.memory.value.ArrayValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.ObjectValue;
import info.julang.memory.value.RefValue;
import info.julang.memory.value.StringValue;
import info.julang.memory.value.TempValueFactory;
import info.julang.modulesystem.ClassInfo;
import info.julang.modulesystem.GlobalScriptRunner;
import info.julang.modulesystem.IncludedFile.ResolutionStrategy;
import info.julang.modulesystem.ModuleInfo;
import info.julang.modulesystem.ModuleManager;
import info.julang.modulesystem.ScriptInfo;
import info.julang.typesystem.JType;
import info.julang.typesystem.jclass.builtin.JStringType;
import info.julang.typesystem.jclass.jufc.System.Reflection.ScriptScript;

/**
 * The native implementation of <code style="color:green">System.Environment</code>.
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
				.add("getEnv", new GetEnvExecutor())
				.add("getAllEnv", new GetAllEnvExecutor())
				.add("evaluate", new EvaluateExecutor())
				.add("getLBS", new GetLBSExecutor())
				.add("getPS", new GetPSExecutor())
				.add("getS", new GetSExecutor());
		}
		
	};
	
	//----------------- native executors -----------------//
	
	private static class GetEnvExecutor extends StaticNativeExecutor<JEnvironment> {

		GetEnvExecutor(){
			super(PACON.Environment.Name, PACON.Environment.Op_read);
		}
		
		@Override
		protected JValue apply(ThreadRuntime rt, Argument[] args) throws Exception {
			String name = this.getString(args, 0);
			String value = System.getenv(name);
			if (value == null) {
				return RefValue.NULL;
			} else {
				return TempValueFactory.createTempStringValue(value);
			}
		}
	}
	
	private static class GetAllEnvExecutor extends StaticNativeExecutor<JEnvironment> {

		GetAllEnvExecutor(){
			super(PACON.Environment.Name, PACON.Environment.Op_read);
		}
		
		@Override
		protected JValue apply(ThreadRuntime rt, Argument[] args) throws Exception {
			Map<String, String> vars = System.getenv();
			
			Set<Entry<String, String>> entries = vars.entrySet();
			ArrayValue av = TempValueFactory.createTemp2DArrayValue(rt.getTypeTable(), JStringType.getInstance(), entries.size(), 2);
			int i = 0;
			for (Entry<String, String> entry : entries) {
				ArrayValue ev = (ArrayValue)av.getValueAt(i).deref();
				
				StringValue k = new StringValue(rt.getHeap(), entry.getKey());
				k.assignTo(ev.getValueAt(0));

				StringValue v = new StringValue(rt.getHeap(), entry.getValue());
				v.assignTo(ev.getValueAt(1));
				
				i++;
			}
			
			return av;
		}
	}
	
	private static class GetScriptExecutor extends StaticNativeExecutor<JEnvironment> {

		GetScriptExecutor(){
			super(PACON.Environment.Name, PACON.Environment.Op_read);
		}
		
		@Override
		protected JValue apply(ThreadRuntime rt, Argument[] args) throws Exception {
			ObjectValue ov = getScript(rt);
			return ov;
		}
	}
	
	private static class EvaluateExecutor extends StaticNativeExecutor<JEnvironment> {

		EvaluateExecutor(){
			super(PACON.Environment.Name, PACON.Environment.Op_eval);
		}
		
		@Override
		protected JValue apply(ThreadRuntime rt, Argument[] args) throws Exception {
			// option
			GlobalScriptRunner.Options opt;
			JValue vopt = args[0].getValue().deref();
			if (vopt == RefValue.NULL) {
				opt = new GlobalScriptRunner.Options();
			} else {
				opt = GlobalScriptRunner.Options.fromObject((ObjectValue)vopt);
			}
			
			// path
			StringValue sv = StringValue.dereference(args[1].getValue(), true);
			String filePath = sv.getStringValue();
			
			// arguments
			String[] sargs;
			JValue vargs = args[2].getValue().deref();
			if (vargs == RefValue.NULL) {
				sargs = new String[0];
			} else {
				ArrayValue av = (ArrayValue)vargs;
				int size = av.getLength();
				sargs = new String[size];
				
				for (int i = 0; i < size; i++) {
					StringValue elev = StringValue.dereference((JValue)av.get(i), false);
					sargs[i] = elev == null ? null : elev.getStringValue();
				}
			}
			
			JValue res = evaluate(rt, filePath, sargs, opt);
			return res;
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
	
	private static JValue evaluate(
		ThreadRuntime rt, String path, String[] args, GlobalScriptRunner.Options opt) throws EngineInvocationError {
		
		// Enforce the callsite constraints:
		// Must be called from a global script and outside any forms of functions.
		boolean deny = true;
		int expDepth = 1; // TODO: may need to change this for interactive mode
		int depth = rt.getThreadStack().getDepth();
		
		if (depth == expDepth) {
			deny = false;
		} else if (depth == expDepth + 1) {
			IStackFrameInfo info = rt.getThreadStack().getFrameInfoFromTop(0);
			if (info.isFromLooseScript() && "script.jul".equals(info.getScriptPath())) { 
				// (The string comparison ensures elimination of user-provided scripts which always have a full path name.)
				
				// Exceptional - support calling two functions from the built-in script.jul
				String fname = info.getName();
				if ("incl".equals(fname)) {
					// Overwrite the option to make it consistent with the behavior of include statement.
					opt = GlobalScriptRunner.Options.fromInclude(ResolutionStrategy.EXTERNAL_THEN_BUILTIN);
					deny = false;
				} else if ("eval".equals(fname)) {
					deny = false;
				}
			}
		}
		
		if (deny) {
			throw new JIllegalStateException(
				"Cannot dynamically evaluate another script from inside any form of functions, including class methods and lambdas.");
		}
		
		GlobalScriptRunner runner = new GlobalScriptRunner(rt, path);
		
		Argument[] sargs = ArgumentUtil.convertArguments(rt.getTypeTable(), args);

		// This may throw EngineInvocationError, but let the platform execution framework handle it.
		JValue res = runner.run(opt, sargs);
		
		return res;
	}
	
	private static ObjectValue getScript(ThreadRuntime rt){
		IStackFrameInfo info = rt.getThreadStack().getFrameInfoFromTop(0);
		JType typ = info.getContainingType();
		ScriptInfo si = null;
		if (typ != null) {
			ClassInfo cinfo = rt.getModuleManager().getClassesByFQName(typ.getName());
			si = cinfo.getScriptInfo();
		} else if (info.isFromLooseScript()) {
			// GlobalScriptRunner.getScriptPath(rt.getJThread());
			
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
