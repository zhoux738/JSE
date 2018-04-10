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

import info.julang.external.interfaces.IExtMemoryArea;

/**
 * A memory area represents a logical region of the underlying memory.
 * <br/>
 * 
 * @author Ming Zhou
 * 
 */
public interface MemoryArea extends IExtMemoryArea {

	/**
	 * The kind of this area. This enables a quicker way (than using instanceof) to determine the class type.
	 * 
	 * @return The type of this area.
	 */
	MemoryAreaType getKind();
	
	/**
	 * Allocate storage for a value.
	 * 
	 * @param value
	 * @return true if successful.
	 * @throws JSEOutOfMemoryException
	 */
	boolean allocate(IStored value) throws JSEOutOfMemoryException, MemoryOpreationException;
	
	/**
	 * Re-allocate storage for a value.
	 * <p/>
	 * The value must either have been allocated storage in the same memory area, or not allocated in any memory area.
	 * 
	 * @param value
	 * @return true if successful.
	 * @throws JSEOutOfMemoryException if no enough memory to allocate for the value.
	 * @throws MemoryOpreationException if trying to allocate memory for a value that is stored in other memory area.
	 */
	boolean reallocate(IStored value) throws JSEOutOfMemoryException, MemoryOpreationException;
	
	/**
	 * Deallocate storage for a value.
	 * <p/>
	 * If the value is not stored, this method does nothing.
	 * 
	 * @param value
	 * @return true if successful; false if the value is not stored in this memory area.
	 * @throws MemoryOpreationException if trying to de-allocate memory for a value that is not stored in any memory area.
	 */
	boolean deallocate(IStored value) throws MemoryOpreationException;
	
}
