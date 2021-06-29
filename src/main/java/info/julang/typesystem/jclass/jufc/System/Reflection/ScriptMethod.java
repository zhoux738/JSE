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

package info.julang.typesystem.jclass.jufc.System.Reflection;

import info.julang.JSERuntimeException;
import info.julang.execution.Argument;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.execution.threading.ThreadRuntimeHelper;
import info.julang.external.exceptions.JSEError;
import info.julang.hosting.HostedMethodProviderFactory;
import info.julang.hosting.SimpleHostedMethodProvider;
import info.julang.hosting.execution.CtorNativeExecutor;
import info.julang.hosting.execution.InstanceNativeExecutor;
import info.julang.interpretation.ReflectedInvocationException;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.errorhandling.JulianScriptException;
import info.julang.interpretation.internal.FuncCallExecutor;
import info.julang.memory.value.ArrayValue;
import info.julang.memory.value.FuncValue;
import info.julang.memory.value.HostedValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.MethodValue;
import info.julang.memory.value.ObjectValue;
import info.julang.memory.value.RefValue;
import info.julang.memory.value.TempValueFactory;
import info.julang.memory.value.TypeValue;
import info.julang.typesystem.JType;
import info.julang.typesystem.jclass.ClassMemberLoaded;
import info.julang.typesystem.jclass.ICompoundType;
import info.julang.typesystem.jclass.JClassMember;
import info.julang.typesystem.jclass.JClassMethodMember;
import info.julang.typesystem.jclass.JClassType;
import info.julang.typesystem.jclass.JParameter;
import info.julang.typesystem.jclass.MemberKey;
import info.julang.typesystem.jclass.builtin.JMethodType;
import info.julang.util.OneOrMoreList;

/**
 * The native implementation of <code style="color:green">System.Reflection.Method</code>.
 * 
 * @author Ming Zhou
 */
public class ScriptMethod extends ScriptMemberBase {
		
	public static final String FQCLASSNAME = "System.Reflection.Method";
	
	//----------------- IRegisteredMethodProvider -----------------//
	
	public static HostedMethodProviderFactory Factory = new HostedMethodProviderFactory(FQCLASSNAME){

		@Override
		protected void implementProvider(SimpleHostedMethodProvider provider) {
			provider
				.add("ctor", new InitExecutor())
				.add("getName", new GetNameExecutor())
				.add("isStatic", new IsStaticExecutor())
				.add("toString", new ToStringExecutor())
				.add("call", new CallExecutor())
				.add("callExact", new CallExactExecutor())
				.add("bind", new BindExecutor())
				.add("getParams", new GetParamsExecutor())
				.add("getReturnType", new GetReturnTypeExecutor())
				.add("getAttributes", new GetAttributesExecutor());
		}
		
	};
	
	private JClassMethodMember jmethod;
	
	public void setMethod(JClassMethodMember jtyp){
		this.jmethod = jtyp;
	}
	
	//----------------- native executors -----------------//
	
	private static class InitExecutor extends CtorNativeExecutor<ScriptMethod> {

		@Override
		protected void initialize(ThreadRuntime rt, HostedValue hvalue, ScriptMethod jmap, Argument[] args) throws Exception {
			// NO-OP
		}
		
	}
	
