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

package info.julang.memory.value.indexable;

import info.julang.memory.value.ArrayIndexOutOfRangeException;
import info.julang.memory.value.ArrayValue;
import info.julang.memory.value.ByteValue;
import info.julang.memory.value.IntValue;
import info.julang.memory.value.JValue;

/**
 * A {@link JIndexable} backed by an {@link ArrayValue}.
 * 
 * @author Ming Zhou
 */
public class ArrayIndexable implements JIndexable {

	private int index;
	
	private ArrayValue av;
	
	public ArrayIndexable(ArrayValue av){
		this.av = av;
	}
	
	@Override
	public JValue getIndexableValue() {
		return av;
	}

	@Override
	public void setIndex(int index) {
		this.index = index;
	}

	@Override
	public int getIndex() {
		return index;
	}

	@Override
	public int getLength() {
		return av.getLength();
	}

	@Override
	public JValue getCurrent() throws ArrayIndexOutOfRangeException {
		return get(index);
	}

	@Override
	public JValue getByIndex(JValue index) throws UnsupportedIndexTypeException, ArrayIndexOutOfRangeException {
		int ii = -1;
		switch(index.getKind()){
		case INTEGER:
			IntValue iv = (IntValue) index;
			ii = iv.getIntValue();
			break;
		case BYTE:
			ByteValue bv = (ByteValue) index;
			ii = bv.getByteValue();
			break;
		default:
			throw new UnsupportedIndexTypeException("The index for an array must be an integer.");		
		}
		
		return get(ii);
	}
	
	@Override
	public JValue setByIndex(JValue index, JValue value) throws UnsupportedIndexTypeException {
		JValue oldValue = getByIndex(index);
		value.assignTo(oldValue);
		return oldValue;
	}
	
	@Override
	public void dispose(){
		// Nothing to be done here.
	}
	
	private JValue get(int index){
		return av.getValueAt(index);
	}

}
