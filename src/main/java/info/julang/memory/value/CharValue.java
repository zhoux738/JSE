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
import info.julang.external.interfaces.IExtValue.ICharVal;
import info.julang.memory.MemoryArea;
import info.julang.memory.value.operable.JAddable;
import info.julang.typesystem.JType;
import info.julang.typesystem.JTypeKind;
import info.julang.typesystem.basic.CharType;
import info.julang.typesystem.jclass.builtin.JStringType;

/**
 * A value that stores an integer.
 */
public class CharValue extends BasicValue implements JAddable, ICharVal {

	public CharValue(MemoryArea memory) {
		super(memory, CharType.getInstance());
	}
	
	public CharValue(MemoryArea memory, char value) {
		super(memory, CharType.getInstance());
		this.setValueInternal(value);
	}

	@Override
	public JType getType() {
		return CharType.getInstance();
	}

	@Override
	public JValueKind getKind() {
		return JValueKind.CHAR;
	}
	
	/**
	 * Get the actual value of character type.
	 * @return
	 */
	public char getCharValue(){
		return getValueInternal();
	}

	/**
	 * Set the char value. Note this is only used only for internal operation.
	 * @param value
	 */
	void setCharValue(char value) {
		this.setValueInternal(value);
	}
	
	/**
	 * Set the char value to \0.
	 */
	@Override
	protected void initialize(JType type, MemoryArea memory){
		this._value = '\0';
	}
	
	@Override
	public JValue replicateAs(JType type, MemoryArea memory) {
		if(type == this.getType()){//Use comparison by reference since all the basic types are singleton
			return new CharValue(memory, getValueInternal());
		} else if(JStringType.isStringType(type)){
			return new StringValue(memory, String.valueOf(getValueInternal()));
		}
		
        // /*  BOOLEAN     	  CHAR        FLOAT          INTEGER   BYTE      */
		// {   UNCONVERTIBLE, EQUIVALENT, UNCONVERTIBLE, CASTABLE, CASTABLE, },
		
		switch(type.getKind()){
		case INTEGER: return new IntValue(memory, (int) getValueInternal());
		case BYTE: return new ByteValue(memory, (byte) getValueInternal());
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
			case CHAR: 
				CharValue cv = (CharValue) assignee;
				cv.setCharValue(getValueInternal());
				break;
			default:
			}
		}
		
		return assignToResult(assigneeType);
	}
	
	@Override
	public boolean isEqualTo(JValue another){
		JType anotherType = another.getType();
		
		if(anotherType != null && anotherType.getKind() == JTypeKind.CHAR){
			CharValue val = (CharValue) another;
			return this.getValueInternal() == val.getCharValue();
		}
		
		return false;
	}
	
	// char can + bool, char, int or string
	@Override
	public JValue add(MemoryArea memory, JValue another) {		
		String result = null;
		switch(another.getKind()){
		case CHAR:
			result = this.getValueInternal() + "" + ((CharValue)another).getCharValue();
			break;
		case BOOLEAN:
			result = this.getValueInternal() + "" + ((BoolValue)another).getBoolValue();
			break;
		case FLOAT:
			result = this.getValueInternal() + "" + ((FloatValue)another).getFloatValue();
			break;
		case INTEGER:
			result = this.getValueInternal() + "" + ((IntValue)another).getIntValue();
			break;
		case BYTE:
			result = this.getValueInternal() + "" + ((ByteValue)another).getByteValue();
			break;
		case OBJECT:
			break;
		default:
			return null;
		}
		
		if(result == null){
			JValueKind kind = ValueUtilities.getBuiltinKind(another);
			if(kind == JValueKind.STRING){
				StringValue sv = (StringValue)another;
				result = this.getValueInternal() + sv.getStringValue();
			}
		}

		if(result != null){
			return new StringValue(memory, result);
		}
		
		return null;
	}
	
	//-------------------- Storage Layer --------------------//
	//-------- WARNING: _value must be NOT accessed ---------//
	//------- outside these two methods and initialize ------//
	
	private char _value;
	
	protected void setValueInternal(char value){
		this._value = value;
	}
	
	protected char getValueInternal(){
		return this._value;
	}
	
	//--------------------- Java Object ---------------------//
	
	@Override
	public String toString(){
		return String.valueOf(getCharValue());
	}

	@Override
	public int hashCode(){
		return getValueInternal();
	}
	
	@Override
	public boolean equals(Object anObject){
		if(anObject instanceof JValue){
			JValue av = ((JValue) anObject).deref();
			if(av.getKind() == JValueKind.CHAR){
				char val2 = ((CharValue)av).getValueInternal();
				return getValueInternal() == val2;
			}
		}
		
		return false;
	}
}
