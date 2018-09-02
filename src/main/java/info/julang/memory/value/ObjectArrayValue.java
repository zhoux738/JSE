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
import info.julang.execution.threading.ThreadRuntime;
import info.julang.external.exceptions.JSEError;
import info.julang.external.interfaces.JValueKind;
import info.julang.interpretation.internal.FuncCallExecutor;
import info.julang.memory.MemoryArea;
import info.julang.memory.value.operable.ValueComparator;
import info.julang.typesystem.AnyType;
import info.julang.typesystem.JType;
import info.julang.typesystem.JTypeKind;
import info.julang.typesystem.jclass.ICompoundType;
import info.julang.typesystem.jclass.JClassType;
import info.julang.typesystem.jclass.JInterfaceType;
import info.julang.typesystem.jclass.JParameter;
import info.julang.typesystem.jclass.builtin.JArrayType;
import info.julang.typesystem.jclass.builtin.JMethodType;
import info.julang.typesystem.jclass.builtin.JObjectType;
import info.julang.typesystem.jclass.builtin.JStringType;

import java.util.HashMap;
import java.util.Map;

public class ObjectArrayValue extends ArrayValue {

	private JValue[] values;
	
	ObjectArrayValue(MemoryArea memory, ITypeTable tt, JType elementType, int[] lengths) {
		super(memory, tt, elementType, lengths);
	}
	
	ObjectArrayValue(MemoryArea memory, JType type) {
		super(memory, type, true);
	}
	
	private ObjectArrayValue(MemoryArea memory, ITypeTable tt, JType elementType, JValue[] values){
		super(memory, JArrayType.createJArrayType(tt, elementType, false), false); // the super constructor calls initialize()
		
		IntValue len = (IntValue) getMemberValue("length");
		len.setIntValue(values.length);
		
		this.values = values;
		
		if(memory!=null){
			memory.reallocate(this);		
		}
	}
	
	@Override
	public boolean isBasicArray(){
		return false;
	}
	
	@Override
	protected void initializeArray(MemoryArea memory, ITypeTable tt, JType elementType, int[] dimensions){
		values = initializeArray(memory, tt, elementType, dimensions, 0);
	}
	
	private JValue[] initializeArray(MemoryArea memory, ITypeTable tt, JType elementType, int[] dimensions, int dIndex){
		// length for this dimension
		int length = dimensions[dIndex];
		// Index of last dimension
		int lastDem = dimensions.length - 1;
		// Initialize values at current dimension
		JValue[] arrVals = new JValue[length];
		
		if(dIndex < lastDem){
			// The element is array too. Initialize recursively.
		    boolean done = false;
			int nextIndex = dIndex + 1;
			if (nextIndex == lastDem){
				// If we are about to create the array at the last dimension, must handle two special cases.
			    
			    if (dimensions[lastDem] == ArrayValueFactory.UndefinedLength) {
			        JArrayType penultimateDimTyp = tt.getArrayType(elementType);
	                // (1) If the last dimension is left undefined, initialize the current array with null.
	                for(int i=0; i<length; i++){
	                    arrVals[i] = RefValue.makeNullRefValue(memory, penultimateDimTyp);
	                }
	                done = true;
			    } else if (elementType.isBasic()) {
			        // (2) If the element type is basic, we must use appropriate basic value array.
	                for(int i=0; i<length; i++){
	                    ArrayValue av = ArrayValueFactory.createArrayValue(memory, tt, elementType, dimensions[lastDem]);
	                    arrVals[i] = new RefValue(memory, av);
	                }
                    done = true;
			    }
			} 

			if (!done) {
				for(int i=0; i<length; i++){
					JValue[] subArrVals = initializeArray(memory, tt, elementType, dimensions, nextIndex);
					arrVals[i] = new RefValue(memory, new ObjectArrayValue(memory, tt, elementType, subArrVals));
				}
			}
		} else {
			// We are at the last dimension.
			JTypeKind kind = elementType.getKind();
			if (kind != JTypeKind.CLASS && kind != JTypeKind.ANY){
				throw new JSEError("Trying to create array of unsupported element type.");
			}
			
			for(int i=0; i<length; i++){
				if (kind == JTypeKind.ANY) {
					arrVals[i] = new UntypedValue(memory, RefValue.makeNullRefValue(memory, JObjectType.getInstance()));
				} else {
					arrVals[i] = RefValue.makeNullRefValue(memory, (ICompoundType) elementType);
				}
			}
		}
		
		return arrVals;
	}

