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

package info.julang.typesystem.jclass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import info.julang.execution.symboltable.ITypeTable;
import info.julang.typesystem.AnyType;
import info.julang.typesystem.BuiltinTypes;
import info.julang.typesystem.JType;
import info.julang.typesystem.VoidType;
import info.julang.typesystem.basic.BoolType;
import info.julang.typesystem.basic.ByteType;
import info.julang.typesystem.basic.CharType;
import info.julang.typesystem.basic.FloatType;
import info.julang.typesystem.basic.IntType;
import info.julang.typesystem.jclass.builtin.BuiltinClassTypeBuilder;
import info.julang.typesystem.jclass.builtin.JArrayBaseType;
import info.julang.typesystem.jclass.builtin.JArrayType;
import info.julang.typesystem.jclass.builtin.JAttributeBaseType;
import info.julang.typesystem.jclass.builtin.JDynamicType;
import info.julang.typesystem.jclass.builtin.JEnumBaseType;
import info.julang.typesystem.jclass.builtin.JFunctionType;
import info.julang.typesystem.jclass.builtin.JObjectType;
import info.julang.typesystem.jclass.builtin.JStringType;
import info.julang.typesystem.jclass.builtin.JTypeStaticDataType;

/**
 * A bootstrapper used to load a set of most fundamental built-in object types.
 * <p>
 * Object types are common loaded during runtime on demand. But those special 
 * types, such as Object, Array and Function, must be loaded at an earlier stage.
 * Since these types are also referring among themselves, they must be loaded 
 * together in a process in which they can reference each other despite their
 * still being loaded.
 *
 * @author Ming Zhou
 */
public final class BuiltinTypeBootstrapper {

	private static boolean done;
	
	private static Set<String> biTypNames;
	
	/**
	 * Check if the given name is a built-in type.
	 * 
	 * @param name
	 * @return
	 */
	public static boolean isBuiltInType(String name) {
		if (biTypNames == null) {
			synchronized(BuiltinTypeBootstrapper.class) {
				if (biTypNames == null) {
					biTypNames = new HashSet<String>();
					
					biTypNames.add(IntType.getInstance().getName());
					biTypNames.add("int");
					biTypNames.add(BoolType.getInstance().getName());
					biTypNames.add("bool");
					biTypNames.add(ByteType.getInstance().getName());
					biTypNames.add("byte");
					biTypNames.add(CharType.getInstance().getName());
					biTypNames.add("char");
					biTypNames.add(FloatType.getInstance().getName());
					biTypNames.add("float");
					biTypNames.add(AnyType.getInstance().getName());
					biTypNames.add(VoidType.getInstance().getName());
					biTypNames.add("void");
					biTypNames.add(JObjectType.FQNAME.toString());
					biTypNames.add(JStringType.FQNAME.toString());
					biTypNames.add(JArrayBaseType.FQNAME.toString());
					biTypNames.add(JEnumBaseType.FQNAME.toString());
					biTypNames.add(JAttributeBaseType.Name);
					biTypNames.add(JFunctionType.FQNAME.toString());
					biTypNames.add(JDynamicType.FQNAME.toString());
				}
			}
		}
		
		return biTypNames.contains(name);
	}

	/**
	 * Initialize built-in non-class types.
	 * 
	 * @param typeTable
	 */
	public static void bootstrapNonClassTypes(ITypeTable typeTable) {
		JType[] typs = new JType[]{
			IntType.getInstance(),
			BoolType.getInstance(),
			ByteType.getInstance(),
			CharType.getInstance(),
			FloatType.getInstance(),
			AnyType.getInstance(),
			VoidType.getInstance()
		};
		
		for (JType t : typs) {
			typeTable.addType(t.getName(), t);
		}
		
		List<String> names = new ArrayList<String>();
		for (int i = 0; i < typs.length; i++){
			names.add(typs[i].getName());
		}
		
		typeTable.finalizeTypes(names);
	}
	
	/**
	 * Initialize built-in class types.
	 * 
	 * @return A map of built-in name~type pairs. Note these include some array types, which must be processed slightly differently
	 * in {@link info.julang.execution.symboltable.TypeTable TypeTable}, where the array type is also tracked through an
	 * internal cache.
	 */
	public static Map<String, JClassType> bootstrapClassTypes(){
		if(!done){
			synchronized(BuiltinTypeBootstrapper.class){
				if(!done){
					bootstrap0();
					done = true;
				}
			}
		}
		
		return allTypes;
	}
	
	/**
	 * Clear all cached built-in types. Calling {@link #bootstrapClassTypes()} will cause rebuild.
	 */
	public static void clearClassTypes() {
		List<BuiltinTypeBuilder> bootstrapperList = new ArrayList<BuiltinTypeBuilder>();
		createBootstrappers(bootstrapperList);
		for (BuiltinTypeBuilder b : bootstrapperList) {
			b.builder.reset();
		}
		allTypes = null;
		arrayType = null;
		done = false;
	}
	
