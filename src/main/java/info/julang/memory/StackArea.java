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

package info.julang.memory;

import info.julang.interpretation.IStackFrameInfo;


/**
 * The stack area holds data of local variables whose life cycle is entirely determined by the function call.
 * <br/>
 * Each thread has one stack area. Therefore a scripting engine can have more than one stack area.
 * 
 * @author Ming Zhou
 * 
 */
public abstract class StackArea implements MemoryArea {

	@Override
	public MemoryAreaType getKind() {
		return MemoryAreaType.STACK;
	}
	
	/**
	 * Push a new frame on top of the stack. 
	 * <p/>
	 * This method is called when a function is called in the current thread (the thread that holds this stack area).
	 * 
	 * @throws JSEStackOverflowException if no more frame can be pushed into stack
	 */
	abstract public void pushFrame(IStackFrameInfo info) throws JSEStackOverflowException;
	
	/**
	 * Pop the topmost frame out of the stack. 
	 * <p/>
	 * This method is called when a function exits in the current thread (the thread that holds this stack area).
	 */
	abstract public void popFrame();
	
	/**
	 * Get the active frame (the frame that corresponds to the function being called).
	 */
	abstract public FrameMemoryArea currentFrame();

	/**
	 * Get the frame at the given index. 0 is the current top. 
	 * 
	 * @param index the index of the target frame. 0 is the top, 1 the one frame below the top, and so on. 
	 * Apparently the maximum index value = <code>{@link #getDepth()} - 1</code>.
	 * @return null if the given index is beyond the range.
	 */
	abstract public FrameMemoryArea getFrameFromTop(int index);
}
