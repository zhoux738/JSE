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

import info.julang.external.interfaces.IExtEngineRuntime;
import info.julang.memory.value.TypeValue;
import info.julang.typesystem.JType;
import info.julang.typesystem.UnknownTypeException;
import info.julang.typesystem.jclass.annotation.IllegalAttributeUsageException;
import info.julang.typesystem.jclass.builtin.JArrayType;
import info.julang.typesystem.jclass.builtin.JEnumType;
import info.julang.typesystem.loading.ClassLoadingException;

/**
 * A type table that only allows access to certain built-in types. Used for expressions to initialize attributes.
 * <p>
 * In attribute initializer, only primitive type, built-in module-less types (such as Object and String), Enum 
 * types (including user-defined), and their one-dimensional array types are allowed.
 *
 * @author Ming Zhou
 */
public class RestrictedTypeTable implements ITypeTable {

	private ITypeTable tt;
	
	public RestrictedTypeTable(ITypeTable tt) {
		this.tt = tt;
	}

	@Override
	public boolean initialize(IExtEngineRuntime rt) {
		// NO-OP (underlying table is initialized)
		return false;
	}

	@Override
	public void finalizeTypes(List<String> typeNames) {
		this.tt.finalizeTypes(typeNames);
	}

	@Override
	public JType getType(String fqname) {
		return this.tt.getType(fqname);
	}

	@Override
	public JType getType(String fqname, boolean requireFinalized) {
		JType typ = this.tt.getType(fqname, requireFinalized);		
		if (typ != null && !isAllowedInAttributContext(typ)) {
			throw new IllegalAttributeUsageException(
				"Trying to use a type which is not allowed in Attribute initializer: " + typ.getName());
		}
		
		return typ;
	}

	@Override
	public TypeValue getValue(String fqname) {
		return this.tt.getValue(fqname);
	}

	@Override
	public TypeValue getValue(String fqname, boolean requireFinalized) {
		return this.tt.getValue(fqname, requireFinalized);
	}

	@Override
	public void addType(String name, JType type) {
		if (!isAllowedInAttributContext(type)) {
			throw new IllegalAttributeUsageException(
				"Trying to load a type which is not allowed in Attribute initializer: " + type.getName());
		}
		
		this.tt.addType(name, type);
	}

	@Override
	public void removeUnfinalizedTypes(List<String> typeNames) {
		this.tt.removeUnfinalizedTypes(typeNames);
	}

	@Override
	public JArrayType getArrayType(JType elementType) {
		if (!isAllowedInAttributContext(elementType, false)) {
			throw new IllegalAttributeUsageException(
				"Trying to load a multi-dimenional type which is not allowed in Attribute initializer. Element type: " + elementType.getName());
		}
		
		return this.tt.getArrayType(elementType);
	}

	@Override
	public void addArrayType(JArrayType arrayType) {
		if (!isAllowedInAttributContext(arrayType.getElementType(), false)) {
			throw new IllegalAttributeUsageException(
				"Trying to load a multi-dimenional type which is not allowed in Attribute initializer: " + arrayType.getName());
		}
		
		this.tt.addArrayType(arrayType);
	}
	
	public ITypeTable getUnderlyingTypeTable(){
		return tt;
	}
	
	public static boolean isAllowedInAttributContext(JType typ){
		return isAllowedInAttributContext(typ, true);
	}
	
	private static boolean isAllowedInAttributContext(JType typ, boolean allowArray){
		if (typ.isBuiltIn()) {
			return true;
		}
		
		if (typ instanceof JEnumType || typ.getName().equals("System.AttributeTarget")) {
			return true;
		}
		
		if (allowArray && typ instanceof JArrayType) {
			JArrayType jat = (JArrayType)typ;
			return isAllowedInAttributContext(jat.getElementType(), false);
		}
		
		return false;
	}
}
