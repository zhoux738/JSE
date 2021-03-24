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
import info.julang.util.OSTool;

@JulianDoc(
alias = "char",
name = "Char",
summary = 
    "/*"
+ "\n * The character type in Julian language. A char value corresponds to the literal representation of "
+ "\n * the code point on ASCII table."
+ "\n * "
+ "\n * In Julian, the char value can be written in literal form quoted with single quote ('). Usually"
+ "\n * a single character is enclosed by ' and ', but it's also possible to use back-slash (\\) to escape"
+ "\n * certain control chracaters, among them line feed (\\r), carriage return (\\n), horizontal tabulation"
+ "\n * (\\t), nul (\\0). One can also use octal representation for a character, which takes the form of"
+ "\n * '\\NNN', where N is a digit that is between 0 and 7, inclusive. Note the max value is '\176' (or"
+ "\n * 126 if cast to a byte or an integer)."
+ "\n * "
+ "\n * Char type can be hard-cast to byte or integer value as the index of its code point on ASCII table."
+ "\n * For example, 'a' has a numerical value of 97."
+ "\n * "
+ "\n * Char type can be concatenated to other characters or string value with '+' operator in the literal"
+ "\n * form of this value."
+ "\n * "
+ "\n * Char is a primitive type. Therefore it can only be assigned to a variable with same type or "
+ "\n * an [untyped](Any) variable."
+ "\n */"
)
public class CharType extends BasicType {

	private final static CharType INSTANCE = new CharType();
	
	private CharType() {
		
	}
	
	@Override
	public JTypeKind getKind() {
		return JTypeKind.CHAR;
	}

	@Override
	public String getName() {
		return "Char";
	}

	public static BasicType getInstance() {
		return INSTANCE;
	}

	@Override
	public String toString() {
		return "char";
	}
	
	@Override
	public NumberKind getNumberKind() {
		return NumberKind.NONE;
	}
	
	@Override
	public int getSize() {
		return 1;
	}
}
