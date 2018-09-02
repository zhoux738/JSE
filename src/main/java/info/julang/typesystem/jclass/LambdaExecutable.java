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

package info.julang.typesystem.jclass;

import org.antlr.v4.runtime.ParserRuleContext;

import info.julang.execution.Argument;
import info.julang.execution.Result;
import info.julang.execution.namespace.NamespacePool;
import info.julang.execution.symboltable.Display;
import info.julang.execution.symboltable.ITypeTable;
import info.julang.execution.symboltable.IVariableTable;
import info.julang.execution.threading.JThread;
import info.julang.execution.threading.JThreadManager;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.interpretation.InterpretedExecutable;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.context.ContextType;
import info.julang.interpretation.context.LambdaContext;
import info.julang.interpretation.context.MethodContext;
import info.julang.interpretation.errorhandling.JSExceptionUtility;
import info.julang.interpretation.statement.StatementOption;
import info.julang.interpretation.syntax.LambdaDeclInfo;
import info.julang.langspec.ast.JulianParser.BlockContext;
import info.julang.langspec.ast.JulianParser.Compound_statementContext;
import info.julang.langspec.ast.JulianParser.ExecutableContext;
import info.julang.langspec.ast.JulianParser.ExpressionContext;
import info.julang.langspec.ast.JulianParser.Expression_statementContext;
import info.julang.langspec.ast.JulianParser.Simple_statementContext;
import info.julang.langspec.ast.JulianParser.StatementContext;
import info.julang.langspec.ast.JulianParser.Statement_listContext;
import info.julang.memory.MemoryArea;
import info.julang.modulesystem.IModuleManager;
import info.julang.parser.ANTLRHelper;
import info.julang.parser.AstInfo;
import info.julang.typesystem.JType;
import info.julang.typesystem.loading.InternalTypeResolver;

/**
 * The executable for lambda invocation.
 * <p/>
 * The lambda executable has two distinct features:
 * <li>The namespace pool is inherited from where it is defined.</li>
 * <li>Has a referential environment, a.k.a. display, replicated from the defining site.</li>
 * <br/>
 * @author Ming Zhou
 */
public class LambdaExecutable extends InterpretedExecutable {

	private Display display;
	
	private LambdaDeclInfo.LambdaType ltyp;
	
	private NamespacePool nsPool;
	
	private ICompoundType containingType;
	
	private ContextType definingContextType;
	
	// AST-based initialization
	public LambdaExecutable(Context context, Display display, LambdaDeclInfo declInfo) {
		super(null, false, true);
		this.nsPool = context.getNamespacePool();
		this.display = display;
		this.ltyp = declInfo.getLambdaType();
		this.definingContextType = context.getContextType();
		switch(definingContextType){
		case SMETHOD:
        case IMETHOD:
			MethodContext mc = (MethodContext) context;
			containingType = mc.getContainingType();
			break;
		case LAMBDA:
			LambdaContext lc = (LambdaContext) context;
			containingType = lc.getContainingType();
			this.definingContextType = lc.getDefiningContextType();
			break;			
		default:
			break;
		}
		
		Statement_listContext stmts = null;
		ExecutableContext ec = new ExecutableContext(null, 0); 
		if (ltyp == LambdaDeclInfo.LambdaType.BLOCK){
			AstInfo<BlockContext> bc = declInfo.getBlockContext();
			ast = bc.create(ec);
			
			stmts = bc.getAST().statement_list();
		} else {
			// Reconstruct an Executable so that the base class can execute it using a BlockStatement.
			
			//statement_list 
			//    : statement+
			//    ;
			//  
			//statement 
			//    : compound_statement
			//    ;
			//  
			//compound_statement 
			//    : simple_statement
			//    ;
			//  
			//simple_statement 
			//    : expression_statement
		    
			AstInfo<ExpressionContext> _ec = declInfo.getExpressionContext();
			ast = _ec.create(ec);
			
			stmts = ANTLRHelper.synthesizeDegenerateAST(
				_ec.getAST(), 
				Statement_listContext.class, 
				StatementContext.class, Compound_statementContext.class, Simple_statementContext.class, Expression_statementContext.class);
		}
		
		if (stmts != null){
			ec.addChild(stmts);
			ec.start = stmts.start;
		}
	}

	@Override
	protected void preExecute(ThreadRuntime runtime, StatementOption option, Argument[] args){
		super.preExecute(runtime, option, args);
		
		runtime.getThreadStack().setNamespacePool(nsPool);
	}
	
	/**
	 * If it's a block, call {@link InterpretedExecutable#execute(ThreadRuntime, Argument[]) 
	 * InterpretedExecutable}'s {@link InterpretedExecutable#execute(ThreadRuntime, Argument[]) 
	 * execute()} method; if it is an expression, start a new {@link LambdaSingleStatement} to 
	 * interpret and return; if it is a throw statement, evaluate the expression and throw the
	 * result.
	 */
	@Override
	protected Result execute(ThreadRuntime runtime, AstInfo<? extends ParserRuleContext> ainfo, StatementOption option, Context ctxt){
		option.setPreserveStmtResult(ltyp != LambdaDeclInfo.LambdaType.BLOCK);
		Result res = super.execute(runtime, ainfo, option, ctxt);
		if (ltyp == LambdaDeclInfo.LambdaType.THROW){
			throw JSExceptionUtility.initializeAsScriptException(ctxt, res.getReturnedValue(false), ainfo);
		} else {
			return res;
		}
	}
	
	@Override
	protected Context prepareContext(
		MemoryArea frame,
		MemoryArea heap,
		IVariableTable varTable, 
		ITypeTable typTable, 
		InternalTypeResolver typResolver,
		IModuleManager mm,
		NamespacePool namespaces, 
		JThreadManager tm,
		JThread jthread){
		return new LambdaContext(
			frame, heap, varTable, typTable, typResolver, mm, namespaces, tm, jthread,
			display, definingContextType, containingType);
	}
	
	//---------------------------- IStackFrameInfo ----------------------------//

	@Override
	public JType getContainingType() {
		return this.containingType;
	}
	
	@Override
	public boolean isFromLooseScript() {
		return this.containingType == null;
	}
}
