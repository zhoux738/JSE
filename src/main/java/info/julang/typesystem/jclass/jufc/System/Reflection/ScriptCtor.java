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
import info.julang.execution.threading.ThreadRuntimeHelper.IObjectPopulater;
import info.julang.hosting.HostedMethodProviderFactory;
import info.julang.hosting.SimpleHostedMethodProvider;
import info.julang.hosting.execution.CtorNativeExecutor;
import info.julang.hosting.execution.InstanceNativeExecutor;
import info.julang.interpretation.ReflectedInvocationException;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.errorhandling.JulianScriptException;
import info.julang.interpretation.internal.NewObjExecutor;
import info.julang.memory.value.ArrayValue;
import info.julang.memory.value.HostedValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.ObjectValue;
import info.julang.memory.value.TempValueFactory;
import info.julang.typesystem.jclass.JClassConstructorMember;
import info.julang.typesystem.jclass.JClassType;
import info.julang.typesystem.jclass.JParameter;

/**
 * The native implementation of <code><font color="green">System.Reflection.Constructor</font></code>.
 * 
 * @author Ming Zhou
 */
public class ScriptCtor {
		
	public static final String FQCLASSNAME = "System.Reflection.Constructor";
	
	//----------------- IRegisteredMethodProvider -----------------//
	
	public static HostedMethodProviderFactory Factory = new HostedMethodProviderFactory(FQCLASSNAME){

		@Override
		protected void implementProvider(SimpleHostedMethodProvider provider) {
			provider
				.add("ctor", new InitExecutor())
				.add("getName", new GetNameExecutor())
				.add("toString", new ToStringExecutor())
				.add("invoke", new InvokeExecutor())
				.add("getParams", new GetParamsExecutor());
		}
		
	};
	
	private JClassConstructorMember jtyp;
	
	public void setCtor(JClassConstructorMember jtyp){
		this.jtyp = jtyp;
	}
	
	//----------------- native executors -----------------//
	
	private static class InitExecutor extends CtorNativeExecutor<ScriptCtor> {

		@Override
		protected void initialize(ThreadRuntime rt, HostedValue hvalue, ScriptCtor jmap, Argument[] args) throws Exception {
			// NO-OP
		}
		
	}
	private static class GetNameExecutor extends InstanceNativeExecutor<ScriptCtor> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, ScriptCtor st, Argument[] args) throws Exception {
			String name = st.getName();
			JValue res = TempValueFactory.createTempStringValue(name);
			return res;
		}
		
	}
	
	private static class ToStringExecutor extends InstanceNativeExecutor<ScriptCtor> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, ScriptCtor st, Argument[] args) throws Exception {
			String name = st.getSignature();
			JValue res = TempValueFactory.createTempStringValue(name);
			return res;
		}
		
	}
	
	private static class InvokeExecutor extends InstanceNativeExecutor<ScriptCtor> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, ScriptCtor st, Argument[] args) throws Exception {
			ArrayValue av = getArray(args, 0);
			JValue res = st.invoke(rt, av);
			return res;
		}
		
	}
	
	private static class GetParamsExecutor extends InstanceNativeExecutor<ScriptCtor> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, ScriptCtor st, Argument[] args) throws Exception {
			ArrayValue av = st.getParams(rt);
			JValue res = TempValueFactory.createTempRefValue(av);
			return res;
		}
		
	}
	
	//----------- implementation at native end -----------//

	public JValue invoke(ThreadRuntime rt, ArrayValue av) {
		JParameter[] params = jtyp.getCtorType().getParams();
		
		NewObjExecutor neo = new NewObjExecutor(rt);
		int len = av.getLength();
		if (len != params.length - 1){
			throw new ReflectedInvocationException(
				"Incorrect number of arguments to constructor: " + jtyp.getCtorType().getSignature());
		}
		Argument[] args = new Argument[len];
		for(int i = 0; i < len; i++){
			JValue jv = av.getValueAt(i);
			args[i] = new Argument(params[i + 1].getName(), jv);
		}
		
		ObjectValue ov = null;
		try {
			ov = neo.newObjectInternal(
				(JClassType)jtyp.getDefiningType(), 
				jtyp, 
				args);
		} catch (JSERuntimeException jrt) {
			Context context = Context.createSystemLoadingContext(rt);
			JulianScriptException jre = jrt.toJSE(rt, context);
			throw new ReflectedInvocationException(
				"Failed when invoking constructor through reflection: " + jtyp.getCtorType().getSignature(), jre);
		} catch (JulianScriptException jre) {
			throw new ReflectedInvocationException(
				"Failed when invoking constructor through reflection: " + jtyp.getCtorType().getSignature(), jre);
		}
		
		return ov;
	}
	
	public ArrayValue getParams(ThreadRuntime rt) {
		// 1) Load System.Reflection.Parameter
		JClassType sysReflParamTyp = (JClassType)ThreadRuntimeHelper.loadSystemType(rt, ScriptParam.FQCLASSNAME);
		JClassConstructorMember sysReflParamTypCtor = sysReflParamTyp.getClassConstructors()[0];
		
		// 2) Get all ctors for this Type
		final JParameter[] params = this.jtyp.getCtorType().getParams();
		
		ArrayValue av = ThreadRuntimeHelper.createAndPopulateObjectArrayValue(
			rt, params.length - 1, sysReflParamTyp, sysReflParamTypCtor, 
			new IObjectPopulater(){

				@Override
				public Argument[] getArguments(int index) {
					return new Argument[0];
				}

				@Override
				public void postCreation(int index, ObjectValue ov) {
					JParameter param = params[index + 1];
					
					HostedValue hv = (HostedValue)ov;
					ScriptParam sc = (ScriptParam)hv.getHostedObject();
					sc.setParam(param);
				}
			});
		
		return av;
	}

	public String getName() {
		return jtyp.getCtorType().getContainingType().getName();
	}

	public String getSignature() {
		return "[CTOR|" + jtyp.getCtorType().getSignature() + "]";
	}
}