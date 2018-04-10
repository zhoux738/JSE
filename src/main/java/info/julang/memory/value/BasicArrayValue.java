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
import info.julang.typesystem.basic.BasicType;
import info.julang.typesystem.basic.BoolType;
import info.julang.typesystem.basic.ByteType;
import info.julang.typesystem.basic.CharType;
import info.julang.typesystem.basic.FloatType;
import info.julang.typesystem.basic.IntType;
import info.julang.typesystem.jclass.builtin.JArrayType;

/**
 * An array value whose element type is a basic type in Julian language.
 * 
 * @author Ming Zhou
 */
public abstract class BasicArrayValue extends ArrayValue {

	protected int length;
	protected MemoryArea memory;
	
	protected BasicArrayValue(MemoryArea memory, JType type) {
		super(memory, type, true);
	}
	
	protected BasicArrayValue(MemoryArea memory, ITypeTable tt, BasicType elementType, int length) {
		super(memory, tt, elementType, length);
		this.length = length;
		this.memory = memory;
	}

	@Override
	protected void initializeArray(
		MemoryArea memory, ITypeTable tt, JType elementType, int[] dimensions) {
		if (dimensions.length > 1){
			throw new JSEError("Cannot create a basic type array of more than one dimension.", BasicArrayValue.class);
		}
		
		initializeArray(memory, dimensions[0]);
	}
	
	@Override
	public int getLength() {
		return length;
	}
	
	@Override
	public boolean isBasicArray(){
		return true;
	}
	
	/**
	 * Get the underlying Java array that stores the data. The type of this object corresponds to
	 * the basic type of this array's element. For example, a {@link ByteArrayValue} returns an 
	 * object of type <code>byte[]</code>.
	 * 
	 * @return
	 */
	public abstract Object getPlatformArrayObject();
	
	protected abstract void initializeArray(MemoryArea memory, int length);

}

//------------------ Implementations of basic type array values ------------------//

class IntArrayValue extends BasicArrayValue {

	// The underlying storage
	private int[] array;
	
	/**
	 * Reserved only for builder.
	 * 
	 * @param memory
	 * @param atype Must be [Integer]
	 */
	IntArrayValue(MemoryArea memory, JArrayType atype) {
		super(memory, atype);
	}
	
	public IntArrayValue(MemoryArea memory, ITypeTable tt, int length) {
		super(memory, tt, IntType.getInstance(), length);
	}
	
	@Override
	public Object getPlatformArrayObject(){
		return array;
	}

	@Override
	protected void initializeArray(MemoryArea memory, int length) {
		array = new int[length];
	}

	@Override
	protected JValue getValue(int index) {
		return new ElementValue(memory, index);
	}

	private class ElementValue extends IntValue {
		
		private int index;
		
		public ElementValue(MemoryArea memory, int index) {
			super(memory);
			this.index = index;
		}
		
		protected void setValueInternal(int value){
			IntArrayValue.this.array[index] = value;
		}
		
		protected int getValueInternal(){
			return IntArrayValue.this.array[index];
		}
	}
	
	static class Builder implements ArrayValueBuilder {

		private IntArrayValue value;
		
		Builder(MemoryArea memory, ITypeTable tt){
			JArrayType jat = tt.getArrayType(IntType.getInstance());
			if (jat == null) {
				jat = JArrayType.createJArrayType(tt, IntType.getInstance(), 1);
				tt.addArrayType(jat);
			}
			
			value = new IntArrayValue(memory, jat);
			value.memory = memory;
		}
		
		@Override
		public void setLength(int length) {
			value.array = new int[length];
			value.length = length;
			
			IntValue len = (IntValue) value.getMemberValue("length");
			len.setIntValue(length);
		}

		@Override
		public void setValue(int index, JValue val) {
			value.array[index] = ((IntValue)val).getIntValue();
		}

		@Override
		public ArrayValue getResult() {
			if (value.array == null) {
				throw new JSEError("Array value not fully initialized.");
			}
			
			return value;
		}
	}
}

class ByteArrayValue extends BasicArrayValue {

	// The underlying storage
	protected byte[] array;

