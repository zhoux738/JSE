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

package info.julang.hosting.mapped.inspect;

import java.lang.reflect.Field;

import info.julang.external.exceptions.JSEError;
import info.julang.interpretation.context.Context;
import info.julang.memory.MemoryArea;
import info.julang.memory.value.JValue;
import info.julang.memory.value.TempValueFactory;
import info.julang.memory.value.ValueUtilities;
import info.julang.typesystem.JType;
import info.julang.typesystem.jclass.builtin.JStringType;

/**
 * The mapped type is already known and resolved. Simply use {@link #getType} to get the mapped type.
 * 
 * @author Ming Zhou
 */
public class KnownMappedType implements IMappedType {

	private JType typ;
	private int dim;
	private Class<?> ocls;
	private String name;
	
	public KnownMappedType(JType typ, int dim, Class<?> ocls, String name){
		this.typ = typ;
		this.dim = dim;
		this.ocls = ocls;
		this.name = name;
	}
	
	//---------------------- IMappedType ----------------------//
	
	@Override
	public boolean isExternal() {
		return false;
	}

	@Override
	public int getDimension() {
		return dim;
	}

	@Override
	public Class<?> getOriginalClass() {
		return ocls;
	}

	@Override
	public String getParamName() {
		return name;
	}
	
	//---------------------- KnownMappedType ----------------------//
	
	public JType getType(){
		return typ;
	}

	/**
	 * Get a value initialized from a field member on platform type.
	 * 
	 * @param mem the memory area to store the new value
	 * @param field the field member from the platform type
	 * @return a value stored in the given memory area.
	 */
	public JValue getStaticValue(MemoryArea mem, Field field, Context context) {
		JValue src = null;
		
		try {
			switch(typ.getKind()){
			case BOOLEAN:
				boolean z = field.getBoolean(null);
				src = TempValueFactory.createTempBoolValue(z);
				break;
			case BYTE:
				byte b = field.getByte(null);
				src = TempValueFactory.createTempByteValue(b);
				break;
			case CHAR:
				char c = field.getChar(null);
				src = TempValueFactory.createTempCharValue(c);
				break;
			case FLOAT:
				float f = field.getFloat(null);
				src = TempValueFactory.createTempFloatValue(f);
				break;
			case INTEGER:
				int i = field.getInt(null);
				src = TempValueFactory.createTempIntValue(i);
				break;
			case CLASS:
				if (typ == JStringType.getInstance()){
					String str = (String)field.get(null);
					src = TempValueFactory.createTempStringValue(str);
					break;
				}
				// FALL THROUGH
			default:
				throw new JSEError(typ.getName() + " is not a known type to map to.");
			}
		} catch (IllegalArgumentException e) {
			// Shouldn't happen since we only map static fields.
			throw new JSEError("Cannot retrieve field value due to argument error: " + e.getMessage());
		} catch (IllegalAccessException e) {
			// Shouldn't happen since we only map public fields.
			throw new JSEError("Cannot retrieve field value due to access error: " + e.getMessage());
		}
		
		if (src == null) {
			throw new JSEError("Cannot retrieve field value from platform type.");
		}
		
		JValue tgt = ValueUtilities.makeDefaultValue(mem, typ, false);
		src.assignTo(tgt);
		
		return tgt;
	}

	//---------------------- Object ----------------------//

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + dim;
		result = prime * result + ((typ == null) ? 0 : typ.getName().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		KnownMappedType other = (KnownMappedType) obj;
		if (dim != other.dim)
			return false;
		if (typ == null) {
			if (other.typ != null)
				return false;
		} else if (!typ.getName().equals(other.typ.getName()))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		String ret = typ.getName();
		if (dim > 0) {
			int rank = dim;
			while (rank > 0) {
				ret += "[]";
			}
		}
		
		return ret;
	}
}
