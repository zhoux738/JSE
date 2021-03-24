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

import static info.julang.typesystem.conversion.Convertibility.CASTABLE;
import static info.julang.typesystem.conversion.Convertibility.DEMOTED;
import static info.julang.typesystem.conversion.Convertibility.EQUIVALENT;
import static info.julang.typesystem.conversion.Convertibility.PROMOTED;
import static info.julang.typesystem.conversion.Convertibility.UNCONVERTIBLE;
import info.julang.typesystem.AnyType;
import info.julang.typesystem.JType;
import info.julang.typesystem.conversion.Convertibility;
import info.julang.typesystem.jclass.builtin.JStringType;
import info.julang.util.OSTool;

/**
 * An overall class representing the basic types supported by Julian Script Language, which are:
 * <pre>   BoolType,
 *   CharType,
 *   FloatType,
 *   IntType,
 *   ByteType</pre>
 *   
 * @author Ming Zhou
 */
public abstract class BasicType implements JType {

	/**
	 * Important note: the correctness of this matrix depends on the ordinal order of
	 * {@link info.julang.typesystem.JTypeKind JTypeKind}.
	 */
	private static Convertibility[][] CONVERSION_TABLE = 
	new Convertibility[][] {
		          /* Row: from; Column: to */ /*  BOOLEAN     	 CHAR           FLOAT          INTEGER       BYTE*/
		new Convertibility[]	/* BOOLEAN */	{ EQUIVALENT,    UNCONVERTIBLE, UNCONVERTIBLE, CASTABLE,     CASTABLE},
		
		new Convertibility[]	/*    CHAR */   { UNCONVERTIBLE, EQUIVALENT,    UNCONVERTIBLE, CASTABLE,     CASTABLE},
			
		new Convertibility[]	/*   FLOAT */   { UNCONVERTIBLE, UNCONVERTIBLE, EQUIVALENT,    DEMOTED,      DEMOTED},
		
		new Convertibility[]	/* INTEGER */   { CASTABLE,      CASTABLE,      PROMOTED,      EQUIVALENT,   DEMOTED},	
		
		new Convertibility[]	/*    BYTE */   { CASTABLE,      CASTABLE,      PROMOTED,      PROMOTED,     EQUIVALENT},			
	};
	
	@Override
	public boolean isBasic() {
		return true;
	}

	@Override
	public boolean isObject() {
		return false;
	}
	
	@Override
	public boolean isBuiltIn() {
		return true;
	}
	
	/**
	 * @return the kind of number type. Will be {@link NumberKind#NONE} if it is not a number type.
	 */
	public abstract NumberKind getNumberKind();
	
	/**
	 * Get the convertibility to other type. 
	 * A basic type can be converted to certain other basic types and string type.
	 * @param other
	 * @return the convertibility to the other type.
	 */
	@Override
	public Convertibility getConvertibilityTo(JType other) {
		if(other == AnyType.getInstance()){
			return Convertibility.DOWNGRADED;
		}
		
		if(!other.isBasic()){
			if(JStringType.isStringType(other)){
				return Convertibility.CASTABLE;
			} else {
				return Convertibility.UNCONVERTIBLE;			
			}
		}
		
		return CONVERSION_TABLE[this.getKind().ordinal()][other.getKind().ordinal()];
	}
}
