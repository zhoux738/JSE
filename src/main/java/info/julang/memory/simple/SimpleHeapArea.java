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

package info.julang.memory.simple;

import info.julang.memory.HeapArea;
import info.julang.memory.JSEOutOfMemoryException;
import info.julang.memory.IStored;
import info.julang.memory.MemoryOpreationException;

/**
 * A very simple implementation of heap memory area that doesn't have GC built into it. But since
 * it doesn't keep track of values stored within, it needn't explicitly GC either (that is, it expects
 * JVM to do the job). 
 * 
 * @author Ming Zhou
 */
public class SimpleHeapArea extends HeapArea {
	
	@Override
	public synchronized boolean allocate(IStored value) throws JSEOutOfMemoryException, MemoryOpreationException {
		if(value.isStored()){
			throw new MemoryOpreationException("Attempt to allocate memory for a value that is already stored.", this.getClass());
		}
		
		value.setMemoryArea(this);
		return true;
	}

	@Override
	public synchronized boolean reallocate(IStored value) throws JSEOutOfMemoryException, MemoryOpreationException {
		//If the heap contains this value, we return as if a re-allocation had been actually done.
		if(value.getMemoryArea() == this){
			return true;
		}
		
		return allocate(value);
	}

	@Override
	public synchronized boolean deallocate(IStored value) throws MemoryOpreationException {
		if(!value.isStored()){
			throw new MemoryOpreationException("Attempt to de-allocate memory for a value that is not stored.", this.getClass());
		}
		
		if(value.getMemoryArea() != this){
			return false; // The value is not stored in this memory area.
		}
		
		value.unstore();
		return true;
	}
	
	@Override	
	public boolean isRecycled() {
		return false;
	}

}
