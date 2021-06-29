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

import org.antlr.v4.runtime.ParserRuleContext;

import info.julang.JSERuntimeException;
import info.julang.dev.GlobalSetting;
import info.julang.execution.Result;
import info.julang.execution.threading.JThread;
import info.julang.execution.threading.JThreadAbortedException;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.interpretation.BadSyntaxException;
import info.julang.interpretation.ExitCause;
import info.julang.interpretation.IllegalLexicalContextException;
import info.julang.interpretation.Statement;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.errorhandling.JSExceptionUtility;
import info.julang.interpretation.errorhandling.JulianScriptException;
import info.julang.interpretation.errorhandling.KnownJSException;
import info.julang.interpretation.syntax.ParsedTypeName;
import info.julang.interpretation.syntax.SyntaxHelper;
import info.julang.langspec.ast.JulianParser;
import info.julang.langspec.ast.JulianParser.BlockContext;
import info.julang.langspec.ast.JulianParser.Compound_statementContext;
import info.julang.langspec.ast.JulianParser.Declaration_statementContext;
import info.julang.langspec.ast.JulianParser.Do_statementContext;
import info.julang.langspec.ast.JulianParser.ExpressionContext;
import info.julang.langspec.ast.JulianParser.Expression_statementContext;
import info.julang.langspec.ast.JulianParser.For_statementContext;
import info.julang.langspec.ast.JulianParser.Foreach_statementContext;
import info.julang.langspec.ast.JulianParser.Function_declaratorContext;
import info.julang.langspec.ast.JulianParser.If_statementContext;
import info.julang.langspec.ast.JulianParser.Return_statementContext;
import info.julang.langspec.ast.JulianParser.Simple_statementContext;
import info.julang.langspec.ast.JulianParser.StatementContext;
import info.julang.langspec.ast.JulianParser.Statement_listContext;
import info.julang.langspec.ast.JulianParser.Switch_statementContext;
import info.julang.langspec.ast.JulianParser.Sync_statementContext;
import info.julang.langspec.ast.JulianParser.Throw_statementContext;
import info.julang.langspec.ast.JulianParser.Try_statementContext;
import info.julang.langspec.ast.JulianParser.TypeContext;
import info.julang.langspec.ast.JulianParser.Variable_declaratorsContext;
import info.julang.langspec.ast.JulianParser.While_statementContext;
import info.julang.parser.ANTLRHelper;
import info.julang.parser.AstInfo;
import info.julang.typesystem.JType;

/**
 * An internal class used to execute a list of statements. Exposes result and exit cause to
 * it caller. Used by other statements which need to invoke a list of sub-statements.
 * <p>
 * If an exception is thrown from any statement in the list, it will be, if possible, 
 * converted to a Julian script exception with part of source info set, and get re-thrown.
 * Later when exiting the current frame the rest of source info will be filled out.
 * 
 * @author Ming Zhou
 */
class StatementsExecutor implements IHasResult, IHasExitCause {

	private Result result;
	
	private StatementOption option;
	
	private ExitCause exitCause;
	
	private ThreadRuntime runtime;
	
	private AstInfo<? extends ParserRuleContext> ainfo;
	
	// Use this to track the current sub-tree being interpreted. It can provide source location info.
	private ParserRuleContext prt;
	
	/**
	 * 
	 * @param runtime
	 * @param ainfo This is only used to pass file name info.
	 * @param option
	 */
	StatementsExecutor(ThreadRuntime runtime, AstInfo<? extends ParserRuleContext> ainfo, StatementOption option){
		this.runtime = runtime;
		this.option = option;
		this.ainfo = ainfo;
		this.exitCause = ExitCause.UNDEFINED;
	}

	boolean interpretStatments(List<StatementContext> stmtCntxs, Context context){
		try {
			boolean abort = false;
			for(StatementContext stmtCntx : stmtCntxs){
				// statement 
				//     : declaration_statement		// 4.1
				//     | compound_statement
				prt = (ParserRuleContext) stmtCntx.children.get(0);
				switch(prt.getRuleIndex()){
				case JulianParser.RULE_declaration_statement:
					// Declaration never aborts the block
					interpretDeclaration((Declaration_statementContext)prt, context);
					break;
				case JulianParser.RULE_compound_statement:
					// compound_statement 
					//     : block
					//     | simple_statement
					// LEFT_CURLY statement_list? RIGHT_CURLY
					Compound_statementContext csc = (Compound_statementContext)prt;
					prt = (ParserRuleContext) csc.children.get(0);
					switch(prt.getRuleIndex()){
					case JulianParser.RULE_simple_statement:
						Simple_statementContext sc = (Simple_statementContext)prt;
						abort = interpretSimple(sc, context);
						break;
					case JulianParser.RULE_block:
						// block 
						//  : LEFT_CURLY statement_list? RIGHT_CURLY
						
						// enter scope
						context.getVarTable().enterScope();
						try {
							BlockContext bc = (BlockContext)prt;
							Statement_listContext slc = bc.statement_list();
							if (slc != null){
								abort = interpretStatments(slc.statement(), context);
							}
						} finally {
							// leave scope
							context.getVarTable().exitScope();
						}
						
						break;
					default:
						throw ANTLRHelper.getUnrecognizedError(prt);
					}
					break;
				default:
					throw ANTLRHelper.getUnrecognizedError(prt);
				}
				
				if (abort || option.noSequential){
					break;
				}
			}
			
			return abort;
		} catch (JSERuntimeException jre){
			KnownJSException kjse = jre.getKnownJSException();
			if (kjse != null){
				// This is only for debugging. Should comment out before shipping.
				if(GlobalSetting.skipCatch(kjse)){
					throw jre;
				}
				
				JulianScriptException jse = jre.toJSE(runtime, context);
				// To avoid excessive nesting of try-catch, replicate what the other catch clause does.
				setSourceInfo(jse);
				throw jse;			
			} else {
				throw jre;	
			}
		} catch (JulianScriptException jse){
			setSourceInfo(jse);
			throw jse;
		}
	}
	
