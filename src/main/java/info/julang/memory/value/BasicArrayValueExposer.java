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

/**
 * A utility class used to expose the underlying primitive array which holds value for a basic array.
 * 
 * @author Ming Zhou
 */
package info.julang.memory.value;

import info.julang.typesystem.JType;
import info.julang.typesystem.jclass.builtin.JArrayType;

public class BasicArrayValueExposer {

	private boolean[] za;
	private byte[] ba;
	private int[] ia;
	private float[] fa;
	private char[] ca;
	
	public boolean[] getBoolArray() {
		return za;
	}

	public byte[] getByteArray() {
		return ba;
	}

	public int[] getIntArray() {
		return ia;
	}

	public float[] getFloatArray() {
		return fa;
	}

	public char[] getCharArray() {
		return ca;
	}

	public BasicArrayValueExposer(BasicArrayValue bav){
		JArrayType at = (JArrayType)bav.getType();
		JType et = at.getElementType();
		Object ua = bav.getPlatformArrayObject();
		switch(et.getKind()){
		case BOOLEAN:
			za = (boolean[])ua;
			break;
		case BYTE:
			ba = (byte[])ua;
			break;
		case CHAR:
			ca = (char[])ua;
			break;
		case FLOAT:
			fa = (float[])ua;
			break;
		case INTEGER:
			ia = (int[])ua;
			break;
		default:
			break;
		}
	}
	
	
}
