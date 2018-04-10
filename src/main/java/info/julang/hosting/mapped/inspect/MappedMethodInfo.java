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

package info.julang.hosting.mapped.inspect;

import info.julang.typesystem.jclass.MemberType;

import java.lang.reflect.Method;

public class MappedMethodInfo extends MappedMemberInfoBase {

	private IMappedType[] paramTyps;
	
	/**
	 * @param name
	 * @param isStatic
	 * @param typ return type.
	 * @param paramTyps Do not include 'this' if the method is non-static.
	 */
	public MappedMethodInfo(String name, boolean isStatic, IMappedType typ, IMappedType[] paramTyps, Method m) {
		super(name, isStatic, typ, m);
		this.paramTyps = paramTyps != null ? paramTyps : new IMappedType[0];
	}
	
	/**
	 * Do not include 'this' if the method is non-static.
	 */
	public IMappedType[] getParamTypes(){
		return paramTyps;
	}

	@Override
	public MemberType getMemberType() {
		return MemberType.METHOD;
	}
	
	public Method getMethodMember(){
		return (Method)getMember();
	}

}
