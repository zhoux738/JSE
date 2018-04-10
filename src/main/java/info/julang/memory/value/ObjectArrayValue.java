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
import info.julang.external.interfaces.JValueKind;
import info.julang.memory.MemoryArea;
import info.julang.typesystem.JType;
import info.julang.typesystem.JTypeKind;
import info.julang.typesystem.jclass.ICompoundType;
import info.julang.typesystem.jclass.builtin.JArrayType;

public class ObjectArrayValue extends ArrayValue {

	private JValue[] values;
	
	ObjectArrayValue(MemoryArea memory, ITypeTable tt, JType elementType, int[] lengths) {
		super(memory, tt, elementType, lengths);
	}
	
	ObjectArrayValue(MemoryArea memory, JType type) {
		super(memory, type, true);
	}
	
	private ObjectArrayValue(MemoryArea memory, ITypeTable tt, JType elementType, JValue[] values){
		super(memory, JArrayType.createJArrayType(tt, elementType, false), false); // the super constructor calls initialize()
		
		IntValue len = (IntValue) getMemberValue("length");
		len.setIntValue(values.length);
		
		this.values = values;
		
		if(memory!=null){
			memory.reallocate(this);		
		}
	}
	
	@Override
	public boolean isBasicArray(){
		return false;
	}
	
	@Override
	protected void initializeArray(MemoryArea memory, ITypeTable tt, JType elementType, int[] dimensions){
		values = initializeArray(memory, tt, elementType, dimensions, 0);
	}
	
	private JValue[] initializeArray(MemoryArea memory, ITypeTable tt, JType elementType, int[] dimensions, int dIndex){
		// length for this dimension
		int length = dimensions[dIndex];
		// Index of last dimension
		int lastDem = dimensions.length - 1;
		// Initialize values at current dimension
		JValue[] arrVals = new JValue[length];
		
		if(dIndex < lastDem){
			// The element is array too. Initialize recursively.
			int nextIndex = dIndex + 1;
			if (nextIndex == lastDem && elementType.isBasic()){
				// If we are about to create the array at the last dimension, and the element type is basic,
				// we must use appropriate basic value array.
				for(int i=0; i<length; i++){
					ArrayValue av = ArrayValueFactory.createArrayValue(memory, tt, elementType, dimensions[lastDem]);
					arrVals[i] = new RefValue(memory, av);
				}
			} else {
				for(int i=0; i<length; i++){
					JValue[] subArrVals = initializeArray(memory, tt, elementType, dimensions, nextIndex);
					arrVals[i] = new RefValue(memory, new ObjectArrayValue(memory, tt, elementType, subArrVals));
				}
			}
		} else {
			// We are at the last dimension.
			if (elementType.getKind() != JTypeKind.CLASS){
				throw new JSEError("Trying to create array of unsupported element type.");
			}
			
			for(int i=0; i<length; i++){
				arrVals[i] = RefValue.makeNullRefValue(memory, (ICompoundType) elementType);
			}
		}
		
		return arrVals;
	}

	@Override
	protected JValue getValue(int index) {
		if(index < 0 || index >= values.length){
			throw new ArrayIndexOutOfRangeException(index, values.length - 1);
		}
		
		return values[index];
	}

	@Override
	public int getLength() {
		return values.length;
	}
	
	public static class Builder implements ArrayValueBuilder {
		
		private ObjectArrayValue value;
		private MemoryArea memory;
		private int length = -1;
		
		/**
		 * @param memory memory to store the built value
		 * @param etype type of element
		 * @param tt type table
		 */
		public Builder(MemoryArea memory, JType etype, ITypeTable tt){
			this.memory = memory;
			etype = JArrayType.createJArrayType(tt, etype, false);
			value = new ObjectArrayValue(memory, etype);
		}

		@Override
		public void setLength(int length){
			IntValue len = (IntValue) value.getMemberValue("length");
			len.setIntValue(length);
			
			value.values = new JValue[length];
			this.length = length;
		}
		
		@Override
		public void setValue(int index, JValue val){
			if (val.getKind() == JValueKind.OBJECT) {
				val = new RefValue(memory, (ObjectValue)val);
			}
			
			value.values[index] = val;
		}
		
		@Override
		public ObjectArrayValue getResult(){
			if (length == -1) {
				throw new JSEError("Array value not fully initialized.");
			}
			
			for(JValue v : value.values){
				if (v == null){
					throw new JSEError("Array value not fully initialized.");
				}
			}
			
			if (memory != null) {
				memory.reallocate(value);
			}
			
			return value;
		}
	}

}
