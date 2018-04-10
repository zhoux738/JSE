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

import java.util.Map;

import info.julang.JSERuntimeException;
import info.julang.interpretation.errorhandling.KnownJSException;
import info.julang.typesystem.jclass.ClassMemberLoaded;
import info.julang.typesystem.jclass.ClassMemberMap;
import info.julang.typesystem.jclass.JClassType;
import info.julang.util.OneOrMoreList;

/**
 * The exception thrown when a non-existing member of some type is referenced.
 *
 * @author Ming Zhou
 */
public class UnknownMemberException extends JSERuntimeException {

	private static final long serialVersionUID = -89458808254036355L;
	
	public UnknownMemberException(JType type, String mName, boolean isStatic) {
		super(createMsg(type, mName, isStatic));
	}

	private static String createMsg(JType type, String mName, boolean isStatic) {
		String tName = type.getName();
		String extInfo = "";
		if (type instanceof JClassType) {			
			JClassType jct = (JClassType)type;
			ClassMemberMap cmm = jct.getMembers(isStatic);
			StringBuilder sb = new StringBuilder();
			sb.append("The following are found: ");
			for (Map<String, OneOrMoreList<ClassMemberLoaded>> map : cmm.getDefinedMembers()){
				for (String s : map.keySet()){
					sb.append(s);
					sb.append(", ");
				}
			}
			
			sb.setLength(sb.length() - 2);
			extInfo = sb.toString();
		}
		
		return "No member of name \"" + mName + "\" is found in type " + tName + ". " + extInfo;
	}
	
	@Override
	public KnownJSException getKnownJSException() {
		return KnownJSException.UnknownMember;
	}
}
