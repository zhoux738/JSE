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
import info.julang.memory.MemoryArea;
import info.julang.typesystem.AnyType;
import info.julang.typesystem.JType;
import info.julang.typesystem.jclass.builtin.JStringType;

/**
 * A value that can be any type.
 * <p/>
 * Untyped value is implemented in a similar way as {@link RefValue}, where the actual value
 * is held in it. When assigned a new value, it simply replaces the held value with the new 
 * one. If the new value is also untyped, it will be unwrapped first.
 * <p/>
 * This however is different from RefValue in that it is completely transparent to users. Under
 * no circumstances will an UntypedValue holds null value. When a user checks the type of an
 * untyped value, it gets the type of the actual value being held.
 * 
 * @author Ming Zhou
 */
public class UntypedValue extends JValueBase {
	
	private JValue actualValue;
	
	/**
	 * Create a new UntypedValue from an actual value. If the actual value is untyped, unwrap it to get the actual value.
	 * An UntypedValue never holds another UntypedValue.
	 * 
	 * @param memory
	 * @param actualValue
	 */
	public UntypedValue(MemoryArea memory, JValue actualValue) {
		super(memory, null, false);
		this.actualValue = actualValue.getKind() != JValueKind.UNTYPED ? actualValue : ((UntypedValue)actualValue).actualValue;
	}
	
	@Override
	public boolean isBasic(){
		return true;
	}
	
	@Override
	public boolean isNull(){
		return actualValue.isNull();
	}

	@Override
	public JValueKind getKind() {
		return JValueKind.UNTYPED;
	}

	/**
	 * Always return {@link AnyType}.
	 */
	@Override
	public JType getType() {
		return AnyType.getInstance();
	}

	@Override
	public boolean isEqualTo(JValue another) {
		return actualValue.equals(another);
	}

	@Override
	public JValue deref(){
		return actualValue.deref();
	}
	
	@Override
	protected void initialize(JType type, MemoryArea memory) {
		// NOP
	}
	
	@Override
	public boolean assignTo(JValue assignee) throws IllegalAssignmentException {
		if(assignee.isConst()){
			throw new AttemptToChangeConstException();
		}
		
		if(assignee.getType() == AnyType.getInstance()){
			JValue rep = null;
			if(actualValue.isBasic()){
				rep = ((BasicValue)actualValue).replicateAs(actualValue.getType(), assignee.getMemoryArea());
			} else {
				rep = RefValue.tryDereference(actualValue);
				if (rep.getType() == JStringType.getInstance()){
					rep = new RefValue(assignee.getMemoryArea(), (StringValue)rep);
				} else if (rep == RefValue.NULL){
					rep = null;
				}
			}
			
			((UntypedValue) assignee).setActual(rep != null ? rep: actualValue);
			return true;
		} else {
			return actualValue.assignTo(assignee);
		}
	}

	void setActual(JValue val) {
		actualValue = val;
	}

	public JValue getActual() {
		return actualValue;
	}
	
	@Override
	public String toString(){
		return "<untyped> " + actualValue.toString();
	}

	@Override
	public int hashCode(){
		return actualValue.hashCode();
	}
	
	@Override
	public boolean equals(Object anObject){	
		if(anObject instanceof JValue){
			JValue av = ((JValue) anObject).deref();
			return actualValue.equals(av);
		}
		
		return false;
	}
	
	/**
	 * Unwrap another value only if it is an UntypedValue.
	 * @param lval
	 * @return
	 */
	public static JValue unwrap(JValue val) {
		if(val.getKind() == JValueKind.UNTYPED){
			return ((UntypedValue)val).actualValue;
		} else {
			return val;			
		}
	}

}
