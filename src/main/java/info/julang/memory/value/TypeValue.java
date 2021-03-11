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

package info.julang.memory.value;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import info.julang.execution.threading.ThreadRuntime;
import info.julang.external.exceptions.JSEError;
import info.julang.external.interfaces.JValueKind;
import info.julang.memory.MemoryArea;
import info.julang.typesystem.JType;
import info.julang.typesystem.JTypeKind;
import info.julang.typesystem.jclass.JClassType;
import info.julang.typesystem.jclass.MemberKey;
import info.julang.typesystem.jclass.builtin.JTypeStaticDataType;

/**
 * A value holding per-class data of class type, namely static fields.
 * 
 * @author Ming Zhou
 */
public class TypeValue extends ObjectValue {

	public TypeValue(MemoryArea memory, JType type) {
		super(memory, type, false);
	}

	@Override
	protected void initialize(JType type, MemoryArea memory) {	
		this.type = type;
		
		if (type.getKind() == JTypeKind.CLASS && type instanceof JClassType){
			// Initialize using ClassMemberMap
			members = new ObjectMemberStorage(
				memory, 
				(JClassType)type, 
				null,			// static members
				true,			// include method members
				false);			// do not seal the const value yet		
		} else {
			// If this is a non-class type, use an empty storage map
			// (need to change this if we would ever allow defining static members on interface)
			members = ObjectMemberStorage.makeEmptyObjectMemberStorage();
		}
	}

	@Override
	public JValueKind getKind() {
		return JValueKind.TYPE;
	}

	/**
	 * This will only return {@link JTypeStaticDataType}. To get the actual class type this type value
	 * represents, call {@link #getValueType()} instead.
	 */
	@Override
	public JType getType() {
		return JTypeStaticDataType.getInstance();
	}
	
	/**
	 * Get the class type for which this type value represents.
	 */
	public JType getValueType() {
		return type;
	}
	
	/**
	 * Set a member as const.
	 * 
	 * @param name
	 */
	public void setMemberConst(String name){
		JValueBase valBase = (JValueBase) getMemberValue(name);
		valBase.setConst(true);
	}
	
	private List<AttrValue> typeAttrs;
	
	private Map<MemberKey, List<AttrValue>> typeMemberAttrs;
	
	/**
	 * Add an attribute value to the class type.
	 * 
	 * @param attr
	 */
	public void addClassAttrValue(AttrValue attr){
		if(typeAttrs == null){
			typeAttrs = new ArrayList<AttrValue>();
		}
		typeAttrs.add(attr);
	}
	
	/**
	 * Add an attribute value to a class member.
	 * 
	 * @param key
	 * @param attr
	 */
	public void addMemberAttrValue(MemberKey key, AttrValue attr){
		// See note for getMemberValue on the missing checks.
		if(typeMemberAttrs == null){
			typeMemberAttrs = new HashMap<MemberKey, List<AttrValue>>();
		}
		List<AttrValue> vals = typeMemberAttrs.get(key);
		if(vals == null){
			vals = new ArrayList<AttrValue>();
			typeMemberAttrs.put(key, vals);
		}
		vals.add(attr);
	}

	/**
	 * Get all the attribute values on this class.
	 * 
	 * @return null if no attributes on this class.
	 */
	public List<AttrValue> getClassAttrValues() {
		return typeAttrs;
	}

	/**
	 * Get all the attribute values on the specified number of this class.
	 * 
	 * @return null if no attributes on this member.
	 */
	public List<AttrValue> getMemberAttrValues(MemberKey mkey) {
		// See note for getMemberValue on the missing checks.
		if(typeMemberAttrs == null){
			return null;
		}
		return typeMemberAttrs.get(mkey);
	}
	
	@Override
	public boolean assignTo(JValue assignee) {
		if(super.assignTo(assignee)){
			return true;
		}
		
		// This should never happen.
		throw new JSEError("Attempted to assign a type value.");
	}
	
	@Override
	public boolean isEqualTo(JValue another){
		if(another instanceof TypeValue){
			TypeValue tv = (TypeValue) another;
			// For now, compare by JVM-reference. This may be changed in future.
			return type == tv.getType();
		}

		return false;
	}
	
	//--------------------- storage of System.Type instance ---------------------//

	private ObjectValue typeObject;
	
	/**
	 * Get <font color="green"><code>System.Type</code></font> object for this type. 
	 * This object will be created the first time this method is called.
	 * 
	 * @param runtime
	 */
	public ObjectValue getScriptTypeObject(ThreadRuntime runtime) {
		if (typeObject == null) {
			synchronized(TypeValue.class){
				if (typeObject == null) {					
					typeObject = JClassType.createScriptTypeObject(runtime, this.type);
				}
			}
		}
		
		return typeObject;
	}
}
