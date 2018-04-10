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

import info.julang.execution.namespace.NamespaceConflictException;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.external.exceptions.JSEError;
import info.julang.interpretation.RuntimeCheckException;
import info.julang.interpretation.StatementBase;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.context.ContextType;
import info.julang.interpretation.internal.NewVarExecutor;
import info.julang.interpretation.syntax.SyntaxHelper;
import info.julang.langspec.ast.JulianParser.Variable_declaratorContext;
import info.julang.langspec.ast.JulianParser.Variable_declaratorsContext;
import info.julang.memory.value.JValue;
import info.julang.parser.AstInfo;
import info.julang.typesystem.JType;
import info.julang.typesystem.VoidType;
import info.julang.typesystem.jclass.Accessibility;
import info.julang.typesystem.jclass.ICompoundType;
import info.julang.typesystem.jclass.builtin.JFunctionType;

/**
 * Statement for declaring local variables.
 * <p/>
 * Basic syntax:
 * <p/>
 * (1) no initializer<br/>
 * <pre><code>int i;</code></pre>
 * (2) with initializer<br/>
 * <pre><code>int i = 5;
 *MyObj m = getMyObj();</code></pre>
 * (3) multiple initialization, each with or without initializer<br/>
 * <pre><code>string s, t = "abc";</code></pre>
 * 
 * @author Ming Zhou
 */
public class LocalVariableDeclarationStatement extends StatementBase {
	
	private JType type;
	private AstInfo<Variable_declaratorsContext> ast;
	
	/**
	 * Create a statement for declaring a local variable.
	 *  
	 * @param runtime
	 * @param tc AST for the type
	 * @param decls AST for declarators
	 */
	public LocalVariableDeclarationStatement(
		ThreadRuntime runtime, JType type, AstInfo<Variable_declaratorsContext> ast){
		super(runtime);
		this.type = type;
		this.ast = ast;
	}

	/**
	 * If successful, add new variables into variable table at current scope.
	 * <pre><code>
	 * int i = ..., j = ...;
	 * </code></pre>
	 */
	@Override
	public void interpret(Context context) {
		//declaration_statement 
	    //  : type variable_declarators SEMICOLON
	    //  | type function_declarator
	    //  ; 
	    
	    //variable_declarators 
	    //  : variable_declarator ( COMMA variable_declarator )*
	    //  ;
	    //variable_declarator 
	    //  : IDENTIFIER ( ASSIGN expression )?
	    //  ;
		
		if (type.isObject()){
			Accessibility.checkTypeVisibility((ICompoundType)type, context.getContainingType(), true);
		}
		
		if (type == VoidType.getInstance()) {
			throw new RuntimeCheckException("Cannot define a void variable.");
		}
		
		Variable_declaratorsContext decls = ast.getAST();
		List<Variable_declaratorContext> vdecls = decls.variable_declarator();
		for (Variable_declaratorContext vdecl : vdecls) {
			// get variable name
			String name = vdecl.IDENTIFIER().getText();
			
			if (context.getContextType() == ContextType.FUNCTION){
                JType typ = context.getTypTable().getType(name);
				if(typ instanceof JFunctionType) {
					// a function with same name is already defined.
					throw new NamespaceConflictException(name + " (variable)", name + " (function)");
				}
			}
			
			SyntaxHelper.checkVarTypeConflict(context, name);
			
			// execute initializer
			JValue initVal = null;
			if (vdecl.ASSIGN() != null){
				ExpressionStatement es = new ExpressionStatement(runtime, ast.create(vdecl.expression()));
				es.interpret(context);
				initVal = es.getResult().getReturnedValue(false);
				if(initVal == null){
					throw new JSEError("The result of variable initializer is null.");
				}
			}
			
			// create new variable with initialized value.
			// here we first initialize the variable by the default value of the declared type, then assign
			// the initialized value to it. This way the type compatibility check/promotion will be 
			// automatically handled by assignTo(val) method.
			NewVarExecutor nve = new NewVarExecutor();
			JValue newVal = nve.newVar(context, name, null, type); 
			if(initVal != null){
				initVal.assignTo(newVal);
			}
		}
	}
}
