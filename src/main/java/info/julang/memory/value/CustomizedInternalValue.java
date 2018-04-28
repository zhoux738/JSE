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

import info.julang.external.interfaces.JValueKind;
import info.julang.memory.MemoryArea;
import info.julang.memory.value.indexable.IIndexable;
import info.julang.memory.value.iterable.IIterator;
import info.julang.typesystem.JType;

/**
 * A very special value object, which doesn't use any script engine intrinsics at all.
 * Instead, it's mostly for carrying some other values over the SE/platform boundary.
 * 
 * @author Ming Zhou
 */
public class CustomizedInternalValue implements JValue {
	
	@Override
	public MemoryArea getMemoryArea() {
		return null;
	}

	@Override
	public boolean setMemoryArea(MemoryArea memory) {
		return false;
	}

	@Override
	public void unstore() {
		// No-op
	}

	@Override
	public boolean isStored() {
		return false;
	}

	@Override
	public JValueKind getKind() {
		return JValueKind.CUSTOMIZED;
	}

	@Override
	public boolean isBasic() {
		return false;
	}

	@Override
	public boolean isConst() {
		return false;
	}

	@Override
	public boolean isNull() {
		return false;
	}

	@Override
	public JType getType() {
		return null;
	}

	@Override
	public boolean assignTo(JValue assignee) throws IllegalAssignmentException {
		throw new IllegalAssignmentException("Cannot assign a customized value to any other values.");
	}

	@Override
	public boolean isEqualTo(JValue another) {
		return false;
	}

	@Override
	public JValue deref() {
		return this;
	}
	
	@Override
	public IIndexable asIndexer(){
		return null;
	}
	
	@Override
	public IIterator asIterator(){
		return null;
	}

}