	private static void bootstrap0(){
		// Get all the bootstrappers
		List<BuiltinTypeBuilder> bootstrapperList = new ArrayList<BuiltinTypeBuilder>();
		int total = createBootstrappers(bootstrapperList);
		if(allTypes == null){
			allTypes = new HashMap<String, JClassType>();
		}
		
		// Create prototypes and put them into the suite collection
		TypeBootstrapper[] bootstrappers = new TypeBootstrapper[total];
		BuiltinClassTypeBuilder[] builders = new BuiltinClassTypeBuilder[total];
		int i = 0;
		Map<BuiltinTypes, TypeSuite> suiteMap = new TreeMap<BuiltinTypes, TypeSuite>();
		for(BuiltinTypeBuilder entry : bootstrapperList){
			BuiltinTypes typeName = entry.type;
			TypeBootstrapper bs = entry.builder;
			TypeSuite suite = new TypeSuite(
				new BuiltinClassTypeBuilder(
					bs.getTypeName(), 
					bs.providePrototype()));
			suiteMap.put(typeName, suite);
			bootstrappers[i] = bs;
			builders[i] = suite.builder;
			i++;
		}
		
		// Prepare a farm to "cultivate" types
		TypeFarm suites = new TypeFarm(suiteMap);
		
		// Add array types
		arrayType = suiteMap.get(BuiltinTypes.ARRAY).stub;
		// Some primitive array types - add more if needed in future
		addArrayType(suites, BuiltinTypes.CHAR, CharType.getInstance());
        addArrayType(suites, BuiltinTypes.BYTE, ByteType.getInstance());
        addArrayType(suites, BuiltinTypes.ANY, AnyType.getInstance());
		for(BuiltinTypeBuilder entry : bootstrapperList){
			TypeBootstrapper bs = entry.builder;
			if (bs.initiateArrayType()) {
				JType elementType = suiteMap.get(entry.type).stub;
				addArrayType(suites, entry.type, elementType);
			}
		}
		
		// Now, implement each type
		for(i=0; i<total; i++){
			bootstrappers[i].implementItself(builders[i], suites);
		}
		
		// Finally, complete bootstrapping
		for(i=0; i<total; i++){
			bootstrappers[i].bootstrapItself(builders[i]);
			allTypes.put(bootstrappers[i].getTypeName(), bootstrappers[i].providePrototype());
		}
	}
	
	private static void addArrayType(TypeFarm suites, BuiltinTypes bt, JType eleType){
		JArrayType jat1 = new JArrayType(eleType, arrayType);
		suites.addArrayType(bt, jat1);
        allTypes.put(jat1.getName(), jat1);
	}
	
	private static Map<String, JClassType> allTypes;
	private static JClassType arrayType; 
	
	private static int createBootstrappers(List<BuiltinTypeBuilder> list){
		list.add(new BuiltinTypeBuilder(BuiltinTypes.OBJECT, new JObjectType.BootstrapingBuilder()));
		list.add(new BuiltinTypeBuilder(BuiltinTypes.STRING, new JStringType.BootstrapingBuilder()));
		list.add(new BuiltinTypeBuilder(BuiltinTypes.ARRAY, new JArrayBaseType.BootstrapingBuilder()));
		list.add(new BuiltinTypeBuilder(BuiltinTypes.TYPE, new JTypeStaticDataType.BootstrapingBuilder()));
		list.add(new BuiltinTypeBuilder(BuiltinTypes.ENUM, new JEnumBaseType.BootstrapingBuilder()));
		list.add(new BuiltinTypeBuilder(BuiltinTypes.ATTRIBUTE, new JAttributeBaseType.BootstrapingBuilder()));
		list.add(new BuiltinTypeBuilder(BuiltinTypes.FUNCTION, JFunctionType.PrototypeBuilder));	
		list.add(new BuiltinTypeBuilder(BuiltinTypes.DYNAMIC, new JDynamicType.BootstrapingBuilder()));
		
		return list.size();
	}
	
	/**
	 * This class contains types in their infancy. One can pull stub or builder for 
	 * any built-in type from this farm.
	 */
	public static class TypeFarm {
		
		private Map<BuiltinTypes, TypeSuite> map;
		
		private Map<BuiltinTypes, JArrayType> arrCache;
		
		private TypeFarm(Map<BuiltinTypes, TypeSuite> map){
			this.map = map;
			this.arrCache = new HashMap<BuiltinTypes, JArrayType>();
		}
		
		private void addArrayType(BuiltinTypes type, JArrayType jat) {
			arrCache.put(type, jat);
		}
		
		public JArrayType getArrayType(BuiltinTypes type) {
			return arrCache.get(type);
		}
		
		public JClassType getStub(BuiltinTypes type){
			return map.get(type).stub;
		}
		
		public JClassTypeBuilder getBuilder(BuiltinTypes type){
			return map.get(type).builder;
		}
	}
	
	private static class TypeSuite {
		
		JClassType stub;
		
		BuiltinClassTypeBuilder builder;
		
		TypeSuite(BuiltinClassTypeBuilder builder){
			this.builder = builder;
			this.stub = (JClassType)builder.getStub();
		}
	}
	
	private static class BuiltinTypeBuilder {
		private BuiltinTypes type;
		private TypeBootstrapper builder;
		
		private BuiltinTypeBuilder(BuiltinTypes type, TypeBootstrapper builder){
			this.type = type;
			this.builder = builder;
		}
	}
	
}
