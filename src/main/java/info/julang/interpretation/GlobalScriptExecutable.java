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

package info.julang.interpretation;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.antlr.v4.runtime.ParserRuleContext;

import info.julang.JSERuntimeException;
import info.julang.execution.Argument;
import info.julang.execution.Result;
import info.julang.execution.namespace.NamespacePool;
import info.julang.execution.symboltable.ITypeTable;
import info.julang.execution.symboltable.IVariableTable;
import info.julang.execution.symboltable.IVariableTableTraverser;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.execution.threading.ThreadStack;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.external.exceptions.JSEError;
import info.julang.external.exceptions.JSEException;
import info.julang.external.interfaces.IExtVariableTable;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.errorhandling.JSExceptionUtility;
import info.julang.interpretation.errorhandling.JulianScriptException;
import info.julang.interpretation.errorhandling.StackTraceKind;
import info.julang.interpretation.statement.StatementOption;
import info.julang.langspec.ast.JulianParser.Include_statementContext;
import info.julang.memory.value.IFuncValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.VoidValue;
import info.julang.modulesystem.GlobalScriptRunner;
import info.julang.modulesystem.IncludedFile;
import info.julang.modulesystem.ModuleInfo;
import info.julang.modulesystem.ModuleManager;
import info.julang.modulesystem.ScriptInfo;
import info.julang.modulesystem.ScriptModuleLoadingMode;
import info.julang.parser.AstInfo;
import info.julang.parser.LazyAstInfo;
import info.julang.util.Pair;

/**
 * The executable to be invoked in global context.
 * <p>
 * In Julian, global context doesn't only mean the outermost scope in scripts. It is also unique in
 * some its distinctive features and restrictions. These include: defining global function (which has 
 * access to global variables too); disallowing module definition (the script itself cannot be part
 * of a module); etc.
 * <p>
 * In two cases there will be more than one script to be run in global mode throughout the same engine
 * runtime session. (1) engine runs in interactive mode; (2) using <code><b>include</b></code>, or its underlying
 * <code><span style="color:green">Environment.evaluate</span>()</code> method.
 * <p>
 * In general, the user may choose to put all the type definition and logic inside this script; but in 
 * any no-trivial project this file should only serve as an entry point to other module-based 
 * script files. Massive usage of <code><b>include</b></code> is not recommended.
 * 
 * @author Ming Zhou
 */
public class GlobalScriptExecutable extends InterpretedExecutable {

	private boolean reenterable;
	private boolean interactiveMode;
	
	private LazyAstInfo lainfo;
	private List<IncludedFile> includes;
	private Argument[] args;
	
	private IVariableTable prevGvt; // the GVT from the previous global script.
	private ScriptModuleLoadingMode smMode; // interactiveMode trumps this value.
	private boolean shareScope; // used only if prevGvt is set.
	private ScriptInfo prevScriptInfo; // temporary storage across the callbacks.
	
	/**
	 * Create a new {@link GlobalScriptExecutable}.
	 * <p>
	 * During {@link #preExecute()}, pushes a new frame with global variable table.
	 * Then use module manager to synthesize a module using only the given script.
	 * The first (and the only) script of this module will then be executed. 
	 * <p>
	 * If calling this executable with the intention of running only once, specify 
	 * <code>reenterable</code> as true. If reentry is expected, use false. During 
	 * {@link #postExecute()}, if the executable is reentrant, the frame pushed
	 * at the beginning will be popped out, otherwise it would be staying there. 
	 * This feature is mainly intended for pose-test inspection. 
	 * <p>
	 * @param lainfo
	 * @param reenterable 
	 * @param interactiveMode if true, print the result of last statement.
	 */
	public GlobalScriptExecutable(LazyAstInfo lainfo, boolean reenterable, boolean interactiveMode) {
		super(null, null, true, false);
		this.reenterable = reenterable;
		this.interactiveMode = interactiveMode;
		this.lainfo = lainfo;
		this.smMode = ScriptModuleLoadingMode.InitialGlobalScript;
	}
	
