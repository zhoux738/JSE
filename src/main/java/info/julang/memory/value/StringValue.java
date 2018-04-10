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

import info.julang.external.exceptions.JSEError;
import info.julang.external.interfaces.JValueKind;
import info.julang.external.interfaces.IExtValue.IStringVal;
import info.julang.interpretation.JIllegalCastingException;
import info.julang.memory.MemoryArea;
import info.julang.memory.value.operable.JAddable;
import info.julang.memory.value.operable.JCastable;
import info.julang.typesystem.AnyType;
import info.julang.typesystem.JType;
import info.julang.typesystem.jclass.builtin.JStringType;

/**
 * StringValue represents a string in Julian scripts. 
 * <p/>
 * StringValue is a special case of {@link ObjectValue}. While still wrapped in a {@link RefValue} like other 
 * object values, string values are replicated whenever they are assigned to other variables, passed as arguments
 * or return values. This nature makes string's manipulation behave more like that of basic values.
 * 
 * @author Ming Zhou
 */
public class StringValue extends ObjectValue implements JAddable, JCastable, IStringVal {
	
	private String value;
	
	/**
	 * Create a new empty string value ("")
	 * @param memory
	 */
	public StringValue(MemoryArea memory) {
		super(memory, JStringType.getInstance(), false);
		this.value = "";
		
		resetLength();
	}
	
	/**
	 * Create a new string value
	 * @param memory
	 * @param value
	 */
	public StringValue(MemoryArea memory, String value) {
		super(memory, JStringType.getInstance(), false);
		this.value = value;
		
		resetLength();
		
		if(memory!=null){
			memory.reallocate(this);		
		}
	}

	@Override
	public JValueKind getKind() {
		return JValueKind.OBJECT;
	}

	@Override
	public JType getType() {
		return JStringType.getInstance();
	}
	
	public String getStringValue(){
		return value;
	}
	
	/**
	 * Set the string value. Note this is only used for internal operation.
	 * @param value
	 */
	void setStringValue(String value){
		this.value = value;
		
		resetLength();
		
		MemoryArea memory = getMemoryArea();
		if(memory!=null){
			memory.reallocate(this);
		}
	}
	
	private void resetLength(){
		IntValue len = (IntValue) getMemberValue("length");
		len.setIntValue(value.length());
	}
	
	@Override
	public boolean assignTo(JValue assignee) {
		if(assignee.getType() == AnyType.getInstance()){
			StringValue sv = new StringValue(assignee.getMemoryArea(), getStringValue());
			RefValue rv = new RefValue(assignee.getMemoryArea(), sv);
			return rv.assignTo(assignee);
		}
		
		// If the assignee is a ref(sring), we delegate to super method.
		// The special treatment between ref(sring)-assignment is handled in RefValue.assignTo()
		
		return super.assignTo(assignee);
	}
	
	@Override
	public boolean compareToDeref(JValue anotherValue){
		if(((ObjectValue)anotherValue).getBuiltInValueKind() == JValueKind.STRING){
			// if the other value is a string, compare the string
			StringValue val = (StringValue) anotherValue;
			return this.value.equals(val.value);
		}
		
		return false;
	}
	
	@Override
	protected void initialize(JType type, MemoryArea memory) {
		super.initialize(type, memory);
	}
	
	@Override
	public JValueKind getBuiltInValueKind(){
		return JValueKind.STRING;
	}
	
	@Override
	public String toString(){
		return getStringValue();
	}
	
	@Override
	public int hashCode(){
		return value.hashCode();
	}
	
	@Override
	public boolean equals(Object anObject){
		if(anObject instanceof JValue){
			JValue av = ((JValue) anObject).deref();
			if(av.getType() == JStringType.getInstance()){
				String s = ((StringValue)av).value;
				return value.equals(s);
			}
		}
		
		return false;
	}
	
	// string can + bool, byte, char, int, float or string
	@Override
	public JValue add(MemoryArea memory, JValue another) {
		String result = null;
		switch(another.getKind()){
		case CHAR:
			result = this.value + ((CharValue)another).getCharValue();
			break;
		case BOOLEAN:
			result = this.value + ((BoolValue)another).getBoolValue();
			break;
		case FLOAT:
			result = this.value + ((FloatValue)another).getFloatValue();
			break;
		case INTEGER:
			result = this.value + ((IntValue)another).getIntValue();
			break;
		case BYTE:
			result = this.value + ((ByteValue)another).getByteValue();
			break;
		default:
			break;
		}
		
		if(result == null){
			JValueKind kind = ValueUtilities.getBuiltinKind(another);
			if(kind == JValueKind.STRING){
				StringValue sv = StringValue.dereference(another);
				result = this.value + sv.getStringValue();
			}
		}
		
		if(result == null && another.isNull()){
			result = this.value + RefValue.NULL.toScriptString();
		}

		if(result != null){
			return new StringValue(memory, result);
		}
		
		return null;
	}

	@Override
	public JValue to(MemoryArea memory, JType type) {
		if(type == this.getType()){
			return this;
		}
		
		JValue result = null;		
		switch(type.getKind()){
		case BOOLEAN:
			String sv = value.trim().toLowerCase();
			if("false".equals(sv)){
				result = new BoolValue(memory, false);	
			} else if("true".equals(sv)){
				result = new BoolValue(memory, true);	
			}
			break;
		case CHAR:
			char c = value.charAt(0);
			result = new CharValue(memory, c);
			break;
		case FLOAT:
			float f = Float.valueOf(value);
			result = new FloatValue(memory, f);
			break;
		case INTEGER:
			int i = Integer.valueOf(value);
			result = new IntValue(memory, i);
			break;	
		case BYTE:
			byte b = Byte.valueOf(value);
			result = new ByteValue(memory, b);
			break;		
		default:
		}
		
		if(result == null){
			throw new JIllegalCastingException(this.getType(), type);
		}
		
		return result;
	}
	
	/**
	 * Dereference the given value if it's a string value (possibly within an untyped and/or ref value).
	 * 
	 * @param val a {@link JValue}
	 * @return null if it's a null string
	 */
	public static StringValue dereference(JValue val){
		val = UntypedValue.unwrap(val);
		ObjectValue ov = RefValue.tryDereference(val);
		if(ov instanceof StringValue){
			return (StringValue) ov;
		}
		
		if(ov == RefValue.NULL){
			return null;
		}
		
		throw new JSEError("Expected a string value, but saw a " + ov.getType().getName());
	}
}
