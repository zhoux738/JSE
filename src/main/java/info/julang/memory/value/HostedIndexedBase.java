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

import java.lang.reflect.Array;

import info.julang.execution.symboltable.ITypeTable;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.interpretation.context.Context;
import info.julang.memory.MemoryArea;
import info.julang.memory.value.indexable.UnsupportedIndexTypeException;
import info.julang.memory.value.operable.InitArgs;
import info.julang.memory.value.operable.JulianObjectAdaptor;
import info.julang.typesystem.JType;
import info.julang.typesystem.PlatformType;
import info.julang.typesystem.jclass.ICompoundType;
import info.julang.typesystem.jclass.builtin.JArrayType;
import info.julang.typesystem.jclass.builtin.JStringType;

/**
 * A base class providing shared logic for indexed access to hosted array.
 * 
 * @author Ming Zhou
 */
class HostedIndexedBase implements JulianObjectAdaptor {

	protected Object arrayValue;
	
	protected Class<?> arrayClass;
	
	protected Context context;
	
	protected JArrayType arrayType;
	
	protected HostedIndexedBase(Class<?> arrayClass, JArrayType type, Object value) {
		this.arrayClass = arrayClass;
		this.arrayValue = value;
		this.arrayType = type;
	}
	
	public int getLength() {
		return Array.getLength(arrayValue);
	}
	
	protected int getIndex(JValue index) throws UnsupportedIndexTypeException {
		JValue indexDeref = index.deref();
		int ind = 0;
		switch(indexDeref.getKind()) {
		case INTEGER:
			IntValue iv = (IntValue)indexDeref;
			ind = iv.getIntValue();
			break;
		case BYTE:
			ByteValue bv = (ByteValue)indexDeref;
			ind = bv.getByteValue();
			break;
		default:
			throw new UnsupportedIndexTypeException("Cannot use a non-integer index to access to platform's array.");
		}
		
		int maxInd = getLength() - 1;
		if (ind < 0 || ind > maxInd) {
			throw new ArrayIndexOutOfRangeException(ind, getLength());
		}
		
		return ind;
	}

	@Override
	public void initialize(ThreadRuntime rt, InitArgs args) {
		this.context = args.getContext();
	}

	protected void setElementValue(int index, JValue value) {
		MemoryArea mem = context.getHeap();
		
		Class<?> eleClass = arrayClass.getComponentType();
		
		JValue src = value.deref();
		
		// Java primitive type => Julian basic type wrapper with mutable interface
		if (eleClass == int.class) {
			src.assignTo(new PlatformIntValue(mem, index, arrayValue));
			return;
		} else if (eleClass == float.class) {
			src.assignTo(new PlatformFloatValue(mem, index, arrayValue));
			return;
		} else if (eleClass == byte.class) {
			src.assignTo(new PlatformByteValue(mem, index, arrayValue));
			return;
		} else if (eleClass == char.class) {
			src.assignTo(new PlatformCharValue(mem, index, arrayValue));
			return;
		} else if (eleClass == boolean.class) {
			src.assignTo(new PlatformBoolValue(mem, index, arrayValue));
			return;
		} else if (eleClass == short.class) {
			throw makeUnsupportedTypeEx(eleClass); // Not supported.
		} else if (eleClass == double.class) {
			throw makeUnsupportedTypeEx(eleClass); // Not supported.
		} else if (eleClass == long.class) { 
			throw makeUnsupportedTypeEx(eleClass); // Not supported.
		}

		// Java String => set directly
		if (eleClass == String.class && JStringType.isStringType(src.getType()) ) {
			Array.set(this.arrayValue, index, ((StringValue)src).getStringValue());
			return;
		}
		
		// Others => Hosted value of the inner type
		JType dstType = arrayType.getElementType();
		JType srcType = src.getType();
		if (src instanceof PlatformType && srcType.getConvertibilityTo(dstType).isSafe()) {
			// Maybe relax the above condition or base the comparison on platform types?
			
			Array.set(
				this.arrayValue,
				index,
				src == RefValue.NULL ? null : ((HostedValue)src).getHostedObject());
			
			return;
		}

		throw new IllegalAssignmentException(
			"Cannot assign a value to the platform value due to the incompatibility: " + 
			"assigner type: " + srcType.getName() + 
			"; assignee type: " + dstType.getName() + 
			"; assignee's platform class: " + eleClass.toString());
	}
	
