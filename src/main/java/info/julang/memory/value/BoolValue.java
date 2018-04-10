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

import info.julang.external.interfaces.JValueKind;
import info.julang.external.interfaces.IExtValue.IBoolVal;
import info.julang.memory.MemoryArea;
import info.julang.memory.value.operable.JAddable;
import info.julang.typesystem.JType;
import info.julang.typesystem.JTypeKind;
import info.julang.typesystem.basic.BoolType;
import info.julang.typesystem.jclass.builtin.JStringType;

/**
 * A value that stores a boolean.
 */
public class BoolValue extends BasicValue implements JAddable, IBoolVal {

	/**
	 * Create a new bool value with default value (false).
	 * @param memory
	 */
	public BoolValue(MemoryArea memory) {
		super(memory, BoolType.getInstance());
	}
	
	/**
	 * Create a new bool value with specified value.
	 * @param memory
	 * @param value
	 */
	public BoolValue(MemoryArea memory, boolean value) {
		super(memory, BoolType.getInstance());
		setValueInternal(value);
	}

	@Override
	public JType getType() {
		return BoolType.getInstance();
	}

	@Override
	public JValueKind getKind() {
		return JValueKind.BOOLEAN;
	}
	
	/**
	 * Get the actual value of boolean type.
	 * @return
	 */
	public boolean getBoolValue(){
		return getValueInternal();
	}

	/**
	 * Set the boolean value to false.
	 */
	@Override
	protected void initialize(JType type, MemoryArea memory){
		this._value = false;
	}
	
	/**
	 * Set the boolean value. Note this is only used only for internal operation.
	 * @param value
	 */
	void setBoolValue(boolean value){
		setValueInternal(value);
	}
	
	@Override
	public JValue replicateAs(JType type, MemoryArea memory) {
		if(type == this.getType()){//Use comparison by reference since all the basic types are singleton
			return new BoolValue(memory, getBoolValue());
		} else if(JStringType.isStringType(type)){
			return new StringValue(memory, String.valueOf(getBoolValue()));
		}
		
        // /*  BOOLEAN     	 CHAR           FLOAT          INTEGER       */
		// { EQUIVALENT,    UNCONVERTIBLE, UNCONVERTIBLE, CASTABLE,      },
		
		switch(type.getKind()){
		case INTEGER: return new IntValue(memory, getBoolValue() ? 1 : 0);
		case BYTE: return new ByteValue(memory, (byte)(getBoolValue() ? 1 : 0));
		default: return null;
		}
	}
	
	@Override
	public boolean assignTo(JValue assignee) throws IllegalAssignmentException {
		if(super.assignTo(assignee)){
			return true;
		}
		
		JType assigneeType = assignee.getType();
		
		if(assigneeType.isBasic()){
			switch(assigneeType.getKind()){
			case BOOLEAN: 
				BoolValue bv = (BoolValue) assignee;
				bv.setBoolValue(getBoolValue());
				break;
			default:
			}
		}
		
		return assignToResult(assigneeType);
	}
	
	@Override
	public boolean isEqualTo(JValue another){
		JType anotherType = another.getType();
		
		if(anotherType != null && anotherType.getKind() == JTypeKind.BOOLEAN){
			BoolValue val = (BoolValue) another;
			return this.getBoolValue() == val.getBoolValue();
		}
		
		return false;
	}

	// bool can + char, string
	@Override
	public JValue add(MemoryArea memory, JValue another) {
		JValueKind kind = ValueUtilities.getBuiltinKind(another);
		if(kind == JValueKind.STRING){
			StringValue sv = (StringValue)another;
			String result = this.getBoolValue() + sv.getStringValue();
			return new StringValue(memory, result);
		} else if(kind == JValueKind.CHAR) {
			String result = this.getBoolValue() + "" + ((CharValue)another).getCharValue();
			return new StringValue(memory, result);
		}
		return null;
	}
	
	//-------------------- Storage Layer --------------------//
	//-------- WARNING: _value must be NOT accessed ---------//
	//------- outside these two methods and initialize ------//
	
	private boolean _value;
	
	protected void setValueInternal(boolean value){
		this._value = value;
	}
	
	protected boolean getValueInternal(){
		return this._value;
	}
	
	//--------------------- Java Object ---------------------//
	
	@Override
	public String toString(){
		return String.valueOf(getBoolValue());
	}

	@Override
	public int hashCode(){
		return getBoolValue() ? 1 : 0;
	}
	
	@Override
	public boolean equals(Object anObject){
		if(anObject instanceof JValue){
			JValue av = ((JValue) anObject).deref();
			if(av.getKind() == JValueKind.BOOLEAN){
				boolean val2 = ((BoolValue)av).getBoolValue();
				return getBoolValue() == val2;
			}
		}
		
		return false;
	}
}
