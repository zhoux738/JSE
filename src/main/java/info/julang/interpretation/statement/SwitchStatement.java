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

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

import info.julang.execution.Result;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.interpretation.ExitCause;
import info.julang.interpretation.StatementBase;
import info.julang.interpretation.UndefinedVariableNameException;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.expression.operator.TypeofOp;
import info.julang.interpretation.syntax.ParsedTypeName;
import info.julang.interpretation.syntax.SyntaxHelper;
import info.julang.langspec.ast.JulianLexer;
import info.julang.langspec.ast.JulianParser.Case_conditionContext;
import info.julang.langspec.ast.JulianParser.Case_sectionContext;
import info.julang.langspec.ast.JulianParser.Default_sectionContext;
import info.julang.langspec.ast.JulianParser.ExpressionContext;
import info.julang.langspec.ast.JulianParser.StatementContext;
import info.julang.langspec.ast.JulianParser.Statement_listContext;
import info.julang.langspec.ast.JulianParser.Switch_blockContext;
import info.julang.langspec.ast.JulianParser.Switch_statementContext;
import info.julang.langspec.ast.JulianParser.TypeContext;
import info.julang.memory.value.EnumValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.RefValue;
import info.julang.memory.value.StringValue;
import info.julang.memory.value.TempValueFactory;
import info.julang.parser.ANTLRHelper;
import info.julang.parser.AstInfo;
import info.julang.typesystem.JType;
import info.julang.typesystem.UnknownTypeException;
import info.julang.typesystem.jclass.builtin.JEnumType;

/**
 * The switch statement in Julian language.
 * <p/>
 * The switch statement is comprised of multiple parts:<pre><code> <b>switch</b>(|expr|){
 * <b>case</b> val1: ... ... (statements)
 * <b>case</b> val2: ... ... (statements)
 * ... ...
 * <b>default</b>: ... ... (statements)
 * } </code></pre>
 * Note that:
 * <p/>
 *   <li>val1, val2, ... must be constants or literals, and can only be 
 *       Boolean, Char, Enum, Integer, String type or a <code>typeof</code> expression.</li>
 *   <li>there can be any number of case clauses</li>
 *   <li>there can be zero or one default clause, which must appear after all the case clauses</li>
 *   <li>the statements following "case val:" are sequentially interpreted, 
 *   and will fall through to the next case clause, unless a break is used.</li>
 *   <li>if encountering a break, exit the block; if encountering a continue, exit a block only if this switch 
 *   statement sits inside a construct which permits continue (such as for statement).</li>
 *   <li>if the case condition is an id that is not resolvable, or a type not recognized, the case will be skipped</li>
 * <br/><br/>
 * 
 * @author Ming Zhou
 */
public class SwitchStatement extends StatementBase implements IHasExitCause, IHasResult {
	
	private AstInfo<Switch_statementContext> ainfo;
	
	private ExitCause exitCause = ExitCause.UNDEFINED;
	
	private Result result = Result.Void;
	
	private StatementOption option;
	
	/**
	 * Create a new switch statement. The PC must be pointed at right <b>after</b> 'switch' keyword.
	 * 
	 * @param runtime
	 * @param stream
	 * @param option
	 */
	public SwitchStatement(ThreadRuntime runtime, AstInfo<Switch_statementContext> ainfo, StatementOption option) {
		super(runtime);
		this.option = option;
		this.ainfo = ainfo;
	}

	@Override
	public void interpret(Context context) {
		//switch_statement 
		//  : SWITCH LEFT_PAREN expression RIGHT_PAREN switch_block
		//  ;
		Switch_statementContext switchContext = ainfo.getAST();
		
		// Evaluate the expression to determine the case value
		ExpressionContext ec = switchContext.expression();
		ExpressionStatement es = new ExpressionStatement(runtime, ainfo.create(ec));

		es.interpret(context);
		JValue jumpTo = es.getResult().getReturnedValue(false);
		
		StringValue enumLiteral = null;
		JType typ = jumpTo.getType();
		if(JEnumType.isEnumType(typ)){
			EnumValue ev = (EnumValue) RefValue.dereference(jumpTo);
			enumLiteral = TempValueFactory.createTempStringValue(ev.getLiteral());
		}
		
		boolean switchOnEnum = enumLiteral != null;
		Switch_blockContext sbc = switchContext.switch_block();
		List<Case_sectionContext> cases = sbc.case_section();
		boolean matched = false, fallthrough = false;
		if (cases != null){
			int total = cases.size();
			for(int i = 0; i < total; i++){
				Case_sectionContext csc = cases.get(i);
				
				//case_section 
				//  : CASE case_condition COLON statement_list?
				//  ;
				//case_condition
				//  : IDENTIFIER | CHAR_LITERAL | INTEGER_LITERAL | STRING_LITERAL ｜ TYPEOF LEFT_PAREN type RIGHT_PAREN
				//  ;
				Case_conditionContext cond = csc.case_condition();
				
				JValue value = getCaseValue(context, cond, switchOnEnum); 

				if (value != null) {
					if(switchOnEnum){
						// It's an enum
						if(value.isEqualTo(enumLiteral)){
							matched = true;
						}
					} else if(value.getType() == jumpTo.getType()){
						if(value.isEqualTo(jumpTo)){
							matched = true;
						}
					}
				}
				
				if(matched){
					// a match, interpret from here.
					List<StatementContext> stmts = collectRemainingStatements(cases, i);
					fallthrough = interpretSection(context, stmts, true);
					break;	
				}
			}
		}
		
		if(!matched ||   // If no case is matched, or
		   fallthrough){ // if we the flow falls through the case sections, execute the default
			//default_section 
			//  : DEFAULT COLON statement_list?
			//  ;
			Default_sectionContext dsc = sbc.default_section();
			if (dsc != null){
				Statement_listContext slc = dsc.statement_list();
				if (slc != null){
					List<StatementContext> stmts = slc.statement();
					interpretSection(context, stmts, false);
				}
			}
		}
	}

