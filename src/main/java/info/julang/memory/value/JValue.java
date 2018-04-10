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
import info.julang.typesystem.JType;

/**
 * JValue is the abstract layer on top of any logically meaningful storage in memory.
 * <p/>
 * From the perspective of upper layer, Value adds logical meaning to the contents in memory. 
 * The callers don't need to know how the data is actually stored in memory.
 *
 * @author Ming Zhou
 */
public interface JValue extends IStored, IExtValue {
	
	/**
	 * Get the type of this value. Note this may not necessarily mirror the result of {@link #getKind()}.
	 * @return 
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
	 * @return
	 */
	JValue deref();
}
