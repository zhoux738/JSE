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
import info.julang.hosting.HostedMethodProviderFactory;
import info.julang.hosting.SimpleHostedMethodProvider;
import info.julang.hosting.execution.CtorNativeExecutor;
import info.julang.hosting.execution.InstanceNativeExecutor;
import info.julang.interpretation.ReflectedInvocationException;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.errorhandling.JulianScriptException;
import info.julang.memory.value.ArrayValue;
import info.julang.memory.value.AttemptToChangeConstException;
import info.julang.memory.value.HostedValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.ObjectMember;
import info.julang.memory.value.ObjectValue;
import info.julang.memory.value.RefValue;
import info.julang.memory.value.TempValueFactory;
import info.julang.memory.value.TypeValue;
import info.julang.memory.value.ValueUtilities;
import info.julang.memory.value.VoidValue;
import info.julang.typesystem.JType;
import info.julang.typesystem.jclass.Accessibility;
import info.julang.typesystem.jclass.ClassMemberLoaded;
import info.julang.typesystem.jclass.ICompoundType;
import info.julang.typesystem.jclass.JClassFieldMember;
import info.julang.typesystem.jclass.JClassMember;
import info.julang.typesystem.jclass.JClassType;
import info.julang.typesystem.jclass.MemberType;
import info.julang.util.OneOrMoreList;

/**
 * The native implementation of <code><font color="green">System.Reflection.Field</font></code>.
 * 
 * @author Ming Zhou
 */
public class ScriptField extends ScriptMemberBase {
		
	public static final String FQCLASSNAME = "System.Reflection.Field";
	
	//----------------- IRegisteredMethodProvider -----------------//
	
	public static HostedMethodProviderFactory Factory = new HostedMethodProviderFactory(FQCLASSNAME){

		@Override
		protected void implementProvider(SimpleHostedMethodProvider provider) {
			provider
				.add("ctor", new InitExecutor())
				.add("getName", new GetNameExecutor())
				.add("isStatic", new IsStaticExecutor())
				.add("isConst", new IsConstExecutor())
				.add("toString", new ToStringExecutor())
				.add("get", new GetExecutor())
				.add("set", new SetExecutor())
				.add("getType", new GetTypeExecutor())
				.add("getAttributes", new GetAttributesExecutor());
		}
		
	};
	
	private JClassFieldMember jfield;
	
	public void setField(JClassFieldMember jtyp){
		this.jfield = jtyp;
	}
	
	//----------------- native executors -----------------//
	
	private static class InitExecutor extends CtorNativeExecutor<ScriptField> {

