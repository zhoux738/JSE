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
import info.julang.external.interfaces.JValueKind;
import info.julang.memory.MemoryArea;
import info.julang.typesystem.JType;
import info.julang.typesystem.jclass.builtin.JArrayType;

/**
 * An abstract layer of array value to isolate storage of its elements.
 * 
 * @author Ming Zhou
 */
public abstract class ArrayValue extends ObjectValue {
	
	private JArrayType type;
	
	/**
	 * Create a new multi-dimensional array value.
	 * 
	 * @param memory
	 * @param value
	 * @param lengths the length for each dimension
	 */
	public ArrayValue(MemoryArea memory, ITypeTable tt, JType elementType, int[] lengths) {
		super(memory, JArrayType.createJArrayType(tt, elementType, lengths.length), false); // the super constructor calls initialize()
		
		IntValue len = (IntValue) getMemberValue("length");
		len.setIntValue(lengths[0]);
		
		initializeArray(memory, tt, elementType, lengths);
		
		if(memory!=null){
			memory.reallocate(this);		
		}
	}
	
	protected ArrayValue(MemoryArea memory, JType type, boolean delayAllocation) {
		super(memory, type, delayAllocation);
	}
	
	/**
	 * Create a new single-dimensional dimensional array value.
	 * 
	 * @param memory
	 * @param value
	 */
	public ArrayValue(MemoryArea memory, ITypeTable tt, JType elementType, int length) {
		this(memory, tt, elementType, new int[]{ length });
	}
	
	@Override
	protected void initialize(JType type, MemoryArea memory) {
		super.initialize(type, memory);
		this.type = (JArrayType) type;
	}

	@Override
	public JValueKind getKind() {
		return JValueKind.OBJECT;
	}

	@Override
	public JValueKind getBuiltInValueKind(){
		return JValueKind.ARRAY;
	}
	
	@Override
	public JType getType() {
		return type;
	}
	
	public JValue getValueAt(int index) throws ArrayIndexOutOfRangeException {
		if(index < 0 || index >= getLength()){
			throw new ArrayIndexOutOfRangeException(index, getLength() - 1);
		}
		return getValue(index);
	}
	
	/**
	 * Initialize this array. The storage of the initialized array members is up to the implementation.
	 * 
	 * @param memory
	 * @param elementType
	 * @param dIndex
	 * @return the initialized values of this array at the given dimension.
	 */
	protected abstract void initializeArray(MemoryArea memory, ITypeTable tt, JType elementType, int[] dimensions);
	
	/**
	 * Get value at the given index. This returns a reference to the element in array. Modification of its value
	 * will affect the contents of array.
	 * 
	 * @param index
	 * @return
	 */
	protected abstract JValue getValue(int index);
	
	/**
	 * Length of this array. This is equivalent to calling "array.length" in script.
	 * 
	 * @return
	 */
	public abstract int getLength();
	
	/**
	 * @return true if this is basic array, convertible to {@link BasicArrayValue}.
	 */
	public abstract boolean isBasicArray();
}
