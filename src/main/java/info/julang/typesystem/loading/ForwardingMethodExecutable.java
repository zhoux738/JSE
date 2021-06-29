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

package info.julang.typesystem.loading;

import org.antlr.v4.runtime.ParserRuleContext;

import info.julang.execution.Argument;
import info.julang.execution.Result;
import info.julang.execution.symboltable.IVariableTable;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.external.exceptions.JSEError;
import info.julang.interpretation.IllegalArgumentsException;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.statement.StatementOption;
import info.julang.memory.value.CustomizedInternalValue;
import info.julang.memory.value.FuncValue;
import info.julang.memory.value.IFuncValue;
import info.julang.parser.AstInfo;
import info.julang.typesystem.jclass.ICompoundType;
import info.julang.typesystem.jclass.MethodExecutable;

public class ForwardingMethodExecutable extends MethodExecutable {

	private static final String PARAM_VAR_NAME = "original_arguments";
	private MethodExecutable fexec;
	private Result resultUponArgumentException;
	
	public ForwardingMethodExecutable(String name, ICompoundType ofType, boolean isStatic, Result resultUponArgumentException) {
		super(name, null, ofType, isStatic);
		this.resultUponArgumentException = resultUponArgumentException;
	}
	
	void setForwardingExecutable(MethodExecutable exec){
		fexec = exec;
	}

	@Override
	protected void prepareArguments(Argument[] args, Context ctxt, IFuncValue func) {
		IVariableTable varTable = ctxt.getVarTable();
		ArgumentsValue av = new ArgumentsValue(args);
		varTable.addVariable(PARAM_VAR_NAME, av);
	}
	
	@Override
	protected Result execute(ThreadRuntime runtime, AstInfo<? extends ParserRuleContext> ast, StatementOption option, Context ctxt){
		ArgumentsValue av = (ArgumentsValue)runtime.getThreadStack().currentFrame().getVariableTable().getVariable(PARAM_VAR_NAME);
		Argument[] args = av.getArguments();

		try {
			return fexec.execute(runtime, FuncValue.DUMMY, args);
		} catch (IllegalArgumentsException ae){
			return resultUponArgumentException;
		} catch (EngineInvocationError e) {
			throw new JSEError("An underlying engine error occured.", e);
		}
	}
}

class ArgumentsValue extends CustomizedInternalValue {

	private Argument[] args;
	
	ArgumentsValue(Argument[] args){
		this.args = args;
	}
	
	Argument[] getArguments() {
		return args;
	}
}
