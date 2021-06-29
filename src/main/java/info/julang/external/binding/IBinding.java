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

package info.julang.external.binding;

import info.julang.external.interfaces.IExtValue;

/**
 * A binding is a pair of name and some kind of value. To the hosting environment, it represents a state of script engine.
 * 
 * @author Ming Zhou
 */
public interface IBinding {

	/**
	 * Whether this binding is mutable.
	 * 
	 * @return true if the binding is mutable.
	 */
	boolean isMutable();
	
	/**
	 * Get the {@link BindingKind kind of binding}.
	 * 
	 * @return The kind of binding
	 */
	BindingKind getKind();
	
	/**
	 * Convert this binding to an {@link ExtValue} that can be accessed
	 * by engine internals.
	 * 
	 * @return The value in a form that is accessible by engine internals.
	 */
	ExtValue toInternal();
	
	/**
	 * Convert this binding to a Java Object instance.
	 * 
	 * @return The value in a form that is accessible by caller's environment (JVM).
	 */
	Object toExternal();
	
	/**
	 * Update the binding with a new value.
	 * 
	 * @param val The value to update.
	 */
	void update(IExtValue val);
	
}
