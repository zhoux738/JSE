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

import java.util.List;

import info.julang.external.interfaces.IExtTypeTable;
import info.julang.memory.value.TypeValue;
import info.julang.typesystem.JType;
import info.julang.typesystem.jclass.builtin.JArrayType;

public interface ITypeTable extends IExtTypeTable {

	/**
	 * Get the type by name.
	 * 
	 * @param fqname fully qualified name
	 * @return null if no type of given name is defined.
	 */
	public JType getType(String fqname);
	
	/**
	 * Get the type by name.
	 * 
	 * @param fqname fully qualified name
	 * @param requireFinalized specify true if the type must be finalized to retrieve. Un-finalized type will be treated as if not present.
	 * @return null if no type of given name is defined.
	 */
	public JType getType(String fqname, boolean requireFinalized);
	
	/**
	 * Get the type value by name.
	 * <p>
	 * Type value is the runtime data of a type (containing static fields, for example)
	 * 
	 * @param fqname fully qualified name
	 * @return null if no type of given name is defined.
	 */
	public TypeValue getValue(String fqname);
	
	/**
	 * Get the type value by name.
	 * <p>
	 * Type value is the runtime data of a type (containing static fields, for example)
	 * 
	 * @param fqname fully qualified name
	 * @param requireFinalized specify true if the type must be finalized to retrieve. Un-finalized type will be treated as if not present.
	 * @return null if no type of given name is defined.
	 */
	public TypeValue getValue(String fqname, boolean requireFinalized);
	
	/**
	 * Add a new type to type table. A {@link info.julang.memory.value.TypeValue type value} 
	 * for this type is also added to heap memory. The type is not finalized yet, therefore
	 * cannot be retrieved by calling {@link #addType(String, JType)} at this point. Only after
	 * {@link #finalizeTypes(List)} with the type's name will this type become available.
	 * 
	 * @param name
	 * @param type
	 */
	public void addType(String name, JType type);
	
	/**
	 * Remove all types which are not finalized.
	 */
	public void removeUnfinalizedTypes(List<String> typeNames);
	
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
	public JArrayType getArrayType(JType elementType);
	
	/**
	 * Add an array type to the array type cache.
	 * 
	 * @param arrayType
	 */
	public void addArrayType(JArrayType arrayType);
}