	@Override
	protected JValue getInternal(int index) {
		return values[index];
	}

	@Override
	public int getLength() {
		return values.length;
	}
	
	public static class Builder implements ArrayValueBuilder {
		
		private ObjectArrayValue value;
		private MemoryArea memory;
		private int length = -1;
		
		/**
		 * @param memory memory to store the built value
		 * @param etype type of element
		 * @param tt type table
		 */
		public Builder(MemoryArea memory, JType etype, ITypeTable tt){
			this.memory = memory;
			etype = JArrayType.createJArrayType(tt, etype, false);
			value = new ObjectArrayValue(memory, etype);
		}

		@Override
		public void setLength(int length){
			IntValue len = (IntValue) value.getMemberValue("length");
			len.setIntValue(length);
			
			value.values = new JValue[length];
			this.length = length;
		}
		
		@Override
		public void setValue(int index, JValue val){
			if (val.getKind() == JValueKind.OBJECT) {
				val = new RefValue(memory, (ObjectValue)val);
			}
			
			value.values[index] = val;
		}
		
		@Override
		public ObjectArrayValue getResult(){
			if (length == -1) {
				throw new JSEError("Array value not fully initialized.");
			}
			
			for(JValue v : value.values){
				if (v == null){
					throw new JSEError("Array value not fully initialized.");
				}
			}
			
			if (memory != null) {
				memory.reallocate(value);
			}
			
			return value;
		}
	}

	@Override
	public void sort(ThreadRuntime rt, boolean desc) {
		ArrayValueComparator comp = new ArrayValueComparator(rt, desc);
		java.util.Arrays.sort(this.values, comp);
	}

	private class ArrayValueComparator extends ValueComparator {

		private Map<String, JMethodType> methods;
		private ThreadRuntime rt;
		
		public ArrayValueComparator(ThreadRuntime rt, boolean desc) {
			super(desc);
			methods = new HashMap<String, JMethodType>();
			this.rt = rt;
		}
		
		@Override
		protected int compareObjectValues(JValue v1, JValue v2) {
			JMethodType jmt = findMethod(v1);
			if (jmt != null) {
				FuncCallExecutor fcall = new FuncCallExecutor(rt);
				JValue jv = fcall.invokeMethodInternal(jmt, method_compare, new JValue[]{ v2 }, v1);
				int r = ((IntValue)jv).getIntValue();
				if (r != 0 || 
				   !(JStringType.getInstance() == v1.getType() && JStringType.getInstance() != v2.getType())){
					// If compare() is defined on a string type and returned 0, while the other operand is not a string,
					// then we know the result of 0 actually means incomparability, not equality.
					return r;
				} 
			}
			
			jmt = findMethod(v2);
			if (jmt != null) {
				FuncCallExecutor fcall = new FuncCallExecutor(rt);
				JValue jv = fcall.invokeMethodInternal(jmt, method_compare, new JValue[]{ v1 }, v2);
				return -((IntValue)jv).getIntValue();
			}
			
			return 0;
		}
		
		private JMethodType findMethod(JValue v1){
			JMethodType jmt = null;
			JType t = v1.getType();
			if (t.isObject()) {
				// 1) Try to find from cache
				String fqname = t.getName();
				jmt = methods.get(fqname);
				if (jmt == null) {
					// 2) Try to find from the type
					jmt = findMethod0(v1);
					if (jmt != null) {
						methods.put(fqname, jmt);
					}
				}				
			}

			return jmt;
		}

		private JMethodType findMethod0(JValue v1){
			ObjectValue o1 = (ObjectValue)v1;
			
			// Only if the object implements IComparable or is special.
			JClassType jct = (JClassType)o1.getType();
			boolean checked = jct == JStringType.getInstance();
			if (!checked) {
				JInterfaceType[] intfs = jct.getInterfaces();
				for(JInterfaceType tf : intfs) {
					if (tf.getName().equals("System.Util.IComparable")){
						checked = true;
						break;
					}
				}
			}

			// Find "int compare(var)"
			if (checked) {
				JMethodType tmt = null;
				MethodValue[] mvs = o1.getMethodMemberValues(method_compare);
				for(MethodValue mv : mvs) {
					JMethodType jmt = mv.getMethodType();
					JParameter[] params = jmt.getParams();
					if (!mv.isStatic() && params.length == 2){
						if (params[1].getType() == AnyType.getInstance()){
							tmt = jmt;
							break;
						}
					}
				}
				
				return tmt;
			}
			
			return null;
		}
		
	}
	
	private static final String method_compare = "compare";

}
