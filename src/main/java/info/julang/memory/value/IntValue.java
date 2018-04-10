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

import info.julang.external.interfaces.IExtValue.IIntVal;
import info.julang.external.interfaces.JValueKind;
import info.julang.memory.MemoryArea;
import info.julang.memory.value.operable.JAddable;
import info.julang.typesystem.JType;
import info.julang.typesystem.basic.IntType;
import info.julang.typesystem.jclass.builtin.JStringType;

/**
 * A value that stores an integer.
 */
public class IntValue extends BasicValue implements JAddable, IIntVal {

	/**
	 * Create a new int value with default value (0)
	 * @param memory
	 */
	public IntValue(MemoryArea memory) {
		super(memory, IntType.getInstance());
	}
	
	/**
	 * Create a new int value with specified value
	 * @param memory
	 * @param value
	 */
	public IntValue(MemoryArea memory, int value) {
		super(memory, IntType.getInstance());
		setValueInternal(value);
	}

	@Override
	public JType getType() {
		return IntType.getInstance();
	}

	@Override
	public JValueKind getKind() {
		return JValueKind.INTEGER;
	}
	
	/**
	 * Get the actual value of integer type.
	 * @return
	 */
	public int getIntValue(){
		return getValueInternal();
	}
	
	/**
	 * Set the integer value. Note this is used only for internal operation.
	 * @param value
	 */
	void setIntValue(int value){
		setValueInternal(value);
	}

	/**
	 * Set the int value to 0.
	 */
	@Override
	protected void initialize(JType type, MemoryArea memory){
		this._value = 0;
	}
	
	@Override
	public JValue replicateAs(JType type, MemoryArea memory) {
		if(type == this.getType()){//Use comparison by reference since all the basic types are singleton
			return new IntValue(memory, getValueInternal());
		} else if(JStringType.isStringType(type)){
			return new StringValue(memory, String.valueOf(getValueInternal()));
		}
		
        //   BOOLEAN     	CHAR           FLOAT          INTEGER       BYTE
		// { CASTABLE,      CASTABLE,      PROMOTED,      EQUIVALENT,   DEMOTED }
		switch(type.getKind()){
		case FLOAT: return new FloatValue(memory, getValueInternal());
		case BYTE: return new ByteValue(memory, (byte)getValueInternal());
		case CHAR: return new CharValue(memory, convertToChar());
		case BOOLEAN: return new BoolValue(memory, convertToBoolean());
		default:
		}
		
		return null;
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
				bv.setBoolValue(convertToBoolean());
				break;
			case CHAR: 
				CharValue cv = (CharValue) assignee;
				cv.setCharValue(convertToChar());
				break;
			case BYTE: 
				ByteValue byv = (ByteValue) assignee;
				byv.setByteValue((byte)getValueInternal());
				break;
			case INTEGER: 
				IntValue iv = (IntValue) assignee;
				iv.setIntValue(getValueInternal());
				break;
			case FLOAT: 
				FloatValue fv = (FloatValue) assignee;
				fv.setFloatValue(getValueInternal());
				break;
			default:
			}
		}
		
		return assignToResult(assigneeType);
	}
	
	@Override
	public boolean isEqualTo(JValue another){
		JType anotherType = another.getType();
		
		if (anotherType != null){
			switch(anotherType.getKind()){
			case BYTE:
				ByteValue byv = (ByteValue) another;
				return this.getValueInternal() == byv.getByteValue();
			case INTEGER:
				IntValue iv = (IntValue) another;
				return this.getValueInternal() == iv.getIntValue();
			case FLOAT:
				FloatValue fv = (FloatValue) another;
				return this.getValueInternal() == fv.getFloatValue();
			default:
				break;
			}
		}
		
		return false;
	}
	
	private boolean convertToBoolean(){
		return getValueInternal() != 0 ? true : false;
	}
	
	private char convertToChar(){
		int v = getValueInternal();
		return v >= 0 && v <= 127 ? (char)v : '\0';
	}
	
	// int can + char, byte, int, float or string
	@Override
	public JValue add(MemoryArea memory, JValue another) {
		String result = null;
		switch(another.getKind()){
		case CHAR:
			result = this.getValueInternal() + "" + ((CharValue)another).getCharValue();
			break;
		case FLOAT:
			float fresult = this.getValueInternal() + ((FloatValue)another).getFloatValue();
			return new FloatValue(memory, fresult);
		case BYTE:
			int bresult = this.getValueInternal() + ((ByteValue)another).getByteValue();
			return new IntValue(memory, bresult); // sum of two bytes promotes to int 
		case INTEGER:
			int iresult = this.getValueInternal() + ((IntValue)another).getIntValue();
			return new IntValue(memory, iresult);
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
	
	private int _value;
	
	protected void setValueInternal(int value){
		this._value = value;
	}
	
	protected int getValueInternal(){
		return this._value;
	}
	
	//--------------------- Java Object ---------------------//

	@Override
	public String toString(){
		return String.valueOf(getIntValue());
	}
	
	@Override
	public int hashCode(){
		return getValueInternal();
	}
	
	@Override
	public boolean equals(Object anObject){
		if(anObject instanceof JValue){
			JValue av = ((JValue) anObject).deref();
			if(av.getKind() == JValueKind.INTEGER){
				int val2 = ((IntValue)av).getValueInternal();
				return getValueInternal() == val2;
			}
		}
		
		return false;
	}
}
