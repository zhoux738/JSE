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

package info.julang.execution.simple;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import info.julang.execution.Argument;
import info.julang.execution.EngineContext;
import info.julang.execution.EngineRuntime;
import info.julang.execution.Executable;
import info.julang.execution.FileScriptProvider;
import info.julang.execution.IScriptEngine;
import info.julang.execution.Result;
import info.julang.execution.ScriptExceptionHandler;
import info.julang.execution.ScriptProvider;
import info.julang.execution.State;
import info.julang.execution.StringScriptProvider;
import info.julang.execution.symboltable.IVariableTable;
import info.julang.execution.threading.JThread;
import info.julang.execution.threading.JThreadManager;
import info.julang.external.EngineInitializationOption;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.external.exceptions.JSEError;
import info.julang.external.exceptions.JSEException;
import info.julang.external.exceptions.ScriptNotFoundException;
import info.julang.external.interfaces.IExtEngineRuntime;
import info.julang.external.interop.IBinding;
import info.julang.interpretation.errorhandling.JulianScriptException;
import info.julang.memory.MemoryArea;
import info.julang.memory.value.ArrayValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.StringValue;
import info.julang.memory.value.TempValueFactory;
import info.julang.memory.value.ValueUtilities;
import info.julang.modulesystem.IModuleManager;
import info.julang.typesystem.jclass.builtin.JStringType;

/**
 * The simplest implementation of Julian Scripting Engine. Single-thread model; simple context for information sharing.
 * <p/>
 * The engine runs in a REPL mode. For each invocation of {@link #run(ScriptProvider)}, it will retain the state of
 * script engine's runtime, including variables and types. To reset it, call {@link #reset()}. It must be noted that
 * the a class will not be loaded until it is first used. So calling {@link #run(ScriptProvider)} with a script that
 * contains only a class definition will not cause that class to be loaded.
 * 
 * @author Ming Zhou
 */
public class SimpleScriptEngine implements IScriptEngine {

	private State state = State.NOT_STARTED;
	
	private Result result;
	
	private SimpleEngineContext context;
	
	private EngineRuntime runtime;
	
	private ScriptExceptionHandler handler;
	
	private SimpleScriptEngineInstrumentation instru;
	
	private boolean allowReentry;
	
	private boolean firstTime;
	
	private boolean interactiveMode;
	
	private JThread mainThread;
	
	/**
	 * [CFOW] Create a new SimpleScriptEngine.
	 * 
	 * @param runtime if null, the engine will create a default runtime. Nominally takes an {@link IExtEngineRuntime},
	 * but it actually <b>must</b> be an instance of {@link EngineRuntime}.
	 * @param option the option to initialize the engine with.
	 */
	public SimpleScriptEngine(IExtEngineRuntime runtime, EngineInitializationOption option){
		this.allowReentry = option.allowReentry();
		this.firstTime = true;
		this.interactiveMode = option.isInteractiveMode();
		
		this.runtime = (EngineRuntime)runtime;
		initializeInteractiveMode(this.runtime);
		
		if(option.useExceptionDefaultHandler()){
			handler = new DefaultExceptionHandler(true);
		}
	}
	
	/**
	 * [CFOW]
	 */
	public boolean abort() {
		JThread tmp = mainThread;
		if (tmp != null) {
			// Terminate the main thread. The entire engine will abort in the wake of main thread.
			tmp.signalTermination();
			// Ensure that a sleeping thread gets the signal too.
			tmp.signalInterruption();
			return true;
		}
		
		return false;
	}
	
	/**
	 * [CFOW]
	 */
	@Override
	public void runFile(String fileName) throws EngineInvocationError {
		run(FileScriptProvider.create(fileName));
	}
	
	/**
	 * [CFOW]
	 */
	@Override
	public void runSnippet(String script) throws EngineInvocationError {
		run(new StringScriptProvider(script, interactiveMode));
	}
	
