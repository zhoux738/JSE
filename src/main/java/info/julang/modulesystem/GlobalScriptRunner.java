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

package info.julang.modulesystem;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import info.julang.execution.Argument;
import info.julang.execution.EngineRuntime;
import info.julang.execution.Executable;
import info.julang.execution.Result;
import info.julang.execution.symboltable.ITypeTable;
import info.julang.execution.symboltable.IVariableTable;
import info.julang.execution.symboltable.IVariableTableTraverser;
import info.julang.execution.threading.JThread;
import info.julang.execution.threading.JThreadManager;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.external.exceptions.JSEError;
import info.julang.interpretation.GlobalScriptExecutable;
import info.julang.interpretation.IStackFrameInfo;
import info.julang.interpretation.InterpretedExecutable;
import info.julang.interpretation.JIllegalStateException;
import info.julang.interpretation.errorhandling.JulianScriptException;
import info.julang.interpretation.errorhandling.StackTraceKind;
import info.julang.memory.value.BoolValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.ObjectValue;
import info.julang.memory.value.RefValue;
import info.julang.memory.value.VoidValue;
import info.julang.modulesystem.IncludedFile.ResolutionStrategy;
import info.julang.typesystem.JType;
import info.julang.typesystem.jclass.builtin.FunctionKind;
import info.julang.typesystem.jclass.builtin.JFunctionType;
import info.julang.typesystem.jclass.jufc.System.IO.JSEIOException;
import info.julang.typesystem.loading.depresolving.CyclicDependencyException;

/**
 * Execute a global script in the main thread.
 * <p>
 * This is the main logic behind Environment.evaluate() and include statement. This runner will try to evaluate the script
 * at the specified script ({@link Options#useDefaultRoot}) and return its result. During the evaluation the thread context 
 * will not switch, but internally it does use a different thread object since it must be able to isolate variable table 
 * and other runtime info.
 * <p>
 * The script always shares types and modules with other global scripts. If this script defines a type and imported a module, 
 * the type or module will stay in the runtime even after the script exits. If this script defines and changes global 
 * variables, the change will only be seen by the previous script if it's configured so ({@link Options#shareScope}).
 * <p>
 * If incorporated through include statement, a script will only be run once throughout the engine's current 
 * execution session.
 * <p>
 * This runner can only be called from a global script outside any function of functors. This is enforced at the callsite
 * (Environment.evaluate()) or by syntax definition (include).
 * 
 * @author Ming Zhou
 */
public class GlobalScriptRunner {
	
	/**
	 * The root directory to resolve a relative path against.
	 */
	public static enum ScriptRoot {
		/**
		 * The root directory is the currently running script's parent folder.
		 */
		Script,
		
		/**
		 * Use the default module path (jse_modules).
		 */
		DefaultModule,
		
		/**
		 * (Not really a directory) Try to find it among the built-in scripts.
		 */
		System
	}
	
	/**
	 * Options to use when evaluating a global script.
	 * A large part of this class mirrors <code style="color:green">System.EvalConfig</code>.
	 */
	public static class Options {
		
		public boolean shareScope;
		
		public boolean returnException;
		
		public ScriptRoot primaryRoot;
		
		public ScriptRoot secondaryRoot;
		
		/**
		 * Whether the runner is invoked by an include statement.
		 */
		public boolean byInclude;

		/**
		 * Create an option from a <code style="color:green">System.EvalConfig</code> instance.
		 * 
		 * @param ov must carry Julian type <code style="color:green">System.EvalConfig</code>.
		 * @return An instance mirroring the settings found in the given Julian object. {@link #byInclude} is set to false.
		 */
		public static Options fromObject(ObjectValue ov) {
			Options opt = new Options();
			opt.byInclude = false;
			opt.shareScope = getBool(ov, "_shareScope");
			opt.primaryRoot = // No secondary root for evaluate()
				getBool(ov, "_useDefaultRoot") ? ScriptRoot.DefaultModule : ScriptRoot.Script;
			opt.returnException = getBool(ov, "_returnException");
			return opt;
		}
		
		/**
		 * Create an option for an include statement.
		 * 
		 * @param rs The strategy for resolving the location of the included script file.
		 * @return An instance mirroring the settings found in the given Julian object. {@link #byInclude} is set to false.
		 */
		public static Options fromInclude(ResolutionStrategy rs) {
			Options opt = new Options();
			opt.byInclude = true;
			switch (rs) {
			case EXTERNAL_ONLY:
				opt.primaryRoot = ScriptRoot.Script;
				break;
			case EXTERNAL_THEN_BUILTIN:
				opt.primaryRoot = ScriptRoot.Script;
				opt.secondaryRoot = ScriptRoot.System;
				break;
			}
			
			// (Default values are what we want)
			// opt.shareScope = false;
			// opt.returnException = false;
			
			return opt;		
		}
		
