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
import info.julang.typesystem.jclass.builtin.JConstructorType;

/**
 * A derivative of {@link FuncValue}, CtorValue represents an instance constructor. 
 * <p/>
 * Its <i>this</i> value can be obtained by calling {@link #getThisValue()}.
 * 
 * @author Ming Zhou
 */
public class CtorValue extends FuncValue {

	private ObjectValue thisValue;

	/**
	 * Create a constructor value.
	 * 
	 * @param memory
	 * @param methodType
	 * @param thisValue
	 * @param initFuncMembers
	 */
	public CtorValue(MemoryArea memory, JConstructorType ctorType, ObjectValue thisValue, boolean initFuncMembers){
		super(memory, ctorType, JValueKind.CONSTRUCTOR, initFuncMembers);
		this.thisValue = thisValue;
	}
	
	/**
	 * The object this constructor belongs to.
	 * 
	 * @return null if this method is static (see {@link #isStatic()}).
	 */
	public JValue getThisValue() {
		return thisValue;
	}
	
	/**
	 * Get method type. This is equivalent to calling:
	 * <p/>
	 * <code>(JConstructorType)getType()</code>
	 * 
	 * @return
	 */
	public JConstructorType getCtorType(){
		return (JConstructorType) type;
	}
}
