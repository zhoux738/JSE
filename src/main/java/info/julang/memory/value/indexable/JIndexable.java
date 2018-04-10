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

import info.julang.memory.value.JValue;

/**
 * An internal interface facilitating processing of iterable and indexable Julian objects, 
 * such as array or <font color="green">System.Collection.List</font>.
 * <p/>
 *  
 * @author Ming Zhou
 */
public interface JIndexable {

	/**
	 * Get the indexable {@link JValue} that backs this object.
	 * @return
	 */
	JValue getIndexableValue();
	
	/**
	 * Set the index. 0-based. 
	 * @return
	 */
	void setIndex(int index);
	
	/**
	 * Get the index of element to retrieve next. 0-based.
	 * @return
	 */
	int getIndex();
	
	/**
	 * Get the total number of elements.
	 * @return
	 */
	int getLength();
	
	/**
	 * Get the value at the current index. 
	 * @return null if the index is out of range.
	 */
	JValue getCurrent();
	
	/**
	 * Get the value at the specified index. 
	 * @param index
	 * @throws UnsupportedIndexTypeException if the type of specified index is not supported.
	 * @return null if the index is out of range.
	 */
	JValue getByIndex(JValue index) throws UnsupportedIndexTypeException;
	
	/**
	 * Set the value at the specified index. Will overwrite.
	 * <p/>
	 * This is used when assigning by index, in the form of <p/><pre>
	 * <code>m[k] = v;</code></pre>
	 * @return the new value at the specified index. Null if the index is out of range.
	 */
	JValue setByIndex(JValue index, JValue value) throws UnsupportedIndexTypeException;
	
	/**
	 * An optional method which disposes of the underlying resource upon being released.
	 */
	void dispose();
	
}
