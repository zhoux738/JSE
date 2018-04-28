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

import info.julang.external.interfaces.IExtValue;
import info.julang.memory.IStored;
import info.julang.memory.value.indexable.IIndexable;
import info.julang.memory.value.iterable.IIterator;
import info.julang.typesystem.JType;

/**
 * JValue is the abstract layer on top of any logically meaningful storage in memory.
 * <p>
 * From the perspective of upper layer, JValue adds logical sense to the contents in memory. The most prominent
 * metadata about a value is its {@link JType type}, which governs how either the entirety, or a particular piece 
 * of this data, shall be accessed. The upper layer thus deals with data only per this typing protocol, without 
 * any need to know how the data is actually stored in memory.
 *
 * @author Ming Zhou
 */
public interface JValue extends IStored, IExtValue {
	
	/**
	 * Get the type of this value. Note this may not necessarily mirror the result of {@link #getKind()}.
	 * @return The {@link JType type} of this value.
	 */
	JType getType();
	
	/**
	 * Assign this value to another value.
	 * 
	 * @param assignee The value to be assigned
	 * @return true is the value is assigned successfully, without precision loss or type demotion; 
	 * false if the value is assigned successfully, but a precision loss or type demotion has also occurred.
	 * @throws IllegalAssignmentException if the assignment is illegal or impossible.
	 */
	boolean assignTo(JValue assignee) throws IllegalAssignmentException;
	
	/**
	 * Compare this value to another value.
	 * <p/>
	 * Note this is different from {@link Object#equals(Object)}, which is used to attest equality in Java.
	 * This method is used to check the equality of two values in Julian. For example, an int value of 1
	 * and a float of 1.0 are considered equal in the script, but their respective Java instance, of type 
	 * {@link IntValue} and {@link FloatValue}, are not equal in Java runtime.
	 * 
	 * @param another
	 * @return true if the two values are deemed equal in Julian script.
	 */
	boolean isEqualTo(JValue another);
	
	/**
	 * Deference this value to get the actual data-holding value. This method actually applies only
	 * to referential values such as {@link RefValue} and {@link UntypedValue}. It is a no-op for 
	 * other values. Under no circumstances will this method throw. If it is the null value being 
	 * referenced, it will return {@link RefValue#NULL NULL}.
	 * 
	 * @return A value that is unwrapped and dereferenced. There is no more redirection, as the memory 
	 * area of this value (returned by {@link #getMemoryArea()}) is exactly where the value's entire 
	 * data set physically resides.
	 * 
	 */
	JValue deref();
	
	/**
	 * Get an indexer for this value. An indexer allows access to an indexed member on this value. 
	 * Wrapper values ({@link RefValue} and {@link UntypedValue}) do not support indexer by themselves.
	 * But after {@link #deref() dereferencing} they may. So make sure to deref the value before calling 
	 * this method, which, by design, will not auto-deref itself.
	 * <p>
	 * This method is implemented by certain built-in values which expose vector semantics (such as array
	 * or string), as well as any user-defined classes which implements 
	 * <font color="green"><code>System.Util.IIndexable</code></font>.
	 * 
	 * @return null if this value is not indexable.
	 */
	IIndexable asIndexer();
	
	/**
	 * Get an iterator for this value. An iterator allows an iteration over this value. 
	 * Wrapper values ({@link RefValue} and {@link UntypedValue}) do not support iterator by themselves.
	 * But after {@link #deref() dereferencing} they may. So make sure to deref the value before calling 
	 * this method, which, by design, will not auto-deref itself.
	 * <p>
	 * This method is implemented by certain built-in values which expose vector semantics (such as array
	 * or string), as well as any user-defined classes which implements either 
	 * <font color="green"><code>System.Util.IIterator</code></font> or
	 * <font color="green"><code>System.Util.IIterable</code></font>.
	 * 
	 * @return null if this value is not iterable.
	 */
	IIterator asIterator();
}