	/**
	 * Reserved only for builder.
	 * 
	 * @param memory
	 * @param atype Must be [Byte]
	 */
	ByteArrayValue(MemoryArea memory, JArrayType atype) {
		super(memory, atype);
	}
	
	public ByteArrayValue(MemoryArea memory, ITypeTable tt, int length) {
		super(memory, tt, ByteType.getInstance(), length);
	}

	@Override
	public Object getPlatformArrayObject(){
		return array;
	}
	
	@Override
	protected void initializeArray(MemoryArea memory, int length) {
		array = new byte[length];
	}

	@Override
	protected JValue getValue(int index) {
		return new ElementValue(memory, index);
	}

	private class ElementValue extends ByteValue {
		
		private int index;
		
		public ElementValue(MemoryArea memory, int index) {
			super(memory);
			this.index = index;
		}
		
		protected void setValueInternal(byte value){
			ByteArrayValue.this.array[index] = value;
		}
		
		protected byte getValueInternal(){
			return ByteArrayValue.this.array[index];
		}
	}
	
	static class Builder implements ArrayValueBuilder {

		private ByteArrayValue value;
		
		Builder(MemoryArea memory, ITypeTable tt){
			JArrayType jat = tt.getArrayType(ByteType.getInstance());
			if (jat == null) {
				jat = JArrayType.createJArrayType(tt, ByteType.getInstance(), 1);
				tt.addArrayType(jat);
			}
			
			value = new ByteArrayValue(memory, jat);
			value.memory = memory;
		}
		
		@Override
		public void setLength(int length) {
			value.array = new byte[length];
			value.length = length;
			
			IntValue len = (IntValue) value.getMemberValue("length");
			len.setIntValue(length);
		}

		@Override
		public void setValue(int index, JValue val) {
			value.array[index] = ((ByteValue)val).getByteValue();
		}

		@Override
		public ArrayValue getResult() {
			if (value.array == null) {
				throw new JSEError("Array value not fully initialized.");
			}
			
			return value;
		}
	}
}

class CharArrayValue extends BasicArrayValue {

	// The underlying storage
	private char[] array;
	
	/**
	 * Reserved only for builder.
	 * 
	 * @param memory
	 * @param atype Must be [Char]
	 */
	CharArrayValue(MemoryArea memory, JArrayType atype) {
		super(memory, atype);
	}

	@Override
	public Object getPlatformArrayObject(){
		return array;
	}
	
	public CharArrayValue(MemoryArea memory, ITypeTable tt, int length) {
		super(memory, tt, CharType.getInstance(), length);
	}

	@Override
	protected void initializeArray(MemoryArea memory, int length) {
		array = new char[length];
	}

	@Override
	protected JValue getValue(int index) {
		return new ElementValue(memory, index);
	}

	private class ElementValue extends CharValue {
		
		private int index;
		
		public ElementValue(MemoryArea memory, int index) {
			super(memory);
			this.index = index;
		}
		
		protected void setValueInternal(char value){
			CharArrayValue.this.array[index] = value;
		}
		
		protected char getValueInternal(){
			return CharArrayValue.this.array[index];
		}
	}
	
	static class Builder implements ArrayValueBuilder {

		private CharArrayValue value;
		
		Builder(MemoryArea memory, ITypeTable tt){
			JArrayType jat = tt.getArrayType(CharType.getInstance());
			if (jat == null) {
				jat = JArrayType.createJArrayType(tt, CharType.getInstance(), 1);
				tt.addArrayType(jat);
			}
			
			value = new CharArrayValue(memory, jat);
			value.memory = memory;
		}
		
		@Override
		public void setLength(int length) {
			value.array = new char[length];
			value.length = length;
			
			IntValue len = (IntValue) value.getMemberValue("length");
			len.setIntValue(length);
		}

		@Override
		public void setValue(int index, JValue val) {
			value.array[index] = ((CharValue)val).getCharValue();
		}

		@Override
		public ArrayValue getResult() {
			if (value.array == null) {
				throw new JSEError("Array value not fully initialized.");
			}
			
			return value;
		}
	}
}

class FloatArrayValue extends BasicArrayValue {

	// The underlying storage
	private float[] array;