		private static boolean getBool(ObjectValue ov, String name) {
			BoolValue bv = (BoolValue)ov.getMemberValue(name);
			return bv.getBoolValue();
		}
	}
	
	private EngineRuntime engineRt;
	private String rawPath;
	
	public GlobalScriptRunner(EngineRuntime engineRt, String path) {
		this.engineRt = engineRt;
		this.rawPath = path;
	}
	
	/**
	 * Run the script with specified option and arguments.
	 * 
	 * @param opt options
	 * @param args arguments
	 * @return The result
	 * @throws EngineInvocationError
	 */
	public JValue run(Options opt, Argument[] args) throws EngineInvocationError {
		
		ModuleManager mm = (ModuleManager)engineRt.getModuleManager();
		JThreadManager tm = engineRt.getThreadManager();
		
		JThread currMainThread = tm.getCurrentMain();
		
		if (!currMainThread.isMain()) {
			throw new JIllegalStateException(
				"Can only " + (opt.byInclude ? "include" : "dynamically evaluate") + " a script from the main thread.");
		}

		GlobalScriptResolver resolver = GlobalScriptResolverFactory.createResolver(rawPath, opt, tm);
		
		String spath = resolver.getFullPath();
		if (spath == null) {
			// Path for the current script cannot be ascertained. A relative path cannot be used in this case.
			throw new JSEIOException("Script file cannot be found: " + rawPath);
		}
		
		// 2) Check against previous evaluation
		// 2.1) Check if the script is being evaluated already
		Result res = mm.tryGetResult(spath);
		if (res == Result.Void) {
			// Circular evaluation with a script not the first one.
			
			// 2.2) If via include statement, try to reuse the cached result
			if (opt.byInclude) {
				return returnOrThrow(res, opt);
			}
			
			String[] pathArray = getCircularScripts(tm, spath);
			throw new CyclicDependencyException(pathArray, false);
		} else {
			// Circular evaluation with the first script. 
			String firstPath = getScriptPath(tm.getFirstMain());
			File first = new File(firstPath);
			if (first.exists()) { // The entrance script may not be loaded from a file.
				try {
					String cpath = first.getCanonicalPath();
					if (spath.equals(cpath)) {
						// 2.2) If via include statement, try to reuse the cached result
						if (opt.byInclude) {
							return returnOrThrow(res == null ? Result.Void : res, opt);
						}
						
						String[] pathArray = getCircularScripts(tm, spath);
						throw new CyclicDependencyException(pathArray, false);
					}
				} catch (IOException e) {
					throw new JSEIOException("Cannot determine the entrance script's path.");
				}
			}
		}
		
		if (opt.byInclude && res != null) {
			return returnOrThrow(res, opt);
		}
		
		// 3) Create a new global script executable, and optionally carry over globals/bindings
		GlobalScriptExecutable exec = resolver.getExecutable(spath);
		if (exec == null) {
			throw new JSEIOException("Script file cannot be found: " + rawPath);
		}

		IVariableTable gvt = currMainThread.getThreadRuntime().getGlobalVariableTable();
		int nestLevel = gvt.getNestLevel();
		exec.setEvaluateMode(gvt, opt.shareScope);

		JThread thread = null;
		boolean succ = false;
		try {
			// 4) Replace main thread with a new executable
			thread = tm.replaceMain(engineRt, exec);
			mm.replaceResult(spath, Result.Void);
			
			// 5) Execute the new thread object with arguments; cache the result, whether forced or not
			Result result = tm.runThreadInline(thread, args);
			if (result == Result.Void) {
				// If it's `Result.Void`, replace with null. `Result.Void` is a used as a special value 
				// to represent the state that the script is being evaluated.
				//
				// Note we don't replace any void value. If the Result legitimately contains a void value
				// as the return value then it will be stored as is. This Result, however, won't be equated
				// to Result.Void per reference-comparison. Its void value will be converted to null when
				// we indeed return from the runner (returnOrThrow).
				result = new Result(RefValue.NULL);
			}
			
			// 6) Cache the result if by include.
			mm.replaceResult(spath, opt.byInclude ? result : null);
			succ = true;
			
			// 7) Return the resultant value
			return returnOrThrow(result, opt);
		} catch (JulianScriptException jse) {
			jse.preserveAcrossPlatformBoundary();
			Result result = new Result(jse);
			return returnOrThrow(result, opt);
		} finally {
			// Reset the evaluation status
			if (!succ) {
				mm.replaceResult(spath, null);
			}
			
			JThread prevThread = tm.resumePreviousMain();
			
			if (thread != null) {
				// If the scope is shared, must copy back the new variables.
				ThreadRuntime threadRt = thread.getThreadRuntime();
				IVariableTable newGvt = threadRt.getGlobalVariableTable();
				Map<String, JValue> newNames = new GlobalVariableCollector(newGvt).collect();
				
				IVariableTable oldGvt = prevThread.getThreadRuntime().getGlobalVariableTable();
				Set<String> oldNames = new GlobalVariableCollector(oldGvt).collectNames();
				
				for (String name : oldNames) {
					newNames.remove(name);
				}

				if (oldGvt.getNestLevel() != nestLevel) {
					throw new JSEError(
						"Cannot copy back global variables to the previous global scope, where " + 
						"the nesting level has shifted for unknown reason.", GlobalScriptRunner.class);
				}
				
				// What remain in newNames are new globals.
				if (opt.shareScope) {
					// If sharing scope, copy all of them back.
					for (Entry<String, JValue> entry : newNames.entrySet()) {
						oldGvt.addVariable(entry.getKey(), entry.getValue());
					}
				} else {
					// Otherwise, only copy functions back. Global functions are special in that they are both 
					// registered as types and values in GVT. Since it cannot opt out of type sharing, we must 
					// also copy the values backs to ensure the integrity of these functions. Besides, function 
					// sharing is the primary usage scenario of script inclusion.
					ITypeTable tt = threadRt.getTypeTable();
					for (Entry<String, JValue> entry : newNames.entrySet()) {
						String name = entry.getKey();
						JValue value = entry.getValue();
						addFunctionVariable(oldGvt, tt, name, value);
					}
				}
			}
		}
	}

