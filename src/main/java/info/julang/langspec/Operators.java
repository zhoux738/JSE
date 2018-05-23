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

package info.julang.langspec;

import info.julang.interpretation.expression.Operator.Associativity;

public enum Operators {

	FUNCCALL		(10, Associativity.LEFT),
	
	INDEX			(10, Associativity.LEFT),	// []
	DOT				(10, Associativity.LEFT),	// .
	
	NEW				(10, Associativity.RIGHT),
	TYPEOF			(10, Associativity.LEFT),
	
	NEGATE			(20, Associativity.RIGHT),	// !
	MINUS			(20, Associativity.RIGHT),	// -
	PLUS			(20, Associativity.RIGHT),	// +
	COMPLEMENT		(20, Associativity.RIGHT),	// ~
	INC				(20, Associativity.LEFT),	// ++
	DEC				(20, Associativity.LEFT),	// --
	CAST			(20, Associativity.RIGHT),	// (type)
	
	MULTIPLY		(30, Associativity.LEFT),
	DIVIDE			(30, Associativity.LEFT),
	MODULO			(30, Associativity.LEFT),	// %
	
	ADD				(35, Associativity.LEFT),
	SUB				(35, Associativity.LEFT),
	
	BLSHIFT			(40, Associativity.LEFT),	// <<
	BRSHIFT			(40, Associativity.LEFT),	// >>
	
	LT				(45, Associativity.LEFT),
	LTEQ			(45, Associativity.LEFT),
	GT				(45, Associativity.LEFT),
	GTEQ			(45, Associativity.LEFT),
	IS				(45, Associativity.LEFT),	// is
	
	EQ				(50, Associativity.LEFT),
	NEQ				(50, Associativity.LEFT),
	
	/**Bitwise AND, as in "a & b"*/
	BAND			(55, Associativity.LEFT),	// &
	/**Bitwise XOR, as in "a ^ b"*/
	BXOR			(56, Associativity.LEFT),	// ^
	/**Bitwise OR, as in "a | b"*/
	BOR				(57, Associativity.LEFT),	// |
	
	AND				(60, Associativity.LEFT),	// &&
	OR				(61, Associativity.LEFT),	// ||
	
	// The following two are used together as the ternary conditional operator:
	COND			(65, Associativity.RIGHT),	// ?
	SELECT			(65, Associativity.RIGHT),	// :
	
	ASSIGN			(70, Associativity.RIGHT),
	ADDSELF			(70, Associativity.RIGHT),  // +=
	SUBSELF			(70, Associativity.RIGHT),  // -=
	MULTIPLYSELF	(70, Associativity.RIGHT),  // *=
	DIVIDESELF		(70, Associativity.RIGHT),  // /=
	MODULOSELF		(70, Associativity.RIGHT),  // %=
	BADDSELF		(70, Associativity.RIGHT),  // &=
	BORSELF			(70, Associativity.RIGHT),  // |=
	BXORSELF		(70, Associativity.RIGHT),  // ^=
	BLSHIFTSELF		(70, Associativity.RIGHT),  // <<=
	BRSHIFTSELF		(70, Associativity.RIGHT),  // >>=
	
	;
	
	Operators (int precedence, Associativity associativity){
		this.precedence = precedence;
		this.associativity = associativity;
	}

	/**
	 * The precedence of this operator. The lesser the higher (0 stands for the highest precedence).
	 */
	public int precedence;
	
	/**
	 * The associativity of this operator.
	 */
	public Associativity associativity;
}