	/**
	 * Reserved only for builder.
	 * 
	 * @param memory
	 * @param atype Must be [Float]
	 */
	FloatArrayValue(MemoryArea memory, JArrayType atype) {
		super(memory, atype);
	}
	
	public FloatArrayValue(MemoryArea memory, ITypeTable tt, int length) {
		super(memory, tt, FloatType.getInstance(), length);
	}
	
	@Override
	public Object getPlatformArrayObject(){
		return array;
	}

	@Override
	protected void initializeArray(MemoryArea memory, int length) {
		array = new float[length];
	}

	@Override
	protected JValue getValue(int index) {
		return new ElementValue(memory, index);
	}

	private class ElementValue extends FloatValue {
		
		private int index;
		
		public ElementValue(MemoryArea memory, int index) {
			super(memory);
			this.index = index;
		}
		
		protected void setValueInternal(float value){
			FloatArrayValue.this.array[index] = value;
		}
		
		protected float getValueInternal(){
			return FloatArrayValue.this.array[index];
		}
	}
	
	static class Builder implements ArrayValueBuilder {

		private FloatArrayValue value;
		
		Builder(MemoryArea memory, ITypeTable tt){
			JArrayType jat = tt.getArrayType(FloatType.getInstance());
			if (jat == null) {
				jat = JArrayType.createJArrayType(tt, FloatType.getInstance(), 1);
				tt.addArrayType(jat);
			}
			
			value = new FloatArrayValue(memory, jat);
			value.memory = memory;
		}
		
		@Override
		public void setLength(int length) {
			value.array = new float[length];
			value.length = length;
			
			IntValue len = (IntValue) value.getMemberValue("length");
			len.setIntValue(length);
		}

		@Override
		public void setValue(int index, JValue val) {
			value.array[index] = ((FloatValue)val).getFloatValue();
		}

		@Override
		public ArrayValue getResult() {
			if (value.array == null) {
				throw new JSEError("Array value not fully initialized.");
			}
			
			return value;
		}
	}
}

class BoolArrayValue extends BasicArrayValue {

	// The underlying storage
	private boolean[] array;
	
	/**
	 * Reserved only for builder.
	 * 
	 * @param memory
	 * @param atype Must be [Bool]
	 */
	BoolArrayValue(MemoryArea memory, JArrayType atype) {
		super(memory, atype);
	}
	
	public BoolArrayValue(MemoryArea memory, ITypeTable tt, int length) {
		super(memory, tt, BoolType.getInstance(), length);
	}
	
	@Override
	public Object getPlatformArrayObject(){
		return array;
	}

	@Override
	protected void initializeArray(MemoryArea memory, int length) {
		array = new boolean[length];
	}

	@Override
	protected JValue getValue(int index) {
		return new ElementValue(memory, index);
	}

	private class ElementValue extends BoolValue {
		
		private int index;
		
		public ElementValue(MemoryArea memory, int index) {
			super(memory);
			this.index = index;
		}
		
		protected void setValueInternal(boolean value){
			BoolArrayValue.this.array[index] = value;
		}
		
		protected boolean getValueInternal(){
			return BoolArrayValue.this.array[index];
		}
	}
	
	static class Builder implements ArrayValueBuilder {

		private BoolArrayValue value;
		
		Builder(MemoryArea memory, ITypeTable tt){
			JArrayType jat = tt.getArrayType(BoolType.getInstance());
			if (jat == null) {
				jat = JArrayType.createJArrayType(tt, BoolType.getInstance(), 1);
				tt.addArrayType(jat);
			}
			
			value = new BoolArrayValue(memory, jat);
			value.memory = memory;
		}
		
		@Override
		public void setLength(int length) {
			value.array = new boolean[length];
			value.length = length;
			
			IntValue len = (IntValue) value.getMemberValue("length");
			len.setIntValue(length);
		}

		@Override
		public void setValue(int index, JValue val) {
			value.array[index] = ((BoolValue)val).getBoolValue();
		}

		@Override
		public ArrayValue getResult() {
			if (value.array == null) {
				throw new JSEError("Array value not fully initialized.");
			}
			
			return value;
		}
	}
}
