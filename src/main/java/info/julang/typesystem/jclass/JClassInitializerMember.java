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

package info.julang.typesystem.jclass;

import info.julang.typesystem.jclass.builtin.JMethodType;

/**
 * A hidden member for initializing a field.
 * <p/>
 * The class field initializer is basically an expression running in instance context, so it is just a special type of method.
 * 
 * @author Ming Zhou
 */
public class JClassInitializerMember extends JClassMethodMember {

	private String fieldName;
	
	public JClassInitializerMember(
		ICompoundType definingType,
		String fieldName,
		boolean isStatic,
		JMethodType type) {
		super(definingType, "__init_" + fieldName, Accessibility.HIDDEN, isStatic, false, type, null);
		this.fieldName = fieldName;
	}

	@Override
	public MemberType getMemberType(){
		return MemberType.INITIALIZER;
	}
	
	public String getFieldName(){
		return fieldName;
	}
}
