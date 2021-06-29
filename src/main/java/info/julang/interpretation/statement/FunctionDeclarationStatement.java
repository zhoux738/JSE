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

import info.julang.execution.namespace.NamespacePool;
import info.julang.execution.symboltable.IVariableTable;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.interpretation.GlobalFunctionExecutable;
import info.julang.interpretation.RuntimeCheckException;
import info.julang.interpretation.StatementBase;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.syntax.ParsedTypeName;
import info.julang.interpretation.syntax.SyntaxHelper;
import info.julang.langspec.ast.JulianParser.ExecutableContext;
import info.julang.langspec.ast.JulianParser.Function_declaratorContext;
import info.julang.langspec.ast.JulianParser.Function_parameterContext;
import info.julang.langspec.ast.JulianParser.Function_parameter_listContext;
import info.julang.langspec.ast.JulianParser.Function_signatureContext;
import info.julang.langspec.ast.JulianParser.Function_signature_mainContext;
import info.julang.langspec.ast.JulianParser.Statement_listContext;
import info.julang.langspec.ast.JulianParser.TypeContext;
import info.julang.memory.MemoryArea;
import info.julang.memory.value.FuncValue;
import info.julang.modulesystem.ModuleInfo;
import info.julang.parser.AstInfo;
import info.julang.typesystem.JType;
import info.julang.typesystem.VoidType;
import info.julang.typesystem.jclass.Accessibility;
import info.julang.typesystem.jclass.ICompoundType;
import info.julang.typesystem.jclass.JParameter;
import info.julang.typesystem.jclass.builtin.JFunctionType;

/**
 * A statement for global function declaration, i.e.
 * <pre><code>int func(string s, int k){
 *   // ...
 * }</code></pre>
 *
 * @author Ming Zhou
 */
public class FunctionDeclarationStatement extends StatementBase {

	private JType retType;
	
	private AstInfo<Function_declaratorContext> ainfo;
	
	/**
	 * Create a FunctionDeclarationStatement.
	 * 
	 * @param runtime
	 * @param type the return type. If null, it is a function returning untyped value.
	 * @param decl AST for function declaration.
	 */
	public FunctionDeclarationStatement(
		ThreadRuntime runtime, JType type, AstInfo<Function_declaratorContext> ainfo) {
		super(runtime);
		this.retType = type;
		this.ainfo = ainfo;
	}

	@Override
	public void interpret(Context context) {
		if (retType != null && retType.isObject()){
			Accessibility.checkTypeVisibility((ICompoundType)retType, null, true);
		}
		
		Function_declaratorContext decl = ainfo.getAST();
		// 1) Function's name
		Function_signatureContext sigAst = decl.function_signature();
		String funName = sigAst.IDENTIFIER().getText();
		
		// 2) Function's parameters
		List<JParameter> params = createParams(context, sigAst.function_signature_main());
		
		// 3) Function's body
		ExecutableContext bodyAst = decl.executable();
		if (bodyAst == null) { // Synthesize an empty body
			bodyAst = new ExecutableContext(null, 0);
			Statement_listContext slist = new Statement_listContext(bodyAst, 0);
			bodyAst.addChild(slist);
		}
		
		FuncValue fv = createFunctionType(context, funName, params, bodyAst);
		IVariableTable gvt = context.getVarTable().getGlobal();
		gvt.addVariable(funName, fv);
	}
	
	//function_signature
    //  : IDENTIFIER function_signature_main
    //  ;
	private List<JParameter> createParams(Context context, Function_signature_mainContext mainAst){
	    //function_signature_main // (int i, string s, ...)
	    //  : LEFT_PAREN function_parameter_list? RIGHT_PAREN
	    //  ;
	    //function_parameter_list 
	    //  : function_parameter ( COMMA function_parameter )*
	    //  ;
	    //function_parameter // Note this is different form lambda parameter in that the type is required.
	    //  : type IDENTIFIER
	    //  ;
		
		List<JParameter> params = new ArrayList<JParameter>();
		Function_parameter_listContext fml = mainAst.function_parameter_list();
		
		if (fml == null){
			return params;
		}
		
		List<Function_parameterContext> plist = fml.function_parameter();
		for(Function_parameterContext fp : plist){
			TypeContext tc = fp.type();
			ParsedTypeName ptn = SyntaxHelper.parseTypeName(tc);
			JType paramTyp = context.getTypeResolver().resolveType(ptn);
			String paramName = fp.IDENTIFIER().getText();
			
			if (paramTyp.isObject()) {
				Accessibility.checkTypeVisibility((ICompoundType)paramTyp, null, true);
			} else if (paramTyp == VoidType.getInstance()) {
				throw new RuntimeCheckException("Cannot use void as parameter type.");
			}
			
			SyntaxHelper.checkVarTypeConflict(context, paramName);
			
			params.add(ptn == ParsedTypeName.ANY?
				new JParameter(paramName):
				new JParameter(paramName, paramTyp));
		}
		
		return params;
	}
	
	private FuncValue createFunctionType(
		Context context, String funcName, List<JParameter> params, ExecutableContext bodyAst){
		JParameter[] paramsArray = new JParameter[params.size()];
		params.toArray(paramsArray);	
		NamespacePool nsPool = context.getNamespacePool();
		String tname = /* ModuleInfo.DEFAULT_MODULE_NAME + "." + */funcName;
		JFunctionType funcTyp = new JFunctionType(
			tname, paramsArray, retType, nsPool, new GlobalFunctionExecutable(funcName, ainfo.create(bodyAst), nsPool));
		
		// Add type, with name same to the function itself, to type table
		// The name uniqueness is guaranteed by enforcing variable name.
		context.getTypTable().addType(tname, funcTyp);
		
		// Add a variable holding function value to global variable table
		MemoryArea memory = context.getHeap();
		FuncValue fv = FuncValue.createGlobalFuncValue(memory, funcTyp);
		
		return fv;
	}
}
