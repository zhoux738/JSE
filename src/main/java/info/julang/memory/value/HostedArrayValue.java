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
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import info.julang.execution.symboltable.ITypeTable;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.external.exceptions.JSEError;
import info.julang.external.interfaces.IExtValue.IHostedVal;
import info.julang.external.interfaces.JValueKind;
import info.julang.hosting.mapped.exec.PlatformOriginalException;
import info.julang.interpretation.JIllegalCastingException;
import info.julang.memory.MemoryArea;
import info.julang.memory.value.indexable.IIndexable;
import info.julang.memory.value.iterable.IIterator;
import info.julang.memory.value.operable.JCastable;
import info.julang.typesystem.JArgumentException;
import info.julang.typesystem.JType;
import info.julang.typesystem.basic.BoolType;
import info.julang.typesystem.basic.ByteType;
import info.julang.typesystem.basic.CharType;
import info.julang.typesystem.basic.FloatType;
import info.julang.typesystem.basic.IntType;
import info.julang.typesystem.jclass.builtin.JArrayType;

/**
 * A value holding a reference to an array of hosted (JVM) object.
 * 
 * @author Ming Zhou
 */
public class HostedArrayValue extends HostedValue implements JCastable, IHostedVal, IArrayValue, IPlatformArrayValue {
	
	private Class<?> arrayClass;
	
	private ITypeTable tt;
	
	private Class<?> elementClass;
	
	private boolean isPrimitive;
	
	public HostedArrayValue(MemoryArea mem, ITypeTable tt, JArrayType type) {
		super(mem, type);
		this.tt = tt;
	}
	
	//---------------------- HostedValue ----------------------//
	
	// It's expected that this method is called, without any exception, immediately after the creation.
	@Override
	public void setHostedObject(Object obj){
		Class<?> clazz = obj.getClass();
		if (!clazz.isArray()) {
			throw new JSEError("Cannot set a hosted object.");
		}
		
		super.setHostedObject(obj);
		
		arrayClass = clazz;
		IntValue len = (IntValue) getMemberValue("length");
		len.setIntValue(Array.getLength(obj));
		
		elementClass = arrayClass.getComponentType();
		
		isPrimitive = elementClass == int.class
					|| elementClass == byte.class
					|| elementClass == char.class
					|| elementClass == float.class
					|| elementClass == boolean.class;
	}
	
	//---------------------- JValue ----------------------//
	
	@Override
	public IIndexable asIndexer(){
		return new HostedIndexable(arrayClass, (JArrayType)type, obj);
	}

	@Override
	public IIterator asIterator(){
		return new HostedIterator(arrayClass, (JArrayType)type, obj);
	}
	
	//---------------------- IArrayValue ----------------------//

	@Override
	public int getLength() {
		return ((IntValue) getMemberValue("length")).getIntValue();
	}

	@Override
	public JValue getValueAt(int index) {
		return HostedIndexedBase.getElementValue(
			index, this.getMemoryArea(), tt, arrayClass, this.getHostedObject(), (JArrayType)getType());
	}

	@Override
	public boolean isBasicArray() {
		return isPrimitive;
	}

	@Override
	public void sort(ThreadRuntime rt, boolean desc) {
		Object arr = this.getHostedObject();
		if (arr instanceof int[]) {
			int[] iarr = (int[])arr;
			Arrays.sort(iarr);
			if (desc) { // Reverse
				int s = 0;
				int e = iarr.length - 1;
				while(s < e) {
					int tmp = iarr[s];
					iarr[s] = iarr[e];
					iarr[e] = tmp;
					s++;
					e--;
				}
			}
			return;
		} else if (arr instanceof byte[]) {
			byte[] barr = (byte[])arr;
			Arrays.sort(barr);
			if (desc) { // Reverse
				int s = 0;
				int e = barr.length - 1;
				while(s < e) {
					byte tmp = barr[s];
					barr[s] = barr[e];
					barr[e] = tmp;
					s++;
					e--;
				}
			}
			return;
		} else if (arr instanceof char[]) {
			char[] carr = (char[])arr;
			Arrays.sort(carr);
			if (desc) { // Reverse
				int s = 0;
				int e = carr.length - 1;
				while(s < e) {
					char tmp = carr[s];
					carr[s] = carr[e];
					carr[e] = tmp;
					s++;
					e--;
				}
			}
			return;
		} else if (arr instanceof boolean[]) {
			boolean[] zarr = (boolean[])arr;
			int tcount = 0;
			int length = zarr.length;
			for (int i = 0; i < length; i++) {
				if (zarr[i]) {
					tcount++;
				}
			}
			if (desc) {
				for (int i = 0; i < tcount; i++) {
					zarr[i] = true;
				}
				for (int i = tcount; i < length; i++) {
					zarr[i] = false;
				}
			} else {
				tcount = length - tcount;
				for (int i = 0; i < tcount; i++) {
					zarr[i] = false;
				}
				for (int i = tcount; i < length; i++) {
					zarr[i] = true;
				}
			}
			return;
		} else if (arr instanceof float[]) {
			float[] farr = (float[])arr;
			Arrays.sort(farr);
			if (desc) { // Reverse
				int s = 0;
				int e = farr.length - 1;
				while(s < e) {
					float tmp = farr[s];
					farr[s] = farr[e];
					farr[e] = tmp;
					s++;
					e--;
				}
			}
			return;
		} else if (arr instanceof Object[]) {
			if (Comparator.class.isAssignableFrom(arr.getClass().getComponentType())) {
				try {
					if (desc) {
						Arrays.sort((Object[])arr, Collections.reverseOrder());
					} else {
						Arrays.sort((Object[])arr);
					}
				} catch (Throwable e) {
					throw new PlatformOriginalException(e, "", 0);
				}
				return;
			}
		}
		
		throw new JArgumentException("src");
	}
	
