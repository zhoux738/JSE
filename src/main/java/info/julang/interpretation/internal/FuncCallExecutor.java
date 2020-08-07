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

package info.julang.interpretation.internal;

import info.julang.execution.Argument;
import info.julang.execution.Executable;
import info.julang.execution.Result;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.external.exceptions.EngineInvocationError;
import info.julang.external.exceptions.JSEError;
import info.julang.external.interfaces.JValueKind;
import info.julang.interpretation.IllegalArgumentsException;
import info.julang.interpretation.RuntimeCheckException;
import info.julang.interpretation.errorhandling.JulianScriptException;
import info.julang.memory.value.FuncValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.RefValue;
import info.julang.memory.value.UntypedValue;
import info.julang.memory.value.ValueUtilities;
import info.julang.typesystem.AnyType;
import info.julang.typesystem.JType;
import info.julang.typesystem.JTypeKind;
import info.julang.typesystem.VoidType;
import info.julang.typesystem.conversion.Convertibility;
import info.julang.typesystem.conversion.TypeIncompatibleException;
import info.julang.typesystem.jclass.ICompoundType;
import info.julang.typesystem.jclass.JParameter;
import info.julang.typesystem.jclass.builtin.FunctionKind;
import info.julang.typesystem.jclass.builtin.JFunctionType;
import info.julang.typesystem.jclass.builtin.JMethodType;
import info.julang.typesystem.jclass.builtin.JObjectType;

/**
 * A stateful facade that executes a script function or method.
 * <p>
 * Generally, the executor performs the following steps:
 * <p>
 * (1) preparing the arguments <br>
 * (2) type-checking arguments <br>
 * (3) calling the executable <br>
 * (4) type-checking returned value <br>
 * (5) replicating returned value <br>
 * (6) filling in stack trace in case of exception
 * <p>
 * Notably, this class doesn't handle which method in a method group is to be chosen.
 * The concrete executable must be always provided as one of the arguments.
 * <p>
 * The executor can operate in two modes: strict typing (by default) and loose typing. 
 * If loose typing is used, it will behave differently in (1) and (4). See 
 * {@link #setLooseTyping(boolean looseTyping)} for more details.
 * 
 * @author Ming Zhou
 */
public class FuncCallExecutor {

	private ThreadRuntime rt;

	private boolean looseTyping;
	
	/**
	 * Create a new executor.
	 * 
	 * @param rt the thread runtime in which the function is to be called.
	 */
	public FuncCallExecutor(ThreadRuntime rt) {
		this.rt = rt;
	}
	
	/**
	 * Set whether to use loose typing. Setting this to true will affect certain behaviors of calling procedure.
	 * <p>
	 * (1) If arguments are less than parameters, remaining parameters are initialized with default values; 
	 * if arguments are more than parameters, excessive arguments are ignored.
	 * <p>
	 * (2) If returned value type is {@link info.julang.typesystem.VoidType void}, create a default value of the declared return type.  
	 * 
	 * @param looseTyping
	 */
	public void setLooseTyping(boolean looseTyping) {
		this.looseTyping = looseTyping;
	}
	
	/**
	 * Invoke a function, regardless of its underlying type (method/globla/lambda/etc.).
	 * <p>
	 * The call will push a new frame, run the code, and pop the frame at the end. 
	 * See {@link info.julang.interpretation.InterpretedExecutable InterpretedExecutable}.
	 * <p>
	 * After the call returns, it replicates the returned result in current frame.
	 * <p>
	 * The caller is responsible for providing arguments that match the signature of executable.
	 * In particular, <b>it must set up an argument for <i>this</i> at index 0</b> if it's an instance method.
	 * 
	 * @param funcType the function declaration
	 * @param funcName the function's simple name
	 * @param args Actual arguments passed in
	 * @return
	 */
	public JValue invokeFunction(
		JFunctionType funcType, 
		String funcName, 
		Argument[] args){
		
		return invoke(funcType, funcType.getExecutable(), funcName, args, funcType.getParams());
	}
	
	/**
	 * Invoke a method with instance (if not static) and value array as arguments.
	 * <p>
	 * This method is reserved for calls by engine internals.
	 * 
	 * @param methodType
	 * @param methodName
	 * @param values doesn't contain 'this' object
	 * @param instance null if it is a static method.
	 * @return
	 */
	public JValue invokeMethodInternal(
		JMethodType methodType,
		String methodName,
		JValue[] values, 
		JValue instance){
		Argument[] args = prepareArguments(methodName, methodType, values, instance, false);
		return invokeFunction(methodType, methodName, args);
	}
	
