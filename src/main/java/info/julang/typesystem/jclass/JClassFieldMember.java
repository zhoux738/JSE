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

import info.julang.typesystem.JType;
import info.julang.typesystem.jclass.annotation.JAnnotation;

/**
 * A field member in a class.
 * 
 * @author Ming Zhou
 */
public class JClassFieldMember extends JClassMember implements IUntyped {

	private boolean _const;
	
	private boolean _untyped;
	
	public JClassFieldMember(
		ICompoundType definingType, String name, Accessibility accessibility, 
		boolean isStatic, boolean isConst, JType type, JAnnotation[] annotations) {
		super(definingType, name, accessibility, isStatic, MemberType.FIELD, type, annotations);
		_const = isConst;
		_untyped = type == null;
	}
	
	public static JClassFieldMember makeInstanceConstField(
		ICompoundType definingType, 
		String name, Accessibility accessibility, JType type){
		return new JClassFieldMember(definingType, name, accessibility, false, true, type, null);
	}

	public boolean isConst() {
		return _const;
	}
	
	@Override
	public boolean isUntyped(){
		return _untyped;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		
		sb.append(getAccessibility().toString());

		if(isStatic()){
			sb.append(" static");
		}
		
		sb.append(" ");
		sb.append(getType().getName());
		sb.append(" ");
		sb.append(getName());
		
		return sb.toString();
	}

}
