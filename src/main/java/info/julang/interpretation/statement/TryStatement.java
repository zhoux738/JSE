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

import info.julang.execution.symboltable.IVariableTable;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.interpretation.BadSyntaxException;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.errorhandling.JSExceptionUtility;
import info.julang.interpretation.errorhandling.JulianScriptException;
import info.julang.interpretation.syntax.ParsedTypeName;
import info.julang.interpretation.syntax.SyntaxHelper;
import info.julang.langspec.ast.JulianParser.BlockContext;
import info.julang.langspec.ast.JulianParser.Catch_blockContext;
import info.julang.langspec.ast.JulianParser.Finally_blockContext;
import info.julang.langspec.ast.JulianParser.Try_statementContext;
import info.julang.memory.value.ObjectValue;
import info.julang.parser.AstInfo;
import info.julang.typesystem.JType;
import info.julang.typesystem.JTypeKind;
import info.julang.typesystem.jclass.ICompoundType;
import info.julang.typesystem.jclass.JClassType;

/**
 * The try statement in Julian language.
 * <p>
 * The try statement is comprised of multiple parts:<pre><code> try {
 *   ... ...
 * } catch (Exception ex) {
 *   ... ...
 * } finally {
 *   ... ...
 * } </code></pre>
 * Note that:
 * <ul>
 *   <li>there can be zero or multiple catch section</li>
 *   <li>there can be zero or one finally section</li>
 * </ul>
 * 
 * @author Ming Zhou
 */
public class TryStatement extends MultiBlockStatementBase {
	
	/**
	 * Type: Try_statementContext
	 */
	private AstInfo<Try_statementContext> ainfo;
	
	/**
	 * Create a new try statement. The PC must be pointed at right <b>after</b> 'try' keyword.
	 * 
	 * @param runtime
	 * @param stream
	 * @param option
	 */
	public TryStatement(ThreadRuntime runtime, AstInfo<Try_statementContext> ainfo, StatementOption option) {
		super(runtime, option);
		this.ainfo = ainfo;
	}

	@Override
	public void interpret(Context context) {
		Try_statementContext ast = ainfo.getAST();
		BlockContext block = ast.block();
		Finally_blockContext finale = ast.finally_block();
		
		//try_statement 
	    //  : TRY block catch_block* finally_block?
	    //  ;
		
		// Execute the try block
		boolean passedInTry = false;
		try{
			performBlock(context, ainfo.create(block), option, true);
			
			passedInTry = true;
			
			// No JSE has been thrown, skip catch and execute finally, if any
			interpretCatchFinally(context, null, finale, null);
		} catch (JulianScriptException jse) {
			if (passedInTry){
				// The exception was thrown from catch/finally. Do not process.
				throw jse;
			}
			
			// If a JSE is thrown, try to handle it in catch and execute finally at the end
			List<Catch_blockContext> catches = ast.catch_block();
			interpretCatchFinally(context, catches, finale, jse);
		}
	}
	
	/**
	 * Interpret the catch and finally clauses.
	 * <p>
	 * In the declared order, try to match each <code>catch</code> clause against the captured exception until one is found, execute the block.
	 * <p>
	 * Whether caught or not, run <code>finally</code> clause at the end.
	 * 
	 * @param context
	 * @param catches can be null
	 * @param finale can be null
	 * @param jse can be null
	 */
	private void interpretCatchFinally(
		Context context, 
		List<Catch_blockContext> catches, 
		Finally_blockContext finale, 
		JulianScriptException jse) {

		//catch_block 
	    //  : CATCH LEFT_PAREN type IDENTIFIER RIGHT_PAREN block
	    //  ;
		//finally_block 
	    //  : FINALLY block
	    //  ;
		
		boolean passedInCatch = false, caught = false;
		try {
			// Try to find the matching the catch clause - this may throw, in that case we will be throwing the new exception instead of the caught one
			if (jse != null && catches != null) {
				if (!JSExceptionUtility.isFatal(jse)){
					for(Catch_blockContext cb : catches){
						caught = interpretCatch(context, cb, jse);
						if (caught){
							break;
						}
					}
				}
			}
			
			passedInCatch = true;
			
			// Execute the finally clause - this may throw, in that case we will be throwing the new exception instead of the caught one
			performFinally(context, finale);
			
			if (!caught && jse != null){ // After finally is done, keep popping the exception if it was not caught
				throw jse;
			}
		} catch (JulianScriptException jse2) {
			if (passedInCatch){
				// The exception was thrown from the finally clause. Do not process.
				throw jse2;
			}

			// Execute the finally clause - this may throw, in that case we will be throwing the new exception instead of the caught one
			performFinally(context, finale);
			
			throw jse2;
		}
	}
	
	private void performFinally(Context context, Finally_blockContext finale){
		if (finale != null){
			if (result != null){
				result.replicateValue();
			}
			performBlock(context, ainfo.create(finale.block()), option, false);
		}
	}

	private boolean interpretCatch(Context context, Catch_blockContext cb, JulianScriptException jse) {
		ParsedTypeName typeName = SyntaxHelper.parseTypeName(cb.type());
		
		// Verify the thrown type. This may be optimized in future by a cache (where to store?)
		JType typ = context.getTypeResolver().resolveType(typeName);
		if(typ.getKind() != JTypeKind.CLASS){
			throw new BadSyntaxException(
				"The exception type declaration in a catch statement " + 
				"must be of, or derive from, System.Exception. " + 
				"But saw " + typ.getName() + ".");		
		}
		
		ICompoundType exTyp = (ICompoundType) typ;
		JClassType sysExTyp = (JClassType) context.getTypTable().getType(JSExceptionUtility.SystemExceptionClass);

		if(!exTyp.isDerivedFrom(sysExTyp, true)){
			throw new BadSyntaxException(
				"The exception type declaration in a catch statement " + 
				"must be of, or derive from, System.Exception. " + 
				"But saw " + typ.getName() + ".");					
		}
		
		String exVarName = cb.IDENTIFIER().getText();
		
		ObjectValue exValue = jse.getExceptionValue();
		ICompoundType caughtExType = (ICompoundType) exValue.getType();
		if(!caughtExType.isDerivedFrom(exTyp, true)){
			// This catch block doesn't match the type of thrown exception
			return false;
		} else {
			// This catch block matches the type of thrown exception
			
			// Add variable definition
			IVariableTable vt = context.getVarTable();
			vt.enterScope();
			vt.addVariable(exVarName, exValue);
			
			// Execute the catch block
			try {
				performBlock(context, ainfo.create(cb.block()), option, true);
				vt.exitScope();
			} catch (JulianScriptException jse2) {
				vt.exitScope();
				throw jse2;
			}
			
			return true;
		}
	}
}