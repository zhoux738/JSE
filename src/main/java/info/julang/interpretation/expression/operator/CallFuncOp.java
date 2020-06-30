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

package info.julang.interpretation.expression.operator;

import static info.julang.langspec.Operators.FUNCCALL;

import info.julang.execution.Argument;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.external.exceptions.JSEError;
import info.julang.external.interfaces.JValueKind;
import info.julang.interpretation.IllegalArgumentsException;
import info.julang.interpretation.JNullReferenceException;
import info.julang.interpretation.RuntimeCheckException;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.expression.Operand;
import info.julang.interpretation.expression.Operator;
import info.julang.interpretation.expression.operand.InstMemberOperand;
import info.julang.interpretation.expression.operand.NameOperand;
import info.julang.interpretation.expression.operand.OperandKind;
import info.julang.interpretation.expression.operand.StaticMemberOperand;
import info.julang.interpretation.internal.FuncCallExecutor;
import info.julang.memory.value.FuncValue;
import info.julang.memory.value.IMethodValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.MethodGroupValue;
import info.julang.memory.value.MethodValue;
import info.julang.memory.value.ObjectValue;
import info.julang.memory.value.RefValue;
import info.julang.typesystem.AnyType;
import info.julang.typesystem.JType;
import info.julang.typesystem.JTypeKind;
import info.julang.typesystem.conversion.Convertibility;
import info.julang.typesystem.jclass.JParameter;
import info.julang.typesystem.jclass.builtin.JFunctionType;
import info.julang.typesystem.jclass.builtin.JMethodType;
import info.julang.typesystem.jclass.builtin.JObjectType;

public class CallFuncOp extends Operator {

	private FuncCallExecutor exec;
	
	/**
	 * Create a new CallFuncOp, the operator for calling a function.
	 * 
	 * @param rt thread runtime in which this function is to be invoked.
	 * @param argNumber the number of arguments. This plus one is equal to the arity of this operator.
	 */
	public CallFuncOp(ThreadRuntime rt, int argNumber) {
		super("func(...)", argNumber + 1, FUNCCALL.precedence, FUNCCALL.associativity);
		exec = new FuncCallExecutor(rt);
	}