	/**
	 * Call this with a non-null global variable table so that the script will, upon entering the first scope,
	 * copy over all of the global variables and bindings.
	 * <p>
	 * If this is called, whether or not a GVT be set, the script will be loaded as an addition to the existing
	 * default module, which was created when the current engine session started.
	 * 
	 * @param gvt If null, won't have any effect.
	 */
	public void setEvaluateMode(IVariableTable gvt, boolean shareScope) {
		this.prevGvt = gvt;
		this.smMode = ScriptModuleLoadingMode.AccumulativeGlobalScript;
		this.shareScope = shareScope;
	}

	@Override
	protected void preExecute(ThreadRuntime runtime, StatementOption option, Argument[] args){
		ThreadStack stack = runtime.getThreadStack();
		
		// If it's interactive mode, do not enter into the first scope. we 
		// trust that the upstream logic has already handled that.
		IVariableTable gvt = runtime.getGlobalVariableTable();
		stack.pushFrame(gvt, this, !interactiveMode);
		
		// If there exists a previous global variable table, copy over all global variables, 
		// which are stored in the first scope, as well as the external bindings.
		if (prevGvt != null) {
			ITypeTable tt = runtime.getTypeTable();
			
			prevGvt.traverse(new IVariableTableTraverser() {
				@Override
				public boolean processScope(int level, Map<String, JValue> scope) {
					if (level == 0) {
						for (Entry<String, JValue> entry : scope.entrySet()) {
							String name = entry.getKey();
							if (!IExtVariableTable.KnownVariableName_Arguments.equals(name)) {
								// Do not copy "arguments", as we are about to create one
								// with this exact name in prepareArguments().
								
								if (shareScope) {
									gvt.addVariable(name, entry.getValue());
								} else {
									// Copy function variables only
									GlobalScriptRunner.addFunctionVariable(gvt, tt, name, entry.getValue());
								}
							}
						}
						
						return true;
					}
					
					return false;
				}
			},
			false);
			
			if (shareScope) {
				for (Pair<String, JValue> p : prevGvt.getAllBindings()) {
					gvt.addBinding(p.getFirst(), p.getSecond());
				}
			}
		}
		
		option.setAllowClassDef(true);
		option.setAllowFunctionDef(true);
		if (interactiveMode) {
			option.setPreserveStmtResult(true);
		}
		
		// Initialize the namespace pool
		NamespacePool pool = new NamespacePool();
		stack.setNamespacePool(pool);
		
		// Abort if the given script doesn't even parse
		JSERuntimeException synex = lainfo.getBadSyntaxException();
		if (synex != null) {
			ast = lainfo;
			return;
		}
		
		// Load this script as module
		ModuleManager modManager = (ModuleManager) runtime.getModuleManager();
		prevScriptInfo = modManager.getScriptInfoForDefaultModule();
		ModuleInfo mod;
		try {
			mod = modManager.loadScriptAsModule(
				runtime, 
				lainfo,
				interactiveMode ? ScriptModuleLoadingMode.SubstitutiveGlobalScript : smMode);
		} catch (JSERuntimeException e) {
			// Set AST for location info
			ast = lainfo;
			throw e;
		}

		ScriptInfo si = mod.getFirstScript();
		includes = si.getIncludedFiles();
		pool.addNamespaceFromScriptInfo(si);
		
		// Set AST for executable
		LazyAstInfo prc = si.getAstInfo();
		if (prc.getBadSyntaxException() == null){
			ast = prc.create(prc.getAST().executable());
		} else {
			ast = prc.create(null);
		}
	}
	
	@Override
	protected void prepareArguments(Argument[] args, Context ctxt, IFuncValue func) {
		this.args = args;
		
		if (!interactiveMode){
			super.replicateArgsAndBindings(args, ctxt, func, false);
		}
		
		// Do not store variables in the interactive mode.
	}
	
