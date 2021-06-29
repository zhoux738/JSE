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

package info.julang.interpretation.errorhandling;

import java.util.ArrayList;
import java.util.List;

import info.julang.execution.threading.ThreadRuntime;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.syntax.ParsedTypeName;
import info.julang.langspec.ast.JulianParser.ArgumentContext;
import info.julang.langspec.ast.JulianParser.Argument_listContext;
import info.julang.langspec.ast.JulianParser.E_function_callContext;
import info.julang.langspec.ast.JulianParser.Expression_statementContext;
import info.julang.langspec.ast.JulianParser.ProgramContext;
import info.julang.langspec.ast.JulianParser.StatementContext;
import info.julang.parser.ANTLRParser;
import info.julang.parser.AstInfo;
import info.julang.typesystem.JType;
import info.julang.typesystem.jclass.JClassType;

/**
 * Provide API to create standard runtime exceptions.
 * 
 * @author Ming Zhou
 */
public final class JSExceptionFactory {
	
	/**
	 * Create a known Julian Script Exception instance.
	 * <p>
	 * The instance will be allocated in heap, initialized, with one of its constructor called.
	 * <b>jse</b> (of type {@link KnownJSException}) provides the Julian source code for calling the constructor.
	 * 
	 * @param jse
	 * @param rt
	 * @param context
	 * @return
	 */
	public static JulianScriptException createException(
			KnownJSException jse, 
			ThreadRuntime rt,
			Context context){
		return createException(jse, rt, context, null);
	}
	
	/**
	 * Create a known Julian Script Exception instance.
	 * <p>
	 * The instance will be allocated in heap, initialized, with one of its constructor, whose parameter list
	 * matches best the given argument list string, called.
	 * 
	 * @param jse
	 * @param rt
	 * @param context
	 * @param initSource the argument list, separated by ',' and enclosed by '(' and ')'. If null, will use
	 * the default arg list as provided by {@link KnownJSException}.
	 * @return
	 */
	public static JulianScriptException createException(
		final KnownJSException jse, 
		final ThreadRuntime rt,
		Context context,
		final String initSource){
		
		String efqn = jse.getFullName();
		
		JSECreator creator = new JSECreator(){
			@Override
			public JulianScriptException create(JClassType etyp, Context context) {
				String code = null;
				if (initSource == null){
					code = "dummy();";
				} else {
					code = "dummy" + initSource;
				}
				ANTLRParser ap = ANTLRParser.createMemoryParser(code);
				ap.parse(true, false);// Since this is called by internals, do not expect a grammar error.
				AstInfo<ProgramContext> ainfo = ap.getAstInfo();

				ProgramContext pc = ainfo.getAST();
				StatementContext sc = pc.executable().statement_list().statement(0);
				Expression_statementContext esc = sc.compound_statement().simple_statement().expression_statement();
				E_function_callContext funcCall = (E_function_callContext) esc.expression();
				Argument_listContext alc = funcCall.function_call().argument_list();
				List<ArgumentContext> alist = alc == null ? new ArrayList<ArgumentContext>() : alc.argument();
				
				JulianScriptException jse = new JulianScriptException(etyp, rt, context, alist, ainfo);
				return jse;
			}
		};
		
		return createException(efqn, context, creator);
	}
	
	private static JulianScriptException createException(String efqn, Context context, JSECreator creator){
		JSExceptionUtility.loadSystemModule(context.getJThread(), context.getModManager(), efqn);
		
		ParsedTypeName ptn = ParsedTypeName.makeFromFullName(efqn);
		
		JType typ = context.getTypeResolver().resolveType(ptn);
		
		JClassType etyp = (JClassType) typ;
		
		JulianScriptException jse = creator.create(etyp, context);
		
		return jse;
	}
	
	private static interface JSECreator {

		JulianScriptException create(JClassType etyp, Context context);
		
	}
	
}
