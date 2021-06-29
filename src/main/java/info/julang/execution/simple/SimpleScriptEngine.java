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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import info.julang.JSERuntimeException;
import info.julang.execution.Argument;
import info.julang.execution.ArgumentUtil;
import info.julang.execution.EngineContext;
import info.julang.execution.EngineRuntime;
import info.julang.execution.FileScriptProvider;
import info.julang.execution.IScriptEngine;
import info.julang.execution.Result;
import info.julang.execution.ScriptExceptionHandler;
import info.julang.execution.ScriptProvider;
import info.julang.execution.StandardIO;
import info.julang.execution.State;
import info.julang.execution.StringScriptProvider;
import info.julang.execution.security.EngineLimit;
import info.julang.execution.security.EnginePolicyEnforcer;
import info.julang.execution.security.IEnginePolicy;
import info.julang.execution.symboltable.IVariableTable;
import info.julang.execution.symboltable.TypeTable;
import info.julang.execution.threading.JThread;
import info.julang.execution.threading.JThreadManager;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.external.EngineInitializationOption;
import info.julang.external.binding.BindingKind;
import info.julang.external.binding.IBinding;
import info.julang.external.binding.ObjectBinding;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.external.exceptions.ExternalBindingException;
import info.julang.external.exceptions.JSEError;
import info.julang.external.exceptions.JSEException;
import info.julang.external.exceptions.ScriptNotFoundException;
import info.julang.external.interfaces.IExtEngineRuntime;
import info.julang.external.interfaces.ResetPolicy;
import info.julang.hosting.HostedMethodManager;
import info.julang.hosting.mapped.implicit.ImplicitPlatformTypeMapper;
import info.julang.hosting.mapped.implicit.ObjectBindingGroup;
import info.julang.interpretation.InterpretedExecutable;
import info.julang.interpretation.errorhandling.JulianScriptException;
import info.julang.memory.MemoryArea;
import info.julang.memory.value.JValue;
import info.julang.memory.value.ValueUtilities;
import info.julang.modulesystem.IModuleManager;
import info.julang.modulesystem.ModuleInfo;
import info.julang.modulesystem.ModuleManager;
import info.julang.modulesystem.ScriptModuleLoadingMode;
import info.julang.parser.ANTLRParser;
import info.julang.parser.LazyAstInfo;
import info.julang.typesystem.loading.ClassLoadingException;
import info.julang.util.Pair;

/**
 * The simplest implementation of Julian Scripting Engine. Single-thread model; simple context for information sharing.
 * <p>
 * The engine runs in a REPL mode. For each invocation of {@link #run(ScriptProvider)}, it will retain the state of
 * script engine's runtime, including variables and types. To reset it, call {@link #reset(ResetPolicy)}. It must be noted that
 * the a class will not be loaded until it is first used. So calling {@link #run(ScriptProvider)} with a script that
 * contains only a class definition will not cause that class to be loaded.
 * 
 * @author Ming Zhou
 */
public class SimpleScriptEngine implements IScriptEngine {

	private State state = State.NOT_STARTED;
	
	private Result result;
	
	private SimpleEngineContext context;
	
	private SimpleEngineRuntime runtime;
	
	private ScriptExceptionHandler handler;
	
	private SimpleScriptEngineInstrumentation instru;
	
	private boolean allowReentry;
	
	private boolean firstTime;
	
	private boolean interactiveMode;
	
	private boolean useDefExHandler;
	
	private boolean clearUserDefinedTypesOnReentry;
	
	private boolean clearUserBindingsOnExit;
	
	private JThread mainThread;
	
	private boolean policyUpdated;
	
	private Map<EngineLimit, Integer> limits;
	
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
		
		this.runtime = (SimpleEngineRuntime)runtime;
		initializeInteractiveMode(this.runtime);
		
		this.useDefExHandler = option.shouldUseExceptionDefaultHandler();
		this.clearUserDefinedTypesOnReentry = allowReentry && option.shouldClearUserDefinedTypesOnReentry();
		this.clearUserBindingsOnExit = allowReentry && option.shouldClearUserBindingsOnExit();
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
		
		InterpretedExecutable exec = null;
		
		try {
			exec = provider.getExecutable(allowReentry);
		} catch (ScriptNotFoundException e) {
			throw new EngineInvocationError("Engine cannot be invoked without script.", e);
		}
		
		EngineRuntime runtime = getRuntime();
		IModuleManager modManager = runtime.getModuleManager();
		
