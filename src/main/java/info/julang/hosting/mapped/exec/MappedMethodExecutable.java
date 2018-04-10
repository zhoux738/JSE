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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import info.julang.JSERuntimeException;
import info.julang.execution.Argument;
import info.julang.execution.Result;
import info.julang.execution.symboltable.ITypeTable;
import info.julang.execution.symboltable.IVariableTable;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.external.exceptions.JSEError;
import info.julang.hosting.HostedMethodManager;
import info.julang.hosting.mapped.MappedTypeConversionException;
import info.julang.hosting.mapped.PlatformConversionUtil;
import info.julang.interpretation.IllegalArgumentsException;
import info.julang.interpretation.RuntimeCheckException;
import info.julang.interpretation.context.Context;
import info.julang.memory.value.JValue;
import info.julang.typesystem.jclass.ICompoundType;

/**
 * An executable for running mapped method. This is effectively a hosted executable.
 * 
 * @author Ming Zhou
 */
public class MappedMethodExecutable extends MappedExecutableBase {

	private Method method;
	
	public MappedMethodExecutable(ICompoundType ofType, Method method, boolean isStatic) {
		super(ofType, isStatic);
		this.method = method;
	}
	
	@Override
	protected void prepareArguments(Argument[] args, Context context) {
		IVariableTable varTable = context.getVarTable();
		ITypeTable tt = context.getTypTable();
		HostedMethodManager hmm = context.getModManager().getHostedMethodManager();
		
		// If it's not static method, the engine will pass "this" object as the first argument. We must exclude this from the 
		// argument list to send to reflection-based method caller, which takes "this" object separately from the arguments. 
		boolean sta = isStatic();
		Object thisArg = null;
		Object[] oargs = new Object[sta ? args.length : args.length - 1];
		
		int i = 0;
		for (Argument arg : args) {
			JValue val = arg.getValue();
			Object obj = null;
			try {
				obj = PlatformConversionUtil.toPlatformObject(val, tt, hmm);
			} catch (MappedTypeConversionException e) {
				throw new IllegalArgumentsException(
					method.getName(), 
					"Unable to call a mapped platform method due to type convertibility issue: " + e.getMessage());
			}
			
			if (i == 0 && !sta) {
				thisArg = obj;
			} else {
				oargs[sta ? i : i - 1] = obj;
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
			
			Object thisInstance = pav.getThis();
			
			Class<?> declClass = method.getDeclaringClass();
			if (thisInstance != null) {
				Class<?> rtClass = thisInstance.getClass();
				if (declClass != rtClass){
					if (declClass.isAssignableFrom(rtClass)) {
						// HACK - but generally safe.
						// Normal, one would use MethodHandles.lookup() to get a default lookup to further get a member defined above
						// the hierarchy of the *current* class. This technique fails when querying arbitrary class. To work around it
						// we locate this secret synthetic field inside MethodHandles.Lookup called IMPL_LOOKUP and bypass its 
						// accessibility check.  
						Field field = null;
						try {
							field = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
						} catch (NoSuchFieldException nsef) {
							throw new RuntimeCheckException("The JVM platform doesn't support designated method dispatching. Java 1.7+ is required.");
						}
						field.setAccessible(true);
						MethodHandles.Lookup lkp = (Lookup) field.get(null);
						
						MethodHandle handle = lkp.findSpecial(
							declClass, // Find a member of the given name on the parent class (rtClass)
							method.getName(),
							MethodType.methodType(method.getReturnType(), method.getParameterTypes()),
							rtClass);
						Object[] objs = pav.getArguments();
						Object obj = null;
						if (objs == null || objs.length == 0) {
							obj = handle.invoke(thisInstance);
						} else {
							// Must use invokeWithArguments if we have an arg list of variable length/types, as 
							// invoke() would interpret Object[] as a single argument. This behavior is very special.
							int l = objs.length;
							Object[] obj2 = new Object[objs.length + 1];
							for(int i = 0; i < l; i++){
								obj2[i+1] = objs[i];
							}
							obj2[0] = thisInstance;
							obj = handle.invokeWithArguments(obj2);
						}
						
						return convertResult(obj, ctxt);
					} else {
						throw new JSEError(
							"Failed calling a mapped method. The runtime type of 'this' object " + rtClass.getName() + 
							" doesn't derive the declared type " + declClass.getName() + ".");
					}				
				}
			}
			
			Object obj = method.invoke(thisInstance, pav.getArguments());

			return convertResult(obj, ctxt);
		} catch (MappedTypeConversionException e) {
			throw new IllegalArgumentsException(
				method.getName(), 
				"Unable to return from a mapped platform method due to type convertibility issue: " + e.getMessage());
		} catch (NoSuchMethodException e) {
			throw new JSEError("Failed calling a mapped method.", e);
		} catch (Throwable e) {
			if (e instanceof JSERuntimeException) {
				throw (JSERuntimeException)e;
			} else {
				Throwable cause = e.getCause();
				throw new InvocationTargetException(cause != null ? cause : e);
			}
		}
	}
	
	private Result convertResult(Object obj, Context ctxt) throws MappedTypeConversionException {
		JValue val = PlatformConversionUtil.fromPlatformObject(obj, ctxt);
		Result res = new Result(val);
		return res;
	}
	
	@Override
	protected String getCalleeType() {
		return "method";
	}
	@Override
	protected String getCalleeName() {
		return method.getName();
		
	}
	@Override
	protected Class<?> getCalleeContainingType() {
		return method.getDeclaringClass();
	}
}
