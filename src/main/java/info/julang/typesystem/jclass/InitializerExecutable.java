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

import info.julang.execution.Argument;
import info.julang.execution.Result;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.statement.StatementOption;
import info.julang.langspec.ast.JulianParser.Expression_statementContext;
import info.julang.memory.value.IFuncValue;
import info.julang.parser.AstInfo;

import org.antlr.v4.runtime.ParserRuleContext;

/**
 * The executable for method member initialization.
 * <p>
 * The main difference of this method from its super-class is to preserve the result.
 * 
 * @author Ming Zhou
 */
public class InitializerExecutable extends MethodExecutable implements Cloneable {

	public InitializerExecutable(String name, AstInfo<Expression_statementContext> ainfo, ICompoundType ofType, boolean isStatic) {
		super(name, ainfo, ofType, isStatic);
	}
	
	@Override
	protected Result execute(ThreadRuntime runtime, AstInfo<? extends ParserRuleContext> ainfo, StatementOption option, Context ctxt) throws EngineInvocationError {
		option.setPreserveStmtResult(true);
		return super.execute(runtime, ainfo, option, ctxt);
	}
	
	@Override
	protected void prepareArguments(Argument[] args, Context ctxt, IFuncValue func) {
		super.replicateArgsAndBindings(args, ctxt, func, false);
	}
}
