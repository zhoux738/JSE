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

package info.julang.execution.symboltable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import info.julang.execution.EngineRuntime;
import info.julang.execution.namespace.NamespacePool;
import info.julang.execution.threading.IThreadLocalObjectFactory;
import info.julang.execution.threading.JThread;
import info.julang.execution.threading.JThreadManager;
import info.julang.execution.threading.StackAreaFactory;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.execution.threading.ThreadStack;
import info.julang.external.exceptions.JSEError;
import info.julang.external.interfaces.IExtEngineRuntime;
import info.julang.external.interfaces.IExtMemoryArea;
import info.julang.interpretation.context.Context;
import info.julang.memory.MemoryArea;
import info.julang.memory.StackArea;
import info.julang.memory.simple.SimpleStackArea;
import info.julang.memory.value.JValue;
import info.julang.memory.value.MethodValue;
import info.julang.memory.value.ObjectMember;
import info.julang.memory.value.TypeValue;
import info.julang.modulesystem.IModuleManager;
import info.julang.modulesystem.ModuleManager;
import info.julang.typesystem.JType;
import info.julang.typesystem.jclass.Accessibility;
import info.julang.typesystem.jclass.BuiltinTypeBootstrapper;
import info.julang.typesystem.jclass.ICompoundType;
import info.julang.typesystem.jclass.JClassMember;
import info.julang.typesystem.jclass.JClassMethodMember;
import info.julang.typesystem.jclass.JClassType;
import info.julang.typesystem.jclass.MemberType;
import info.julang.typesystem.jclass.builtin.IDeferredBuildable;
import info.julang.typesystem.jclass.builtin.JArrayType;
import info.julang.typesystem.jclass.builtin.JEnumType;
import info.julang.typesystem.loading.InternalTypeResolver;
import info.julang.util.OneOrMoreList;

/**
 * Type table contains all the definition of types during runtime.
 * <p/>
 * Part of type information is stored by a backing memory area. This includes static fields of a class,
 * and the code block of functions.
 * <p/>
 * Type table is globally unique and thread safe. When adding a new type to the table, the type should be
 * added with <code>finalized = false</code>. Then after the initialization is done, mark <code>finalized
 * </code> as <code>true</code>. This is because initialization cannot be performed while the type is not
 * present in type table.
 *
 * @author Ming Zhou
 */
public class TypeTable implements ITypeTable {

	/**
	 * Type info stored inside type table, including:
	 * <p>
	 * 1) type metadata <br>
	 * 2) type value - storing static members <br>
	 * 3) a list of references to array types of different dimensions with this type as element <br>
	 */
	private static class TypeInfo {
		JType type;
		TypeValue value;
		OneOrMoreList<JArrayType> art;
		boolean finalized;
		
		// TODO - when we add support of type overwriting in REPL, must call this when loading new types
		// so that we can remove all types of the same name.
		public void addArrayType(JArrayType arrayType) {
			if (art == null) {
				art = new OneOrMoreList<JArrayType>(arrayType);
			} else {
				art.add(arrayType);
			}
		}
		
		@Override
		public String toString() {
			return type.getName() + (finalized ? " (F)" : " (NF)");
		}
	}
	
	private static class ArrayTypeInfo {
		JArrayType type;
		TypeValue value;
	}
	
	private boolean initialized;
	
	private MemoryArea heap;
	
	private Map<String, TypeInfo> types = new HashMap<String, TypeInfo>();
	
	private ExtMethodCache extMethodCache = new ExtMethodCache();
	
	private Map<String, ArrayTypeInfo> arrayTypes = Collections.synchronizedMap(new HashMap<String, ArrayTypeInfo>());
	
	/**
	 * [CFOW]
	 * 
	 * Create a new type table. 
	 * @param heap nominally accepts an {@link IExtMemoryArea}, but in fact it <b>must</b> be an instance of {@link MemoryArea}.
	 */
	public TypeTable(IExtMemoryArea heap){
		this.heap = (MemoryArea)heap;
	}
	
	/**
	 * Get the type by name.
	 * 
	 * @param fqname fully qualified name
	 * @return null if no type of given name is defined.
	 */
	public synchronized JType getType(String fqname){
		return getType(fqname, false);
	}
	
	/**
	 * Get the type by name.
	 * 
	 * @param fqname fully qualified name
	 * @param requireFinalized specify true if the type must be finalized to retrieve. Un-finalized type will be treated as if not present.
	 * @return null if no type of given name is defined.
	 */
	public synchronized JType getType(String fqname, boolean requireFinalized){
		TypeInfo info = types.get(fqname);
		return 
			info == null ? null : 
				(requireFinalized && !info.finalized) ? null : info.type;
	}
	
	/**
	 * Get the type value by name.
	 * <p/>
	 * Type value is the runtime data of a type (containing static fields, for example)
	 * 
	 * @param fqname fully qualified name
	 * @return null if no type of given name is defined.
	 */
	public synchronized TypeValue getValue(String fqname){
		return getValue(fqname, false);
	}
	
