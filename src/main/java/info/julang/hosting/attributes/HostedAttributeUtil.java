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

package info.julang.hosting.attributes;

import info.julang.external.exceptions.JSEError;
import info.julang.memory.value.AttrValue;
import info.julang.typesystem.jclass.JClassMember;
import info.julang.typesystem.jclass.annotation.JAnnotation;

public class HostedAttributeUtil {

	public static String BRIDGED = "System.Bridged";
	
	public static String MAPPED = "System.Mapped";
	
	public static String UNTYPED = "System.Untyped";
	
	/**
	 * Given an annotation, determines if it is a hosted attribute and returns the name if so.
	 * @param ja
	 * @return {@link HostedAttributeType#BRIDGED} or {@link HostedAttributeType#MAPPED} or 
	 * null if the annotation is not a hosted attribute.
	 */
	public static HostedAttributeType getHostedType(JAnnotation ja){
		String name = ja.getAttributeType().getName();
		if(BRIDGED.equals(name)){
			return HostedAttributeType.BRIDGED;
		} else if (MAPPED.equals(name)){
			return HostedAttributeType.MAPPED;
		} else if (UNTYPED.equals(name)){
			return HostedAttributeType.UNTYPED;
		} else {
			return null;
		}
	}
	
	/**
	 * Make a hosted attribute using data from the given attribute value, based on the attribute type.
	 * 
	 * @param type
	 * @param av
	 * @param member the class member this attribute decorates
	 * @return
	 */
	public static HostedAttribute makeHostedAttribute(HostedAttributeType type, AttrValue av, JClassMember member){
		switch(type){
		case BRIDGED:
			return new BridgedHostedAttribute(av, member);
		case MAPPED:
			return new MappedHostedAttribute(av);
		default:
			throw new JSEError("Unknown hosted attribute type: " + type);
		}
	}
	
	/**
	 * @return the mapped platform class name if the attribute is a <code><font color="green">System.Mapped</font><code> instance. 
	 * null if the attribute is any other kind.
	 */
	public static String getMappedClassName(AttrValue av){
		if (MAPPED.equals(av.getType().getName())){
			MappedHostedAttribute mha = new MappedHostedAttribute(av);
			return mha.getClassName();
		}
		
		return null;
	}
	
}
