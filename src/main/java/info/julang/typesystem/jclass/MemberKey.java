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

import info.julang.typesystem.jclass.builtin.JFunctionType;

import java.util.Arrays;

/**
 * A key-like object used to represent a class member in map structures. Since this is to be used only
 * purely within the same class, the containing class's equality is not considered (two members, both 
 * named "fun" and have same method signature, from two unrelated classes A and B, will be equated by 
 * this key).
 * 
 * @author Ming Zhou
 */
public interface MemberKey {
	int hashCode();
	boolean equals(Object obj);
}

class MemberKeyBase implements MemberKey {
	
	private String name;
	private MemberType type;
	
	protected MemberKeyBase(JClassMember mem){
		this.name = mem.getName();
		this.type = mem.getMemberType();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MemberKeyBase other = (MemberKeyBase) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (type != other.type)
			return false;
		return true;
	}
}

class ExecutableMemberKey extends MemberKeyBase {

	private String[] paramTypeNames;
	
	ExecutableMemberKey(JFunctionType mtyp, JClassMember mem){
		super(mem);
		
		boolean isInstance = false;
		JParameter[] params = mtyp.getParams();
		if(!mem.isStatic() && params.length > 0 && "this".equals(params[0].getName())){
			paramTypeNames = new String[params.length - 1];
			isInstance = true;
		} else {
			paramTypeNames = new String[params.length];
		}
		for(int i = 0; i < params.length; i++){
			int j = isInstance ? i-1 : i;
			if(j >= 0){
				paramTypeNames[j] = params[i].getType().getName();
			}
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Arrays.hashCode(paramTypeNames);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExecutableMemberKey other = (ExecutableMemberKey) obj;
		if (!Arrays.equals(paramTypeNames, other.paramTypeNames))
			return false;
		return true;
	}
}