	@Override
	public Operand apply(Context context, Operand[] operands){
		// get the callee (the function to be called)
		Operand callee = operands[0];
		
		JValue lval = getValue(context, callee);
		
		String calleeAsName = null;
		boolean tryNameAndValue = false;
		OperandKind odkind = callee.getKind();
		if(odkind == OperandKind.NAME){
			calleeAsName = ((NameOperand)callee).getName();
			tryNameAndValue = true;
		} else if (odkind == OperandKind.VALUE || odkind == OperandKind.INDEX) {
			tryNameAndValue = true;
		}
		
		if(tryNameAndValue){
			// 1) if the callee is a name or value, check if it is a method
			lval = lval.deref();
			ObjectValue ov = null;
			if(lval == RefValue.NULL) {
				throw new JNullReferenceException();
			} else if (lval instanceof ObjectValue) {
				ov = (ObjectValue)lval;
			}
			
			if (ov != null && ov.getBuiltInValueKind() == JValueKind.FUNCTION) {
				FuncValue fv = (FuncValue) ov;
				JMethodType mt = null;
				
				switch(fv.getFuncValueKind()){
				case METHOD:
					MethodValue mv = (MethodValue) fv;
					mt = mv.getMethodType();
					// Fall through
				case METHOD_GROUP:
					IMethodValue imv = (IMethodValue) fv;
					boolean isStatic = imv.isStatic();
					JValue thisVal = isStatic ? null : imv.getThisValue();
					
					// Select one from overloaded methods.
					if(mt == null){
						MethodValue[] mvs = extractMethodValues(imv);
						if (mvs != null) {
							JValue[] args = retrieveArgumentValues(context, operands);
							mv = selectOverloadedMethod(mvs, args, isStatic);
							if (mv != null){
								mt = mv.getMethodType();
							}
						}
					}
					
					if(mt == null){
						throw new IllegalArgumentsException(calleeAsName, "No overloaded method matches the arguments.");
					}
					
					return callMethod(context, mt, thisVal, calleeAsName, operands, false);
				case FUNCTION:
					// 1.2) a global function
					return callFunction(
						context, 
						(JFunctionType)fv.getType(), 
						calleeAsName != null ? calleeAsName : "<function unknown>", 
						operands);
				default:
					throw new JSEError("The callee operand in function call has a function type not recognized.");
				}				
			}
			
			throw new RuntimeCheckException("The target is not a function and cannot be invoked.");
		}

// (This branch is no longer in use since global function is added to GVT as a variable)
//		// 2) try the callee as a function type 
//		// this is true for the global function
//		if(lval.getKind() == JValueKind.TYPE){
//			return callFunc(context, (TypeValue)lval, calleeAsName != null ? calleeAsName : "<function unknown>", operands);
//		}
		
		// 3) try the callee as a method type
		if(callee.getKind() == OperandKind.IMEMBER){
			// 3.1) instance method
			InstMemberOperand memOd = (InstMemberOperand) callee;
			
			// get *this* object
			JValue instance = memOd.ofObject();
			String mName = memOd.getName();
			ObjectValue val = RefValue.dereference(memOd.getMemberValue());
			
			Operand op = callMethodValue(context, val, memOd.getExtensionMethods(), instance, mName, operands);
			if (op != null) {
				return op;
			} else {
				throw new RuntimeCheckException("The target is not a function and cannot be invoked.");
			}
		} else if(callee.getKind() == OperandKind.SMEMBER){
			// 3.2) static method
			StaticMemberOperand memOd = (StaticMemberOperand) callee;
			
			String mName = memOd.getName();
			ObjectValue val = RefValue.dereference(memOd.getValue());
			
			Operand op = callMethodValue(context, val, null, null, mName, operands);
			if (op != null) {
				return op;
			} else {
				throw new RuntimeCheckException("The target is not a function and cannot be invoked.");
			}
		}
		
		throw new JSEError("An operand of type " + callee.getKind() + " cannot be invoked.");
	}
	
	// args doesn't contain "this"
	private MethodValue selectOverloadedMethod(MethodValue[] mvs, JValue[] args, boolean isStatic) {
		for(MethodValue mv : mvs){
			JMethodType mt = mv.getMethodType();
			JParameter[] params = mt.getParams();
			int start = isStatic ? 0 : 1;
			int total = isStatic ? params.length : params.length - 1;
			if(args.length == total){
				boolean found = true;
				for(int i = start, j = 0; i < params.length; i++, j++){
					JType ptyp = params[i].getType();
					JType atyp = args[j].getType();
					if (atyp == null || atyp == AnyType.getInstance()){
						JValue aval = args[j];
						// Special Case: if the argument is an untyped null value, consider it a match as long as the param is Object
						if (RefValue.isGenericNull(aval) && 
							(ptyp.getKind() == JTypeKind.PLATFORM || 
							 ptyp.getConvertibilityTo(JObjectType.getInstance()).isSafe())){
							continue;
						} else {
							atyp = aval.deref().getType();
						}
					} 

					Convertibility conv = atyp.getConvertibilityTo(ptyp);
					if(conv.isSafe()){
						continue;
					}
					
					found = false;
					break;
				}
				
				if (found){
					return mv;
				}
			}
		}
		
		return null;
	}