	/**
	 * Add the name/value as a variable to the variable table, if the 
	 * value is of function type. If not a function, nothing happens.
	 * 
	 * @param gvt The variable table
	 * @param tt The type table
	 * @param name Variable name
	 * @param value Variable value
	 */
	public static void addFunctionVariable(IVariableTable gvt, ITypeTable tt, String name, JValue value) {
		JType type = tt.getType(name);
		if (type != null && type instanceof JFunctionType) {
			JFunctionType ftype = (JFunctionType)type;
			if (ftype.getFunctionKind() == FunctionKind.FUNCTION) {
				gvt.addVariable(name, value);
			}
		}
	}

	private class GlobalVariableCollector implements IVariableTableTraverser {

		private Map<String, JValue> names;
		private IVariableTable gvt;
		
		private GlobalVariableCollector(IVariableTable gvt) {
			this.gvt = gvt;
		}
		
		private void traverseOnce() {
			if (names == null) {
				names = new HashMap<>();
				gvt.traverse(this, false);
			}
		}
		
		private Set<String> collectNames(){
			traverseOnce();
			return names.keySet();
		}
		
		private Map<String, JValue> collect(){
			traverseOnce();
			return names;
		}
		
		@Override
		public boolean processScope(int level, Map<String, JValue> scope) {
			if (level == 0) {
				for (Entry<String, JValue> entry : scope.entrySet()) {
					names.put(entry.getKey(), entry.getValue());
				}
				
				return true;
			}
			
			return false;
		}		
	}
	
	private String[] getCircularScripts(JThreadManager tm, String spath) {
		boolean found = false;
		JThread[] mains = tm.getAllMains();
		List<String> paths = new ArrayList<>();
		for (JThread main : mains) {
			InterpretedExecutable iexec = (InterpretedExecutable)main.getExecutable();
			String mpath = iexec.getScriptPath();
			try {
				mpath = new File(mpath).getCanonicalPath();
			} catch (IOException e) {
				// Ignore. We were just trying to get a better representation of the path. If we couldn't, use the default.
			}
			
			if (!found && spath.equals(mpath)) {
				found = true;
			}
			
			if (found) {
				paths.add(mpath);
			}
		}
		
		paths.add(spath);
		String[] pathArray = new String[paths.size()];
		pathArray = paths.toArray(pathArray);
		return pathArray;
	}

	// If not returned explicitly, return null; if thrown, re-throw.
	private JValue returnOrThrow(Result result, Options opt) {
		if (result.isSuccess()) {
			JValue val = result.getReturnedValue(false);
			if (val == VoidValue.DEFAULT) {
				val = RefValue.NULL;
			}
			
			return val;
		} else {
			JulianScriptException ex = result.getException();
			
			if (opt.returnException) {
				return ex.getExceptionValue();
			} else {
				ex.setTraceKind(opt.byInclude ? StackTraceKind.INCLUDE : StackTraceKind.EVALUATE);
				throw ex;
			}
		}
	}
	
	// Get the script path of main thread.
	static String getScriptPath(JThread mainThread) {
		Executable exec = mainThread.getExecutable();
		IStackFrameInfo info = (IStackFrameInfo)exec;
		return info.getScriptPath();
	}
}
