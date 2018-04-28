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

package info.julang.memory.value;

import info.julang.execution.symboltable.ITypeTable;
import info.julang.external.exceptions.JSEError;
import info.julang.memory.MemoryArea;
import info.julang.typesystem.JType;
import info.julang.typesystem.JTypeKind;

/**
 * A factory class used to create various array values. This is the only way throughout
 * the codebase to create any array values. Constructors of those underlying implementations
 * shall not (and cannot) be called.
 * 
 * @author Ming Zhou
 */
public final class ArrayValueFactory {

	/**
	 * Create a one-dimensional array value with the given type and length.
	 * 
	 * @param memory
	 * @param type element type
	 * @param length length of this array
	 * @return
	 */
	public static ArrayValue createArrayValue(MemoryArea memory, ITypeTable tt, JType type, int length){
		return createArrayValue(memory, tt, type, new int[]{ length } );
	}
	
	/**
	 * Create an array value with the given type and lengths at each dimension.
	 * 
	 * @param memory
	 * @param tt type table
	 * @param type element type
	 * @param lengths the lengths at each dimension.
	 * @return
	 */
	public static ArrayValue createArrayValue(MemoryArea memory, ITypeTable tt, JType type, int[] lengths){
		if (type.getKind() == JTypeKind.CLASS || type.getKind() == JTypeKind.ANY || lengths.length > 1){
			return new ObjectArrayValue(memory, tt, type, lengths);
		}
		
		int length = lengths[0];
		switch(type.getKind()){
		case INTEGER:
			return new IntArrayValue(memory, tt, length);
		case BOOLEAN:
			return new BoolArrayValue(memory, tt, length);
		case BYTE:
			return new ByteArrayValue(memory, tt, length);
		case CHAR:
			return new CharArrayValue(memory, tt, length);
		case FLOAT:
			return new FloatArrayValue(memory, tt, length);
		default:
		}
		
		throw new JSEError("Cannot create a basic type array of type " + type.getName());
	}
}