	/**
	 * Invoke a function value with instance (if an instance method) and value array as arguments.
	 * <p>
	 * This method is reserved for calls by engine internals.
	 * 
	 * @param funcVal
	 * @param funcName
	 * @param values
	 * @param instance
	 * @return
	 */
	public JValue invokeFuncValueInternal(
		FuncValue funcVal, 
		String funcName,
		JValue[] values,  
		JValue instance){
		JFunctionType funcType = (JFunctionType)funcVal.getType();
		if (funcType.getFunctionKind() == FunctionKind.METHOD_GROUP){
			// TODO - support this.
			throw new JSEError("Overloaded methods cannot be invoked by FuncCallExecutor.");
		}
		Argument[] args = prepareArguments(funcName, funcType, values, instance, instance != null);
		return invoke(funcType, funcType.getExecutable(), funcName, args, funcType.getParams());
	}
	
	/**
	 * Prepare arguments based on given array of {@link JValue}.
	 * <p>
	 * This method is a separate API endpoint from the various invocation methods, enabling the user
	 * to inspect the prepared arguments before making a call.
	 * 
	 * @param funcName
	 * @param funcType
	 * @param values the values to be used as arguments. Must not contain <b>this</b> for instance method.
	 * @param instance null if it is a function or static method.
	 * @param skipFirstValue if true, the first element in <code>values</code> will not be converted to 
	 * an argument in the resultant array.
	 * @return
	 */
	public Argument[] prepareArguments(
		String funcName, 
		JFunctionType funcType, 
		JValue[] values, 
		JValue instance, 
		boolean skipFirstValue){
		JParameter[] params = funcType.getParams();
		
		Argument[] args = new Argument[params.length];
		int start = 0;
		if(instance != null){
			args[0] = Argument.CreateThisArgument(instance);
			start = 1;
		}

		int startIndex = skipFirstValue ? 1 : 0;
		int valuesPassedIn = values.length - startIndex + start;
		if (!looseTyping && valuesPassedIn > args.length){
			// More arguments than params
			throw new RuntimeCheckException("Wrong number of arguments when calling " + funcName + ".");
		}
		
		for(int i = start, j = startIndex; i < args.length; i++, j++){
			JValue aval = null;
			JParameter param = params[i];
			if(j >= values.length){
				// Less arguments than params
				if (!looseTyping){
					throw new RuntimeCheckException("Wrong number of arguments when calling " + funcName + ".");
				} else {
					aval = ValueUtilities.makeDefaultValue(rt.getStackMemory().currentFrame(), param.getType(), false);
				}
			}
			
			if (aval == null){
				aval = values[j];
				if(param.isUntyped() && aval.getKind() != JValueKind.UNTYPED){
					// If the parameter is untyped, but what is passed in is not, 
					// we must wrap the argument in an untyped value.
					aval = new UntypedValue(rt.getStackMemory().currentFrame(), aval);
				}
			}
			
			args[i] = new Argument(
				param.getName(), // argument name is from parameter name
				aval); // argument value is from operand
		}
		
		return args;
	}
	
