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

package info.julang.typesystem.jclass.jufc.System;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import info.julang.JSERuntimeException;
import info.julang.execution.Argument;
import info.julang.execution.symboltable.ITypeTable;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.execution.threading.ThreadRuntimeHelper;
import info.julang.execution.threading.ThreadRuntimeHelper.IObjectPopulater;
import info.julang.hosting.HostedMethodProviderFactory;
import info.julang.hosting.SimpleHostedMethodProvider;
import info.julang.hosting.execution.InstanceNativeExecutor;
import info.julang.hosting.execution.StaticNativeExecutor;
import info.julang.interpretation.ReflectedInvocationException;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.errorhandling.JulianScriptException;
import info.julang.interpretation.syntax.ParsedTypeName;
import info.julang.interpretation.syntax.SyntaxHelper;
import info.julang.langspec.ast.JulianParser.TypeContext;
import info.julang.memory.MemoryArea;
import info.julang.memory.value.ArrayValue;
import info.julang.memory.value.ArrayValueFactory;
import info.julang.memory.value.AttrValue;
import info.julang.memory.value.BoolValue;
import info.julang.memory.value.HostedValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.ObjectArrayValue;
import info.julang.memory.value.ObjectValue;
import info.julang.memory.value.RefValue;
import info.julang.memory.value.TempValueFactory;
import info.julang.memory.value.TypeValue;
import info.julang.modulesystem.ModuleManager;
import info.julang.modulesystem.naming.FQName;
import info.julang.parser.ANTLRParser;
import info.julang.typesystem.AnyType;
import info.julang.typesystem.JType;
import info.julang.typesystem.VoidType;
import info.julang.typesystem.jclass.Accessibility;
import info.julang.typesystem.jclass.ICompoundType;
import info.julang.typesystem.jclass.JClassConstructorMember;
import info.julang.typesystem.jclass.JClassFieldMember;
import info.julang.typesystem.jclass.JClassMember;
import info.julang.typesystem.jclass.JClassMethodMember;
import info.julang.typesystem.jclass.JClassType;
import info.julang.typesystem.jclass.JInterfaceType;
import info.julang.typesystem.jclass.MemberType;
import info.julang.typesystem.jclass.builtin.JArrayBaseType;
import info.julang.typesystem.jclass.builtin.JArrayType;
import info.julang.typesystem.jclass.builtin.JAttributeBaseType;
import info.julang.typesystem.jclass.builtin.JEnumBaseType;
import info.julang.typesystem.jclass.builtin.JFunctionType;
import info.julang.typesystem.jclass.jufc.System.Reflection.ScriptCtor;
import info.julang.typesystem.jclass.jufc.System.Reflection.ScriptField;
import info.julang.typesystem.jclass.jufc.System.Reflection.ScriptMethod;
import info.julang.typesystem.loading.LoadingInitiative;
import info.julang.util.OneOrMoreList;

/**
 * The native implementation of <code><font color="green">System.Type</font></code>.
 * 
 * @author Ming Zhou
 */
public class ScriptType {
		
	public static final String FQCLASSNAME = "System.Type";
	
	//----------------- IRegisteredMethodProvider -----------------//
	
	public static HostedMethodProviderFactory Factory = new HostedMethodProviderFactory(FQCLASSNAME){

		@Override
		protected void implementProvider(SimpleHostedMethodProvider provider) {
			provider
				.add("getFullName", new GetFullNameExecutor())
				.add("load", new LoadExecutor())
				.add("isPrimitive", new IsPrimitiveExecutor())
				.add("isArray", new IsArrayExecutor())
				.add("isFinal", new IsFinalExecutor())
				.add("isInterface", new IsInterfaceExecutor())
				.add("isClass", new IsClassExecutor())
				.add("getCtors", new GetCtorsExecutor())
				.add("getMethods", new GetMethodsExecutor())
				.add("getFields", new GetFieldsExecutor())
				.add("getParent", new GetParentExecutor())
				.add("getInterfaces", new GetInterfacesExecutor())
				.add("getExtensions", new GetExtensionsExecutor())
				.add("getAttributes", new GetAttributesExecutor());
		}
		
	};
	
	private JType jtyp;
	
	public void setType(JType jtyp){
		this.jtyp = jtyp;
	}
	
	public JType getType() {
		return this.jtyp;
	}
	
	//----------------- native executors -----------------//
	
