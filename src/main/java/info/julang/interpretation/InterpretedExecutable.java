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

import java.util.Map.Entry;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;

import info.julang.JSERuntimeException;
import info.julang.execution.Argument;
import info.julang.execution.Executable;
import info.julang.execution.Result;
import info.julang.execution.StandardIO;
import info.julang.execution.namespace.NamespacePool;
import info.julang.execution.symboltable.ITypeTable;
import info.julang.execution.symboltable.IVariableTable;
import info.julang.execution.symboltable.LocalBindingTable;
import info.julang.execution.threading.JThread;
import info.julang.execution.threading.ThreadFrame;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.execution.threading.ThreadStack;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.external.exceptions.JSEError;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.context.FunctionContext;
import info.julang.interpretation.errorhandling.IHasLocationInfo;
import info.julang.interpretation.errorhandling.JSExceptionUtility;
import info.julang.interpretation.errorhandling.JulianScriptException;
import info.julang.interpretation.statement.BlockStatement;
import info.julang.interpretation.statement.ExpressionStatement;
import info.julang.interpretation.statement.IHasResult;
import info.julang.interpretation.statement.StatementOption;
import info.julang.langspec.ast.JulianParser;
import info.julang.langspec.ast.JulianParser.ExecutableContext;
import info.julang.langspec.ast.JulianParser.ExpressionContext;
import info.julang.langspec.ast.JulianParser.Expression_statementContext;
import info.julang.langspec.ast.JulianParser.Method_bodyContext;
import info.julang.memory.MemoryArea;
import info.julang.memory.value.IFuncValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.ValueUtilities;
import info.julang.modulesystem.IModuleManager;
import info.julang.parser.ANTLRHelper;
import info.julang.parser.AstInfo;
import info.julang.typesystem.JType;
import info.julang.typesystem.loading.InternalTypeResolver;

/**
 * The interpreted executable is the core class of the scripting engine. It reads token stream from 
 * a scanner, and interprets the contents as per Julian language specification.
 * <p/>
 * This class roughly corresponds to the sequences involved in a function invocation. At start of
 * its {@link #InterpretedExecutable(TokenScanner, Token[])} method it switches context to a new 
 * one, then initiates a new {@link info.julang.interpretation.statement.BlockStatement 
 * BlockStatement} and interprets it within the new context. At the end, it switches back to the 
 * previous context and returns the result which itself was returned from the block statement.
 * <p/>
 * Since the same executable can be run in different threads, we must use extra caution in order to
 * allow concurrent call to the instance's {@link #execute(ThreadRuntime, Argument[])} method. For 
 * example, only store global and immutable objects as instance member, and make all per-thread 
 * variables as locals within the {@link #execute (ThreadRuntime, Argument[]) execute} method.
 *  
 * @author Ming Zhou
 */
public class InterpretedExecutable implements Executable, IStackFrameInfo {

	/**
	 * The global type table
	 */
	private ITypeTable typTable;
	
	/**
	 * The current type resolver
	 */
	private InternalTypeResolver typResolver;

	/**
	 * The global heap memory
	 */
	private MemoryArea heap;
	
	/**
	 * The global module manager
	 */
	private IModuleManager mm;
	
	/**
	 * The standard IO streams
	 */
	private StandardIO io;
	
	/**
	 * The AST to be interpreted
	 */
	protected AstInfo<? extends ParserRuleContext> ast;
	
	protected boolean isGlobalScript;
	
	protected boolean isFunctionScript;
	
	protected void copyFrom(InterpretedExecutable ie){
		this.ast = ie.ast;
		this.isGlobalScript = ie.isGlobalScript;
		this.isFunctionScript = ie.isFunctionScript;
	}
	
	/**
	 * Create a new interpreted executable.
	 * 
	 * @param ast the AST to interpret. 
	 * @param endingTokens if null, keep interpreting until EOF.
	 * @param isGlobalScript if true, this executable is able to define types.
	 * @param isFunctionScript if true, this executable can return from the middle. This option is incompatible with isGlobalScript.
	 */
	protected InterpretedExecutable(AstInfo<? extends ParserRuleContext> ast, boolean isGlobalScript, boolean isFunctionScript){
		this.ast = ast;
		this.isGlobalScript = isGlobalScript;
		this.isFunctionScript = isFunctionScript;
	}
	