	@Override
	public boolean assignTo(JValue assignee) {
		if(assignee.getKind() == JValueKind.REFERENCE){
			JType typ = ((RefValue)assignee).getType();
			ObjectValue tmp = convertTo(assignee.getMemoryArea(), typ, false);
			if (tmp != null) {
				// Assign to array of corresponding Julian type
				RefValue refThis = TempValueFactory.createTempRefValue(tmp);
				return refThis.assignTo(assignee);
			}
		}

		// Assign to array of same platform type
		return super.assignTo(assignee);
	}

	@Override
	public JValue to(MemoryArea memory, JType type) {
		return convertTo(memory, type, true);
	}
	
	private ObjectValue convertTo(MemoryArea memory, JType type, boolean shouldThrow) {
		if (this.type == type) {
			return this;
		}
		
		if (!JArrayType.isArrayType(type)) {
			if (shouldThrow) {
				throw new JIllegalCastingException(this.type, type);
			} else {
				return null;
			}
		}
		
		BasicArrayValue bav = toBasicArrayValue(memory, tt, this, (JArrayType)type);
		if (bav != null) {
			return bav;
		}
		
		if (shouldThrow) {
			throw new JIllegalCastingException(this.type, type);
		} else {
			return null;
		}
	}
	
	/**
	 * Try to convert an implicit platform primitive 1-D array to a Julian basic 1-D array.
	 * 
	 * @param memory The memory area to store the new value.
	 * @param tt Type table, used to create array type.
	 * @param platform1DArrayValue The platform 1D array, wrapped in a HostedArrayValue. Example: int[]
	 * @param jul1DArrayType The JSE 1D array. Example: Integer[]
	 * @return null if the conversion cannot happen for whatever reason: 
	 * the platform array is not primitive; 
	 * the target type is not basic 1-D array; 
	 * the primitive type doesn't have a counterpart in JSE; 
	 * etc.
	 */
	public static BasicArrayValue toBasicArrayValue(
		MemoryArea memory, ITypeTable tt, HostedArrayValue platform1DArrayValue, JArrayType jul1DArrayType) {
		JType etype = jul1DArrayType.getElementType();
		
		Class<?> eleClass = platform1DArrayValue.elementClass;
		
		// Java primitive type => Julian basic type wrapper
		if (eleClass == int.class && etype == IntType.getInstance()) {
			return PresetBasicArrayValueFactory.fromIntArray(memory, tt, (int[])platform1DArrayValue.obj);
		} else if (eleClass == byte.class && etype == ByteType.getInstance()) {
			return PresetBasicArrayValueFactory.fromByteArray(memory, tt, (byte[])platform1DArrayValue.obj);
		} else if (eleClass == char.class && etype == CharType.getInstance()) {
			return PresetBasicArrayValueFactory.fromCharArray(memory, tt, (char[])platform1DArrayValue.obj);
		} else if (eleClass == float.class && etype == FloatType.getInstance()) {
			return PresetBasicArrayValueFactory.fromFloatArray(memory, tt, (float[])platform1DArrayValue.obj);
		} else if (eleClass == boolean.class && etype == BoolType.getInstance()) {
			return PresetBasicArrayValueFactory.fromBoolArray(memory, tt, (boolean[])platform1DArrayValue.obj);
		}
		
		return null;
	}

	@Override
	public Object getPlatformArrayObject() {
		return this.obj;
	}
}