		boolean inited = runtime.getTypeTable().initialize(runtime);
		if (!firstTime && !inited && clearUserDefinedTypesOnReentry) {
			// The engine is being re-entered without resetting.
			resetUserDefinedTypes(modManager);
		}
		
		// Set module paths
		modManager.clearExecutionData();
		String defaultModPath = provider.getDefaultModulePath();
		if (defaultModPath != null) {
			// The default module path, if available, must appear first.
			modManager.addModulePath(defaultModPath);
		}
		for(String path : context.modulePaths){
			modManager.addModulePath(path);
		}
		
		// Configure platform access
		if (policyUpdated) {
			modManager.resetPlatformAccess();
			if (context.policies != null) {
				for (PolicyConfig pc : context.policies) {
					modManager.setPlatformAccess(
						pc.allowOrDeny, pc.category, pc.operations);
				}
			}
			
			// Set limits
			if (limits != null && limits.size() > 0) {
				for (Entry<EngineLimit, Integer> lm : limits.entrySet()) {
					modManager.setEngineLimit(lm.getKey(), lm.getValue());
				}
			}
			
			policyUpdated = false;
		} else {
			// Reset stateful policies.
			EnginePolicyEnforcer enforcer = modManager.getEnginePolicyEnforcer();
			enforcer.resetLimits();
		}
		
		// Execute the script in blocking mode
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
				List<Pair<String, ObjectBinding>> objBindings = null;
				for(Entry<String, IBinding> entry : bindings){
					String name = entry.getKey();
					IBinding binding = entry.getValue();
					JValue val = gvt.getBinding(name);
					if(val == null){
						if (binding.getKind() == BindingKind.Object) {
							if (objBindings == null) {
								objBindings = new ArrayList<>();
							}
							
							objBindings.add(new Pair<String, ObjectBinding>(name, (ObjectBinding)binding));
						} else {
							// Add new binding
							gvt.addBinding(
								name, 
								ValueUtilities.convertFromExtValue(heap, name, binding.toInternal()));
						}
					} else if (binding.isMutable()) {
						// Update existing binding
						JValue newVal = ValueUtilities.convertFromExtValue(heap, name, binding.toInternal());
						newVal.assignTo(val);
						heap.deallocate(newVal);
					} // otherwise, throw an exception?
				}
				