	/**
	 * Get the type value by name.
	 * <p/>
	 * Type value is the runtime data of a type (containing static fields, for example)
	 * 
	 * @param fqname fully qualified name
	 * @param requireFinalized specify true if the type must be finalized to retrieve. Un-finalized type will be treated as if not present.
	 * @return null if no type of given name is defined.
	 */
	public synchronized TypeValue getValue(String fqname, boolean requireFinalized){
		if(fqname.startsWith("[")) {
			ArrayTypeInfo info = arrayTypes.get(fqname);
			return info == null ? null : info.value;
		} else {
			TypeInfo info = types.get(fqname);
			return 
				info == null ? null : 
					(requireFinalized && !info.finalized) ? null : info.value;
		}
	}
	
	/**
	 * Get all extension methods of the given name targeting the specified type.
	 * 
	 * @param methodName The name of the extension method
	 * @param extendee The extended type
	 * @return never null
	 */
	public OneOrMoreList<ObjectMember> getExtensionMethodsByClass(String methodName, ICompoundType extendee){
		// Try cache first
		OneOrMoreList<ObjectMember> result = extMethodCache.get(extendee, methodName);
		if (result != null) {
			return result;
		}
		
		JClassType[] allExts = extendee.getAllExtensionClasses();
		if (allExts.length > 0) {
			int rank = Integer.MAX_VALUE >> 1;
			for (JClassType extClass : allExts) {
				JClassMember[] members = extClass.getClassStaticMembers();
				for (JClassMember jcm : members) {
					if (jcm.getName().equals(methodName)
						&& jcm.getMemberType() == MemberType.METHOD 
						&& jcm.getAccessibility() == Accessibility.PUBLIC) {
						JClassMethodMember jcmm = (JClassMethodMember)jcm;
						if(jcmm.getMethodType().mayExtend(extendee)) {
							JValue metVal = getStaticMethodValue(extClass, jcmm);
							if (metVal != null) {
								ObjectMember om = new ObjectMember(metVal, rank);
								rank++;
								if (result == null) {
									result = new OneOrMoreList<ObjectMember>(om);
								} else {
									result.add(om);
								}
							}
						}
					}
				}
			}
		}
		
		if (result == null) {
			result = new OneOrMoreList<ObjectMember>();
		}
		
		// Add to cache
		extMethodCache.put(extendee, methodName, result);
		
		return result;
	}
	
	private JValue getStaticMethodValue(JClassType jclass, JClassMethodMember jcmm) {
		TypeValue tval = getValue(jclass.getName());
		if (tval == null){//DELETEME
			TypeValue tv = getValue(jclass.getName(), false);
			System.out.println(jclass.getName() + (tv == null ? " is not initialized. " : " is not finalized."));
		}
		MethodValue[] methods = tval.getMethodMemberValues(jcmm.getName());
		for (MethodValue method : methods) {
			if (method.getMethodType() == jcmm.getMethodType()) {
				// This is the value matching the located member
				return method;
			}
		}

		return null;
	}
	
	public void addType(String name, JType type){
		addType(name, type, true, false);
	}
	
	TypeInfo addBuiltinType(String name, JType type) {
		return addType(name, type, false, true);
	}
	
	private synchronized TypeInfo addType(String name, JType type, boolean createValue, boolean finalized){
		if(types.containsKey(name)){
			throw new SymbolDuplicatedDefinitionException(name);
		}
		
		TypeInfo info = new TypeInfo();
		info.type = type;
		if (createValue) {
			info.value = new TypeValue(heap, type);	
		}
		
		if (type instanceof JEnumType) {
			((JEnumType)type).setValue(info.value);
		}
		info.finalized = finalized;
		types.put(name, info);
		
		return info;
	}
	
	/**
	 * Initialize this type table with Julian's built-in class types.
	 * 
	 * @param rt
	 */
	public synchronized void initialize(IExtEngineRuntime rt) {
		if(!initialized){
			// Build built-in class types
			Map<String, JClassType> builtinTypes = BuiltinTypeBootstrapper.bootstrapClassTypes();
			
			// Add built-in class types to type table, but do not create type values yet 
			// (addBuiltinType won't create type value)
			List<IDeferredBuildable> deferred = new ArrayList<IDeferredBuildable>();
			List<TypeInfo> builtinTypeInfos = new ArrayList<TypeInfo>();
			for(Entry<String, JClassType> entry : builtinTypes.entrySet()) {
				JClassType type = entry.getValue();
				if (type.deferBuild()){
					deferred.add((IDeferredBuildable)type);
				}
				
				builtinTypeInfos.add(addBuiltinType(entry.getKey(), type));
				
				if (JArrayType.isArrayType(type)) {
					ArrayTypeInfo ati = new ArrayTypeInfo();
					ati.type = (JArrayType)type;
					// ati.value = new TypeValue(heap, type);
					arrayTypes.put(type.getName(), ati);
				}
			}

			// Add built-in primitive types to type table
			BuiltinTypeBootstrapper.bootstrapNonClassTypes(this);

			// Complete building for certain built-in class types which have references to JuFC types
			EngineRuntime ert = (EngineRuntime)rt;
			ModuleManager mmg = (ModuleManager)ert.getModuleManager();
			PreRunningThreadRuntime tr = new PreRunningThreadRuntime(ert);
			mmg.loadModule(tr.getJThread(), "System.Util");
			Context context = Context.createSystemLoadingContext(tr);
			for(IDeferredBuildable bd : deferred){
				bd.completeBuild(context);
			}

			// Create type values now
			for (TypeInfo ti : builtinTypeInfos) {
				ti.value = new TypeValue(heap, ti.type);
			}
			for (ArrayTypeInfo ati : arrayTypes.values()) {
				ati.value = new TypeValue(heap, ati.type);
			}
			
			initialized = true;
		}
	}
	
