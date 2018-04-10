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

import java.util.List;

import info.julang.execution.threading.ThreadRuntime;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.errorhandling.JulianScriptException;
import info.julang.interpretation.syntax.ParsedTypeName;
import info.julang.interpretation.syntax.SyntaxHelper;
import info.julang.langspec.ast.JulianParser.Compound_statementContext;
import info.julang.langspec.ast.JulianParser.ExpressionContext;
import info.julang.langspec.ast.JulianParser.For_conditionContext;
import info.julang.langspec.ast.JulianParser.For_initializerContext;
import info.julang.langspec.ast.JulianParser.For_post_loopContext;
import info.julang.langspec.ast.JulianParser.For_statementContext;
import info.julang.langspec.ast.JulianParser.For_statment_headContext;
import info.julang.langspec.ast.JulianParser.Statement_expression_listContext;
import info.julang.langspec.ast.JulianParser.TypeContext;
import info.julang.langspec.ast.JulianParser.Variable_declarationContext;
import info.julang.langspec.ast.JulianParser.Variable_declaratorsContext;
import info.julang.parser.AstInfo;
import info.julang.typesystem.JType;

/**
 * The for statement in Julian language.
 * <p/>
 * The for statement is comprised of multiple parts:<pre><code> for(|for-init|;|for-cond|;|for-step|)
 *   expression</code></pre>or<pre><code> for(|for-init|;|for-cond|;|for-step|){
 *   for-body
 * } </code></pre>
 * The head that contains </code>for-init|</code>, <code>|for-cond|</code> and <code>|for-step|</code> 
 * is required; body is not. Within the head, all the parts are all optional. The minimal form for head 
 * can be as simple as <code>for(;;)</code>, which constitutes a infinite loop.
 * <p/>
 * @author Ming Zhou
 */
public class ForStatement extends MultiBlockStatementBase {
	
	/**
	 * Type: For_statementContext
	 */
	private AstInfo<For_statementContext> ainfo;
	
	/**
	 * Create a new for statement. The PC must be pointed at right <b>after</b> 'for' keyword.
	 * 
	 * @param runtime
	 * @param stream
	 * @param option
	 */
	public ForStatement(ThreadRuntime runtime, AstInfo<For_statementContext> ainfo, StatementOption option) {
		super(runtime, option);
		this.ainfo = ainfo;
	}

	@Override
	public void interpret(Context context) {		
		try {
			// enter for scope
			context.getVarTable().enterScope();
			interpretFor(context);
			context.getVarTable().exitScope();
		} catch (JulianScriptException jse){
			context.getVarTable().exitScope();
			throw jse;
		}
	}
	
	public void interpretFor(Context context){
		//for_statement 
		//    : FOR LEFT_PAREN for_statment_head RIGHT_PAREN compound_statement
		//    ;
		//for_statment_head 
		//    : for_initializer? SEMICOLON for_condition? SEMICOLON for_post_loop?
		//    ;
		//for_initializer 
		//    : variable_declaration
		//    | statement_expression_list
		//    ;
		//for_condition 
		//    : expression
		//    ;
		//for_post_loop 
		//    : statement_expression_list
		//    ;
		For_statementContext forStmt = ainfo.getAST();
		For_statment_headContext head = forStmt.for_statment_head();
		
		// locate and execute initializer
		For_initializerContext init = head.for_initializer();
		if (init != null) {
			Variable_declarationContext vdc = init.variable_declaration();
			
			if (vdc != null){
				// the initializer declares new variables
				// for(int i = 0, j = 0; ...
				TypeContext tc = vdc.type();
				ParsedTypeName ptn = SyntaxHelper.parseTypeName(tc);
				JType typ = context.getTypeResolver().resolveType(ptn);
				Variable_declaratorsContext declCntx = vdc.variable_declarators();
				
				LocalVariableDeclarationStatement decl = 
					new LocalVariableDeclarationStatement(runtime, typ, ainfo.create(declCntx));
				decl.interpret(context);
			} else {
				// the initializer contains expressions
				// for (i = 0, j = 0; ...
				Statement_expression_listContext sel = init.statement_expression_list();
				List<ExpressionContext> exs = sel.expression();
				
				for (ExpressionContext expr : exs){
					ExpressionStatement es = new ExpressionStatement(runtime, ainfo.create(expr));
					es.interpret(context);
				}
			}
		}
		
		Compound_statementContext csc = forStmt.compound_statement();
		For_conditionContext condCntx = head.for_condition();
		For_post_loopContext postCntx = head.for_post_loop();
		
		performLoop(context, csc, condCntx, postCntx);
	}
	
	private void performLoop(
		Context context, 
		Compound_statementContext csc,
		For_conditionContext condCntx,
		For_post_loopContext postCntx) {
		// for_condition? 
		ExpressionContext condEx = null;
		if (condCntx != null){
			condEx = condCntx.expression();
		}
		// for_post_loop?
		List<ExpressionContext> postExs = null;
		if (postCntx != null) {
			Statement_expression_listContext pstCntxs = postCntx.statement_expression_list();
			postExs = pstCntxs.expression();
		}
		
		while(true){
			// pre-loop condition check
			if (condCntx != null) {
				ExpressionStatement es = new ExpressionStatement(runtime, ainfo.create(condEx));
				es.interpret(context);
				boolean result = es.getBooleanResult();
				if(!result){
					// exit for loop if check didn't pass.
					break;
				}
			}
			
			// main loop body
			boolean shouldBreak = performLoopBody(context, ainfo.create(csc));
			if(shouldBreak){
				break;
			}
			
			// post-loop step
			if (postExs != null) {
				for (ExpressionContext expr : postExs){
					ExpressionStatement es = new ExpressionStatement(runtime, ainfo.create(expr));
					es.interpret(context);
				}
			}
		}
	}
}
