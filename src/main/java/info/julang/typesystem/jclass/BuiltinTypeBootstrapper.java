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
import info.julang.typesystem.jclass.builtin.JArrayBaseType;
import info.julang.typesystem.jclass.builtin.JArrayType;
import info.julang.typesystem.jclass.builtin.JAttributeBaseType;
import info.julang.typesystem.jclass.builtin.JEnumBaseType;
import info.julang.typesystem.jclass.builtin.JFunctionType;
import info.julang.typesystem.jclass.builtin.JObjectType;
import info.julang.typesystem.jclass.builtin.JStringType;
import info.julang.typesystem.jclass.builtin.JTypeStaticDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A bootstrapper used to load a set of most fundamental built-in object types.
 * <p/>
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
			typeTable.addType(t.getName(), t, true);
		}
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
	
	private static void bootstrap0(){
		// Get all the bootstrappers
		List<BuiltinTypeBuilder> bootstrapperList = new ArrayList<BuiltinTypeBuilder>();
		int total = createBootstrappers(bootstrapperList);
		if(allTypes == null){
			allTypes = new HashMap<String, JClassType>();
		}
		
		// Create prototypes and put them into the suite collection
		TypeBootstrapper[] bootstrappers = new TypeBootstrapper[total];
		JClassTypeBuilder[] builders = new JClassTypeBuilder[total];
		int i = 0;
		Map<BuiltinTypes, TypeSuite> suiteMap = new HashMap<BuiltinTypes, TypeSuite>();
		for(BuiltinTypeBuilder entry : bootstrapperList){
			BuiltinTypes typeName = entry.type;
			TypeBootstrapper bs = entry.builder;
			TypeSuite suite = new TypeSuite(
				new JClassTypeBuilder(
					bs.getTypeName(), 
					bs.providePrototype(), 
					true)); // skip sanity check
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
			bootstrappers[i].boostrapItself(builders[i]);
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
	
	private static int createBootstrappers(List<BuiltinTypeBuilder> map){
		map.add(new BuiltinTypeBuilder(BuiltinTypes.OBJECT, new JObjectType.BoostrapingBuilder()));
		map.add(new BuiltinTypeBuilder(BuiltinTypes.STRING, new JStringType.BoostrapingBuilder()));
		map.add(new BuiltinTypeBuilder(BuiltinTypes.ARRAY, new JArrayBaseType.BoostrapingBuilder()));
		map.add(new BuiltinTypeBuilder(BuiltinTypes.TYPE, new JTypeStaticDataType.BoostrapingBuilder()));
		map.add(new BuiltinTypeBuilder(BuiltinTypes.ENUM, new JEnumBaseType.BoostrapingBuilder()));
		map.add(new BuiltinTypeBuilder(BuiltinTypes.ATTRIBUTE, new JAttributeBaseType.BoostrapingBuilder()));
		map.add(new BuiltinTypeBuilder(BuiltinTypes.FUNCTION, JFunctionType.PrototypeBuilder));	
		return map.size();
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
		
		JClassTypeBuilder builder;
		
		TypeSuite(JClassTypeBuilder builder){
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
