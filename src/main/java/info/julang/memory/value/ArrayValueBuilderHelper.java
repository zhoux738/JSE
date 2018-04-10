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

/**
 * A helper class used to get array value builder for a given element type.
 * 
 * @author Ming Zhou
 */
public final class ArrayValueBuilderHelper {

	/**
	 * Get array value builder for a given element type.
	 * 
	 * @param type the element type
	 * @param memory
	 * @param tt
	 * @return
	 */
	public static ArrayValueBuilder getBuilder(JType type, MemoryArea memory, ITypeTable tt){
		ArrayValueBuilder b = null;
		
		switch(type.getKind()){
		case BOOLEAN:
			b = new BoolArrayValue.Builder(memory, tt);
			break;
		case BYTE:
			b = new ByteArrayValue.Builder(memory, tt);
			break;
		case CHAR:
			b = new CharArrayValue.Builder(memory, tt);
			break;
		case FLOAT:
			b = new FloatArrayValue.Builder(memory, tt);
			break;
		case INTEGER:
			b = new IntArrayValue.Builder(memory, tt);
			break;
		case CLASS:
			b = new ObjectArrayValue.Builder(memory, type, tt);
			break;
		default:
			break;
		}
		
		if (b == null) {
			throw new JSEError("No array builder is found for " + type.getName());
		}
		
		return b;
	}
	
}
