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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import info.julang.external.exceptions.JSEError;
import info.julang.external.interfaces.IExtMemoryArea;
import info.julang.memory.MemoryArea;
import info.julang.memory.value.TypeValue;
import info.julang.typesystem.JType;
import info.julang.typesystem.jclass.BuiltinTypeBootstrapper;
import info.julang.typesystem.jclass.JClassType;
import info.julang.typesystem.jclass.builtin.JArrayType;
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
	}
	
	private boolean initialized;
	
	private MemoryArea heap;
	
	private Map<String, TypeInfo> types = new HashMap<String, TypeInfo>();
	
	private Map<String, JArrayType> arrayTypes = Collections.synchronizedMap(new HashMap<String, JArrayType>());
	
	public static TypeTable singleton;
	
	/**
	 * [CFOW]
	 * 
	 * Create a new type table. 
	 * @param heap nominally accepts an {@link IExtMemoryArea}, but in fact it <b>must</b> be an instance of {@link MemoryArea}.
	 */
	public TypeTable(IExtMemoryArea heap){
		this.heap = (MemoryArea)heap;
		singleton = this;
	}
	
	/**
	 * Get the current global instance of type table.
	 * <p>
	 * Within a JSE runtime there is only one type table. This method works as long as the type table is created through engine 
	 * factory's interface, such that a unique class loader is used for a different engine instance. 
	 *  
	 * @return The type table hosted by the current JSE runtime.
	 */
	public static TypeTable getInstance(){
		return singleton;
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
		TypeInfo info = types.get(fqname);
		return 
			info == null ? null : 
				(requireFinalized && !info.finalized) ? null : info.value;
	}
	
	/**
	 * Add a new type to type table. A {@link info.julang.memory.value.TypeValue type value} 
	 * for this type is also added to heap memory.
	 * 
	 * @param name
	 * @param type
	 * @param finalized
	 */
	public synchronized void addType(String name, JType type, boolean finalized){
		if(types.containsKey(name)){
			throw new SymbolDuplicatedDefinitionException(name);
		}
		
		TypeInfo info = new TypeInfo();
		info.type = type;
		info.value = new TypeValue(heap, type);	
		info.finalized = finalized;
		types.put(name, info);
	}
	
	/**
	 * Initialize this type table with Julian's built-in class types.
	 * 
	 * @param builtinTypes
	 */
	public synchronized void initialize() {
		if(!initialized){
			Map<String, JClassType> builtinTypes = BuiltinTypeBootstrapper.bootstrapClassTypes();

			for(Entry<String, JClassType> entry : builtinTypes.entrySet()) {
				JClassType type = entry.getValue();
				addType(entry.getKey(), type, true);
				
				if (JArrayType.isArrayType(type)) {
					arrayTypes.put(type.getName(), (JArrayType)type);
				}
			}
			
			BuiltinTypeBootstrapper.bootstrapNonClassTypes(this);

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
		return arrayTypes.get(name);
	}
	
	/**
	 * Add an array type to the array type cache.
	 * 
	 * @param arrayType
	 */
	public synchronized void addArrayType(JArrayType arrayType){
		String etname = arrayType.getElementType().getName();
		String name = "[" + etname + "]";
		JType type = arrayTypes.get(name);
		if(type==null){
			arrayTypes.put(name, arrayType);
			
			// TODO - Add a reference to the element type. Note we must use the innermost element type.
			//TypeInfo tinfo = types.get(getInnerMostElementType(arrayType));
			//tinfo.addArrayType(arrayType);
		}
	}
}