	// return null if the given method value is not callable against the given arguments.
	private Operand callMethodValue(
		Context context, 
		ObjectValue methodVal,
		MethodGroupValue extMethodVals, // null if it's static (but not extension) method
		JValue thisObj,  				// null if it's static (but not extension) method
		String methodName, 
		Operand[] operands){
		
		JMethodType mTyp = null;
		JValue[] args = null;
		boolean checkedOverloaded = false;
		boolean emptyOverloaded = false;
		
		// First try to use the method value, if provided
		if(methodVal instanceof IMethodValue){
			IMethodValue imv = (IMethodValue) methodVal;
			switch(imv.getFuncValueKind()){
			case METHOD:
				mTyp = (JMethodType) methodVal.getType();
				break;
			case METHOD_GROUP:
				// Special handling for invoke()
				if (thisObj instanceof MethodGroupValue && JFunctionType.MethodName_invoke.equals(methodName)){
					return invokeDynamic(context, (FuncValue)thisObj, operands, methodName);
				}
				
				checkedOverloaded = true;
				MethodValue[] mvs = extractMethodValues(imv);
				if (mvs != null) {
					args = retrieveArgumentValues(context, operands);
					MethodValue mv = selectOverloadedMethod(mvs, args, thisObj == null);
					if (mv != null){
						mTyp = mv.getMethodType();
					}
				} else {
					emptyOverloaded = true;
				}

				break;
				
			default:
				break;
			}
		}

		// If no inherent members are found, try extension methods.
		//
		// DESIGN NOTE: Extension methods do not participate in regular overloading resolution. If a member of 
		// the same name is already defined, either directly or by inheritance, on the object's type, no 
		// extension methods will be tried. 
		if (mTyp == null && emptyOverloaded && extMethodVals != null) {
			if (args == null) {
				args = retrieveArgumentValues(context, operands);
			}
			
			// Extension method is indeed static, but we prepare the arguments as if it were instance-scoped.
			MethodValue mv = selectOverloadedMethod(extMethodVals.getMethodValues(), args, /* isStatic */ false);
			if (mv != null){
				mTyp = mv.getMethodType();
			}
		}
		
		if (mTyp != null) {
			return callMethod(
				context,
				mTyp,
				thisObj, // Used for both instance and extension methods
				methodName,
				operands,
				false); // Strong-typed
		} else if (checkedOverloaded) {
			if (args == null) {
				args = retrieveArgumentValues(context, operands);
			}
			
			StringBuilder sb = new StringBuilder("Cannot find an overloaded version that accepts arguments of type (");
			int len = args.length - 1;
			for(int i = 0; i < len; i++){
				sb.append(args[i].getType().getName());
				sb.append(", ");
			}
			if (len >= 0){
				sb.append(args[len].getType().getName());
			}
			if (args.length == 0){
				sb.append("void");
			}
			sb.append(")");
			throw new IllegalArgumentsException(methodName, sb.toString());
		}

		return null;
	}
	
	/**
	 * Return null if the group value contains no method values.
	 */
	private MethodValue[] extractMethodValues(IMethodValue imv) {
		MethodValue[] mvs = ((MethodGroupValue)imv).getMethodValues();
		return mvs.length > 0 ? mvs : null;
		
	}
	
	private Operand callMethod(
		Context context, 
		JMethodType mTyp, 
		JValue thisObj, 
		String methodName,
		Operand[] operands, 
		boolean looseTyping){
		if(mTyp != null){
			// Special handling for invoke()
			if (thisObj instanceof FuncValue && JFunctionType.MethodName_invoke.equals(mTyp.getName())){
				return invokeDynamic(context, (FuncValue)thisObj, operands, methodName);
			}
			
			Argument[] args = prepareArguments(context, operands, mTyp, thisObj, methodName);
			Operand res = Operand.createOperand(exec.invokeFunction(mTyp, methodName, args));
			return res;
		}
		return null;
	}

	private Operand callFunction(
		Context context, 
		JFunctionType funcType, 
		String funcName, 
		Operand[] operands){
		// prepare argument list
		Argument[] args = prepareArguments(context, operands, funcType, null, funcName);

		Operand res = Operand.createOperand(
			exec.invokeFunction(funcType, funcName, args));
		
		return res;
	}
	
