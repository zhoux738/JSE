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
import info.julang.typesystem.JType;

public abstract class JValueBase implements JValue {

	/**
	 * The common constructor of JValue asks for a memory area to store the created JValue.
	 * @param memory the {@link info.julang.memory.MemoryArea MemoryArea} to store this value.
	 * @param type the type of this value
	 */
	public JValueBase(MemoryArea memory, JType type, boolean delayAllocation) {
		initialize(type, memory);
		if(memory!=null && !delayAllocation){
			memory.allocate(this);
		}
	}
	
	/**
	 * Initialize the value. Called before the memory is allocated in JValueBase's constructor.
	 * 
	 * @param type the type of this value.
	 * @param memory the memory area in which this value is to be stored.
	 */
	protected abstract void initialize(JType type, MemoryArea memory);
	
	/* IStored START */
	
	public boolean isStored(){
		MemoryArea ma = getMemoryArea();
		return ma != null && !ma.isRecycled();
	}
	
	public MemoryArea getMemoryArea(){
		return memoryArea;
	}
	
	public void unstore(){
		memoryArea = null;
	}
	
	public boolean setMemoryArea(MemoryArea memory){
		if(memoryArea!=null){
			return false;
		}
		
		memoryArea = memory;
		return true;
	}
	
	/* IStored END */
	
	private MemoryArea memoryArea;

	@Override
	public boolean isConst() {
		return isConst;
	}
	
	void setConst(boolean isConst) {
		this.isConst = isConst;
	}

	private boolean isConst;
	
	// All the values, except UntypedValue, must call this first. UntypedValue must not call this.
	@Override
	public boolean assignTo(JValue assignee) throws IllegalAssignmentException {
		if(assignee.isConst()){
			throw new AttemptToChangeConstException();
		}
		
		if(assignee.getKind() == JValueKind.UNTYPED){
			((UntypedValue) assignee).setActual(this);
			return true;
		}
		
		return false;
	}
	
	// By default, this is a no-op
	@Override
	public JValue deref(){
		return this;
	}
}
