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

package info.julang.interpretation.syntax;

import info.julang.external.exceptions.JSEError;
import info.julang.typesystem.jclass.MemberType;

import java.util.ArrayList;
import java.util.List;

/**
 * Basic info about a method member.
 * 
 * @author Ming Zhou
 */
public class MethodDeclInfo extends MemberDeclInfo {

	public class TypeAndName{
		
		public TypeAndName(ParsedTypeName typeName, String paramName) {
			this.typeName = typeName;
			this.paramName = paramName;
		}
		
		public ParsedTypeName getTypeName() {
			return typeName;
		}

		public String getParamName() {
			return paramName;
		}

		private ParsedTypeName typeName;
		
		private String paramName;
		
	}
	
	@Override
	public MemberType getMemberType() {
		return MemberType.METHOD;
	}
	
	List<TypeAndName> paramList;
	
	void addParameter(ParsedTypeName typeName, String paramName){
		if(paramList==null){
			paramList = new ArrayList<TypeAndName>();
		}
		paramList.add(new TypeAndName(typeName, paramName));
	}
	
	/**
	 * 
	 * @return can be null if there is no parameter.
	 */
	public List<TypeAndName> getParameters(){
		if(paramList == null){
			return null;
		}
		return new ArrayList<TypeAndName>(paramList);
	}
	
	/**
	 * 
	 * @return same to {@link #getTypeName()}.
	 */
	public ParsedTypeName getReturnTypeName(){
		return getTypeName();
	}
	
	@Override
	boolean allowModifier(Modifier modifier){
		switch (modifier){
		case PUBLIC:    return true;
		case PROTECTED: return true;
		case PRIVATE:   return true;
		case INTERNAL:  return true;
		case STATIC:    return true;
		case HOSTED:    return true;
		case FINAL:     return false;
		case CONST:     return false;
		case ABSTRACT:  return true;
		default: break;
		}
		
		throw new JSEError("Unknown modifier: " + modifier.name());
	}
}