	private void setSourceInfo(JulianScriptException jse){
		// Capture JSE (step 1/2):
		// At this point we have file and location info, but we don't know what function is called and
		// what are the parameters. So we set source info and throw again.
		int lineNo = jse.getLineNumber();
		if(lineNo == JulianScriptException.UNSET_LINENO){
			lineNo = prt.getStart().getLine();
		}

		JSExceptionUtility.setSourceInfo(jse, ainfo, lineNo);
	}
	
	/*
	 * declaration_statement 
	 *     : type variable_declarators SEMICOLON
	 *     | type function_declarator
	 *     ;
	 */
	private void interpretDeclaration(Declaration_statementContext prt, Context context) {
		Statement stmt;

		TypeContext tc = (TypeContext) prt.children.get(0);
		ParsedTypeName ptn = SyntaxHelper.parseTypeName(tc);
		JType type = context.getTypeResolver().resolveType(ptn);
		
		ParserRuleContext prt1 = (ParserRuleContext) prt.children.get(1);
		
		//declaration_statement 
	    //  : type variable_declarators SEMICOLON
	    //  | type function_declarator
	    //  ;
		switch(prt1.getRuleIndex()){
		case JulianParser.RULE_variable_declarators:
			Variable_declaratorsContext dsc = (Variable_declaratorsContext) prt1;
			stmt = new LocalVariableDeclarationStatement(runtime, type, ainfo.create(dsc));
			stmt.interpret(context);
			break;
		case JulianParser.RULE_function_declarator:
			if(!option.allowFunctionDef){
				throw new IllegalLexicalContextException("Function definition", ainfo.create(prt1));
			}

			Function_declaratorContext fdc = (Function_declaratorContext)prt1;
			stmt = new FunctionDeclarationStatement(runtime, type, ainfo.create(fdc));
			stmt.interpret(context);
			break;
		default:
			throw ANTLRHelper.getUnrecognizedError(prt1);
		}
	}
	
