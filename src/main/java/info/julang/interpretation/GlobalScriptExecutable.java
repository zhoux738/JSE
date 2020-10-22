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

import info.julang.execution.Argument;
import info.julang.execution.Result;
import info.julang.execution.namespace.NamespacePool;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.execution.threading.ThreadStack;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.statement.StatementOption;
import info.julang.memory.value.JValue;
import info.julang.memory.value.VoidValue;
import info.julang.modulesystem.ModuleInfo;
import info.julang.modulesystem.ModuleManager;
import info.julang.modulesystem.ScriptInfo;
import info.julang.parser.LazyAstInfo;

/**
 * The executable to be invoked in global context.
 * <p>
 * In Julian, global context doesn't only mean the outermost scope in scripts. It is also unique in
 * that its certain rather imperative behaviors. These include: defining global function (which has 
 * access to global variables too); disallowing module definition (the script itself cannot be part
 * of a module); etc.
 * <p>
 * Unless running in interactive mode, there will be one and only one script to be run in global 
 * mode. The user may choose to put all the type definition and logic inside this script; but in 
 * any no-trivial project this file should only serve as an entry point to other module-based 
 * script files.
 * 
 * @author Ming Zhou
 */
public class GlobalScriptExecutable extends InterpretedExecutable {

	private boolean reenterable;
	private boolean interactiveMode;
	
	private LazyAstInfo lainfo;
	
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
		super(null, true, false);
		this.reenterable = reenterable;
		this.interactiveMode = interactiveMode;
		this.lainfo = lainfo;
	}

	@Override
	protected void preExecute(ThreadRuntime runtime, StatementOption option, Argument[] args){
		ThreadStack stack = runtime.getThreadStack();
		
		// If it's interactive mode, do not enter into the first scope. we 
		// trust that the upstream logic has already handled that.
		stack.pushFrame(runtime.getGlobalVariableTable(), this, !interactiveMode); 
		
		option.setAllowClassDef(true);
		option.setAllowFunctionDef(true);
		if (interactiveMode) {
			option.setPreserveStmtResult(true);
		}
		
		// Initialize the namespace pool
		NamespacePool pool = new NamespacePool();
		stack.setNamespacePool(pool);
		
		// Load this script as module
		ModuleManager modManager = (ModuleManager) runtime.getModuleManager();
		ModuleInfo mod = modManager.loadScriptAsModule(runtime, lainfo, ModuleInfo.DEFAULT_MODULE_NAME, interactiveMode);
		ScriptInfo si = mod.getFirstScript();
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
	protected void prepareArguments(Argument[] args, Context ctxt) {
		if (!interactiveMode){
			super.prepareArguments(args, ctxt);
		}
		
		// Do not store variables in the interactive mode.
	}
	
	@Override
	protected void postExecute(ThreadRuntime runtime, Result result){
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
