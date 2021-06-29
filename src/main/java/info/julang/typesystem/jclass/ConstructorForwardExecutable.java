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

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;

import info.julang.execution.Argument;
import info.julang.execution.MultiValueResult;
import info.julang.execution.Result;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.statement.ExpressionStatement;
import info.julang.interpretation.statement.StatementOption;
import info.julang.langspec.Keywords;
import info.julang.langspec.ast.JulianParser.ArgumentContext;
import info.julang.langspec.ast.JulianParser.Argument_listContext;
import info.julang.langspec.ast.JulianParser.Function_callContext;
import info.julang.memory.value.IFuncValue;
import info.julang.memory.value.JValue;
import info.julang.parser.AstInfo;

/**
 * The executable for a constructor forward call.
 * <p>
 * A constructor's executable is made up by two parts: forward call and main body. 
 * The forward call is about calling another constructor before the main body is 
 * executed. In Julian, a forward call can be made against either another constructor 
 * in the same class, or a constructor in a parent class. If no forward call is present, 
 * will default to call the parent's parameter-less constructor.
 * <p>
 * The {@link ConstructorForwardExecutable#execute(ThreadRuntime runtime, IFuncValue func, Argument[] args) execute()} 
 * method will return a multi-value result representing each of the argument expression.
 * It is safe to cast it to the type of {@link MultiValueResult}.
 * <br>
 * @author Ming Zhou
 */
public class ConstructorForwardExecutable extends MethodExecutable implements Cloneable {
	
	public ConstructorForwardExecutable(boolean isSuper, AstInfo<Function_callContext> ainfo, JClassType ofType) {
		super(isSuper ? Keywords.SUPER : Keywords.THIS, ainfo, ofType, false);
	}

	@Override
	protected Result execute(ThreadRuntime runtime, AstInfo<? extends ParserRuleContext> ast, StatementOption option, Context ctxt){
		@SuppressWarnings("unchecked")
		AstInfo<Function_callContext> ainfo = (AstInfo<Function_callContext>)ast;
		List<JValue> values = new ArrayList<JValue>();
		
		List<ArgumentContext> args = null;
		Argument_listContext alc = ((Function_callContext)ainfo.getAST()).argument_list();
		if (alc != null) {
			args = alc.argument();
		} else {
			args = new ArrayList<ArgumentContext>();
		}
		
		for (ArgumentContext arg : args) {
			ExpressionStatement es = new ExpressionStatement(runtime, ainfo.create(arg.expression()));
			es.interpret(ctxt);
			
			JValue val = es.getResult().getReturnedValue(false);
			values.add(val);
		}
		
		// Return the results of all the expression in a MultiValueResult
		JValue[] varray = new JValue[values.size()];
		values.toArray(varray);
		Result res = new MultiValueResult(varray);
		return res;
	}
	
	@Override
	protected void prepareArguments(Argument[] args, Context ctxt, IFuncValue func) {
		super.replicateArgsAndBindings(args, ctxt, func, false);
	}
}