	/**
	 * Execute the script (token stream).
	 * <p>
	 * This executable first switches to a new context within the given thread runtime. At the exit,
	 * it switches the context back. The subclass can override, in the order of calling-back, 
	 * {@link #preExecute()}, {@link #prepareArguments()}, {@link #prepareContext()} and 
	 * {@link #postExecute()} to customize certain behaviors. This method itself is called after 
	 * {@link #prepareContext()} and before {@link #postExecute()}.
	 */
	@Override
	public Result execute(ThreadRuntime runtime, IFuncValue func, Argument[] args) throws EngineInvocationError {
		// IMPORTANT: per-JThread variables must be declared here as local variable instead of field member.	
		ThreadStack stack = runtime.getThreadStack(); 
		                           // The runtime thread stack
		IVariableTable varTable;   // The variable table for current frame.
		MemoryArea frame;          // The memory for current frame.
		NamespacePool namespaces;  // The namespace pool for current frame.
		
		// prepare option
		StatementOption option = new StatementOption();
		option.setAllowReturn(true);
		
		// push frame and initialize runtime fields.
		JSERuntimeException preExeException = null;
		BadSyntaxException bse = null;
		try {
			preExecute(runtime, option, args);
			preExeException = bse = ast != null ? ast.getBadSyntaxException() : null;
		} catch (JSERuntimeException e) {
			preExeException = e;
			if (preExeException instanceof BadSyntaxException){
				bse = (BadSyntaxException)preExeException;
			}
		}
		
		ThreadFrame tframe = stack.currentFrame();
		
		// stack memory
		frame = tframe.getMemory();
		
		// variable table
		varTable = tframe.getVariableTable();
		
		// namespace pool
		namespaces = stack.getNamespacePool();
		
		// heap memory
		heap = runtime.getHeap();
		
		// type table
		typTable = runtime.getTypeTable();
		
		// type resolver
		typResolver = runtime.getTypeResolver();
		
		// module manager
		mm = runtime.getModuleManager();
		
		// standard IO
		io = runtime.getStandardIO();

		// prepare context
		Context ctxt = prepareContext(
			func, frame, heap, varTable, typTable, typResolver, mm, namespaces, io, runtime.getJThread());
		
		try {
			if (preExeException != null) {
				JulianScriptException jse = preExeException.toJSE(runtime, ctxt);
				if (preExeException instanceof IHasLocationInfo){
					JSExceptionUtility.setSourceInfo(jse, (IHasLocationInfo)preExeException);
				} else if (ast != null){
					JSExceptionUtility.setSourceInfo(jse, ast, bse != null ? bse.getLineNumber() : -1);
				}
				
				throw jse != null ? jse : preExeException;
			}
			
			// save arguments into current context.
			prepareArguments(args, ctxt, func);
			
			// call internal execute() to get a result back
			Result res = execute(runtime, ast, option, ctxt);
			postExecute(runtime, res);
			return res;
		} catch (JulianScriptException jse){
			// in case of script exception, re-throw it after normal post-execution process.
			Result res = new Result(jse);
			postExecute(runtime, res);
			JulianScriptException ex = res.getException();
			if (ex != null){
				throw ex;
			} else {
				return res;
			}
		}
	}

	/**
	 * Prepare arguments. Normally just store them into the variable table.
	 * 
	 * @param args
	 * @param varTable
	 */
	protected void prepareArguments(Argument[] args, Context ctxt, IFuncValue func) {
		repliateArgsAndBindings(args, ctxt, func, true);
	}
	
	protected void repliateArgsAndBindings(Argument[] args, Context ctxt, IFuncValue func, boolean addLocalBindings) {
		IVariableTable varTable = ctxt.getVarTable();
		for(Argument arg : args){
			varTable.addVariable(arg.getName(), arg.getValue());
		}
		
		if (addLocalBindings) {
			addLocalBindings(ctxt, func);
		}
	}
	
