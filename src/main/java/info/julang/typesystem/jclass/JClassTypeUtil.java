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

import info.julang.external.exceptions.JSEError;
import info.julang.memory.value.JValue;
import info.julang.memory.value.ObjectValue;
import info.julang.typesystem.JType;
import info.julang.typesystem.JTypeKind;
import info.julang.typesystem.conversion.Convertibility;
import info.julang.typesystem.jclass.builtin.JConstructorType;
import info.julang.typesystem.jclass.builtin.JMethodType;
import info.julang.util.OneOrMoreList;

public final class JClassTypeUtil {

	/**
	 * Given an argument array, find the matching constructors.
	 * 
	 * @param ctors
	 * @param value
	 * @param matchThisObj whether checking the argument for the implicit "this" parameter, 
	 * which must be the first one in the given array.
	 * @return null if not found
	 */
	public static JClassConstructorMember findConstructors(
		JClassConstructorMember[] ctors, 
		JValue[] values, 
		boolean matchThisObj){
		
		if(ctors == null){
			throw new JSEError("Constructor candidates cannot be null.", JClassTypeUtil.class);
		}
		if(values == null){
			values = new JValue[0];
		}
		
		int total = values.length;
		for(JClassConstructorMember ctor : ctors){
			JConstructorType ctorTyp = ctor.getCtorType();
			JParameter[] params = ctorTyp.getParams();
			if(params.length != values.length){
				continue; // number of arguments not matching, try next
			}
			JType pTyp = null;
			JType vTyp = null;
			boolean found = true;
			int start = matchThisObj ? 0 : 1;
			for(int i = start; i<total; i++){
				pTyp = params[i].getType(); // must not be null
				vTyp = values[i].getType(); // can be null
				if (vTyp == null) {
					// If value doesn't have a type, consider it generally applicable since it is a generic null.
					switch(pTyp.getKind()){
					case ANY:
					case CLASS:
						continue;
					default:
						found = false;
						break;	
					}
				}
				
				if (!found) {
					continue;
				}
				
				if(pTyp == vTyp){
					continue;
				} else if (pTyp.getKind() == JTypeKind.CLASS) {
					if(vTyp.getKind() == JTypeKind.CLASS){
						ICompoundType cpTyp = (ICompoundType) pTyp;
						ICompoundType cvTyp = (ICompoundType) vTyp;
						if(cvTyp.isDerivedFrom(cpTyp, true)){
							continue;
						}
					}
				} else {
					Convertibility conv = vTyp.getConvertibilityTo(pTyp);
					if(conv.isSafe()){
						continue;
					} else {
						found = false;
						break;		
					}
				}
			}
			
			if(found){
				return ctor;
			}
		}
		
		return null;
	}
	
	/**
	 * Check if the class member is a public abstract instance method.
	 * <p>
	 * NOTE: a public abstract instance method defined in a class will also pass the check.
	 * 
	 * @param jcm the class member to inspect
	 * @return {@link JClassMethodMember} converted from the given member if the check passed; null if not.
	 */
	public static JClassMethodMember isInterfaceMember(JClassMember jcm){
		if (jcm.getMemberType() == MemberType.METHOD && 
			jcm.getAccessibility() == Accessibility.PUBLIC && 
			!jcm.isStatic()){
			JClassMethodMember jcmm = (JClassMethodMember)jcm;
			if (jcmm.isAbstract() ){
				return jcmm;
			}
		}
		
		return null;
	}
	
	/**
	 * Check if a type is deriving from the parent class type.
	 * 
	 * @param type
	 * @param parent
	 * @return if it derives from parent, return the 1st argument cast to JClassType.
	 */
	public static JClassType isDerivingFrom(JType type, JClassType parent, boolean includeIdentical){
		if(!type.isObject()){
			return null;
		}
		JClassType jct = (JClassType) type;
		if(jct.isDerivedFrom(parent, includeIdentical)){
			return jct;
		}
		return null;
	}
	
	/**
	 * Find a method on the given object with exactly type mapping.
	 * 
	 * @param obj the Julian runtime object
	 * @param methodName the name of method
	 * @param ptypes the parameter list, excluding the "this" object 
	 * @param isStatic
	 * @return null if not found.
	 */
	public static JMethodType findMethodWithStrictSignatureMapping(
		ObjectValue obj, String methodName, JType[] ptypes, boolean isStatic){
		OneOrMoreList<ClassMemberLoaded> list = obj.getClassType().getMembers(isStatic).getLoadedMemberByName(methodName);
		for(ClassMemberLoaded cml : list){
			JClassMember cm = cml.getClassMember();
			if (cm.getMemberType() == MemberType.METHOD){
				JClassMethodMember mem = (JClassMethodMember)cm;
				JMethodType mtyp = mem.getMethodType();
				JParameter[] jps = mtyp.getParams();
				int start = isStatic ? 0 : 1;
				if (jps.length == (ptypes.length + start)){
					for(int i = start, j = 0; i < jps.length; i++, j++){
						if (jps[i].getType() != ptypes[j]){
							mtyp = null;
							break;
						}
					}
				} else {
					mtyp = null;
				}
				
				if (mtyp != null){
					return mtyp;
				}
			}
		}
		
		return null;
	}	
}
