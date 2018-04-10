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

import info.julang.execution.Argument;
import info.julang.execution.Result;
import info.julang.execution.symboltable.ITypeTable;
import info.julang.execution.symboltable.IVariableTable;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.external.exceptions.JSEError;
import info.julang.hosting.HostedMethodManager;
import info.julang.hosting.HostingPlatformException;
import info.julang.hosting.PlatformExceptionInfo;
import info.julang.hosting.mapped.MappedTypeConversionException;
import info.julang.hosting.mapped.PlatformConversionUtil;
import info.julang.interpretation.IllegalArgumentsException;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.statement.StatementOption;
import info.julang.memory.value.HostedValue;
import info.julang.memory.value.JValue;
import info.julang.parser.AstInfo;
import info.julang.typesystem.jclass.ICompoundType;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.antlr.v4.runtime.ParserRuleContext;

/**
 * An executable for running mapped constructor. This is effectively a hosted executable.
 * <p>
 * Analogous to most programming languages, a constructor is actually called as an initializer.
 * The allocation of the "this" object is not handled by this executable, which trusts that 
 * the value sent to is a freshly allocated clean object waiting to be initialized. In this case,
 * the value is also a {@link HostedValue}. The upstream logic is responsible for creating
 * object of right type based on the annotation.
 * 
 * @author Ming Zhou
 */
public class MappedConstructorExecutable extends MappedExecutableBase {

	private Constructor<?> ctor;
	
	public MappedConstructorExecutable(ICompoundType ofType, Constructor<?> ctor) {
		super(ofType, false);
		this.ctor = ctor;
	}
	
	@Override
	protected void prepareArguments(Argument[] args, Context context) {
		IVariableTable varTable = context.getVarTable();
		ITypeTable tt = context.getTypTable();
		HostedMethodManager hmm = context.getModManager().getHostedMethodManager();
		
		// Since this is an instance constructor, the engine will pass "this" object as the first argument. 
		// We must exclude it from the argument list to send to reflection-based method caller, which will
		// create a JVM object on its own to initialize.
		HostedValue thisArg = null;
		Object[] oargs = new Object[args.length - 1];
		
		int i = 0;
		for (Argument arg : args) {
			JValue val = arg.getValue();			
			if (i == 0) {
				// For "this" object, we are expecting a hosted value, inside which the platform object shall reside.
				// Save it aside, and set platform object after getting result from the ctor call.
				thisArg = (HostedValue)val;
			} else {
				Object obj = null;
				try {
					// Convert other arguments to platform objects to send to ctor
					obj = PlatformConversionUtil.toPlatformObject(val, tt, hmm);
				} catch (MappedTypeConversionException e) {
					throw new IllegalArgumentsException(
						ctor.getName(), 
						"Unable to call a mapped platform constructor due to type convertibility issue: " + e.getMessage());
				}
				oargs[i-1] = obj;
			}
			i++;
		}
		
		// Use the thread-specific variable table to carry the arguments to execute() method. 
		PlatformArgumentsValue pav = new PlatformArgumentsValue(thisArg, oargs);
		varTable.addVariable(PARAM_VAR_NAME, pav);
	}
	
	@Override
	protected Result executeInternal(ThreadRuntime runtime, Context ctxt) throws InvocationTargetException, IllegalAccessException {
		try {
			PlatformArgumentsValue pav = (PlatformArgumentsValue)
				runtime.getThreadStack().currentFrame().getVariableTable().getVariable(PARAM_VAR_NAME);
			
			Object obj = ctor.newInstance(pav.getArguments());
			
			HostedValue thisArg = (HostedValue)pav.getThis();
			thisArg.setHostedObject(obj);
			Result res = new Result(thisArg);
			return res;
		} catch (InstantiationException ex) {
			throw new HostingPlatformException(new PlatformExceptionInfo(
				getCalleeContainingType().getName(), ex.getStackTrace().length, ex));
		}
	}
	
	@Override
	protected String getCalleeType() {
		return "constructor";
	}
	@Override
	protected String getCalleeName() {
		return ctor.getName();
		
	}
	@Override
	protected Class<?> getCalleeContainingType() {
		return ctor.getDeclaringClass();
	}
}