	static JValue getElementValue(
		int index, MemoryArea mem, ITypeTable tt, Class<?> arrayClass, Object arrayValue, JArrayType arrayType) {
		
		Class<?> eleClass = arrayClass.getComponentType();
		
		// Java primitive type => Julian basic type wrapper
		if (eleClass == int.class) {
			return new PlatformIntValue(mem, index, arrayValue);
		} else if (eleClass == float.class) {
			return new PlatformFloatValue(mem, index, arrayValue);
		}  else if (eleClass == byte.class) {
			return new PlatformByteValue(mem, index, arrayValue);
		} else if (eleClass == char.class) {
			return new PlatformCharValue(mem, index, arrayValue);
		} else if (eleClass == boolean.class) {
			return new PlatformBoolValue(mem, index, arrayValue);
		} else if (eleClass == short.class) {
			throw makeUnsupportedTypeEx(eleClass); // Not supported.
		} else if (eleClass == double.class) {
			throw makeUnsupportedTypeEx(eleClass); // Not supported.
		} else if (eleClass == long.class) { 
			throw makeUnsupportedTypeEx(eleClass); // Not supported.
		}

		// Java String => Julian String replica
		if (eleClass == String.class) {
			return createStringValue(mem, index, arrayValue);
		}
		
		// Others => Hosted value of the inner type
		JType eleType = arrayType.getElementType();
		Object eleObj = Array.get(arrayValue, index);
		if (eleObj == null) {
			return RefValue.makeNullRefValue(
				mem, eleType instanceof ICompoundType ? (ICompoundType)eleType : null);
		} else {
			HostedValue hv;
			if (JArrayType.isArrayType(eleType)) {
				hv = new HostedArrayValue(mem, tt, (JArrayType)eleType);
			} else {
				hv = new HostedValue(mem, eleType);
			}
			hv.setHostedObject(eleObj);
			RefValue rv = new RefValue(mem, hv);
			return rv;
		}	
	}
	
	protected JValue getElementValue(int index) {
		MemoryArea mem = context.getHeap();
		return getElementValue(index, mem, context.getTypTable(), arrayClass, arrayValue, arrayType);
	}
	
	private static IllegalAssignmentException makeUnsupportedTypeEx(Class<?> tgtClass) {
		return new IllegalAssignmentException(
			"Cannot assign a value to the platform value since the assignee's platform class (" + 
			tgtClass.toString() + 
			") is not supported.");
	}

	static JValue createStringValue(MemoryArea memory, int index, Object arrayValue) {
		String value = (String)Array.get(arrayValue, index);
		if (value == null) {
			return RefValue.makeNullRefValue(memory, JStringType.getInstance());
		}

		String raw = (String)Array.get(arrayValue, index);
		return new StringValue(memory, raw);
	}
	
	static class PlatformIntValue extends IntValue {
		
		private int index;
		private Object arrayValue;
		
		public PlatformIntValue(MemoryArea memory, int index, Object arrayValue) {
			super(memory);
			this.index = index;
			this.arrayValue = arrayValue;
		}
		
		protected void setValueInternal(int value){
			Array.setInt(this.arrayValue, index, value);
		}
		
		protected int getValueInternal(){
			return Array.getInt(this.arrayValue, index);
		}
	}
	
	static class PlatformFloatValue extends FloatValue {
		
		private int index;
		private Object arrayValue;
		
		public PlatformFloatValue(MemoryArea memory, int index, Object arrayValue) {
			super(memory);
			this.index = index;
			this.arrayValue = arrayValue;
		}
		
		protected void setValueInternal(int value){
			Array.setFloat(this.arrayValue, index, value);
		}
		
		protected float getValueInternal(){
			return Array.getFloat(this.arrayValue, index);
		}
	}
	
	static class PlatformByteValue extends ByteValue {
		
		private int index;
		private Object arrayValue;
		
		public PlatformByteValue(MemoryArea memory, int index, Object arrayValue) {
			super(memory);
			this.index = index;
			this.arrayValue = arrayValue;
		}
		
		protected void setValueInternal(byte value){
			Array.setByte(this.arrayValue, index, value);
		}
		
		protected byte getValueInternal(){
			return Array.getByte(this.arrayValue, index);
		}
	}
	
	static class PlatformCharValue extends CharValue {
		
		private int index;
		private Object arrayValue;
		
		public PlatformCharValue(MemoryArea memory, int index, Object arrayValue) {
			super(memory);
			this.index = index;
			this.arrayValue = arrayValue;
		}
		
		protected void setValueInternal(char value){
			Array.setChar(this.arrayValue, index, value);
		}
		
		protected char getValueInternal(){
			return Array.getChar(this.arrayValue, index);
		}
	}
	
	static class PlatformBoolValue extends BoolValue {
		
		private int index;
		private Object arrayValue;
		
		public PlatformBoolValue(MemoryArea memory, int index, Object arrayValue) {
			super(memory);
			this.index = index;
			this.arrayValue = arrayValue;
		}
		
		protected void setValueInternal(boolean value){
			Array.setBoolean(this.arrayValue, index, value);
		}
		
		protected boolean getValueInternal(){
			return Array.getBoolean(this.arrayValue, index);
		}
	}
}