	@Override	
	protected Result execute(
		ThreadRuntime runtime, AstInfo<? extends ParserRuleContext> ast, StatementOption option, Context ctxt)
		throws EngineInvocationError {
		
		if (includes != null) {
			for (IncludedFile inf : includes) {
				// Evaluate each script in the source order.
				GlobalScriptRunner.Options opts = GlobalScriptRunner.Options.fromInclude(inf.getResultionStrategy());
				GlobalScriptRunner runner = new GlobalScriptRunner(runtime, inf.getFullPath());
				try {
					runner.run(opts, this.args);
				} catch (JSERuntimeException jse) {
					// This happens when we throw outside the interpretation of the script. For example, 
					// GlobalScriptRunner throws various IOExceptions and IllegalModuleExceptions during
					// the preparation stage.
					// 
					// Capture JSE (step 1/2):
					JulianScriptException enEx = jse.toJSE(runtime, ctxt);
					AstInfo<Include_statementContext> ainfo = inf.getAstInfo();
					JSExceptionUtility.setSourceInfo(enEx, ainfo, ainfo.getLineNumber());
					throw enEx;
				} catch (JulianScriptException jse) {
					/* IMPLEMENTATION NOTES
					 This section properly sets exception info during the script interpretation.
					 
					 As an example, let's say we have a script main.jul that includes dep.jul:
					 
					 -- main.jul-------
					 1: // empty line
					 2: include "dep.jul";
					 
				     -- dep.jul-------
					 1: // empty line
					 2: // empty line
                     3: return 5 / 0;			 
					 
					 Apparently at line 3 of dep.jul a DivideByZeroException will be thrown by the engine.
					 
					 At the time the attempt to divide by zero occurs, information for the textual location, dep.jul:3, is saved into the stack trace.
					 
					 Here, information for the semantic context, namely the include statement, is added to the stack trace. With both semantics and
					 location info ready, a new entry is added to the trace:
					 +---------------------------------------------------+
					 |   on including  (.../dep.jul, 3)                  |
					   
					 Right after this, information for the textual location where the include happens, main.jul:2, is saved into the stack trace.
					 
					 Later on, when the engine is about to exit it will add one more entry to the trace.
					 +---------------------------------------------------+
					 |   from  (.../main.jul, 2)                         |
					 
					 So in the end we have:
					 +---------------------------------------------------+
					 | System.DivByZeroException: Cannot divide by zero. |
                     |   on including  (.../dep.jul, 3)                  |
                     |   from  (.../main.jul, 2)                         |
					 */
					
					// Capture JSE (step 2/2):
					// Add a stack trace into the exception for what happened as result of the include statement.
					String fn = jse.getFileName();
					int lineNo = jse.getLineNumber();
					StackTraceKind stk = jse.resetTraceKind();
					if (stk != StackTraceKind.INCLUDE) {
						throw new JSEError(
							"An exception thrown when including a file has unexpected trace kind " + stk.name(), GlobalScriptExecutable.class);
					}
					jse.addStackTrace(runtime.getTypeTable(), stk.getTraceName(), null, fn, lineNo);
					
					// Set the new location info pointing whether the include statement occurs.
					AstInfo<Include_statementContext> ainfo = inf.getAstInfo();
					JSExceptionUtility.setSourceInfo(jse, ainfo, ainfo.getLineNumber());
					throw jse;
				}
			}
		}
		
		return super.execute(runtime, ast, option, ctxt);
	}
	
	@Override
	protected void postExecute(ThreadRuntime runtime, Result result) {
		// Reset the script info
		if (prevScriptInfo != null) {
			ModuleManager modManager = (ModuleManager) runtime.getModuleManager();
			modManager.setScriptInfoForDefaultModule(prevScriptInfo);
		}
		
		// Print the result in interactive console
		if (interactiveMode) {
			JValue val = result.getReturnedValue(true);
			if (val != null && val != VoidValue.DEFAULT){
				try {
					ConsoleWrapper cw = new ConsoleWrapper(runtime);
					cw.println(val);
				} catch (Exception e) {
					// Since a user can cause error in toString() callback, must keep running.
					// Using System.err is not ideal, but should be fine for now.
					System.err.println("Failed to print result from last statement. Error: " + e.getMessage());
				}
			}
		}

		// don't pop the first frame, unless we can re-enter (and thus pushing a new frame - see preExecute)
		if (reenterable) {
			runtime.getThreadStack().popFrame();
		}
	}
	
	//---------------------------- IStackFrameInfo ----------------------------//

	@Override
	public String getScriptPath() {
		return lainfo != null ? lainfo.getFileName() : null;
	}
	
	@Override
	public boolean isFromLooseScript() {
		return true;
	}
}