	public synchronized void finalizeTypes(List<String> typeNames) {
		for(String fqname : typeNames){
			TypeInfo info = types.get(fqname);
			if(info != null){
				info.finalized = true;
			}
		}
	}
	
	public synchronized void removeUnfinalizedTypes(List<String> typeNames){
		for(String fqname : typeNames){
			TypeInfo info = types.remove(fqname);
			if(info != null && info.finalized){
				throw new JSEError("Removed a finalized type.");
			}
		}
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		for(Entry<String, TypeInfo> entry : types.entrySet()){
			sb.append("  " + entry.getKey());
			sb.append(":  " + entry.getValue().type.getName());	
			sb.append("\n");
		}
		
		return sb.toString();
	}

	//----------------------- Array Type Cache ------------------------//
	// Array types are created implicitly in the script (through array
	// definitions like "Object[]". To make sure that we have only one
	// instance for each kind of array, type table maintains a cache to
	// store the mapping. Note a cache hit doesn't necessarily mean that
	// the type is also added to the type table itself, although a cache
	// miss is surely indicative of the fact that the type is not to be 
	// found in the type table either.
	
	/**
	 * Try to get array type from array type cache.
	 * 
	 * @param elementType
	 * @return
	 */
	public JArrayType getArrayType(JType elementType){
		String name = "[" + elementType.getName() + "]";
		ArrayTypeInfo ati = arrayTypes.get(name);
		return ati != null ? ati.type : null;
	}
	
	/**
	 * Add an array type to the array type cache.
	 * 
	 * @param arrayType
	 */
	public synchronized void addArrayType(JArrayType arrayType){
		String etname = arrayType.getElementType().getName();
		String name = "[" + etname + "]";
		ArrayTypeInfo ati = arrayTypes.get(name);
		if(ati == null){
			ati = new ArrayTypeInfo();
			ati.type = arrayType;
			ati.value = new TypeValue(heap, arrayType);
			arrayTypes.put(name, ati);
			
			// TODO - Add a reference to the element type. Note we must use the innermost element type.
			//TypeInfo tinfo = types.get(getInnerMostElementType(arrayType));
			//tinfo.addArrayType(arrayType);
		}
	}
	
	/**
	 * An internal thread runtime used only for pre-runtime type loading.
	 * 
	 * @author Ming Zhou
	 */
	private class PreRunningThreadRuntime implements ThreadRuntime {

		private EngineRuntime engineRt;
		private ThreadStack ts;
		
		private PreRunningThreadRuntime(EngineRuntime engineRt){
			this.engineRt = engineRt;
			final StackArea ssa = new SimpleStackArea();
			this.ts = new ThreadStack(new StackAreaFactory(){

				@Override
				public StackArea createStackArea() {
					return ssa;
				}
				
			}, new VariableTable(null));
			this.ts.pushFrame();
			this.ts.setNamespacePool(new NamespacePool());
		}
		
		@Override
		public JThread getJThread(){
			return null;
		}
		
		@Override
		public MemoryArea getHeap() {
			return engineRt.getHeap();
		}

		@Override
		public ITypeTable getTypeTable() {
			return engineRt.getTypeTable();
		}
		
		@Override
		public IVariableTable getGlobalVariableTable() {
			return engineRt.getGlobalVariableTable();
		}

		@Override
		public StackArea getStackMemory() {
			return ts.getStackArea();
		}

		@Override
		public ThreadStack getThreadStack() {
			return ts;
		}

		@Override
		public IModuleManager getModuleManager() {
			return engineRt.getModuleManager();
		}

		@Override
		public InternalTypeResolver getTypeResolver() {
			return engineRt.getTypeResolver();
		}

		@Override
		public JThreadManager getThreadManager() {
			return engineRt.getThreadManager();
		}
		
        @Override
        public Object putLocal(String key, IThreadLocalObjectFactory factory) {
            // No support for local storage
            return null;
        }

        @Override
        public Object getLocal(String key) {
            // No support for local storage
            return null;
        }
	}
}
