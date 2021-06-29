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
import info.julang.memory.value.RefValue;
import info.julang.memory.value.TempValueFactory;
import info.julang.typesystem.jclass.ICompoundType;
import info.julang.typesystem.jclass.JClassConstructorMember;
import info.julang.typesystem.jclass.JClassType;
import info.julang.typesystem.jclass.JParameter;

/**
 * The native implementation of <code style="color:green">System.Reflection.Constructor</code>.
 * 
 * @author Ming Zhou
 */
public class ScriptCtor extends ScriptMemberBase {
		
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
				.add("getParams", new GetParamsExecutor())
				.add("getAttributes", new GetAttributesExecutor());
		}
		
	};
	
	private JClassConstructorMember ctor;
	
	public void setCtor(JClassConstructorMember jtyp){
		this.ctor = jtyp;
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
	
	private static class GetAttributesExecutor extends InstanceNativeExecutor<ScriptCtor> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, ScriptCtor inst, Argument[] args) throws Exception {
			ArrayValue av = inst.getAttributes(rt);
			return av == null ? RefValue.NULL : av;
		}
		
	}
	
	//----------- implementation at native end -----------//

	public JValue invoke(ThreadRuntime rt, ArrayValue av) {
		JParameter[] params = ctor.getCtorType().getParams();
		
		NewObjExecutor neo = new NewObjExecutor(rt);
		int len = av.getLength();
		if (len != params.length - 1){
			throw new ReflectedInvocationException(
				"Incorrect number of arguments to constructor: " + ctor.getCtorType().getSignature());
		}
		Argument[] args = new Argument[len];
		for(int i = 0; i < len; i++){
			JValue jv = av.getValueAt(i);
			args[i] = new Argument(params[i + 1].getName(), jv);
		}
		
		ObjectValue ov = null;
		try {
			ov = neo.newObjectInternal(
				(JClassType)ctor.getDefiningType(), 
				ctor, 
				args);
		} catch (JSERuntimeException jrt) {
			Context context = Context.createSystemLoadingContext(rt);
			JulianScriptException jre = jrt.toJSE(rt, context);
			throw new ReflectedInvocationException(
				"Failed when invoking constructor through reflection: " + ctor.getCtorType().getSignature(), jre);
		} catch (JulianScriptException jre) {
			throw new ReflectedInvocationException(
				"Failed when invoking constructor through reflection: " + ctor.getCtorType().getSignature(), jre);
		}
		
		return ov;
	}
	
	public ArrayValue getParams(ThreadRuntime rt) {
		final JParameter[] params = this.ctor.getCtorType().getParams();
		return super.getParams(rt, params);
	}

	public ArrayValue getAttributes(ThreadRuntime rt) {
		ICompoundType deftyp = ctor.getDefiningType();
		ArrayValue array = super.getAttributes(rt, deftyp, ctor.getKey());
		return array;
	}

	public String getName() {
		return ctor.getCtorType().getContainingType().getName();
	}

	public String getSignature() {
		return "[CTOR|" + ctor.getCtorType().getSignature() + "]";
	}
}