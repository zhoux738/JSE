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

package info.julang.typesystem;

import info.julang.typesystem.basic.BasicType;
import info.julang.typesystem.conversion.Convertibility;

/**
 * JType represents a type recognized by Julian Scripting Engine.
 * 
 * @author Ming Zhou
 *
 */
public interface JType {

	/**
	 * The kind of this type. This enables a quicker way (than using instanceof) to determine the class type.
	 * 
	 * @return The kind of this type.
	 */
	JTypeKind getKind();
	
	/**
	 * Get the fully qualified name of this type.
	 * @return
	 */
	String getName();
	
	/**
	 * Get the convertibility of this type to another type as specified by the parameter.
	 * 
	 * @param type the type this is to convert to
	 * @return A convertibility value
	 */
	Convertibility getConvertibilityTo(JType type);
	
	/**
	 * Whether this type is basic type.
	 * <br/>
	 * For the basic types supported in Julian, see {@link BasicType}.
	 * <br/>
	 * If this returns true, {@link isObject()} must return false. If this returns false, 
	 * the other method may return either true of false.
	 * @return
	 */
	boolean isBasic();
	
	/**
	 * Whether this type is object type.
	 * <br/>
	 * If this returns true, {@link isBasic()} must return false. If this returns false, 
	 * the other method may return either true of false.
	 * @return
	 */
	boolean isObject();
	
	/**
	 * Whether this type is built-in by script engine.
	 * @return
	 */
	boolean isBuiltIn();
	
	/**
	 * Calculate the size of this type.
	 * @return
	 */
	int getSize();
	
}
