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

import info.julang.execution.symboltable.ITypeTable;
import info.julang.external.binding.ExtValue;
import info.julang.external.binding.ExtValue.ExtBoolValue;
import info.julang.external.binding.ExtValue.ExtCharValue;
import info.julang.external.binding.ExtValue.ExtFloatValue;
import info.julang.external.binding.ExtValue.ExtIntValue;
import info.julang.external.binding.ExtValue.ExtObjValue;
import info.julang.external.binding.ExtValue.ExtStringValue;
import info.julang.external.exceptions.ExternalBindingException;
import info.julang.external.exceptions.JSEError;
import info.julang.external.interfaces.JValueKind;
import info.julang.interpretation.expression.Operator;
import info.julang.memory.MemoryArea;
import info.julang.typesystem.JArgumentException;
import info.julang.typesystem.JType;
import info.julang.typesystem.JTypeKind;
import info.julang.typesystem.basic.BasicType;
import info.julang.typesystem.jclass.ICompoundType;
import info.julang.typesystem.jclass.builtin.JEnumType;
import info.julang.typesystem.jclass.builtin.JObjectType;
import info.julang.typesystem.jclass.builtin.JStringType;

public final class ValueUtilities {

	/**
	 * Try to get an integer from the given value. Throws if it's not an int or byte.
	 * 
	 * @param val
	 * @param argName If the value is compatible with integer, throws an JArgumentException with this argument.
	 * @return An integer extracted from the given value.
	 */
	public static int getIntValue(JValue val, String argName){
		val = val.deref();
		switch(val.getKind()){
		case BYTE:
			return ((ByteValue)val).getByteValue();
		case INTEGER:
			return ((IntValue)val).getIntValue();
		default:
			break;
		}
		
		throw new JArgumentException(argName);
	}
	
	/**
	 * Get the built-in kind of this value, if it is an object value (or reference to an object value).
	 * 
	 * @param val
	 * @return
	 */
	public static JValueKind getBuiltinKind(JValue val){
		val = val.deref();
		if(val.getKind() == JValueKind.OBJECT){
			ObjectValue ov = (ObjectValue) val;
			return ov.getBuiltInValueKind();
		} else {
			return val.getKind();
		}
	}
	
	/**
	 * Replicate a value in the given Memory Area.
	 * 
	 * @param val the value to be replicated
	 * @param typ if null, use the type of <code>val</code>.
	 * @param memory the memory area in which the new value is to be stored
	 * @return a new value stored in the given memory area.
	 */
	public static JValue replicateValue(JValue val, JType typ, MemoryArea memory) {
		if (typ == null){
			typ = val.getType();
		}
		
		// Create a new value if the value is of basic or string type.
		if (typ == null){
			if(val.getKind() == JValueKind.REFERENCE){
				RefValue rval = (RefValue) val;
				if(rval.isNull()){
					return val;
				}
			}
			
			if (val == RefValue.NULL) {
				return RefValue.NULL;
			}
			throw new JSEError("Unable to convert value without type.");
		} else if (typ.isBasic()){
			BasicValue bval = (BasicValue) val;
			val = bval.replicateAs((BasicType)typ, memory);
		} else if (typ.getKind() == JTypeKind.CLASS){
			if(val.getKind() == JValueKind.REFERENCE){
				RefValue rval = (RefValue) val;
				if(rval.isNull()){
					val = RefValue.makeNullRefValue(memory, (ICompoundType)typ);
					return val;
				}
			}
			
			ObjectValue ov = RefValue.dereference(val);
			if (ov.getType() == JStringType.getInstance()){
				// Special treatment for string value: create a new one.
				StringValue strRep = new StringValue(memory, ((StringValue)ov).getStringValue());
				val = new RefValue(memory, strRep);
			} else {
				val = new RefValue(memory, ov);
			}
		} else if (typ.getKind() == JTypeKind.ANY){
			UntypedValue uval = (UntypedValue) val;
			val = new UntypedValue(memory, replicateValue(uval.getActual(), null, memory));
			return val;
		} else {
			throw new JSEError("Unable to convert value of type " + typ.getName(), Operator.class);
		}
		
		return val;
	}
	
	public static JValue convertFromExtValue(MemoryArea memory, String name, ExtValue value) throws ExternalBindingException {
		switch(value.getKind()){
		case BOOLEAN:
			ExtBoolValue zval = (ExtBoolValue)value;
			return new BoolValue(memory, zval.getValue());
		case CHAR:
			ExtCharValue cval = (ExtCharValue)value;
			return new CharValue(memory, cval.getValue());
		case FLOAT:
			ExtFloatValue fval = (ExtFloatValue)value;
			return new FloatValue(memory, fval.getValue());
		case INTEGER:
			ExtIntValue ival = (ExtIntValue)value;
			return new IntValue(memory, ival.getValue());
		case OBJECT:
			ExtObjValue oval = (ExtObjValue)value;
			switch(oval.getBuiltInValueKind()){
			case STRING:
				ExtStringValue sval = (ExtStringValue)oval;
				return new RefValue(memory, new StringValue(memory, sval.getValue()));
			default:
				break;
			}
		default:
			break;
		}
		
		throw ExternalBindingException.create(name, ExternalBindingException.Type.ILLEGAL_TYPE);
	}
	
	/**
	 * Make a default value to be allocated on the given memory area.
	 * 
	 * @param memory
	 * @param type
	 * @param isConst
	 * @param tt type table
	 * @return
	 */
	public static JValue makeDefaultValue(MemoryArea memory, JType type, boolean isConst) {
		JValueBase val = null;
		if(type != null){
			switch(type.getKind()){
			case BOOLEAN:
				val = new BoolValue(memory);
				break;
			case BYTE:
				val = new ByteValue(memory);
				break;
			case CHAR:
				val = new CharValue(memory);
				break;
			case FLOAT:
				val = new FloatValue(memory);
				break;
			case INTEGER:
				val = new IntValue(memory);
				break;
			case CLASS:
				// If it is an object, create a reference value and initialize to null.
				val = RefValue.makeNullRefValue(memory, (ICompoundType)type);
				
				if (type instanceof JEnumType) {
					// Special: if it's an Enum, initialize it with the default constant (the first enum entry).
					JEnumType etype = (JEnumType) type;
					TypeValue tVal = etype.getValue();
					
					RefValue rv = null;
					if (tVal == null) {
						// This happens when we initialize type value itself
						rv = new RefValue(
							memory,
							new EnumValue(memory, etype, etype.getDefaultOrdinal(), etype.getDefaultLiteral()));
						
					} else {
						rv = (RefValue) tVal.getMemberValue(etype.getDefaultLiteral());
					}
					
					rv.assignTo(val);
				}
				break;
			default:
				throw new JSEError("Trying to create value for a value of " + type.getKind().name());
			}
		} else {
			val = new UntypedValue(memory, makeDefaultValue(memory, JObjectType.getInstance(), isConst));
		}

		if(val != null){
			val.setConst(isConst);
			return val;
		}
		
		throw new JSEError("Cannot initialize an object whose type is unknown.", ValueUtilities.class);
	}
	
}
