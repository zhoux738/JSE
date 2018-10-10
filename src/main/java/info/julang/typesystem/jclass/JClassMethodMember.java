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

import info.julang.typesystem.jclass.annotation.JAnnotation;
import info.julang.typesystem.jclass.builtin.JMethodType;

import java.util.Arrays;

/**
 * A method member in a class.
 * 
 * @author Ming Zhou
 *
 */
public class JClassMethodMember extends JClassMember {

	private boolean _abstract;
	
	private JMethodType ftype;
	
	public JClassMethodMember(
		ICompoundType definingType,
		String name, 
		Accessibility accessibility, 
		boolean isStatic, 
		boolean isAbstract, 
		JMethodType type,
		JAnnotation[] annotations) {
		super(definingType, name, accessibility, isStatic, MemberType.METHOD, type, annotations);
		_abstract = isAbstract;
		ftype = type;
	}

	public boolean isAbstract() {
		return _abstract;
	}
	
	/**
	 * A quicker way than calling {@link #getType()} to cast from.
	 * @return
	 */
	public JMethodType getMethodType(){
		return ftype;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		
		sb.append(getAccessibility().toString());
		if(isAbstract()){
			sb.append(" abstract");
		}
		boolean sta = isStatic();
		if(sta){
			sb.append(" static");
		}
		sb.append(" ");
		sb.append(getName());
		sb.append("(");
		JParameter[] params = ftype.getParams();
		boolean first = false;
		for(JParameter jp : params){
			// Skip the 1st parameter ("this") for instance method.
			if (first && sta){
				first = false;
				continue;
			}
			
			sb.append(jp.getType().getName());
			sb.append(" ");
			sb.append(jp.getName());
			sb.append(" ");
		}
		sb.append(")");
		
		return sb.toString();
	}
	
	@Override
	public MemberKey getKey(){
		return new ExecutableMemberKey(this.getMethodType(), this);
	}
}