	protected void addLocalBindings(Context ctxt, IFuncValue func){
		LocalBindingTable lbt = func.getLocalBindings();
		if (lbt != null) {
			for (Entry<String, JValue> entry : lbt.getAll().entrySet()) {
				String key = entry.getKey();
				if (!"this".equals(key)) { // Exclude 'this'. Name resolvers will access to LBT directly.
					IVariableTable varTable = ctxt.getVarTable();
					varTable.addVariable(
						entry.getKey(),
						ValueUtilities.replicateValue(
							entry.getValue(),
							null, // It might be a better idea to also carry the type info in LBT.
							ctxt.getFrame()));
				}
			}
		}
	}
	
	/**
	 * Prepare a execution context.
	 */
	protected Context prepareContext(
		IFuncValue func,
		MemoryArea frame, 
		MemoryArea heap,
		IVariableTable varTable, 
		ITypeTable typTable, 
		InternalTypeResolver typResolver,
		IModuleManager mm,
		NamespacePool namespaces,
		StandardIO io,
		JThread jthread){
		return new FunctionContext(func, frame, heap, varTable, typTable, typResolver, mm, namespaces, io, jthread);
	}
	
	/**
	 * The internal execute call. For {@link InterpretedExecutable} class, start a
	 * new {@link BlockStatement} to interpret.
	 * 
	 * @param runtime
	 * @param ast
	 * @param option
	 * @param ctxt
	 * @return
	 */
	protected Result execute(ThreadRuntime runtime, AstInfo<? extends ParserRuleContext> ast, StatementOption option, Context ctxt){
		ParserRuleContext ec = ast.getAST();
		if (ec != null){ // An empty input can generate null Executable.
			Statement bs = null;
			switch(ec.getRuleIndex()){
			case JulianParser.RULE_executable:
				ExecutableContext exec = (ExecutableContext)ec;
				AstInfo<ExecutableContext> ast0 = ast.create(exec);
				bs = new BlockStatement(runtime, ast0, option);
				break;
			case JulianParser.RULE_method_body:
				Method_bodyContext mbc = (Method_bodyContext)ec;
				exec = mbc.executable();
				AstInfo<ExecutableContext> ast1 = ast.create(exec);
				bs = new BlockStatement(runtime, ast1, option);
				break;
			case JulianParser.RULE_expression_statement:
				Expression_statementContext esc = (Expression_statementContext)ec;
				ExpressionContext exc = esc.expression();
				AstInfo<ExpressionContext> ast2 = ast.create(exc);
				bs = new ExpressionStatement(runtime, ast2);
				break;
			default:
				throw new JSEError("Node cannot be executed: " + ANTLRHelper.getRuleName(ec));
			}
			
			bs.interpret(ctxt);		
			
			// get result and pop frame.
			Result res = ((IHasResult)bs).getResult();
			return res;
		} else {
			return Result.Void;
		}
	}

	/**
	 * Operation sequences performed before invoking {@link #execute(ThreadRuntime, Argument[])}.
	 * 
	 * @param runtime
	 * @param option
	 * @param args
	 */
	protected void preExecute(ThreadRuntime runtime, StatementOption option, Argument[] args){
		ThreadStack stack = runtime.getThreadStack();
		stack.pushFrame(this);
	}
	
	/**
	 * Operation sequences performed at the end of invoking {@link #execute(ThreadRuntime, Argument[])}.
	 * 
	 * @param runtime
	 * @param result
	 */
	protected void postExecute(ThreadRuntime runtime, Result result){
		runtime.getThreadStack().popFrame();
	}
	
	//---------------------------- IStackFrameInfo ----------------------------//

	@Override
	public String getScriptPath() {
		return ast != null ? ast.getFileName() : null;
	}

	@Override
	public JType getContainingType() {
		return null;
	}
	
	@Override
	public boolean isFromLooseScript() {
		return false;
	}
}