	private static class GetNameExecutor extends InstanceNativeExecutor<ScriptMethod> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, ScriptMethod st, Argument[] args) throws Exception {
			String name = st.getName();
			JValue res = TempValueFactory.createTempStringValue(name);
			return res;
		}
		
	}
	
	private static class IsStaticExecutor extends InstanceNativeExecutor<ScriptMethod> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, ScriptMethod st, Argument[] args) throws Exception {
			boolean sta = st.isStatic();
			JValue res = TempValueFactory.createTempBoolValue(sta);
			return res;
		}
		
	}
	
	private static class ToStringExecutor extends InstanceNativeExecutor<ScriptMethod> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, ScriptMethod st, Argument[] args) throws Exception {
			String name = st.getSignature();
			JValue res = TempValueFactory.createTempStringValue(name);
			return res;
		}
		
	}
	
	private static class CallExecutor extends InstanceNativeExecutor<ScriptMethod> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, ScriptMethod st, Argument[] args) throws Exception {
			ArrayValue av = getArray(args, 0);
			JValue res = st.invoke(rt, av, true);
			return res;
		}
		
	}
	
	private static class CallExactExecutor extends InstanceNativeExecutor<ScriptMethod> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, ScriptMethod st, Argument[] args) throws Exception {
			ArrayValue av = getArray(args, 0);
			JValue res = st.invoke(rt, av, false);
			return res;
		}
		
	}
	
	private static class BindExecutor extends InstanceNativeExecutor<ScriptMethod> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, ScriptMethod st, Argument[] args) throws Exception {
			JValue val = args[0].getValue();
			JValue res = st.bind(rt, val);
			return res;
		}
		
	}
	
	private static class GetParamsExecutor extends InstanceNativeExecutor<ScriptMethod> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, ScriptMethod st, Argument[] args) throws Exception {
			ArrayValue av = st.getParams(rt);
			JValue res = TempValueFactory.createTempRefValue(av);
			return res;
		}
		
	}
	
	private static class GetReturnTypeExecutor extends InstanceNativeExecutor<ScriptMethod> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, ScriptMethod st, Argument[] args) throws Exception {
			ObjectValue ov = st.getReturnType(rt);
			JValue res = TempValueFactory.createTempRefValue(ov);
			return res;
		}
		
	}
	
	private static class GetAttributesExecutor extends InstanceNativeExecutor<ScriptMethod> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, ScriptMethod inst, Argument[] args) throws Exception {
			ArrayValue av = inst.getAttributes(rt);
			return av == null ? RefValue.NULL : av;
		}
		
	}
	
	//----------- implementation at native end -----------//

	/**
	 * Invoke the method with given arguments.
	 * <p>
	 * For static method, no dynamic dispatching will happen. The method will be called as is.
	 * <p>
	 * For instance method, based on the value of <code>dynamic</code>, will try dynamic dispatching.
	 * 
	 * @param rt
	 * @param av The array value that contains the arguments. The first argument is 'this' in case of instance method.
	 * @param dynamic Whether to invoke with dynamic dispatching.
	 * @return
	 */
	public JValue invoke(ThreadRuntime rt, ArrayValue av, boolean dynamic) {
		JValue val = null;
		ICompoundType defType = jmethod.getDefiningType();
		if (jmethod.isStatic()) {
			// SHARED CASE: static method doesn't require dynamic dispatching
			FuncCallExecutor fcall = new FuncCallExecutor(rt);
			val = invokeInternal(rt, jmethod, fcall, av, null);
		} else {
			if (av.getLength() <= 0) {
				throw new ReflectedInvocationException("Cannot invoke an instance method without a target object.");
			}
			JValue thisVal = av.getValueAt(0).deref();
			if (thisVal == RefValue.NULL){
				throw new ReflectedInvocationException("Cannot invoke an instance method without a target object.");
			}
			JType typ = thisVal.getType();
			if(!typ.isObject()) {
				throw new ReflectedInvocationException("Cannot invoke an instance method against a primitive value.");
			}
			
			ICompoundType valType = (ICompoundType)typ;
			FuncCallExecutor fcall = new FuncCallExecutor(rt);
			if (valType == defType) {
				// SHARED CASE: 'this' is of the same type on which the method is defined.
				val = invokeInstance(rt, jmethod, fcall, av, thisVal);
			} else {
				if (dynamic) {
					val = invokeDynamic(rt, av, valType, defType, fcall, thisVal);
				} else {
					val = invokeExact(rt, av, valType, defType, fcall, thisVal);
				}
			}
		}
		
		return val;
	}
	
	public ArrayValue getAttributes(ThreadRuntime rt) {
		ICompoundType deftyp = jmethod.getDefiningType();
		ArrayValue array = super.getAttributes(rt, deftyp, jmethod.getKey());
		return array;
	}

	private JValue bind(ThreadRuntime rt, JValue target) {
		ICompoundType defType = jmethod.getDefiningType();
		JMethodType mtyp = jmethod.getMethodType();
		String methodName = jmethod.getName();
		if (jmethod.isStatic()) {
			// 1) static method
			TypeValue tv = rt.getTypeTable().getValue(defType.getName());
			MethodValue mv = findMethodByType(tv, methodName, mtyp);
			if (mv != null){
				return mv;
			}
			
			// The method is not found among the static members.
			throw new JSEError("No method with name = '" + jmethod.getName() + "' and a matching signature is found on the type.");
		}
		
		JValue thisVal = target.deref();
		if (thisVal == RefValue.NULL){
			throw new ReflectedInvocationException("Cannot bind an instance method without a target object.");
		}
		JType typ = thisVal.getType();
		if(!typ.isObject()) {
			throw new ReflectedInvocationException("Cannot bind an instance method against a primitive value.");
		}
		ICompoundType valType = (ICompoundType)typ;
		ObjectValue ov = (ObjectValue)thisVal;
		
		if (valType == defType) {
			// 2) 'this' is of the same type in which the method to bind is defined.
			MethodValue mv = findMethodByType(ov, methodName, mtyp);
			if (mv != null){
				return mv;
			}
			
			// The method is not found among the instance members.
			throw new JSEError("No method with name = '" + jmethod.getName() + "' and a matching signature is found on the object instance.");
		}
		
		if (valType.isDerivedFrom(defType, false)){
			// 3) 'this' is of derived type of method's defining type.
			
			// Cannot proceed if the method is invisible to subclasses.
			if(!jmethod.getAccessibility().isSubclassVisible()) {
				throw new ReflectedInvocationException(
					"Cannot bind a non-public/protected method with an object which is derived from the method's defining class.");
			}
			
			// Find the method type on 'this' object that corresponds to the given
			JClassMethodMember jmethodOnVal = findSameMethod(valType);
			
			if (jmethodOnVal == null) {
				throw new JSEError("A method with same signature cannot be found on the given object despite it having a class on which the method is defined or inherited.");
			}
			
			mtyp = jmethodOnVal.getMethodType();
			MethodValue mv = findMethodByType(ov, methodName, mtyp);
			if (mv != null){
				return mv;
			}

			// The method is not found among the instance members.
			throw new JSEError("No method with name = '" + jmethod.getName() + "' and a matching signature is found on the object instance.");
		} 
		
		if (defType.isDerivedFrom(valType, false)){
			// 4) 'this' is a parent type of method's defining type.
			
			// Cannot proceed if the method is invisible to subclasses.
			if(!jmethod.getAccessibility().isSubclassVisible()) {
				throw new ReflectedInvocationException(
					"Cannot bind a non-public/protected method with an object of a type that is the ancestor of the method's defining class.");
			}
			
			// Find the method type on 'this' object that corresponds to the given
			JClassMethodMember jmethodOnVal = findSameMethod(valType);
			
			if (jmethodOnVal == null) {
				throw new ReflectedInvocationException(
					"A method with same signature cannot be found on the given object, which has a type that is the ancestor of the method's defining class.");
			}
			
			mtyp = jmethodOnVal.getMethodType();
			MethodValue mv = findMethodByType(ov, methodName, mtyp);
			if (mv != null){
				return mv;
			}

			// The method is not found among the instance members.
			throw new JSEError("No method with name = '" + jmethod.getName() + "' and a matching signature is found on the object instance.");
		}

		throw new ReflectedInvocationException("Cannot bind a method to an instance due to incompatible types.");
	}
	
	private MethodValue findMethodByType(ObjectValue ov, String methodName, JMethodType mtyp){
		MethodValue[] mvs = ov.getMethodMemberValues(methodName);
		for(MethodValue mv : mvs){
			if (mv.getMethodType() == mtyp){
				return mv;
			}
		}
		
		return null;
	}

	/**
	 * Invoke the instance method as is.
	 * <p>
	 * If 'this' object is of the same type in which the method is defined (a.k.a. defining type), there shouldn't 
	 * be any problem. If 'this' object is derived of the defining type, we would allow it as long as the member is 
	 * visible to the offspring type. This methods throws in all other cases.
	 */
	private JValue invokeExact(ThreadRuntime rt, ArrayValue av, ICompoundType valType, ICompoundType defType, FuncCallExecutor fcall, JValue thisVal) {
		JValue val = null;
		if (valType.isDerivedFrom(defType, false)){
			// CASE 1: 'this' is of derived type of method's defining type.
			
			// Cannot proceed if the method is invisible to subclasses.
			if(!jmethod.getAccessibility().isSubclassVisible()) {
				throw new ReflectedInvocationException(
					"Cannot invoke a non-public/protected method against an object which is derived from the method's defining class.");
			}
			
			val = invokeInstance(rt, jmethod, fcall, av, thisVal);
		} else {
			throw new ReflectedInvocationException(
				"Cannot invoke, as-is, an instance method against an object which is of neither the method's declaring type, nor an ancestor type thereof.");
		}
		
		return val;
	}

	/**
	 * Invoke the instance method by dynamic dispatching. 
	 * <p>
	 * The method which is actually invoked is not necessarily the one represented by the current instance of 
	 * {@link ScriptMethod}. If it's a public/protected instance method, will try to find the appropriate method of 
	 * same signature on the type of the given target object, which is passed in as the 0th argument in array value. 
	 * If the method's defining type is an interface implemented by the object, or a super class from which the object 
	 * is derived, it's guaranteed that a method of corresponding signature can be retrieved; if the method's defining 
	 * type is a subclass of the object's type, there is a chance that the method is also present on the target object.
	 */
	private JValue invokeDynamic(ThreadRuntime rt, ArrayValue av, ICompoundType valType, ICompoundType defType, FuncCallExecutor fcall, JValue thisVal) {
		JValue val = null;
		if (valType.isDerivedFrom(defType, false)){
			// CASE 1: 'this' is of derived type of method's defining type.
			
			// Cannot proceed if the method is invisible to subclasses.
			if(!jmethod.getAccessibility().isSubclassVisible()) {
				throw new ReflectedInvocationException(
					"Cannot invoke a non-public/protected method against an object which is derived from the method's defining class.");
			}
			
			// Find the method type on 'this' object that corresponds to the given
			JClassMethodMember jmethodOnVal = findSameMethod(valType);
			
			if (jmethodOnVal == null) {
				throw new JSEError("A method with same signature cannot be found on the given object despite it having a class on which the method is defined or inherited.");
			}
			
			val = invokeInstance(rt, jmethodOnVal, fcall, av, thisVal);
		} else if (defType.isDerivedFrom(valType, false)){
			// CASE 2: 'this' is a parent type of method's defining type.
			
			// Cannot proceed if the method is invisible to subclasses.
			if(!jmethod.getAccessibility().isSubclassVisible()) {
				throw new ReflectedInvocationException(
					"Cannot invoke a non-public/protected method against an object of a type that is the ancestor of the method's defining class.");
			}
			
			// Find the method type on 'this' object that corresponds to the given
			JClassMethodMember jmethodOnVal = findSameMethod(valType);
			
			if (jmethodOnVal == null) {
				throw new ReflectedInvocationException(
					"A method with same signature cannot be found on the given object, which has a type that is the ancestor of the method's defining class.");
			}
			
			val = invokeInstance(rt, jmethodOnVal, fcall, av, thisVal);
		} else {
			throw new ReflectedInvocationException(
				"Cannot invoke an instance method against an object which is of neither the method's declaring type, nor any type on the declaring type's class heirarchy.");
		}
		
		return val;
	}
	
	private JValue invokeInstance(ThreadRuntime rt, JClassMethodMember methodMem, FuncCallExecutor fcall, ArrayValue args, JValue thisVal){
		if (methodMem.isAbstract()) {
			throw new ReflectedInvocationException("The selected method is not implemented.");
		}
		
		return invokeInternal(rt, methodMem, fcall, args, thisVal);
	}
	
	private JValue invokeInternal(
		ThreadRuntime rt, JClassMethodMember methodMem, FuncCallExecutor fcall, ArrayValue args, JValue thisVal){
		try {
			JValue val = fcall.invokeMethodInternal(
				FuncValue.DUMMY, methodMem.getMethodType(), methodMem.getName(), toValueArray(args, thisVal != null), thisVal);
			return val;
		} catch (JSERuntimeException jrt) {
			Context context = Context.createSystemLoadingContext(rt);
			JulianScriptException jre = jrt.toJSE(rt, context);
			throw new ReflectedInvocationException(
				"Failed when invoking method through reflection: " + jmethod.getMethodType().getSignature(), jre);
		} catch (JulianScriptException jre) {
			throw new ReflectedInvocationException(
				"Failed when invoking method through reflection: " + jmethod.getMethodType().getSignature(), jre);
		}
	}
	
	private JClassMethodMember findSameMethod(ICompoundType valType){
		MemberKey key = jmethod.getKey();
		JClassType jct = (JClassType)valType;
		OneOrMoreList<ClassMemberLoaded> overloaded = jct.getMembers(false).getLoadedMemberByName(jmethod.getName());
		for(ClassMemberLoaded cml : overloaded) {
			JClassMember jcm = cml.getClassMember();
			if (key.equals(jcm.getKey())) {
				return (JClassMethodMember)jcm;
			}
		}
		
		return null;
	}
	
	private JValue[] toValueArray(ArrayValue av, boolean skipFirst) {
		int len = av.getLength();
		int offset = skipFirst ? 1 : 0;
		JValue[] array = new JValue[len - offset];
		for (int i = offset; i < len; i++) {
			array[i - offset] = av.getValueAt(i);
		}
		return array;
	}

	public ArrayValue getParams(ThreadRuntime rt) {
		final JParameter[] params = this.jmethod.getMethodType().getParams();
		return super.getParams(rt, params);
	}

	public String getName() {
		return jmethod.getName();
	}
	
	public boolean isStatic() {
		return jmethod.isStatic();
	}

	public String getSignature() {
		return "[METHOD|" + jmethod.getMethodType().getSignature() + "]";
	}

	public ObjectValue getReturnType(ThreadRuntime rt) {
		return ThreadRuntimeHelper.getScriptTypeObject(rt, jmethod.getMethodType().getReturnType());
	}
}