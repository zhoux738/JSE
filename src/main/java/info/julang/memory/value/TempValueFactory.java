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
import info.julang.external.exceptions.JSEError;
import info.julang.typesystem.JType;
import info.julang.typesystem.jclass.ICompoundType;
import info.julang.typesystem.jclass.builtin.JMethodType;
import info.julang.util.OneOrMoreList;

/**
 * A factory used to create temporary value. Values created by this factory won't get stored.
 * 
 * @author Ming Zhou
 */
public final class TempValueFactory {

	/**
	 * Create a new int value.
	 * 
	 * @param i the actual value of this integer.
	 * @return
	 */
	public static IntValue createTempIntValue(int i){
		return new IntValue(null, i);
	}
	
	/**
	 * Create a new byte value.
	 * @param b the actual value of this byte.
	 * @return
	 */
	public static ByteValue createTempByteValue(byte b){
		return new ByteValue(null, b);
	}
	
	/**
	 * Create a new float value.
	 * 
	 * @param f the actual value of this float.
	 * @return
	 */
	public static FloatValue createTempFloatValue(float f){
		return new FloatValue(null, f);
	}
	
	/**
	 * Create a new boolean value.
	 * 
	 * @param z the actual value of this boolean.
	 * @return
	 */
	public static BoolValue createTempBoolValue(boolean z) {
		return new BoolValue(null, z);
	}
	
	/**
	 * Create a new char value.
	 * 
	 * @param c the actual value of this char.
	 * @return
	 */
	public static CharValue createTempCharValue(char c) {
		return new CharValue(null, c);
	}
	
	/**
	 * Create a new string value.
	 * 
	 * @param s the actual value of this string.
	 * @return
	 */
	public static StringValue createTempStringValue(String s) {
		return new StringValue(null, s);
	}
	
	/**
	 * Create a new ref value with type derived from the referee.
	 * 
	 * @param v the actual value being referenced.
	 * @return
	 */
	public static RefValue createTempRefValue(ObjectValue v){
		return new RefValue(null, v);
	}
	
	/**
	 * Create a new ref value with overridden type.
	 * 
	 * @param v the actual value being referenced.
	 * @return
	 */
	public static RefValue createTempRefValue(ObjectValue v, ICompoundType type){
		return new RefValue(null, v, type);
	}
	
	/**
	 * Create a new null-ref value.
	 * 
	 * @return
	 */
	public static RefValue createTempNullRefValue(){
		return new RefValue(null, RefValue.NULL);
	}
	
	/**
	 * Create a new 1-dimensional array value.
	 * 
	 * @param type
	 * @param length
	 * @return
	 */
	public static ArrayValue createTemp1DArrayValue(ITypeTable tt, JType type, int length) {
		return ArrayValueFactory.createArrayValue(null, tt, type, length);
	}
	
	/**
	 * Create a new 2-dimensional array value.
	 * 
	 * @param type
	 * @param length
	 * @return
	 */
	public static ArrayValue createTemp2DArrayValue(ITypeTable tt, JType type, int rLength, int cLength) {
		return ArrayValueFactory.createArrayValue(null, tt, type, new int[]{rLength, cLength});
	}
	
	/**
	 * Create a method group value.
	 * 
	 * @param members
	 * @return
	 */
	public static MethodGroupValue createTempMethodGroupValue(OneOrMoreList<ObjectMember> members){
		MethodValue[] methodValues = new MethodValue[members.size()];
		int i = 0;
		for(ObjectMember om : members){
			JValue vl = om.getValue();
			if(!(vl.getType() instanceof JMethodType)){
				throw new JSEError("Cannot create a method group value with non-method values.");
			} else {
				methodValues[i] = (MethodValue) RefValue.dereference(vl);
			}
			
			i++;
		}
		
		MethodGroupValue mgv = new MethodGroupValue(null, methodValues);
		return mgv;
	}
	
	/**
	 * Create a method group value.
	 * 
	 * @param members
	 * @return
	 */
	public static MethodGroupValue createTempMethodGroupValue(MethodValue[] mvs){
		return new MethodGroupValue(null, mvs);
	}
}