		@Override
		protected void initialize(ThreadRuntime rt, HostedValue hvalue, ScriptField jmap, Argument[] args) throws Exception {
			// NO-OP
		}
		
	}
	private static class GetNameExecutor extends InstanceNativeExecutor<ScriptField> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, ScriptField st, Argument[] args) throws Exception {
			String name = st.getName();
			JValue res = TempValueFactory.createTempStringValue(name);
			return res;
		}
		
	}
	
	private static class IsStaticExecutor extends InstanceNativeExecutor<ScriptField> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, ScriptField st, Argument[] args) throws Exception {
			boolean sta = st.isStatic();
			JValue res = TempValueFactory.createTempBoolValue(sta);
			return res;
		}
		
	}
	
	private static class IsConstExecutor extends InstanceNativeExecutor<ScriptField> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, ScriptField st, Argument[] args) throws Exception {
			boolean sta = st.isConst();
			JValue res = TempValueFactory.createTempBoolValue(sta);
			return res;
		}
		
	}
	
	private static class ToStringExecutor extends InstanceNativeExecutor<ScriptField> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, ScriptField st, Argument[] args) throws Exception {
			String name = st.getSignature();
			JValue res = TempValueFactory.createTempStringValue(name);
			return res;
		}
		
	}
	
	private static class GetExecutor extends InstanceNativeExecutor<ScriptField> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, ScriptField st, Argument[] args) throws Exception {
			ObjectValue inst = getObject(args, 0);
			JValue res = st.get(rt, inst);
			res = ValueUtilities.replicateValue(res, res.getType(), rt.getThreadStack().getStackArea().currentFrame());
			return res;
		}
		
	}
	
	private static class SetExecutor extends InstanceNativeExecutor<ScriptField> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, ScriptField st, Argument[] args) throws Exception {
			ObjectValue inst = getObject(args, 0);
			JValue val = args[1].getValue().deref();
			st.set(rt, inst, val);
			return VoidValue.DEFAULT;
		}
	}
	
	private static class GetTypeExecutor extends InstanceNativeExecutor<ScriptField> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, ScriptField st, Argument[] args) throws Exception {
			ObjectValue ov = st.getFieldType(rt);
			JValue res = TempValueFactory.createTempRefValue(ov);
			return res;
		}
		
	}
	
	private static class GetAttributesExecutor extends InstanceNativeExecutor<ScriptField> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, ScriptField inst, Argument[] args) throws Exception {
			ArrayValue av = inst.getAttributes(rt);
			return av == null ? RefValue.NULL : av;
		}
		
	}
	
	//----------- implementation at native end -----------//
	
	public void set(ThreadRuntime rt, ObjectValue inst, JValue value) {
		if (jfield.isConst()) {
			AttemptToChangeConstException ex = new AttemptToChangeConstException();
			JulianScriptException jse = ex.toJSE(rt, Context.createSystemLoadingContext(rt));
			throw new ReflectedInvocationException("Failed when accessing to field through reflection: " + jfield.getName(), jse);
		}
		
		String fname = jfield.getName();
		if (jfield.isStatic()){
			TypeValue tv = rt.getTypeTable().getValue(jfield.getDefiningType().getName());
			JValue jval = tv.getMemberValue(fname);
			setFieldInternal(rt, jval, value);
		} else {
			if (inst == null) {
				throw new ReflectedInvocationException("Cannot access to an instance field without a instance as the target.");
			}

			ICompoundType defType = jfield.getDefiningType();
			JClassType objType = inst.getClassType();
			
			// if objType == defType, allow
			// if objType : defType, if member.visibility == Pub/Pro, allow - although the member might be a private that hides the inherited
			// if defType : objType, if member.visibility == Pub/Pro and a member of same name/visibility is defined on objType, allow
			// if two types are not related, disallow
			boolean set = false;
			if (objType == defType) {
				set = setInstanceField(rt, inst, value, fname, objType);
			} else if (objType.isDerivedFrom(defType, false)) {
				if (jfield.getAccessibility().isSubclassVisible()){
					set = setInstanceField(rt, inst, value, fname, objType);
				} else {
					throw new ReflectedInvocationException(
						"Cannot access to a non-public/protected field on an object which is derived from the field's defining class.");
				}
			} else if (defType.isDerivedFrom(objType, false)) {
				boolean found = false;
				Accessibility acc = jfield.getAccessibility();
				if (acc.isSubclassVisible()){
					JClassType objClassType = (JClassType)objType;
					OneOrMoreList<ClassMemberLoaded> mems = objClassType.getMembers(false).getLoadedMemberByName(fname);
					for(ClassMemberLoaded mem : mems){
						JClassMember jcm = mem.getClassMember();
						if (jcm.getMemberType() == MemberType.FIELD && jcm.getAccessibility().isSubclassVisible()) {
							set = setInstanceField(rt, inst, value, fname, objClassType);
							found = true;
							break;
						}
					}
				}
				
				if (!found) {
					throw new ReflectedInvocationException(
						"Cannot access to a field on an object of type that is the ancestor of the field's defining class. " + 
						"Such access is only allowed if the field is also defined on the object's type with visibility to subclasses.");
				}
			} else {
				throw new ReflectedInvocationException(
					"Cannot access to a field on an object which is of neither the field's declaring type, " + 
					"nor any type on the declaring type's class heirarchy.");
			}
			
			if (!set) {
				throw new ReflectedInvocationException("Unable to find corresponding field member on the given target.");
			}
		}
	}
	
	public ArrayValue getAttributes(ThreadRuntime rt) {
		ICompoundType deftyp = jfield.getDefiningType();
		ArrayValue array = super.getAttributes(rt, deftyp, jfield.getKey());
		return array;
	}

	private boolean setInstanceField(ThreadRuntime rt, ObjectValue inst, JValue value, String fname, JClassType searchStartType){
		OneOrMoreList<ObjectMember> oms = inst.getMemberValueByClass(fname, searchStartType, true);
		for(ObjectMember om : oms) {
			JValue jval = om.getValue();
			JType typ = jval.deref().getType();
			if (typ == this.jfield.getType()) {
				setFieldInternal(rt, jval, value);
				return true;
			}
		}
		
		return false;
	}
	
	private void setFieldInternal(ThreadRuntime rt, JValue target, JValue value){
		try {
			value.assignTo(target);
		} catch (JSERuntimeException jse) {
			Context context = Context.createSystemLoadingContext(rt);
			JulianScriptException jre = jse.toJSE(rt, context);
			throw new ReflectedInvocationException(
				"Failed when accessing to field through reflection: " + jfield.getName(), jre);
		}
	}

	public JValue get(ThreadRuntime rt, ObjectValue inst) {
		String fname = jfield.getName();
		if (jfield.isStatic()){
			TypeValue tv = rt.getTypeTable().getValue(jfield.getDefiningType().getName());
			JValue jval = tv.getMemberValue(fname);
			return jval;
		} else {
			if (inst == null) {
				throw new ReflectedInvocationException("Cannot access to an instance field without a instance as the target.");
			}

			ICompoundType defType = jfield.getDefiningType();
			JClassType objType = inst.getClassType();
			
			// if objType == defType, allow
			// if objType : defType, if member.visibility == Pub/Pro, allow
			// if defType : objType, if member.visibility == Pub/Pro and a member of same name/visibility is defined on objType, allow
			// if two types are not related, disallow
			if (objType == defType) {
				return inst.getMemberValue(fname);
			} else if (objType.isDerivedFrom(defType, false)) {
				if (jfield.getAccessibility().isSubclassVisible()){
					return inst.getMemberValue(fname);
				} else {
					throw new ReflectedInvocationException(
						"Cannot access to a non-public/protected field on an object which is derived from the field's defining class.");
				}
			} else if (defType.isDerivedFrom(objType, false)) {
				Accessibility acc = jfield.getAccessibility();
				if (acc.isSubclassVisible()){
					JClassType objClassType = (JClassType)objType;
					OneOrMoreList<ClassMemberLoaded> mems = objClassType.getMembers(false).getLoadedMemberByName(fname);
					for(ClassMemberLoaded mem : mems){
						JClassMember jcm = mem.getClassMember();
						if (jcm.getMemberType() == MemberType.FIELD && jcm.getAccessibility().isSubclassVisible()) {
							return inst.getMemberValue(fname);
						}
					}
				}
				
				throw new ReflectedInvocationException(
					"Cannot access to a field on an object of type that is the ancestor of the field's defining class. " + 
					"Such access is only allowed if the field is also defined on the object's type with visibility to subclasses.");
			} else {
				throw new ReflectedInvocationException(
					"Cannot access to a field on an object which is of neither the field's declaring type, " + 
					"nor any type on the declaring type's class heirarchy.");
			}
		}
	}

	public String getName() {
		return jfield.getName();
	}

	public boolean isStatic() {
		return jfield.isStatic();
	}
	
	public boolean isConst() {
		return jfield.isConst();
	}

	public String getSignature() {
		return "[FIELD|" + jfield.getName() + ":" + jfield.getType().getName() + "]";
	}

	public ObjectValue getFieldType(ThreadRuntime rt) {
		return ThreadRuntimeHelper.getScriptTypeObject(rt, jfield.getType());
	}
}