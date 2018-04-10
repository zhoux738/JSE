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

import info.julang.memory.FrameMemoryArea;
import info.julang.memory.JSEOutOfMemoryException;
import info.julang.memory.JSEStackOverflowException;
import info.julang.memory.IStored;
import info.julang.memory.MemoryOpreationException;
import info.julang.memory.StackArea;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * A very simple implementation of stack memory area that doesn't have GC built into it. But since
 * it doesn't keep track of values stored within, it needn't explicitly GC either (that is, it expects
 * JVM to do the job). 
 * 
 * @author Ming Zhou
 */
public class SimpleStackArea extends StackArea {

	private class SimpleFrameArea extends FrameMemoryArea {
		
		private boolean recycled;
		
		@Override
		public boolean allocate(IStored value) {
			if(value.isStored()){
				throw new MemoryOpreationException("Attempt to allocate memory for a value that is already stored.", this.getClass());
			}

			value.setMemoryArea(this);
			return true;
		}

		@Override
		public boolean reallocate(IStored value)
			throws JSEOutOfMemoryException, MemoryOpreationException {
			//If the frame contains this value, we return as if a re-allocation had been actually done.
			if(value.getMemoryArea() == this){
				return true;
			}
			
			return allocate(value);
		}

		@Override
		public boolean deallocate(IStored value) throws MemoryOpreationException {
			if(!value.isStored()){
				throw new MemoryOpreationException("Attempt to de-allocate memory for a value that is not stored.", this.getClass());
			}
			
			if(value.getMemoryArea() != this){// The value is not stored in this memory area.
				return false;
			}
			
			value.unstore();
			return true;
		}

		private void recycle() {
			recycled = true;
		}

		@Override
		public boolean isRecycled() {
			return recycled;
		}
		
	}
	
	private Deque<SimpleFrameArea> stack = new ArrayDeque<SimpleFrameArea>();
	
	private int STACK_DEPTH_LIMIT = 200; // TODO - make this configurable
	
	private SimpleFrameArea getActiveFrame(){
		return stack.peekFirst();
	}
	
	@Override
	public boolean allocate(IStored value) 
		throws JSEOutOfMemoryException, MemoryOpreationException {
		SimpleFrameArea frame = getActiveFrame();
		if(frame == null){
			throw new MemoryOpreationException("Cannot allocate memory from a uninitialized stack memory area.", this.getClass());
		}
		// Dispatch operation to the active frame.
		return frame.allocate(value);
	}

	@Override
	public boolean reallocate(IStored value) 
		throws JSEOutOfMemoryException, MemoryOpreationException {
		SimpleFrameArea frame = getActiveFrame();
		if(frame == null){
			throw new MemoryOpreationException("Cannot allocate memory from a uninitialized stack memory area.", this.getClass());
		}
		// Dispatch operation to the active frame.
		return frame.reallocate(value);
	}

	@Override
	public boolean deallocate(IStored value) throws MemoryOpreationException {
		SimpleFrameArea frame = getActiveFrame();
		if(frame == null){
			throw new MemoryOpreationException("Cannot allocate memory from a uninitialized stack memory area.", this.getClass());
		}
		// Dispatch operation to the active frame.
		return frame.deallocate(value);
	}

	@Override
	public void pushFrame() throws JSEStackOverflowException {
		if (stack.size() > STACK_DEPTH_LIMIT) {
			// When we hit this, the engine is not really recoverable. 
			// So relax the cap to ensure that at least error handling can perform as expected.
			STACK_DEPTH_LIMIT *= 2;
			throw new JSEStackOverflowException();
		}
		
		stack.push(new SimpleFrameArea());
	}

	@Override
	public void popFrame() {
		SimpleFrameArea frame = stack.pop();
		if(frame!=null){
			frame.recycle();
		}
	}

	@Override
	public FrameMemoryArea currentFrame() {
		return stack.peek();
	}

	@Override
	public boolean isRecycled() {
		return false;
	}

}
