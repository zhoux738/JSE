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

import info.julang.typesystem.conversion.Convertibility;
import info.julang.typesystem.jclass.builtin.doc.JulianDoc;

/**
 * The "untyped" type in Julian script. An untyped value wraps a typed value inside.
 * 
 * @author Ming Zhou
 */
@JulianDoc(
alias = "var",
name = "Any",
summary = 
    "/*"
+ "\n * Any is the type in Julian language to provide an expereince for non-typed system."
+ "\n * "
+ "\n * Any cannot be instantiated directly, but a variable of Any can always be assigned from"
+ "\n * variables of any other types, including [Object] and primitive types such as [int]. Wherever type"
+ "\n * checking is performed, such as in the cases of paramters, Any is always treated as a match."
+ "\n * "
+ "\n * Any cannot be used as the element type of [Array]. Attempt to create such array will result in"
+ "\n * runtime error:"
+ "\n * [code]"
+ "\n * var v = new var[5]; // Throws!"
+ "\n * [code: end]"
+ "\n * "
+ "\n * However, an array value can be assigned to an Any variable. Then one can use ```is``` operator"
+ "\n * to check if the variable is actually an Array."
+ "\n * [code]"
+ "\n * var v = new MyClass[5];"
+ "\n * v is Array; // true"
+ "\n * v is MyClass[]; // true"
+ "\n * v is Object[]; // false - array doesn't support covariance"
+ "\n * [code: end]"
+ "\n * "
+ "\n * Similarly, an Any variable of Array type can be cast to [Array] or the concrete array type. But casting"
+ "\n * to an array of the element's ancestor type would fail since Julian doesn't support type covariance."
+ "\n */"
)
public class AnyType implements JType {

	public static AnyType getInstance() {
		return INSTANCE;
	}
	
	private final static AnyType INSTANCE = new AnyType();
	
	private AnyType(){
		
	}
	
	@Override
	public JTypeKind getKind() {
		return JTypeKind.ANY;
	}

	@Override
	public String getName() {
		return "Any";
	}
	
	@Override
	public String toString() {
		return "any";
	}

	/**
	 * Returns {@link Convertibility#UNSAFE} if the target type is not ANY.
	 */
	@Override
	public Convertibility getConvertibilityTo(JType type) {
		return type.getKind() == JTypeKind.ANY ? Convertibility.EQUIVALENT : Convertibility.UNSAFE;
	}

	@Override
	public boolean isBasic() {
		return false;
	}

	@Override
	public boolean isObject() {
		return false;
	}

	@Override
	public boolean isBuiltIn() {
		return true;
	}
	
}