				if (objBindings != null) {
					addObjectBindings(mainThread.getThreadRuntime(), objBindings);
				}
			}
			
			// Bindings from the last run is no more use.
			if (clearUserBindingsOnExit) {
				context.detachedBindings = null;
			}
			
			Argument[] scriptArguments = ArgumentUtil.convertArguments(runtime.getTypeTable(), context.getArguments());
			
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
			if (handler == null && useDefExHandler) {
				handler = new DefaultExceptionHandler(this.runtime.getStandardIO(), true);
			}
			
			if (handler != null) {
				handler.onException(jse); // the handler can decide if it needs to throw.
			}

			// If not using default handler, but no other handler is specified either, then 
			// fail silently. This is by design.
		} catch (JSERuntimeException error) {
			state = State.FAULTED;
			throw new EngineInvocationError("A fatal error occurs when invoking script engine.", error);
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
			if (allowReentry) {
				context.reset();
				if (!interactiveMode) {
					modManager.clearExecutionData();
				}
				
				if (clearUserBindingsOnExit) {
					context.detachBindings();
				}
			}
		}
	}
	
	private void resetUserDefinedTypes(IModuleManager modManager) {
		TypeTable tt = (TypeTable)runtime.getTypeTable();
		tt.clearAllUserDefinedTypes();
		
		ModuleManager mm = (ModuleManager)modManager;
		mm.clearAllUserDefinedModules();
	}
	
	// Translate bound objects to mapped types and create objects with the corresponding type.
	private void addObjectBindings(
		ThreadRuntime rt, List<Pair<String, ObjectBinding>> objBindings) throws ExternalBindingException {
		// 1. Collect all types.
		ObjectBindingGroup grp = new ObjectBindingGroup();
		for (Pair<String, ObjectBinding> kvp : objBindings) {
			grp.add(kvp.getFirst(), kvp.getSecond());
		}
		
		// 2. Synthesize a script that can trigger type mapping and loading.
		String script = grp.getLoadingScript();
		ANTLRParser parser = ANTLRParser.createMemoryParser(script);
		LazyAstInfo ainfo = parser.scan(true);
		
		// 3. Rig the mapper inside Hosted Method Manager. In the following type mapping process 
		// the engine must use our special mapper instead of the default implementation that 
		// loads classes using the engine loader and is detrimental to our cause.
		ModuleManager mm = (ModuleManager)rt.getModuleManager();
		HostedMethodManager hmm = mm.getHostedMethodManager();
		ImplicitPlatformTypeMapper mapper = new ImplicitPlatformTypeMapper(grp);
		hmm.setPlatformTypeMapper(mapper);
		
		// 4. Map all types.
		try {
			mm.loadScriptAsModule(rt, ainfo, ScriptModuleLoadingMode.ImplicitModuleScript);
			
			// 5. Create values wrapping the raw objects.
			List<Pair<String, JValue>> bds = grp.getBindingValues(rt);
			
			// 6. Bind them into the global variable table.
			IVariableTable gvt = runtime.getGlobalVariableTable();
			for (Pair<String, JValue> bd : bds) {
				gvt.addBinding(bd.getFirst(), bd.getSecond());
			}
		} catch (ClassLoadingException ex) {
			JSERuntimeException jse = ex.getJSECause();
			if (jse != null) {
				throw jse;
			} else {
				throw ex;
			}
		} finally {
			// Reset the mapper. All in-runtime mapping must use the default implementation.
			hmm.reset();
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
	public void setExceptionHandler(ScriptExceptionHandler handler) {
		this.handler = handler;
	}
	
	/**
	 * Wipe out all the defined variables and loaded types.
	 * <p>
	 * Note if this method is called, the runtime passed into the constructor may be dropped, and a new runtime
	 * gets created based off of it. To continue using the old runtime from outside would have no effect on the 
	 * engine anymore.
	 */
	@Override
	public void reset(ResetPolicy pol) {
		if (pol == ResetPolicy.FULL || runtime == null) {
			runtime = null;
			mainThread = null;
			runtime = getRuntime();
		} else { // ResetPolicy.USER_DEFINED_ONLY
			// variables
			runtime.getGlobalVariableTable().clear();
			
			// types
			IModuleManager modManager = runtime.getModuleManager();
			resetUserDefinedTypes(modManager);
		}
		
		state = State.NOT_STARTED;
	}
	
	/**
	 * [CFOW]
	 */
	@Override
	public void setLimit(String name, int value) {
		EngineLimit lim = EngineLimit.fromString(name);
		if (lim != null) {
			if (limits == null) {
				limits = new HashMap<>();
			}
			
			limits.put(lim, value);
		}
		
		this.policyUpdated = true;
	}
	
	/**
	 * [CFOW]
	 */
	@Override
	public void setRedirection(OutputStream os, OutputStream err, InputStream is) {
		SimpleEngineRuntime ert = getRuntime();
		ert.setStandardIO(new StandardIO(is, os, err));
	}
	
	public SimpleEngineRuntime getRuntime() {
		if(runtime == null){
			runtime = SimpleEngineRuntime.createDefault();
			initializeInteractiveMode(runtime);
		}
		
		return runtime;
	}
	
	public void setInstrumentation(SimpleScriptEngineInstrumentation instru){
		this.instru = instru;
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
		
		private Map<String, IBinding> bindings;
		
		private Map<String, IBinding> detachedBindings;
		
		private String[] arguments;
		
		private JulianScriptException exception;
		
		private List<PolicyConfig> policies;
		
		private void detachBindings() {
			detachedBindings = bindings;
			bindings = null;
		}
		
		@Override
		public IBinding getBinding(String name) {
			// Try current bindings first.
			IBinding binding = bindings != null ? bindings.get(name) : null;
			if (binding != null) {
				return binding;
			}
			
			// If not found, try detached bindings.
			if (detachedBindings != null) {
				return detachedBindings.get(name);
			} else {
				return null;
			}
		}
		
		@Override
		public void addBinding(String name, IBinding binding) {
			if(isMutable()){
				if (bindings == null) {
					bindings = new HashMap<String, IBinding>();
				}
				
				bindings.put(name, binding);
				
				if (detachedBindings != null) {
					detachedBindings.remove(name);
				}
			}
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
		
		@Override
		public void addPolicy(boolean allowOrDeny, String category, String[] operations) {
			if (policies == null) {
				policies = new ArrayList<>();
			}
			
			PolicyConfig conf = new PolicyConfig();
			conf.allowOrDeny = allowOrDeny;
			conf.category = category.trim();
			conf.operations = operations;
			
			if (IEnginePolicy.WILDCARD.equals(conf.category)) {
				policies.clear();
			}
			
			policies.add(conf);
			policyUpdated = true;
		}
	}
	
	class PolicyConfig {
		boolean allowOrDeny;
		String category;
		String[] operations;
	}
}