	@Override
	public void run(ScriptProvider provider) throws EngineInvocationError {
		if(!firstTime && !allowReentry){
			throw new EngineInvocationError("The engine doesn't support re-entrance.");
		} else {
			firstTime = false;
		}
		
		initializeContext();
		
		Executable exec = null;
		
		try {
			exec = provider.getExecutable(allowReentry);
		} catch (ScriptNotFoundException e) {
			throw new EngineInvocationError("Engine cannot be invoked without script.", e);
		}
		
		EngineRuntime runtime = getRuntime();
		
		runtime.getTypeTable().initialize(runtime);
		
		IModuleManager modManager = runtime.getModuleManager();
		modManager.clearModulePath();
		for(String path : context.modulePaths){
			modManager.addModulePath(path);
		}
		
		// execute the script in blocking mode
		try {
			state = State.RUNNING;
			
			JThreadManager tm = runtime.getThreadManager();
			mainThread = tm.createMain(runtime, exec);
			
			if(instru != null){
				instru.setThreadRuntime(mainThread.getThreadRuntime());
			}
			
			IVariableTable gvt = runtime.getGlobalVariableTable();
			
			// Add bindings
			if(context.bindings != null){	
				MemoryArea heap = runtime.getHeap();
				Set<Entry<String, IBinding>> bindings = context.bindings.entrySet();
				for(Entry<String, IBinding> entry : bindings){
					String name = entry.getKey();
					IBinding binding = entry.getValue();
					JValue val = gvt.getBinding(name);
					if(val == null){
						// Add new binding
						gvt.addBinding(
							name, 
							ValueUtilities.convertFromExtValue(heap, name, binding.toInternal()));
					} else if (binding.isMutable()) {
						// Update existing binding
						JValue newVal = ValueUtilities.convertFromExtValue(heap, name, binding.toInternal());
						newVal.assignTo(val);
						heap.deallocate(newVal);
					} // otherwise, throw an exception?
				}
			}
			
			Argument[] scriptArguments = convertArguments(context.getArguments());
			
			// Clear the result from last run
			result = null;
			
			// Run main thread in blocking mode.
			result = tm.runMain(scriptArguments);
			
			// Update bindings
			if(context.bindings != null){
				Set<Entry<String, IBinding>> bindings = context.bindings.entrySet();
				for(Entry<String, IBinding> entry : bindings){
					String name = entry.getKey();
					IBinding binding = entry.getValue();
					JValue val = gvt.getBinding(name);
					if(val != null && binding.isMutable()) {
						binding.update(val);
					}	
				}
			}
			
			state = mainThread.isFaulted() ? State.FAULTED : State.SUCCESS;
		} catch (JulianScriptException jse) {
			state = State.FAULTED;
			context.exception = jse;
			result = new Result(jse);
			if(handler != null){
				handler.onException(jse); // the handler can decide if it needs to throw.
			}
		} catch (JSEException error) {
			state = State.FAULTED;
			if (error instanceof EngineInvocationError){
				throw (EngineInvocationError) error;
			} else {
				throw new EngineInvocationError("A fatal error occurs when invoking script engine.", error);
			}
		} catch (JSEError error) {
			state = State.FAULTED;
			throw new EngineInvocationError("A fatal error occurs when invoking script engine.", error);
		} catch (Throwable err) {
			state = State.FAULTED;
			throw new EngineInvocationError(
				"A fatal error occurs when invoking script engine and is not handled. This is a bug. Exception: " + err.getMessage(), err);
		} finally {
			mainThread = null;
			
			// Clean up if we are to call this engine again.
			if(allowReentry){
				context.reset();
				if (!interactiveMode){
					modManager.clearModulePath();
				}
			}
		}
	}
	
	@Override
	public void run(ScriptProvider provider, String[] arguments) throws EngineInvocationError {
		initializeContext();
		context.setArguments(arguments);
		
		run(provider);
	}

	@Override
	public EngineContext getContext() {
		initializeContext();
		
		return context;
	}

	@Override
	public State getState() {
		return state;
	}
	
