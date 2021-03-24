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
alias = "byte",
name = "Byte",
summary = 
    "/*"
+ "\n * The byte type in Julian language. A byte type has a value range from -128 to 127, so it"
+ "\n * can be promoted to [int](Integer) without losing precision."
+ "\n * "
+ "\n * Byte type can participate in arithmetic operations such as '+', '*' and '/'. It can also be"
+ "\n * concatenated to string value with '+' operator in the literal form of this value."
+ "\n * "
+ "\n * Byte is a primitive type. Therefore it can only be assigned to a variable with same type or "
+ "\n * an [untyped](Any) variable."
+ "\n */"
)
public class ByteType extends BasicType {

	private final static ByteType INSTANCE = new ByteType();
	
	private ByteType() {
		
	}
	
	@Override
	public JTypeKind getKind() {
		return JTypeKind.BYTE;
	}

	@Override
	public String getName() {
		return "Byte";
	}

	public static BasicType getInstance() {
		return INSTANCE;
	}
	
	@Override
	public String toString() {
		return "byte";
	}
	
	@Override
	public NumberKind getNumberKind() {
		return NumberKind.WHOLE;
	}
	
	@Override
	public int getSize() {
		return 1;
	}
}