	// If calling invoke() on a function value, we call the function itself instead.
	private Operand invokeDynamic(
		Context context, 
		FuncValue funcObj, 
		Operand[] operands,
		String methodName){
		exec.setLooseTyping(true);
		switch(funcObj.getFuncValueKind()){
		case FUNCTION:
			JFunctionType jtp = (JFunctionType)funcObj.getType();
			return callFunction(context, jtp, methodName, operands);
		case METHOD:
			JMethodType jmtp = (JMethodType)funcObj.getType();
			boolean sta = false;
			if (jmtp.isHosted()){
				sta = jmtp.getHostedExecutable().isStatic();
			} else {
				sta = jmtp.getMethodExecutable().isStatic();
			}
			
			if (sta){
				return callMethod(context, jmtp, null, jmtp.getName(), operands, true);
			} else {
				MethodValue mv = (MethodValue)funcObj;
				return callMethod(context, jmtp, mv.getThisValue(), jmtp.getName(), operands, true);
			}
		case METHOD_GROUP:
			// Matching rule for dynamic invocation on overloaded methods. Guideline - use as few default values as possible.
			// 1) the method with exact same number of params (no default value is needed)
			// 2) the method with largest number of params yet less than that of args (no default value is needed)
			// 3) the method with smallest number of params yet more than that of args
			// (as of 0.1.9, param type is not considered)
			
			MethodGroupValue mgv = (MethodGroupValue) funcObj;
			MethodValue[] mvs = mgv.getMethodValues();
			MethodValue defaultMethod = mvs[0];
			MethodValue moreArgsMethod = null;
			MethodValue lessArgsMethod = null;
			// The arg 0 is the function object, which we count as "this" in instance method 
			// (although they are completely unrelated). Therefore -1 for static method.
			int num = defaultMethod.isStatic() ? operands.length - 1: operands.length;
			int maxArg = -1;
			int minArg = Integer.MAX_VALUE;
			for (int i = 0; i < mvs.length; i++){
				MethodValue fv = mvs[i];
				int cnt = fv.getMethodType().getParams().length;
				if (cnt == num){
					return invokeDynamic(context, fv, operands, methodName);
				} else if (cnt < num && cnt > maxArg){
					// Default to a method which has the highest number of params yet less than that of args 
					maxArg = cnt;
					moreArgsMethod = fv;
				} else if (cnt > num && cnt < minArg){
					// Default to a method which has the highest number of params yet less than that of args 
					minArg = cnt;
					lessArgsMethod = fv;
				} 
			}

			if (moreArgsMethod != null){
				defaultMethod = moreArgsMethod;
			} else if (lessArgsMethod != null){
				defaultMethod = lessArgsMethod;
			}
			
			// If none is found, just return the 1st one.
			return invokeDynamic(context, defaultMethod, operands, methodName);
		case CONSTRUCTOR:
			// Impossible
		default:
			throw new JSEError("Cannot call a function with kind = " + funcObj.getFuncValueKind().name());
		}
	}
	
	// Extract value from operands
	private JValue[] retrieveArgumentValues(Context context, Operand[] operands){
		JValue[] vals = new JValue[operands.length - 1];
		for(int i=1;i<operands.length;i++){ // skip the 1st operand (function itself)
			vals[i-1] = getValue(context, operands[i], true, false);
		}
		
		return vals;
	}
	
	// Extract value from operands and replicate them in current frame
	private Argument[] prepareArguments(
		Context context, Operand[] operands, JFunctionType funcType, JValue instance, String funcName){
		JValue[] vals = new JValue[operands.length];
		for(int i=0;i<operands.length;i++){
			vals[i] = getValue(context, operands[i], true, false);
		}
		
		// Note vals[0] is the function value and will not be used to convert to an argument, 
		// so we passed true to last argument (skipFirstValue).
		return exec.prepareArguments(funcName, funcType, vals, instance, true);
	}

	@Override
	protected Operand doApply(Context context, Operand[] operands) {
		throw new JSEError("This method should not be called.");
	}
}
