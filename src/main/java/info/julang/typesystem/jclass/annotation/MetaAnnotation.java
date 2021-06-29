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

package info.julang.typesystem.jclass.annotation;

import java.util.List;

import info.julang.execution.symboltable.ITypeTable;
import info.julang.memory.value.AttrValue;
import info.julang.memory.value.BoolValue;
import info.julang.memory.value.IntValue;
import info.julang.memory.value.TypeValue;
import info.julang.typesystem.jclass.MemberType;
import info.julang.typesystem.jclass.builtin.JAttributeType;

/**
 * An on-platform representation of <code style="color:green">System.AttributeType</code><pre><code> module System;
 * 
 * class AttributeTarget {
 *   static const int CLASS       = 0b000001;
 *   static const int ATTRIBUTE   = 0b000010;
 *   static const int ENUM        = 0b000100;
 *   static const int METHOD      = 0b001000;
 *   static const int FIELD       = 0b010000;
 *   static const int CONSTRUCTOR = 0b100000;
 * }
 * 
 * attribute AttributeType {
 *   bool allowMultiple;
 *   int target;
 * }
 * </code></pre>
 * Implementation node: the data and logic of this class must be synchronized with what is defined in 
 * <code style="color:green">System.AttributeType</code>.
 * 
 * @author Ming Zhou
*/
public class MetaAnnotation {

	public static final String AttributeTypeName = "System.AttributeType";
	
	public static final String Field_Bool_AllowMultiple = "allowMultiple";
	public static final String Field_Int_Target = "target";
	
	/**
	 * Target types for meta annotation.
	 */
	public static class Target {
		public static final int CLASS       = 0b000001;
		public static final int ATTRIBUTE   = 0b000010;
		public static final int ENUM        = 0b000100;
		public static final int METHOD      = 0b001000;
		public static final int FIELD       = 0b010000;
		public static final int CONSTRUCTOR = 0b100000;
	}
	
	private int targets;
	
	private boolean allowMultiple;
	
	MetaAnnotation(int targets, boolean allowMultuple){
		this.targets = targets;
		this.allowMultiple = allowMultuple;
	}

	/**
	 * Check if the attribute is applicable to any of the specified target type.
	 * 
	 * @param target Any static member of {@link MetaAnnotation.Target}, or a bitwise-ORed
	 * combination of any of them.
	 * @return true if at least one of specified target types is permitted.
	 */
	public boolean isApplicableTo(int target) {
		return (targets & target) != 0;
	}

	public boolean isAllowMultiple() {
		return allowMultiple;
	}
	
	public static final MetaAnnotation defaultMetaAnnotation = new MetaAnnotation(Target.CLASS, false);
	
	public static int convertMemberTypeToTarget(MemberType mtype){
		switch(mtype){
		case FIELD:
			return Target.FIELD;
		case METHOD:
			return Target.METHOD;
		case CONSTRUCTOR:
			return Target.CONSTRUCTOR;
		default:
			// Ignore
		}
		
		return 0;
	}
	
	/**
	 * Get an instance of {@link MetaAnnotation} from a defined {@link JAttributeType}.
	 * 
	 * @param tt
	 * @param atype
	 * @return
	 */
	public static MetaAnnotation getFromAttributeType(ITypeTable tt, JAttributeType atype){
		TypeValue tv = tt.getValue(atype.getName());
		List<AttrValue> attrvs = tv.getClassAttrValues();
		
		if(attrvs != null && attrvs.size() > 0){
			for(AttrValue avalue : attrvs){
				if(AttributeTypeName.equals(avalue.getType().getName())){
					return convertToMetaAnnotation(avalue);
				}
			}
		}
		
		return defaultMetaAnnotation;
	}

	private static MetaAnnotation convertToMetaAnnotation(AttrValue avalue) {
		BoolValue bv = (BoolValue) avalue.getMemberValue(Field_Bool_AllowMultiple);
		IntValue iv = (IntValue) avalue.getMemberValue(Field_Int_Target);
		return new MetaAnnotation(iv.getIntValue(), bv.getBoolValue());
	}
	
}
