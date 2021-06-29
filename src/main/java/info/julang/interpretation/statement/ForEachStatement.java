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

package info.julang.interpretation.statement;

import org.antlr.v4.runtime.ParserRuleContext;

import info.julang.execution.threading.ThreadRuntime;
import info.julang.interpretation.JNullReferenceException;
import info.julang.interpretation.RuntimeCheckException;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.errorhandling.JSExceptionFactory;
import info.julang.interpretation.errorhandling.JSExceptionUtility;
import info.julang.interpretation.errorhandling.JulianScriptException;
import info.julang.interpretation.errorhandling.KnownJSException;
import info.julang.interpretation.internal.NewVarExecutor;
import info.julang.interpretation.syntax.ParsedTypeName;
import info.julang.interpretation.syntax.SyntaxHelper;
import info.julang.langspec.ast.JulianParser.Compound_statementContext;
import info.julang.langspec.ast.JulianParser.ExpressionContext;
import info.julang.langspec.ast.JulianParser.Foreach_statement_headContext;
import info.julang.langspec.ast.JulianParser.TypeContext;
import info.julang.memory.value.JValue;
import info.julang.memory.value.RefValue;
import info.julang.memory.value.iterable.IIterator;
import info.julang.memory.value.operable.InitArgs;
import info.julang.parser.AstInfo;

/**
 * The foreach statement in Julian language.
 * <p>
 * The foreach statement is comprised of multiple parts:<pre><code> for(|type| |name| : |iterable-expr|)
 *   expression</code></pre>or<pre><code> for(|type| |name| : |iterable-expr|){
 *   foreach-body
 * } </code></pre>
 * The head that contains <code>|type|</code>, <code>|name|</code> and <code>|iterable-expr|</code> 
 * is required; body is not. <code>|iterable-expr|</code> is an expression that must evaluate to
 * an iterable object, such as Array, <code style="color:green">System.Collection.List</code>, or a user-defined
 * class that implements either <code style="color:green">System.Util.IIterable</code> or 
 * <code style="color:green">System.Util.IIterator</code>.
 * <p>
 * For certain iterable objects, such as <code style="color:green">System.Collection.List</code>, a write-lock
 * will be applied by this statement. If another thread is trying to modify the same object in the same
 * time, an exception of <code style="color:green">System.Collection.ConcurrentModificationException</code>
 * will be thrown.
 * <p>
 * 
 * @author Ming Zhou
 */
public class ForEachStatement extends MultiBlockStatementBase {

	/**
	 * Type: For_statementContext or Foreach_statementContext
	 */
	private AstInfo<? extends ParserRuleContext> ainfo;
	
	private ParsedTypeName varTypName;
	private String varId;
	private IIterator iter;
	private JValue loopVar;
	private Foreach_statement_headContext head;
	private Compound_statementContext compound;
	
	/**
	 * Create a new ForEachStatement. The stream PC must be pointing at right after ':'.
	 * 
	 * @param runtime
	 * @param stream
	 * @param option
	 * @param varTypName
	 * @param varId
	 */
	public ForEachStatement(
		ThreadRuntime runtime, 
		AstInfo<? extends ParserRuleContext> ainfo,
		StatementOption option, 
		Foreach_statement_headContext head,
		Compound_statementContext compound) {
		super(runtime, option);
		this.ainfo = ainfo;
		initialize(head, compound);
	}
	
	private void initialize(Foreach_statement_headContext head, Compound_statementContext compound){
		//	foreach_statement_head
		//		: type IDENTIFIER ( IN | COLON ) expression
		//		;
		//		
		//	foreach_statement
		//		: FOREACH LEFT_PAREN foreach_statement_head RIGHT_PAREN compound_statement
		//		;
		TypeContext tc = head.type();
		this.varTypName = SyntaxHelper.parseTypeName(tc);
		this.varId = head.IDENTIFIER().getText();
		this.head = head;
		this.compound = compound;
	}
	
	@Override
	public void interpret(Context context) {		
		try {
			// enter for scope
			context.getVarTable().enterScope();
			interpretForEach(context);
			context.getVarTable().exitScope();
		} catch (JulianScriptException jse){
			context.getVarTable().exitScope();
			throw jse;
		}
	}
	
	private void interpretForEach(Context context) {
		// 1) get iterable object
		AstInfo<ExpressionContext> ahead = ainfo.create(head.expression());
		ExpressionStatement es = new ExpressionStatement(runtime, ahead);
		es.interpret(context);
		JValue iVal = es.getResult().getReturnedValue(false);
		
		try {
			iter = RefValue.dereference(iVal).asIterator();
		} catch (JNullReferenceException e) {
			JulianScriptException jse = JSExceptionFactory.createException(
				KnownJSException.NullReference, runtime, context);
			// This is somewhat special. Built-in exceptions are typically thrown by 
			// evaluating ExpressionStstements. But we are demanding a value in this 
			// for-each statement.
			JSExceptionUtility.setSourceInfo(jse, ainfo, head.getStart().getLine());
			throw jse;
		}
		
		// 2) Fail if the object is not iterable.
		if (iter == null) {
			throw new RuntimeCheckException("Cannot perform iteration over a non-iterable object.");
		}
		
		try {
			iter.initialize(this.runtime, new InitArgs(context, true)); //applyAccLock
			
			// 3) create loop variable
			NewVarExecutor nve = new NewVarExecutor();
			loopVar = nve.newVar(context, varId, varTypName);
			
			performLoop(context, compound);
		} finally {
			iter.dispose();
		}
	}
	
	private void performLoop(Context context, Compound_statementContext compStmts) {
		while(iter.hasNext()){
			// get current element
			JValue ele = iter.next();
			ele.assignTo(loopVar);
			
			// main loop body
			boolean shouldBreak = performLoopBody(context, ainfo.create(compStmts));
			if(shouldBreak){
				break;
			}
		}
	}

}
