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
import info.julang.typesystem.jclass.builtin.JMethodType;

/**
 * A derivative of {@link FuncValue}, MethodValue represents a method. 
 * <p/>
 * It can be either instance or static, as determined by {@link #isStatic()}. 
 * If {@link #isStatic()} returns false, it is an instance method whose <i>this</i> value can be
 * obtained by calling {@link #getThisValue()}.
 * 
 * @author Ming Zhou
 */
public class MethodValue extends FuncValue implements IMethodValue {

	private ObjectValue thisValue;
	
	/**
	 * Create a instance method value.
	 * @param memory
	 * @param methodType
	 * @param thisValue
	 * @return
	 */
	public static MethodValue createInstanceMethodValue(MemoryArea memory, JMethodType methodType, ObjectValue thisValue, boolean initFuncMembers){
		return new MethodValue(memory, methodType, thisValue, initFuncMembers);
	}
	
	/**
	 * Create a static method value.
	 * @param memory
	 * @param methodType
	 * @return
	 */
	public static MethodValue createStaticMethodValue(MemoryArea memory, JMethodType methodType, boolean initFuncMembers){
		return new MethodValue(memory, methodType, null, initFuncMembers);
	}
	
	private MethodValue(MemoryArea memory, JMethodType methodType, ObjectValue thisValue, boolean initFuncMembers){
		super(memory, methodType, JValueKind.METHOD, initFuncMembers);
		this.thisValue = thisValue;
	}
	
	/**
	 * Get method type. This is equivalent to calling:
	 * <p/>
	 * <code>(JMethodType)getType()</code>
	 * 
	 * @return
	 */
	public JMethodType getMethodType(){
		return (JMethodType) type;
	}
	
	//------------ IMethodValue ------------//
	
	public boolean isStatic(){
		return thisValue == null;
	}
	
	public JValue getThisValue() {
		return thisValue;
	}
}