	/**
	 * Invoke a given executable.
	 * 
	 * @param funcType the function declaration
	 * @param exec The executable to invoke
	 * @param funcName the function's simple name
	 * @param args Actual arguments passed in
	 * @param params Parameters as declared
	 * 
	 * @return the returned value now sitting in current frame.
	 */
	private JValue invoke(
		JFunctionType funcType, 
		Executable exec, 
		String funcName, 
		Argument[] args, 
		JParameter[] params){		
		// 1) (For typed function) Check arguments' type
		boolean isTyped = funcType.isTyped();
		if(isTyped){
			checkArgTypes(funcName, args, params);
		}
		
		try {
			// 2) Execute
			Result result = exec.execute(rt, args);
			
			// 3) Get the returned value
			JValue val = result.getReturnedValue(false);
			JType typ = val.getType();
			if(typ == null){
				// We allow typ to be null only if it is a generic null value.
				if(val != RefValue.NULL && !RefValue.isGenericNull(val)){
					throw new JSEError("A returned value has no type.");
				}
			}
			
			// 4) (For typed function) Check the type of returned value
			JType retTyp = null;
			// (Optional behavior: if we are using loose typing, and function returns 
			// void, coerce the value to the default of declared type. For example, 
			// return 0 if the declared return type is int; null if Object or untyped)
			boolean looseReturn = this.looseTyping && typ == VoidType.getInstance();
	
			if (isTyped && !funcType.getReturn().isUntyped() && typ != null){
				retTyp = funcType.getReturnType();
				if(typ == AnyType.getInstance()){
					typ = UntypedValue.unwrap(val).getType();
				}
				Convertibility conv = typ.getConvertibilityTo(retTyp);
				switch(conv){
				case DEMOTED:
				case PROMOTED:
					// If we return a value with different but somewhat compatible basic type, 
					// we must replicate it in the original frame using the declared type. 
					break;
				case CASTABLE:
					if (!looseReturn){
						throw new TypeIncompatibleException(typ, retTyp, true);
					}
				case UNCONVERTIBLE:
				case UNSAFE:
					if (!looseReturn){
						throw new TypeIncompatibleException(typ, retTyp);
					}
				default:
					// Reset this to null so that replicateValue() can derive its type from the actual value.
					retTyp = null;
				}
			}

			// 5) Replicate the returned value in current frame
			if (typ != null && typ.getKind() != JTypeKind.VOID){
				// At this moment, if retTyp == null, the new value will use the type of the one passed in (val)
				val = ValueUtilities.replicateValue(val, retTyp, rt.getStackMemory().currentFrame());
			} else if (looseReturn){
				// If the declared function returns void, coerce it to a null of type Object
				retTyp = funcType.getReturnType();
				if (retTyp == VoidType.getInstance()){
					retTyp = JObjectType.getInstance();
				}
				val = ValueUtilities.makeDefaultValue(rt.getStackMemory().currentFrame(), retTyp, false);
			}
			return val;
		} catch (JulianScriptException jse){
			// Capture JSE (step 2/2):
			// At this point we have method's name and parameter information, so we can add a stack trace into the exception.
			String fn = jse.getFileName();
			int lineNo = jse.getLineNumber();
			jse.addStackTrace(rt.getTypeTable(), funcType.getName(), JParameter.getParamNames(params), fn, lineNo);
			throw jse;
		} catch (EngineInvocationError e) {
			throw new JSEError("An error occurs while invoking " + funcType.getName());
		}
	}

	/**
	 * Check length equality and type compatibilities between arguments and parameters.
	 * <p>
	 * This method will replace arguments having untyped null value with a typed null, according to 
	 * the declared type of corresponding parameter.
	 * 
	 * @param funcName
	 * @param args
	 * @param params
	 */
	static void checkArgTypes(
		String funcName, Argument[] args, JParameter[] params) {
		if(args.length != params.length){
			throw new IllegalArgumentsException(funcName, "Wrong number of arguments");
		}
		
		for(int i=0;i<args.length;i++){
			JValue val = args[i].getValue();
			JParameter jp = params[i];
			if(jp.isUntyped()){
				continue;
			}
			
			JType typ = jp.getType();
			if(RefValue.isGenericNull(val)){
				JTypeKind kind = typ.getKind();
				if (kind == JTypeKind.CLASS || kind == JTypeKind.PLATFORM){
					// If it is a generic null, replace it with a typed null to comply with function declaration.
					RefValue rv = RefValue.makeNullRefValue(
						val.getMemoryArea(), kind == JTypeKind.CLASS ? (ICompoundType)typ : JObjectType.getInstance());
					args[i].setValue(rv);
					continue;
				}
			}
			
			checkConvertibility(val, typ);
		}
	}

	/**
	 * Check the convertibility from argument to parameter type.
	 * 
	 * @param val argument value
	 * @param typ declared parameter type
	 */
	private static void checkConvertibility(JValue val, JType typ){
		JType argTyp = val.getType();
		Convertibility conv = argTyp.getConvertibilityTo(typ);
		if(conv == Convertibility.UNCONVERTIBLE){
			throw new TypeIncompatibleException(argTyp, typ);
		} else if (conv == Convertibility.UNSAFE && argTyp == AnyType.getInstance()){
			UntypedValue uv = (UntypedValue) val;
			checkConvertibility(uv.getActual(), typ);
		}
	}
}
