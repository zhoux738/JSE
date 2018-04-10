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

import info.julang.memory.MemoryArea;
import info.julang.typesystem.JType;

/**
 * A value holding data of basic type supported by Julian Scripting Engine.
 * 
 * @author Ming Zhou
 */
public abstract class BasicValue extends JValueBase {
	
	public BasicValue(MemoryArea memory, JType type) {
		super(memory, type, false);
	}

	/**
	 * Replicate this basic value by casting it to another type. Precision loss by demotion or semantic change is possible.
	 * <p/>
	 * Note this method would create (and return) a new value to be stored in the given memory area.
	 * @param type
	 * @return the replicated value, stored in the given memory area. 
	 * Null if this type is not convertible to the target type.
	 */
	public abstract JValue replicateAs(JType type, MemoryArea memory);
	
	public boolean isBasic(){
		return true;
	}
	
	public boolean isNull(){
		return false;
	}
	
	protected boolean assignToResult(JType assigneeType){
		switch(getType().getConvertibilityTo(assigneeType)){
		case PROMOTED:
		case EQUIVALENT:
			return true;
		case DEMOTED:
			return false;
		default:
			throw new IllegalAssignmentException(getType(), assigneeType);
		}
	}

}
