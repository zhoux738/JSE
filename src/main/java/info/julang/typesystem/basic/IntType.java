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

package info.julang.typesystem.basic;

import info.julang.typesystem.JTypeKind;
import info.julang.typesystem.jclass.builtin.doc.JulianDoc;

@JulianDoc(
alias = "int",
name = "Integer",
summary = 
    "/*"
+ "\n * The integer type in Julian language. An int type has a value range from -2147483648 (-2^31) "
+ "\n * to 2147483647 (2^-31 - 1). It can be hard-cast to [byte](Byte) with all digits above the 7th "
+ "\n * lost; it can also be implicitly cast to [float](Float) without precision change."
+ "\n * "
+ "\n * int type can participate in arithmetic operations such as '+' and '*' and '/'. It can aslo be"
+ "\n * concatenated to string value with '+' operator in the literal form of this value."
+ "\n * "
+ "\n * int is a primitive type. Therefore it can only be assigned to a variable with same type or "
+ "\n * an [untyped](Any) variable."
+ "\n */"
)
public class IntType extends BasicType {

	private final static IntType INSTANCE = new IntType();
	
	private IntType() {
		
	}
	
	@Override
	public JTypeKind getKind() {
		return JTypeKind.INTEGER;
	}

	@Override
	public String getName() {
		return "Integer";
	}

	public static BasicType getInstance() {
		return INSTANCE;
	}
	
	@Override
	public String toString() {
		return "int";
	}
	
	@Override
	public NumberKind getNumberKind() {
		return NumberKind.WHOLE;
	}
}
