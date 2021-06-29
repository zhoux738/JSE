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

package info.julang.interpretation.expression.sub;

import java.util.ArrayList;
import java.util.List;

import info.julang.execution.symboltable.Display;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.interpretation.RuntimeCheckException;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.context.ContextType;
import info.julang.interpretation.context.LambdaContext;
import info.julang.interpretation.expression.ExpressionBase;
import info.julang.interpretation.expression.Operand;
import info.julang.interpretation.expression.operand.ValueOperand;
import info.julang.interpretation.syntax.LambdaDeclInfo;
import info.julang.interpretation.syntax.MethodDeclInfo.TypeAndName;
import info.julang.interpretation.syntax.ParsedTypeName;
import info.julang.interpretation.syntax.SyntaxHelper;
import info.julang.langspec.ast.JulianParser.E_lambdaContext;
import info.julang.langspec.ast.JulianParser.ExpressionContext;
import info.julang.memory.value.FuncValue;
import info.julang.memory.value.LambdaValue;
import info.julang.parser.AstInfo;
import info.julang.typesystem.JType;
import info.julang.typesystem.VoidType;
import info.julang.typesystem.jclass.JParameter;
import info.julang.typesystem.jclass.LambdaExecutable;
import info.julang.typesystem.jclass.builtin.JLambdaType;

/**
 * Lambda expression represents a lambda definition, in ABNF form of
 * <pre><code> lambda := param-list '=&lt;' lambda-body
 * 
 * param-list := single-param
 *             | multi-params
 *             
 * single-param := id
 * 
 * multi-params := '(' param ')'    
 * 
 * param := [type] id   
 * 
 * lambda-body := block
 *              | expr</code></pre>
 * Examples:
 * <pre>  1) (string s, int i) =&lt; { ... }
 *  2) (s, i) =&lt; { ... }
 *  3) s =&lt; { ... }
 *  4) (MyPkg.MyClass c, int i) =&lt; ...;
 *  5) (c, i) =&lt; ...;
 *  6) c =&lt; ...;</pre>
 * Notes:
 * <p>
 * (1) In cases of 2), 3), 5), 6), the parameters are untyped. No type checking will be 
 * done at the start of calling the lambda, and runtime exception will be thrown if a
 * type incompatibility cannot be overcome.<br>
 * (2) Untyped and typed parameters can be mixed. <br>
 * <p>
 * 
 * @author Ming Zhou
 */
// | lambda_signature LAMBDA ( ( RETURN? expression ) | block )             # e_lambda        // 70 -- expression is causing ambiguity
public class LambdaExpression extends ExpressionBase {
	
	public LambdaExpression(ThreadRuntime rt, AstInfo<ExpressionContext> ec) {
		super(rt, ec, null);
	}

	@Override
	public Operand evaluate(Context context) {
		E_lambdaContext elc = (E_lambdaContext)ec.getAST();
		LambdaDeclInfo declInfo = SyntaxHelper.parseLambdaExpression(ec.create(elc));
		
		List<JParameter> params = getParameters(context, declInfo);
		JParameter[] paramsArray = new JParameter[params.size()];
		params.toArray(paramsArray);
		
		Display pd = null;
		if (context.getContextType() == ContextType.LAMBDA){
			LambdaContext ldc = (LambdaContext)context;
			pd = ldc.getDisplay();
		}
		
		Display display = new Display(pd, context.getVarTable());
		LambdaExecutable lexe = new LambdaExecutable(context, display, declInfo);
		JLambdaType lambTyp = new JLambdaType(ec, paramsArray, lexe);
		
		FuncValue fv = LambdaValue.createLambdaValue(context.getHeap(), lambTyp, display);
		
		return new ValueOperand(fv);
	}

	private List<JParameter> getParameters(Context context, LambdaDeclInfo declInfo){
		List<TypeAndName> list = declInfo.getParameters();
		List<JParameter> params = new ArrayList<JParameter>();
		if(list != null){
			for(TypeAndName tan : list){
				ParsedTypeName ptn = tan.getTypeName();
				String pn = tan.getParamName();
				SyntaxHelper.checkVarTypeConflict(context, pn);
				
				if(ptn == ParsedTypeName.ANY){
					params.add(new JParameter(pn));
				} else {
					JType typ = context.getTypeResolver().resolveType(ptn);
					if (typ == VoidType.getInstance()) {
						throw new RuntimeCheckException("Cannot use void as parameter type.");
					}
					params.add(new JParameter(pn, typ));
				}
			}
		}

		return params;
	}
}