	private JValue getCaseValue(Context context, Case_conditionContext cond, boolean switchOnEnum) {
		TerminalNode tnode = (TerminalNode) cond.children.get(0);
		Token sym = tnode.getSymbol();
		String text = sym.getText();
		switch(sym.getType()){
		case JulianLexer.INTEGER_LITERAL:
			return TempValueFactory.createTempIntValue(Integer.parseInt(text));
			
		case JulianLexer.CHAR_LITERAL:
			return TempValueFactory.createTempCharValue(ANTLRHelper.reEscapeAsChar(text, true));
			
		case JulianLexer.STRING_LITERAL:
			return TempValueFactory.createTempStringValue(ANTLRHelper.reEscapeAsString(text, true));
			
		case JulianLexer.IDENTIFIER:
			JValue val = null;
			if (switchOnEnum) {
				val = TempValueFactory.createTempStringValue(sym.getText());
			} else {
				try {
					val = context.getResolver().resolve(text);
				} catch (UndefinedVariableNameException e){
					// Ignore
				}		
			}
			
			return val;
			
		case JulianLexer.TYPEOF:
			RefValue rv = null;
			try {
				TypeContext tc = cond.type();
				ParsedTypeName ptn = SyntaxHelper.parseTypeName(tc);
				JType type = context.getTypeResolver().resolveType(ptn);
				rv = TypeofOp.getTypeObject(runtime, context, type);
			} catch (UnknownTypeException e) {
				// Ignore
			}
			
			return rv;
			
		default:
			throw ANTLRHelper.getUnrecognizedTerminalError(tnode);
		}
	}
	
	// Starting from index i, add all statements under each case section into an aggregated list.
	// Note this list doesn't include any case labels or default label.
	private List<StatementContext> collectRemainingStatements(List<Case_sectionContext> cases, int startIndex) {
		List<StatementContext> stmts = new ArrayList<StatementContext>();
		int total = cases.size();
		for(int i = startIndex; i < total; i++){
			Case_sectionContext csc = cases.get(i);
			Statement_listContext slc = csc.statement_list();
			if (slc != null){
				List<StatementContext> scs = slc.statement();
				stmts.addAll(scs);
			}
		}
		
		return stmts;
	}
	
	/**
	 * @return true if the flow falls through the sections.
	 */
	private boolean interpretSection(Context context, List<StatementContext> stmts, boolean allowCaseOrDefault) {
		StatementOption clauseOption = StatementOption.createInheritedOption(option);
		clauseOption.setIgnoreCase(allowCaseOrDefault);
		clauseOption.setIgnoreDefault(allowCaseOrDefault);
		clauseOption.setAllowBreak(true);
		
		StatementsExecutor se = new StatementsExecutor(runtime, ainfo, clauseOption);
		se.interpretStatments(stmts, context);
		exitCause = se.getExitCause();
		if(!ExitCause.isAborted(exitCause)){
			return true;
		} else if(exitCause == ExitCause.BROKEN){
			// Consume the break, since a break inside switch only means breaking the switch, 
			// not whatever contains it. This is different from using break inside an if statement.
			exitCause = ExitCause.THROUGH;
		} else if(exitCause == ExitCause.RETURNED){
			exitCause = ExitCause.RETURNED;
			result = se.getResult();
		}
		
		return false;
	}

	@Override
	public ExitCause getExitCause() {
		return exitCause;
	}
	
	@Override
	public Result getResult(){
		return result;
	}
}
