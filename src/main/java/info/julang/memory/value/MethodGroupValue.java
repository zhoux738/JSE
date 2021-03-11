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
import info.julang.memory.MemoryArea;
import info.julang.typesystem.JType;
import info.julang.typesystem.VoidType;
import info.julang.typesystem.jclass.builtin.JMethodGroupType;
import info.julang.typesystem.jclass.builtin.JMethodType;

/**
 * A wrapper value that contains all the method values of same name in a given type.
 * 
 * @author Ming Zhou
 */
public class MethodGroupValue extends FuncValue implements IMethodValue {

	private MethodValue[] methodVals;
	
	public MethodGroupValue(MemoryArea memory, MethodValue[] methodVals) {
		super(memory, makeMethodGroupType(methodVals), JValueKind.METHOD_GROUP, false);
		this.methodVals = methodVals;
	}
	
	protected MethodGroupValue(MemoryArea memory, JMethodGroupType metGrpTyp) {
		super(memory, metGrpTyp, JValueKind.METHOD_GROUP, false);
	}
	
	private static JMethodGroupType makeMethodGroupType(MethodValue[] methodVals) {
		JMethodType first = methodVals[0].getMethodType();
		JType containingTyp = first.getContainingType();
		String name = first.getName();
		JType retTyp = first.getReturnType();
		JMethodType[] typs = new JMethodType[methodVals.length];
		typs[0] = first;
		
		for(int i = 1; i < methodVals.length; i++){
			JMethodType typ = methodVals[0].getMethodType();
			if(typ.getContainingType() != containingTyp){
				throw new JSEError("Cannot create a method group value with methods belonging to different types.");
			}
			if(!typ.getName().equals(name)){
				throw new JSEError("Cannot create a method group value with methods of different name.");
			}
			if(typ.getReturnType() != retTyp){
				throw new JSEError("Cannot create a method group value with methods of different return types.");
			}
			
			typs[i] = typ;
		}
		
		JMethodGroupType mgt = new JMethodGroupType(name, retTyp, containingTyp, typs);
		return mgt;
	}

	public MethodValue[] getMethodValues(){
		return methodVals;
	}
	
	//------------ IMethodValue ------------//
	
	public boolean isStatic(){
		return methodVals[0].isStatic();
	}
	
	public JValue getThisValue() {
		return methodVals[0].getThisValue();
	}

	public JValueKind getFuncValueKind() {
		return JValueKind.METHOD_GROUP;
	}
	
	//------------ Special: an empty group ------------//
	
	public static EmptyMethodGroupValue createEmptyMethodGroupValue(MemoryArea memory, String methodName, JType containingType) {
		return new EmptyMethodGroupValue(memory, methodName, containingType);
	}
	
	private static class EmptyMethodGroupValue extends MethodGroupValue {
		
		private EmptyMethodGroupValue(MemoryArea memory, String methodName, JType containingType) {
			super(memory, new JMethodGroupType(methodName, VoidType.getInstance(), containingType, new JMethodType[0]));
		}
		
		@Override
		public MethodValue[] getMethodValues(){
			return new MethodValue[0];
		}

		@Override
		public boolean isStatic(){
			return true;
		}

		@Override
		public JValue getThisValue() {
			return null;
		}
	}
}
