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

import info.julang.execution.Executable;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.expression.Operand;
import info.julang.memory.value.TypeValue;
import info.julang.typesystem.JType;
import info.julang.typesystem.jclass.ExecutableType;

/**
 * A type operand refers to a (class) type. 
 *
 * @author Ming Zhou
 */
public class TypeOperand extends Operand {
	
	private TypeValue value;
	
	private JType type;
	
	/**
	 * Create a new TypeOperand based on the type provided. If it is basic type the created instance
	 * will not contain TypeValue.
	 * 
	 * @param context
	 * @param type
	 * @return
	 */
	public static TypeOperand createNew(Context context, JType type){
		if(type.isBasic()){
			return new TypeOperand(type);
		} else {
			TypeValue tv = context.getTypTable().getValue(type.getName());
			return new TypeOperand(tv);
		}
	}
	
	public TypeOperand(TypeValue value){
		this.value = value;
		this.type = value.getType();
	}
	
	public TypeOperand(JType type){
		this.type = type;
	}
	
	/**
	 * Get the referenced type.
	 * 
	 * @return
	 */
	public JType getType() {
		return type;
	}
	
	/**
	 * Get the runtime value for the referenced type.
	 * <p>
	 * This will return null if it is a basic type, which doesn't have a runtime value.
	 * 
	 * @return
	 */
	public TypeValue getValue() {
		return value;
	}

	@Override
	public OperandKind getKind() {
		return OperandKind.TYPE;
	}
	
	/**
	 * Get the executable instance of this type, if the type is indeed executable (a function/lambda...).
	 * 
	 * @return null if this type is not executable
	 */
	public Executable getExecutable(){
		if(type instanceof ExecutableType){
			return ((ExecutableType) type).getExecutable();
		}
		return null;
	}
	
}
