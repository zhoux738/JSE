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
import info.julang.external.interfaces.IExtValue.IEnumVal;
import info.julang.interpretation.JIllegalCastingException;
import info.julang.memory.MemoryArea;
import info.julang.memory.value.operable.JCastable;
import info.julang.typesystem.JType;
import info.julang.typesystem.jclass.builtin.JEnumBaseType;
import info.julang.typesystem.jclass.builtin.JEnumType;
import info.julang.typesystem.jclass.builtin.JStringType;

/**
 * A value of enum type.
 * <p/>
 * Note enum type is class type too. An enum value contains two fields: <b>ordinal</b> and <b>literal</b>.
 * This class provides easy-to-use API for accessing to these two fields, but under the hood
 * they are still ordinary fields of a class.
 * <p/>
 * In Julian, Enum value can be explicitly cast to string and int value.
 * @author Ming Zhou
 */
public class EnumValue extends ObjectValue implements JCastable, IEnumVal {

	private JEnumType enumType;
	
	public EnumValue(MemoryArea memory, JEnumType type, int ordinal, String literal) {
		super(memory, type, false);
		setLiteral(literal, memory);
		setOrdinal(ordinal);
	}

	@Override
	public JValueKind getBuiltInValueKind(){
		return JValueKind.ENUM;
	}

	@Override
	public boolean isEqualTo(JValue another) {
		if(another != RefValue.NULL){
			another = RefValue.dereference(another);
		}
		if(getType() == another.getType()){
			EnumValue ev = (EnumValue) another;
			return ev.getOrdinal() == this.getOrdinal();
		}
		return false;
	}

	@Override
	protected void initialize(JType type, MemoryArea memory) {
		super.initialize(type, memory);
		enumType = (JEnumType) type;
	}
	
	@Override
	protected boolean shouldSealConst(){
		return false;
	}

	/**
	 * Get <code><font color="green">Enum</font>.ordinal</code> (<font color="green">int</font> type).
	 * @return
	 */
	public int getOrdinal() {
		return ((IntValue)getMemberValue(JEnumBaseType.FIELD_ORDINAL)).getIntValue();
	}

	private void setOrdinal(int ordinal) {
		((IntValue)getMemberValue(JEnumBaseType.FIELD_ORDINAL)).setIntValue(ordinal);
		((JValueBase)getMemberValue(JEnumBaseType.FIELD_ORDINAL)).setConst(true);
	}

	/**
	 * Get <code><font color="green">Enum</font>.literal</code> (<font color="green">string</font> type).
	 * @return
	 */
	public String getLiteral() {
		return (StringValue.dereference(getMemberValue(JEnumBaseType.FIELD_LITERAL))).getStringValue();
	}

	private void setLiteral(String literal, MemoryArea memory) {
		RefValue rv = (RefValue) getMemberValue(JEnumBaseType.FIELD_LITERAL);
		TempValueFactory.createTempStringValue(literal).assignTo(rv);
		((JValueBase)getMemberValue(JEnumBaseType.FIELD_LITERAL)).setConst(true);
	}
	
	public JEnumType getEnumType() {
		return enumType;
	}
	
	@Override
	public JValue to(MemoryArea memory, JType type) {
		if(type == this.getType()){
			return this;
		}
		
		JValue result = null;		
		switch(type.getKind()){
		case INTEGER:
			int i = Integer.valueOf(getOrdinal());
			result = new IntValue(memory, i);
			break;
		case CLASS:
			if (type == JStringType.getInstance()){
				result = new StringValue(memory, getLiteral());
			}			
			break;
		default:
		}
		
		if(result == null){
			throw new JIllegalCastingException(this.getType(), type);
		}
		
		return result;
	}
}
