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
import info.julang.execution.threading.ThreadRuntime;
import info.julang.external.exceptions.JSEError;
import info.julang.external.interfaces.JValueKind;
import info.julang.memory.MemoryArea;
import info.julang.typesystem.JType;
import info.julang.typesystem.basic.BasicType;
import info.julang.typesystem.basic.BoolType;
import info.julang.typesystem.basic.ByteType;
import info.julang.typesystem.basic.CharType;
import info.julang.typesystem.basic.FloatType;
import info.julang.typesystem.basic.IntType;
import info.julang.typesystem.jclass.builtin.JArrayType;

import java.util.Arrays;
import java.util.Comparator;

/**
 * An array value whose element type is a basic type in Julian language.
 * 
 * @author Ming Zhou
 */
public abstract class BasicArrayValue extends ArrayValue implements IPlatformArrayValue {

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
	
	protected abstract void initializeArray(MemoryArea memory, int length);
	
	public abstract void fill(JValue jv);

}

//------------------ Implementations of basic type array values ------------------//

class IntArrayValue extends BasicArrayValue {

	// The underlying storage
	protected int[] array;
	
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
	protected JValue getInternal(int index) {
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

	@Override
	public void fill(JValue jv) {
		if (jv.getKind() == JValueKind.INTEGER) {
			IntValue iv = (IntValue)jv;
			Arrays.fill(this.array, iv.getIntValue());
		} else if (jv.getKind() == JValueKind.BYTE) {
			ByteValue bv = (ByteValue)jv;
			Arrays.fill(this.array, bv.getByteValue());
		} else {
			throw new IllegalAssignmentException(jv.getType(), IntType.getInstance());
		}
	}
	
	@Override
	public void sort(ThreadRuntime rt, boolean desc) {
		Arrays.sort(array);
		if (desc) {
			int len = array.length;
			int median = len / 2;
			for (int i=0; i<median; i++) {
			    int temp = array[i];
			    array[i] = array[len - i - 1];
			    array[len - i - 1] = temp;
			}
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
	protected JValue getInternal(int index) {
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

	@Override
	public void fill(JValue jv) {
		if (jv.getKind() == JValueKind.BYTE) {
			ByteValue bv = (ByteValue)jv;
			Arrays.fill(this.array, bv.getByteValue());
		} else {
			throw new IllegalAssignmentException(jv.getType(), ByteType.getInstance());
		}
	}
	
	@Override
	public void sort(ThreadRuntime rt, boolean desc) {
		Arrays.sort(array);
		if (desc) {
			int len = array.length;
			int median = len / 2;
			for (int i=0; i<median; i++) {
			    byte temp = array[i];
			    array[i] = array[len - i - 1];
			    array[len - i - 1] = temp;
			}
		}
	}
}

class CharArrayValue extends BasicArrayValue {

	// The underlying storage
	protected char[] array;
	
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
	protected JValue getInternal(int index) {
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

	@Override
	public void fill(JValue jv) {
		if (jv.getKind() == JValueKind.CHAR) {
			CharValue bv = (CharValue)jv;
			Arrays.fill(this.array, bv.getCharValue());
		} else {
			throw new IllegalAssignmentException(jv.getType(), CharType.getInstance());
		}
	}

	@Override
	public void sort(ThreadRuntime rt, boolean desc) {
		Arrays.sort(array);
		if (desc) {
			int len = array.length;
			int median = len / 2;
			for (int i=0; i<median; i++) {
			    char temp = array[i];
			    array[i] = array[len - i - 1];
			    array[len - i - 1] = temp;
			}
		}
	}
}

class FloatArrayValue extends BasicArrayValue {

	// The underlying storage
	protected float[] array;

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
	protected JValue getInternal(int index) {
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

	@Override
	public void fill(JValue jv) {
		if (jv.getKind() == JValueKind.FLOAT) {
			FloatValue bv = (FloatValue)jv;
			Arrays.fill(this.array, bv.getFloatValue());
		} else {
			throw new IllegalAssignmentException(jv.getType(), FloatType.getInstance());
		}
	}
	
	@Override
	public void sort(ThreadRuntime rt, boolean desc) {
		Arrays.sort(array);
		if (desc) {
			int len = array.length;
			int median = len / 2;
			for (int i=0; i<median; i++) {
			    float temp = array[i];
			    array[i] = array[len - i - 1];
			    array[len - i - 1] = temp;
			}
		}
	}
}

class BoolArrayValue extends BasicArrayValue {

	// The underlying storage
	protected boolean[] array;
	
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
	protected JValue getInternal(int index) {
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

	@Override
	public void fill(JValue jv) {
		if (jv.getKind() == JValueKind.BOOLEAN) {
			BoolValue bv = (BoolValue)jv;
			Arrays.fill(this.array, bv.getBoolValue());
		} else {
			throw new IllegalAssignmentException(jv.getType(), BoolType.getInstance());
		}
	}
	
	@Override
	public void sort(ThreadRuntime rt, final boolean desc) {
		// Must do this manually as Java doesn't have native support for sorting boolean array.
		int len = array.length;
		Boolean[] boxed = new Boolean[len];
		for(int i = 0; i < len; i++){
			boxed[i] = array[i];
		}
		
		Arrays.sort(boxed, new Comparator<Boolean>(){
			@Override
			public int compare(Boolean o1, Boolean o2) {
				boolean b1 = o1.booleanValue();
				boolean b2 = o2.booleanValue();
				if (b1 == b2) {
					return 0;
				}
				
				if (b1 && !b2) {
					return desc ? -1 : 1;
				}
				
				return desc ? 1 : -1;
			}
		});
		
		for(int i = 0; i < len; i++){
			array[i] = boxed[i];
		}
	}
}