	/**
	 * Might be null if the engine encountered an error.
	 */
	@Override
	public Result getResult() {
		return result;
	}

	@Override
	public void setExceptionHandler(ScriptExceptionHandler hanlder) {
		this.handler = hanlder;
	}
	
	/**
	 * Wipe out all the defined variables, added bindings, as well as loaded types.
	 * <p/>
	 * Note if this method is called, the runtime passed into the constructor may be dropped, and a new runtime
	 * gets created based off of it. To continue using the old runtime from outside would have no effect on the 
	 * engine anymore.
	 */
	@Override
	public void reset() {
		runtime = null;
		mainThread = null;
		runtime = getRuntime();
		state = State.NOT_STARTED;
	}
	
	public EngineRuntime getRuntime() {
		if(runtime == null){
			runtime = SimpleEngineRuntime.createDefault();
			initializeInteractiveMode(runtime);
		}
		
		return runtime;
	}
	
	public void setInstrumentation(SimpleScriptEngineInstrumentation instru){
		this.instru = instru;
	}
	
	/**
	 * Convert context arguments to executable arguments
	 * @param arguments
	 * @param staticArea 
	 * @return
	 */
	private Argument[] convertArguments(String[] arguments) {
		int len = arguments.length;
		ArrayValue array = TempValueFactory.createTemp1DArrayValue(runtime.getTypeTable(), JStringType.getInstance(), len);
		for(int i=0;i<len;i++){
			StringValue sv = TempValueFactory.createTempStringValue(arguments[i]); 
			sv.assignTo(array.getValueAt(i));
		}
		return new Argument[]{new Argument("arguments", array)};
	}
	
	private void initializeContext() {
		if(context == null){
			context = new SimpleEngineContext();
		}
	}
	
	private void initializeInteractiveMode(EngineRuntime runtime){
		// IMPORTANT: if it's interactive mode, the first (outermost) scope must be kept
		// alive. To achieve this we must enter the scope here and ask global script 
		// executable to not enter into new scope in pre-execution phase.
		if (interactiveMode){
			runtime.getGlobalVariableTable().enterScope();
		}
	}
	
	private class SimpleEngineContext implements EngineContext {

		private List<String> modulePaths = new ArrayList<String>();
		
		private Set<String> modulePathSet = new HashSet<String>();
		
		private Map<String, String> envVars;
		
		private Map<String, IBinding> bindings;
		
		private String[] arguments;
		
		private JulianScriptException exception;
		
		@Override
		public IBinding getBinding(String name) {
			return bindings != null ? bindings.get(name) : null;
		}
		
		@Override
		public void addBinding(String name, IBinding binding) {
			if(isMutable()){
				if(bindings == null){
					bindings = new HashMap<String, IBinding>();
				}
				bindings.put(name, binding);
			}
		}

		@Override
		public void setEnviromentVariable(String name, String value) {
			if(isMutable()){
				if(envVars == null){
					envVars = new HashMap<String, String>(); 
				}
				envVars.put(name, value);
			}
		}

		@Override
		public String getEnviromentVariable(String name) {
			return envVars.get(name);
		}

		@Override
		public void setArguments(String[] args) {
			if(isMutable() && args != null){
				arguments = copyArguments(args);
			}
		}

		@Override
		public String[] getArguments() {
			if(arguments==null){
				return new String[0];
			}
			return copyArguments(arguments);
		}
		
		@Override
		public JulianScriptException getException(){
			return exception;
		}
		
		private String[] copyArguments(String[] src){
			String[] dst = new String[src.length];
			for(int i = 0; i<src.length; i++){
				dst[i] = src[i];
			}
			
			return dst;
		}
		
		private boolean isMutable(){
			return SimpleScriptEngine.this.getState() != State.RUNNING;
		}

		@Override
		public void addModulePath(String path) {
			if(!modulePathSet.contains(path)){
				modulePaths.add(path);
				modulePathSet.add(path);
			}
		}
		
		void reset(){
			arguments = null;
			exception = null;
		}
		
	}

}
