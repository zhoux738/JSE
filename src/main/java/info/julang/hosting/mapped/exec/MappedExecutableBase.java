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

package info.julang.hosting.mapped.exec;

import java.lang.reflect.InvocationTargetException;

import org.antlr.v4.runtime.ParserRuleContext;

import info.julang.execution.Argument;
import info.julang.execution.Result;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.execution.threading.ThreadStack;
import info.julang.external.exceptions.JSEError;
import info.julang.hosting.HostingPlatformException;
import info.julang.hosting.PlatformExceptionInfo;
import info.julang.interpretation.IllegalArgumentsException;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.statement.StatementOption;
import info.julang.parser.AstInfo;
import info.julang.typesystem.jclass.ICompoundType;
import info.julang.typesystem.jclass.MethodExecutable;

public abstract class MappedExecutableBase extends MethodExecutable {

	static final String PARAM_VAR_NAME = "platform_arguments";
	
	public MappedExecutableBase(ICompoundType ofType, boolean isStatic) {
		super(null, ofType, isStatic);
	}

	@Override
	protected void preExecute(ThreadRuntime runtime, StatementOption option, Argument[] args){
		ThreadStack stack = runtime.getThreadStack();
		stack.pushFrame();
	}
	
	@Override
	protected void postExecute(ThreadRuntime runtime, Result result){
		runtime.getThreadStack().popFrame();
	}
	
	@Override
	protected Result execute(ThreadRuntime runtime, AstInfo<? extends ParserRuleContext> ast, StatementOption option, Context ctxt) {
		try {
			return executeInternal(runtime, ctxt);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentsException(
					getCalleeName(), 
				"Failed to call a mapped platform " + getCalleeType() + ": " + e.getMessage());
		} catch (InvocationTargetException e) {
			// Convert to JSE exception
			Exception ex = new Exception();
			throw new HostingPlatformException(new PlatformExceptionInfo(
				getCalleeContainingType().getName(), ex.getStackTrace().length, e.getCause()));
		} catch (IllegalAccessException e) {
			throw new JSEError("Failed to call a mapped platform " + getCalleeType() + ": " + e.getMessage());
		}
	}

	protected abstract Result executeInternal(ThreadRuntime runtime, Context ctxt) throws InvocationTargetException, IllegalAccessException;
	protected abstract String getCalleeType();
	protected abstract String getCalleeName();
	protected abstract Class<?> getCalleeContainingType();
}
