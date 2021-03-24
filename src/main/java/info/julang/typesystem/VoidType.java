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

/**
 * The void type means the type is irrelevant in the context. Note this is different from {@link AnyType type of "var"}.
 * <p>
 * Void type can only be used as the return type of function/methods. It cannot be used as types for parameters.
 * 
 * @author Ming Zhou
 */
public class VoidType implements JType {

	public static VoidType getInstance() {
		return INSTANCE;
	}
	
	private final static VoidType INSTANCE = new VoidType();
	
	private VoidType(){
		
	}
	
	@Override
	public JTypeKind getKind() {
		return JTypeKind.VOID;
	}

	@Override
	public String getName() {
		return "Void";
	}
	
	@Override
	public String toString() {
		return "void";
	}

	@Override
	public Convertibility getConvertibilityTo(JType type) {
		return type.getKind() == JTypeKind.VOID ? Convertibility.EQUIVALENT : Convertibility.UNCONVERTIBLE;
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

	@Override
	public int getSize() {
		return 0;
	}	
}
