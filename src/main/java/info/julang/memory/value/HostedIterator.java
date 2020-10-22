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

import info.julang.execution.threading.ThreadRuntime;
import info.julang.memory.value.iterable.IIterator;
import info.julang.memory.value.operable.InitArgs;
import info.julang.typesystem.jclass.builtin.JArrayType;

/**
 * An iterator that is backed by a hosted array object (Java's array).
 * 
 * @author Ming Zhou
 */
public class HostedIterator extends HostedIndexedBase implements IIterator {

	private int index;
	private int length;
	
	public HostedIterator(Class<?> arrayClass, JArrayType type, Object value) {
		super(arrayClass, type, value);
	}

	@Override
	public void initialize(ThreadRuntime rt, InitArgs args) {
		super.initialize(rt, args);
		index = 0;
		length = getLength();
	}

	@Override
	public boolean hasNext() {
		return index < length;
	}

	@Override
	public JValue next() {
		JValue value = getElementValue(index);
		index++;
		return value;
	}

	@Override
	public void dispose() {
		// NO-OP
	}

}