	private static class LoadExecutor extends StaticNativeExecutor<ScriptType> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, Argument[] args) throws Exception {
			try {
				return apply0(rt, args);
			} catch (JSERuntimeException jrt) {
				Context context = Context.createSystemLoadingContext(rt);
				JulianScriptException jre = jrt.toJSE(rt, context);
				throw new ReflectedInvocationException(
					"Failed when loading type through reflection.", jre);
			} catch (JulianScriptException jre) {
				throw new ReflectedInvocationException(
					"Failed when loading type through reflection.", jre);
			}
		}
		
		private JValue apply0(ThreadRuntime rt, Argument[] args) throws Exception {
			// 1) Get type identifier AST
			String s = this.getString(args, 0);
			String src = s + " dummy;";
			ANTLRParser parser = ANTLRParser.createMemoryParser(src);
			
			parser.parse(true, true);
			
			TypeContext tc = parser.getAstInfo().getAST().executable().statement_list().statement(0).declaration_statement().type();
			ParsedTypeName ptn = SyntaxHelper.parseTypeName(tc);
			
			// 2) Load module and type
			if (ptn.getBasicType() == null && ptn != ParsedTypeName.ANY){
				FQName fqname = ptn.getFQName();
				String fname = fqname.toString();
				int end = fname.length() - fqname.getSimpleName().length() - 1;
				if (end > 0) {
					fname = fname.substring(0, end);
				}
				ModuleManager mm = (ModuleManager)rt.getModuleManager();
				mm.loadModule(rt.getJThread(), fname);
			}
			
			Context context = Context.createSystemLoadingContext(rt);
			JType typ = rt.getTypeResolver().resolveType(context, ptn, true, LoadingInitiative.DYNAMIC); // Will throw if not found
			
			// 3) Get System.Type object for this type
			ObjectValue ov = ThreadRuntimeHelper.getScriptTypeObject(rt, typ);
			JValue res = TempValueFactory.createTempRefValue(ov);
			return res;
		}
		
	}
	
	private static class GetParentExecutor extends InstanceNativeExecutor<ScriptType> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, ScriptType st, Argument[] args) throws Exception {
			ObjectValue av = st.getParentType(rt);
			JValue res = TempValueFactory.createTempRefValue(av);
			return res;
		}
		
	}
	
	private static class GetInterfacesExecutor extends InstanceNativeExecutor<ScriptType> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, ScriptType st, Argument[] args) throws Exception {
			ArrayValue av = st.getInterfaces(rt);
			JValue res = TempValueFactory.createTempRefValue(av);
			return res;
		}
		
	}
	
	private static class GetExtensionsExecutor extends InstanceNativeExecutor<ScriptType> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, ScriptType st, Argument[] args) throws Exception {
			ArrayValue av = st.getExtensions(rt);
			JValue res = TempValueFactory.createTempRefValue(av);
			return res;
		}
		
	}
	
	private static class GetAttributesExecutor extends InstanceNativeExecutor<ScriptType> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, ScriptType st, Argument[] args) throws Exception {
			ArrayValue av = st.getAttributes(rt);
			return av == null ? RefValue.NULL : av;
		}
		
	}
	
	private static class GetCtorsExecutor extends InstanceNativeExecutor<ScriptType> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, ScriptType st, Argument[] args) throws Exception {
			ArrayValue av = st.getConstructors(rt);
			JValue res = TempValueFactory.createTempRefValue(av);
			return res;
		}
		
	}
	
	private static class GetMethodsExecutor extends InstanceNativeExecutor<ScriptType> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, ScriptType st, Argument[] args) throws Exception {
			ArrayValue av = st.getMethods(rt);
			JValue res = TempValueFactory.createTempRefValue(av);
			return res;
		}
		
	}
	
	private static class GetFieldsExecutor extends InstanceNativeExecutor<ScriptType> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, ScriptType st, Argument[] args) throws Exception {
			ArrayValue av = st.getFields(rt);
			JValue res = TempValueFactory.createTempRefValue(av);
			return res;
		}
		
	}
	
	private static class GetFullNameExecutor extends InstanceNativeExecutor<ScriptType> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, ScriptType st, Argument[] args) throws Exception {
			String name = st.getName();
			JValue res = TempValueFactory.createTempStringValue(name);
			return res;
		}
		
	}
	
	private static class IsPrimitiveExecutor extends InstanceNativeExecutor<ScriptType> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, ScriptType st, Argument[] args) throws Exception {
			boolean b = st.isPrimitive();
			JValue res = TempValueFactory.createTempBoolValue(b);
			return res;
		}
		
	}
	
	private static class IsClassExecutor extends InstanceNativeExecutor<ScriptType> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, ScriptType st, Argument[] args) throws Exception {
			boolean b = st.isClass();
			JValue res = TempValueFactory.createTempBoolValue(b);
			return res;
		}
		
	}
	
	private static class IsArrayExecutor extends InstanceNativeExecutor<ScriptType> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, ScriptType st, Argument[] args) throws Exception {
			boolean b = st.isArray();
			JValue res = TempValueFactory.createTempBoolValue(b);
			return res;
		}
		
	}
	
	private static class IsFinalExecutor extends InstanceNativeExecutor<ScriptType> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, ScriptType st, Argument[] args) throws Exception {
			boolean b = st.isFinal();
			JValue res = TempValueFactory.createTempBoolValue(b);
			return res;
		}
		
	}
	
	private static class IsInterfaceExecutor extends InstanceNativeExecutor<ScriptType> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, ScriptType st, Argument[] args) throws Exception {
			boolean b = st.isInterface();
			JValue res = TempValueFactory.createTempBoolValue(b);
			return res;
		}
		
	}
	
	//----------- implementation at native end -----------//

	public String getName() {
		return jtyp.getName();
	}

	private static JClassType[] specialTyps = null; 
	
	private static JClassType[] getSpecialTypes(){
		if (specialTyps == null) {
			synchronized(ScriptType.class) {
				if (specialTyps == null) {
					specialTyps = new JClassType[] {
						JFunctionType.getInstance(),
						JArrayBaseType.getInstance(),
						JAttributeBaseType.getInstance(),
						JEnumBaseType.getInstance()
					};
				}
			}
		}
		
		return specialTyps;
	}
	
	public ArrayValue getFields(ThreadRuntime rt) {
		// 1) Load System.Reflection.Field
		JClassType sysReflFieldTyp = (JClassType)ThreadRuntimeHelper.loadSystemType(rt, ScriptField.FQCLASSNAME);
		JClassConstructorMember sysReflFieldTypCtor = sysReflFieldTyp.getClassConstructors()[0];
		
		// 2) Get all ctors for this Type
		final JClassFieldMember[] methods = getFields0(rt);
		
		// 3) Create and populate an array of System.Reflection.Constructor
		ArrayValue array = ThreadRuntimeHelper.createAndPopulateObjectArrayValue(
			rt, methods.length, sysReflFieldTyp, sysReflFieldTypCtor, 
			new IObjectPopulater(){

				@Override
				public Argument[] getArguments(int index) {
					return new Argument[0];
				}
	
				@Override
				public void postCreation(int index, ObjectValue ov) {
					HostedValue hv = (HostedValue) ov;
					ScriptField sc = (ScriptField)hv.getHostedObject();
					sc.setField(methods[index]);
				}
			});
		
		return array;
	}
	
	public ArrayValue getMethods(ThreadRuntime rt) {
		// 1) Load System.Reflection.Method
		JClassType sysReflMethodTyp = (JClassType)ThreadRuntimeHelper.loadSystemType(rt, ScriptMethod.FQCLASSNAME);
		JClassConstructorMember sysReflMethodTypCtor = sysReflMethodTyp.getClassConstructors()[0];
		
		// 2) Get all ctors for this Type
		final JClassMethodMember[] methods = getMethods0(rt);
		
		// 3) Create and populate an array of System.Reflection.Constructor
		ArrayValue array = ThreadRuntimeHelper.createAndPopulateObjectArrayValue(
			rt, methods.length, sysReflMethodTyp, sysReflMethodTypCtor, 
			new IObjectPopulater(){

				@Override
				public Argument[] getArguments(int index) {
					return new Argument[0];
				}
	
				@Override
				public void postCreation(int index, ObjectValue ov) {
					HostedValue hv = (HostedValue) ov;
					ScriptMethod sc = (ScriptMethod)hv.getHostedObject();
					sc.setMethod(methods[index]);
				}
			});
		
		return array;
	}

	public ArrayValue getConstructors(ThreadRuntime rt) {
		// 1) Load System.Reflection.Constructor
		JClassType sysReflCtorTyp = (JClassType)ThreadRuntimeHelper.loadSystemType(rt, ScriptCtor.FQCLASSNAME);
		JClassConstructorMember sysReflCtorTypCtor = sysReflCtorTyp.getClassConstructors()[0];
		
		// 2) Get all ctors for this Type
		final JClassConstructorMember[] ctors = getConstructors0(rt);
		
		// 3) Create and populate an array of System.Reflection.Constructor
		ArrayValue array = ThreadRuntimeHelper.createAndPopulateObjectArrayValue(
			rt, ctors.length, sysReflCtorTyp, sysReflCtorTypCtor, 
			new IObjectPopulater(){

				@Override
				public Argument[] getArguments(int index) {
					return new Argument[0];
				}
	
				@Override
				public void postCreation(int index, ObjectValue ov) {
					HostedValue hv = (HostedValue) ov;
					ScriptCtor sc = (ScriptCtor)hv.getHostedObject();
					sc.setCtor(ctors[index]);
				}
			});
		
		return array;
	}

	private JClassMethodMember[] getMethods0(ThreadRuntime rt) {
		if (jtyp.isObject()){
			List<JClassMember> l1 = collectMembers(rt);
			JClassMethodMember[] methods = toMethodArray(l1);
			return methods;
		}

		return new JClassMethodMember[0];
	}
	
	private JClassFieldMember[] getFields0(ThreadRuntime rt) {
		if (jtyp.isObject()){
			List<JClassMember> l1 = collectMembers(rt);
			JClassFieldMember[] methods = toFieldArray(l1);
			return methods;
		}

		return new JClassFieldMember[0];
	}
	
	private List<JClassMember> collectMembers(ThreadRuntime rt){
		ICompoundType ict = (ICompoundType)jtyp;
		
		// Instance members
		JClassMember[] mems1 = ict.getClassInstanceMembers();
		List<JClassMember> l1 = filterMembers(rt, mems1);
		// Static members
		JClassMember[] mems2 = ict.getClassStaticMembers();
		List<JClassMember> l2 = filterMembers(rt, mems2);
		l1.addAll(l2);
		
		return l1;
	}
	
	private List<JClassMember> filterMembers(ThreadRuntime rt, JClassMember[] mems){
		TypeValue tv = rt.getTypeTable().getValue(jtyp.getName());
		List<JClassMember> list = new ArrayList<JClassMember>();
		Collections.addAll(list, mems);
		list = filter(tv, list);
		return list;
	}
	
	private JClassMethodMember[] toMethodArray(List<JClassMember> list){
		List<JClassMethodMember> methods = new ArrayList<JClassMethodMember>();
		for(JClassMember mem : list) {
			if (mem.getMemberType() == MemberType.METHOD) {
				methods.add((JClassMethodMember)mem);
			}
		}
		
		return methods.toArray(new JClassMethodMember[0]);
	}
	
	private JClassFieldMember[] toFieldArray(List<JClassMember> list){
		List<JClassFieldMember> methods = new ArrayList<JClassFieldMember>();
		for(JClassMember mem : list) {
			if (mem.getMemberType() == MemberType.FIELD) {
				methods.add((JClassFieldMember)mem);
			}
		}
		
		return methods.toArray(new JClassFieldMember[0]);
	}

	private JClassConstructorMember[] getConstructors0(ThreadRuntime rt) {
		if (jtyp.isObject()){
			ICompoundType ict = (ICompoundType)jtyp;
			if (ict.isClassType()) {
				JClassType jct = (JClassType)jtyp;
				JClassType[] specialTyps = getSpecialTypes();
				for (JClassType spt : specialTyps) {
					if (jct.isDerivedFrom(spt, true)){
						return new JClassConstructorMember[0];
					}
				}
				
				JClassConstructorMember[] mems = jct.getClassConstructors();
				TypeValue tv = rt.getTypeTable().getValue(jtyp.getName());
				List<JClassConstructorMember> list = new ArrayList<JClassConstructorMember>();
				Collections.addAll(list, mems);
				list = filter(tv, list);
				mems = list.toArray(new JClassConstructorMember[0]);
				
				return mems;
			}
		}

		return new JClassConstructorMember[0];
	}
	
	private <T extends JClassMember> List<T> filter(TypeValue tv, Iterable<T> all){
		List<T> list = new ArrayList<T>();
		String name = jtyp.getName();
		boolean defaultToExclude = jtyp.isBuiltIn() || name.startsWith("System.");
		for (T mem : all) {
			boolean added = false;
			boolean explicitlyExcluded = false;
			List<AttrValue> lav = tv.getMemberAttrValues(mem.getKey());
			if (lav != null) {
				for(AttrValue av : lav){
					// The attribute type for an attribute value
					JType jat = av.getType();
					
					// If Reflected is applied, it overrides the default behavior
					if (jat.getName().equals("System.Reflection.Reflected")){
						BoolValue visible = (BoolValue)av.getMemberValue("visible");
						if (visible.getBoolValue()) {
							list.add(mem);
							added = true;
						} else {
							explicitlyExcluded = true;
						}
						
						break;
					}
				}
			}
			
			if (!added && !explicitlyExcluded && !(defaultToExclude && mem.getAccessibility() != Accessibility.PUBLIC)) {
				list.add(mem);
			}
		}
		
		return list;
	}
	
	public ObjectValue getParentType(ThreadRuntime rt){
		JClassType parent = null;
		if (jtyp.isObject()){
			ICompoundType ict = (ICompoundType)jtyp;
			parent = ict.getParent();
		}
		
		if (parent != null) {
			return ThreadRuntimeHelper.getScriptTypeObject(rt, parent);
		} else {
			return RefValue.NULL;
		}
	}
	
	public ArrayValue getInterfaces(ThreadRuntime rt) {
		// 1) Get all interfaces
		JInterfaceType[] intfs = null;
		if (jtyp.isObject()){
			ICompoundType ict = (ICompoundType)jtyp;
			intfs = ict.getInterfaces();
		}

		// 2) Create a type array to return
        ITypeTable tt = rt.getTypeTable();
        MemoryArea mem = rt.getHeap();
        int len = intfs != null ? intfs.length : 0;
        JClassType sysTypTyp = (JClassType)tt.getType(FQCLASSNAME);
        ObjectArrayValue array = (ObjectArrayValue)ArrayValueFactory.createArrayValue(mem, tt, sysTypTyp, len);
		
		// 3) Populate the array with Type instances for each interface
		for (int i = 0; i < len; i++) {
			TypeValue tv = tt.getValue(intfs[i].getName());
			ObjectValue ov = tv.getScriptTypeObject(rt);
			RefValue rv = new RefValue(mem, ov);		
			rv.assignTo(array.getValueAt(i));
		}
		
		return array;
	}
	
	public ArrayValue getExtensions(ThreadRuntime rt) {
		// 1) Get all extension types
		OneOrMoreList<JClassType> exts = null;
		if (jtyp.isObject()){
			ICompoundType ict = (ICompoundType)jtyp;
			exts = ict.getExtensionClasses();
		}

		// 2) Create a type array to return
        ITypeTable tt = rt.getTypeTable();
        MemoryArea mem = rt.getHeap();
        int len = exts.size();
        JClassType sysTypTyp = (JClassType)tt.getType(FQCLASSNAME);
        ObjectArrayValue array = (ObjectArrayValue)ArrayValueFactory.createArrayValue(mem, tt, sysTypTyp, len);
		
		// 3) Populate the array with Type instances for each extension type
        int i = 0;
		for (JClassType jct : exts) {
			TypeValue tv = tt.getValue(jct.getName());
			ObjectValue ov = tv.getScriptTypeObject(rt);
			RefValue rv = new RefValue(mem, ov);		
			rv.assignTo(array.getValueAt(i));
			i++;
		}
		
		return array;
	}
	
	public ArrayValue getAttributes(ThreadRuntime rt) {
		if (!(jtyp instanceof ICompoundType) ){
			return null;
		}

		ICompoundType ict = (ICompoundType)jtyp;
		TypeValue tv = rt.getTypeTable().getValue(ict.getName());
		List<AttrValue> avs = tv.getClassAttrValues();
		if (avs == null || avs.size() == 0){
			return null;
		}

		JValue[] vals = new JValue[avs.size()];
		avs.toArray(vals);

		JClassType typ = (JClassType)ThreadRuntimeHelper.loadSystemType(rt, JAttributeBaseType.Name);
		ArrayValue array = ThreadRuntimeHelper.createAndPopulateArrayValue(rt, typ, vals);
		
		return array;
	}
	
	public boolean isClass() {
		return jtyp instanceof ICompoundType && ((ICompoundType)jtyp).isClassType();
	}

	public boolean isArray() {
		return JArrayType.isArrayType(jtyp);
	}

	public boolean isFinal() {
		return 
			jtyp.isBasic() || 
			JArrayType.isArrayType(jtyp) || 
			jtyp == AnyType.getInstance() ||
			jtyp == VoidType.getInstance() ||
			jtyp instanceof ICompoundType && ((ICompoundType)jtyp).getClassProperties().isFinal();
	}

	public boolean isInterface() {
		return jtyp instanceof ICompoundType && !((ICompoundType)jtyp).isClassType();
	}

	public boolean isPrimitive() {
		return jtyp.isBasic();
	}

}