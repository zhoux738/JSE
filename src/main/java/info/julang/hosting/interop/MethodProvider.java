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

package info.julang.hosting.interop;

import info.julang.external.exceptions.JSEError;
import info.julang.typesystem.AnyType;
import info.julang.typesystem.JType;
import info.julang.typesystem.jclass.ClassMemberLoaded;
import info.julang.typesystem.jclass.ClassMemberMap;
import info.julang.typesystem.jclass.ICompoundType;
import info.julang.typesystem.jclass.JClassMember;
import info.julang.typesystem.jclass.JClassMethodMember;
import info.julang.typesystem.jclass.JClassType;
import info.julang.typesystem.jclass.JParameter;
import info.julang.typesystem.jclass.MemberType;
import info.julang.typesystem.jclass.builtin.JMethodType;
import info.julang.util.OneOrMoreList;

/**
 * A provider that can lazily find the method and cache the result.
 * 
 * @author Ming Zhou
 */
class MethodProvider {

	private String name;
	private boolean isStatic;
	private JType[] types;
	private JClassType jcp;
	
	private JMethodType jmt;
	
	MethodProvider(String name, JClassType jcp, boolean isStatic, JType[] types){
		this.name = name;
		this.isStatic = isStatic;
		this.types = types;
		this.jcp = jcp;
	}
	
	JMethodType provide(){
		// Initialize
		if (jmt == null){
			ClassMemberMap mmap = jcp.getMembers(isStatic);
			OneOrMoreList<ClassMemberLoaded> cands = mmap.getLoadedMemberByName(name);
			for(ClassMemberLoaded cand : cands){
				JClassMember mem = cand.getClassMember();
				if (mem.getMemberType() == MemberType.METHOD){
					JClassMethodMember jcmm = (JClassMethodMember)mem;
					JMethodType jmt0 = jcmm.getMethodType();
					JParameter[] params = jmt0.getParams();
					int start = isStatic ? 0 : 1;
					boolean unmatched = false;
					if (params.length == (types.length + start)){
						for(int i = start, j = 0; i<params.length; i++, j++){
							JType declaredType = params[i].getType();
							JType expectedType = types[j];
							if (declaredType == null || declaredType == AnyType.getInstance()){
								continue;
							} else if (declaredType.isBasic() && declaredType == expectedType){
								continue;
							} else if (expectedType.isObject() && declaredType.isObject()){
								JClassType expectedClassType = (JClassType)expectedType;
								if (expectedClassType.isDerivedFrom((ICompoundType)declaredType, true)){
									continue;
								}
							}
							
							unmatched = true;
							break;
						}
					} else {
						unmatched = true;
					}
					
					if (!unmatched){ // found the method
						jmt = jmt0;
						break;
					}
				}
			}
		}
		
		// Return
		if (jmt != null){
			return jmt;
		} else {
			throw new JSEError(
				"A method of name \"" + name + 
				"\" with given parameter types cannot be found on the resolved type \"" + 
				jcp.getName() + "\"."); 
		}
	}

	public String getMethodName() {
		return name;
	}
	
	public boolean isStatic(){
		return isStatic;
	}
	
}
