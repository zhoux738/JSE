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
import info.julang.memory.MemoryArea;

/**
 * A static factory that creates various primitive array backed by an already allocated pltaform array.
 * 
 * @author Ming Zhou
 */
public class PresetBasicArrayValueFactory {

	public static ByteArrayValue fromByteArray(MemoryArea memory, ITypeTable tt, byte[] barray) {
		return new PresetByteArrayValue(memory, tt, barray);
	}
	
	public static IntArrayValue fromIntArray(MemoryArea memory, ITypeTable tt, int[] barray) {
		return new PresetIntArrayValue(memory, tt, barray);
	}
	
	public static CharArrayValue fromCharArray(MemoryArea memory, ITypeTable tt, char[] barray) {
		return new PresetCharArrayValue(memory, tt, barray);
	}
	
	public static BoolArrayValue fromBoolArray(MemoryArea memory, ITypeTable tt, boolean[] barray) {
		return new PresetBoolArrayValue(memory, tt, barray);
	}
	
	public static FloatArrayValue fromFloatArray(MemoryArea memory, ITypeTable tt, float[] barray) {
		return new PresetFloatArrayValue(memory, tt, barray);
	}
	
	private static class PresetIntArrayValue extends IntArrayValue {

		public PresetIntArrayValue(MemoryArea memory, ITypeTable tt, int[] barray) {
			super(memory, tt, barray.length);
			this.array = barray;
		}

		@Override
		protected void initializeArray(MemoryArea memory, int length) {
			// Do not create a new one
		}
	}
	
	private static class PresetByteArrayValue extends ByteArrayValue {

		public PresetByteArrayValue(MemoryArea memory, ITypeTable tt, byte[] barray) {
			super(memory, tt, barray.length);
			this.array = barray;
		}

		@Override
		protected void initializeArray(MemoryArea memory, int length) {
			// Do not create a new one
		}
	}
	
	private static class PresetCharArrayValue extends CharArrayValue {

		public PresetCharArrayValue(MemoryArea memory, ITypeTable tt, char[] barray) {
			super(memory, tt, barray.length);
			this.array = barray;
		}

		@Override
		protected void initializeArray(MemoryArea memory, int length) {
			// Do not create a new one
		}
	}
	
	private static class PresetBoolArrayValue extends BoolArrayValue {

		public PresetBoolArrayValue(MemoryArea memory, ITypeTable tt, boolean[] barray) {
			super(memory, tt, barray.length);
			this.array = barray;
		}

		@Override
		protected void initializeArray(MemoryArea memory, int length) {
			// Do not create a new one
		}
	}
	
	private static class PresetFloatArrayValue extends FloatArrayValue {

		public PresetFloatArrayValue(MemoryArea memory, ITypeTable tt, float[] barray) {
			super(memory, tt, barray.length);
			this.array = barray;
		}

		@Override
		protected void initializeArray(MemoryArea memory, int length) {
			// Do not create a new one
		}
	}
}
