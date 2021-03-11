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

package info.julang.interpretation.expression.operand;

import info.julang.memory.value.FuncValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.MethodGroupValue;
import info.julang.memory.value.TempValueFactory;
import info.julang.typesystem.JType;

/**
 * A member operand that represents a member of certain object ({@link #ofObject()}).
 * 
 * @author Ming Zhou
 */
public class InstMemberOperand extends MemberOperand {
	
	private JValue ofObject;
	private FuncValue extMethods;
	
	/**
	 * The object whose member is wrapped by this operand.
	 * 
	 * @return
	 */
	public JValue ofObject() {
		return ofObject;
	}
	
	/**
	 * The extension methods for the same name.
	 * 
	 * @return could be {@link info.julang.memory.value.MethodValue MethodValue} or 
	 * {@link info.julang.memory.value.MethodGroupValue MethodGroupValue};
	 * null if no extension is installed to the type of this object.
	 */
	public FuncValue getExtensionMethods() {
		return extMethods;
	}
	
	public InstMemberOperand(JValue value, JType type, FuncValue extMethodGroup, JValue ofObject, String memberName){
		super(value != null // Create an empty method group value since operand must hold a non-null value.
				? value 
				: TempValueFactory.createEmptyMethodGroupValue(memberName, type),
			  memberName);
		this.ofObject = ofObject;
		this.extMethods = extMethodGroup;
	}

	@Override
	public OperandKind getKind() {
		return OperandKind.IMEMBER;
	}
	
	@Override
	public JValue getValue() {
		return extMethods == null ? super.getValue() : extMethods;
	}
	
	public JValue getMemberValue() {
		return super.getValue();
	}
}