	/*
	 * Return true to indicate exit.
	 * 
	 * simple_statement 
     *     : empty_statement
	 *     | expression_statement		// 4.2
	 *     | if_statement 				// 4.3
	 *     | switch_statement			// 4.4
	 *     | while_statement			// 4.5
	 *     | do_statement				// 4.6
	 *     | for_statement				// 4.7
	 *     | foreach_statement			// 4.8
	 *     | try_statement				// 4.9
	 *     | throw_statement			// 4.10
	 *     | break_statement			// 4.11
	 *     | continue_statement			// 4.12
	 *     | return_statement			// 4.13
	 *     | sync_statement				// 4.14
	 *     ;
	 */
	private boolean interpretSimple(Simple_statementContext prt, Context context) {
		boolean exit = false;
		StatementOption nestedOption;
		Statement stmt = null;
		ExpressionStatement estmt2 = null;
		ParserRuleContext prt0 = (ParserRuleContext) prt.children.get(0);
		switch(prt0.getRuleIndex()){
		case JulianParser.RULE_empty_statement:
			if (++totalEmptyStmt > 0b1111111111){ // Check this for every 1K statements.
				JThread thread = runtime.getJThread();
				if(thread.checkTermination()){
					throw new JThreadAbortedException(thread);
				}
				totalEmptyStmt = 1;
			}
			break;
		case JulianParser.RULE_expression_statement:
			ExpressionContext ec0 = ((Expression_statementContext)prt0).expression();
			stmt = estmt2 = new ExpressionStatement(runtime, ainfo.create(ec0));
			estmt2.interpret(context);
			break;
		case JulianParser.RULE_if_statement:
			nestedOption = StatementOption.createInheritedOption(option);
			stmt = new IfStatement(runtime, ainfo.create((If_statementContext)prt0), nestedOption);
			stmt.interpret(context);
			break;
		case JulianParser.RULE_switch_statement:
			nestedOption = StatementOption.createInheritedOption(option);
			stmt = new SwitchStatement(runtime, ainfo.create((Switch_statementContext)prt0), nestedOption);
			stmt.interpret(context);
			break;
		case JulianParser.RULE_while_statement:
			nestedOption = StatementOption.createInheritedOption(option);
			stmt = new WhileStatement(runtime, ainfo.create((While_statementContext)prt0), nestedOption);
			stmt.interpret(context);
			break;
		case JulianParser.RULE_do_statement:
			nestedOption = StatementOption.createInheritedOption(option);
			stmt = new DoWhileStatement(runtime, ainfo.create((Do_statementContext)prt0), nestedOption);
			stmt.interpret(context);
			break;
		case JulianParser.RULE_for_statement:
			nestedOption = StatementOption.createInheritedOption(option);
			For_statementContext fsc = (For_statementContext)prt0;
			if (fsc.for_statment_head() != null) {
				stmt = new ForStatement(runtime, ainfo.create(fsc), nestedOption);
			} else {
				stmt = new ForEachStatement(runtime, ainfo.create(fsc), nestedOption, fsc.foreach_statement_head(), fsc.compound_statement());
			}
			stmt.interpret(context);
			break;
		case JulianParser.RULE_foreach_statement:
			nestedOption = StatementOption.createInheritedOption(option);
			Foreach_statementContext fesc = (Foreach_statementContext)prt0;
			stmt = new ForEachStatement(runtime, ainfo.create(fesc), nestedOption, fesc.foreach_statement_head(), fesc.compound_statement());
			stmt.interpret(context);
			break;
		case JulianParser.RULE_try_statement:
			nestedOption = StatementOption.createInheritedOption(option);
			stmt = new TryStatement(runtime, ainfo.create((Try_statementContext)prt0), nestedOption); 
			stmt.interpret(context);
			break;
		case JulianParser.RULE_throw_statement:
			stmt = new ThrowStatement(runtime, ainfo.create((Throw_statementContext)prt0)); 
			stmt.interpret(context);// Note: ThrowStatement will throw a Java exception
			break;
		case JulianParser.RULE_continue_statement:
			skipRest(context, ExitCause.CONTINUED, option.allowContinue, "continue");
			break;
		case JulianParser.RULE_break_statement:
			skipRest(context, ExitCause.BROKEN, option.allowBreak, "break");
			break;
		case JulianParser.RULE_return_statement:
			// NOTE: when we return from the middle of function, we leave PC at where it is.
			if(!option.allowReturn){
				throw new IllegalLexicalContextException("Return statement", ainfo.create(prt0));				
			}
			
			Return_statementContext rsc = (Return_statementContext)prt0;
			ExpressionContext ec = rsc.expression();
			if(ec == null){
				// return;
				result = Result.Void;
			} else {
				// return {expression};
				stmt = estmt2 = new ExpressionStatement(runtime, ainfo.create(ec));
				estmt2.interpret(context);
				result = estmt2.getResult();
			}
			
			exitCause = ExitCause.RETURNED;
			break;
		case JulianParser.RULE_sync_statement:
			nestedOption = StatementOption.createInheritedOption(option);
			stmt = new SyncStatement(runtime, ainfo.create((Sync_statementContext)prt0), nestedOption);
			stmt.interpret(context);
			break;
		default:
			throw ANTLRHelper.getUnrecognizedError(prt0);
		}
		
		if (stmt != null && stmt instanceof IHasExitCause){
			IHasExitCause hec = (IHasExitCause)stmt;
			exitCause = hec.getExitCause();
			
			// Pass along the returned value
			if (exitCause == ExitCause.RETURNED){
				if(stmt instanceof IHasResult){
					result = ((IHasResult)stmt).getResult();
				} else {
					result = Result.Void;
				}
			}
		}
		
		if (option.preserveStmtResult && stmt instanceof IHasResult){
			result = ((IHasResult)stmt).getResult();
		}
		
		exit = ExitCause.isAborted(exitCause);
		
		return exit;
	}

	@Override
	public Result getResult(){
		return result != null ? result : Result.Void;
	}

	@Override
	public ExitCause getExitCause(){
		return exitCause != null ? exitCause : ExitCause.UNDEFINED;
	}
	
	private void skipRest(Context context, ExitCause cause, boolean isCauseAllowed, String statementName){
		if(isCauseAllowed){
			exitCause = cause;
			result = Result.Void;
			return;
		} else {
			throw new BadSyntaxException("Illegal place to use " + statementName + ".");
		}
	}
	
	// The access to totalEmptyStmt is not thread safe, but that should be OK, as we only rely on 
	// the fact that the count is increasing, while the accuracy is less important.
	private static int totalEmptyStmt = 1;
}