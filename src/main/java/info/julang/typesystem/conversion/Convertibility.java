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

package info.julang.typesystem.conversion;

/**
 * Describe whether and how one type is convertible to another.
 * <p/>
 * Convertibility is used by both basic and object types. Some basic types are convertible to each other,
 * but (1) the precision can be lost after the conversion. {@link #PROMOTED} and {@link #DEMOTED} describes the
 * result of such conversion in this light. (2) a semantic change is implicated, therefore an explicit casting
 * operation is required. {@link #CASTABLE} is for marking such requirement. 
 * <p/>
 * For object types, the convertibility mainly concerns the class/interface inheritance. A subclass is convertible 
 * to its parent class, but the functionality (usable methods) might be narrowed as the result. This is considered 
 * as a {@link #DOWNGRADED} conversion.
 * <p/>
 * In contrast, a parent class is not necessarily convertible to its subclass. But since the classes are in the same 
 * line of hierarchy, the possibility exists that an object of some parent class is indeed an instance of its subclass.
 * We define such conversion as {@link #UNSAFE}.
 * <p/>
 * Other kinds of type conversion are {@link #IMPOSSIBLE}. This include converting between basic and object types 
 * (with the exception of String type), and between classes which are not in the same line of hierarchy.
 * <p/>
 * A type is always convertible to itself ({@link #EQUIVALENT}).
 * 
 * @author Ming Zhou
 */
public enum Convertibility {

	/**
	 * After conversion, the type is promoted in terms of precision. Only used for basic types.
	 * <pre>
	 * Example: integer -> float
	 * </pre>
	 */
	PROMOTED(true),
	
	/**
	 * After conversion, the type is demoted in terms of precision or length of effective bits. Only used for basic types.
	 * <pre>
	 * Example: float -> integer
	 * </pre>
	 */
	DEMOTED(true),
	
	/**
	 * The type is convertible to another with semantic change, and thus an explicit cast operation is needed. 
	 * Only used with certain conversion among basic types, and between string type and other types.
	 * <pre>
	 * Example: byte -> char
	 * </pre>
	 */
	CASTABLE(false),
	
	/**
	 * The type is convertible to another without semantic change, although the two types are not grammatically related.
	 * Only used for describing the semantic equivalence between certain platform types and JSE types.
	 */
	ORTHOGONAL(true),
	
	/**
	 * The conversion doesn't alter the type's precision.
	 * <pre>
	 * Example: any type casting to itself
	 * </pre>
	 */
	EQUIVALENT(true),
	
	/**
	 * After conversion, the type appears higher in the hierarchy, but most likely is downgraded in terms of functionality.
	 * <pre>
	 * Example: class A -> class B, where A derives from (is a subclass of) B
	 * </pre>
	 */
	DOWNGRADED(true),
	
	/**
	 * The conversion is not safe.
	 * <pre>
	 * Example: class A -> class B, where B derives from (is a subclass of) A
	 * </pre>
	 */
	UNSAFE(false),
	
	/**
	 * The conversion is not possible
	 * <pre>
	 * Example: class A --> class B, where B doesn't derive from A
	 * </pre>
	 */
	UNCONVERTIBLE(false);
	
	private boolean safe;
	
	private Convertibility(boolean safe){
		this.safe = safe;
	}

	/**
	 * Whether this conversion is always safe to perform.
	 * @return true if it is always safe.
	 */
	public boolean isSafe() {
		return safe;
	}
	
}
