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
import info.julang.typesystem.JType;
import info.julang.typesystem.jclass.builtin.JFunctionType;

/**
 * A function value that stores a function.
 * <p/>
 * As first-class object, function is stored in value and treated the same way as other kinds such as int or string.
 * This class for itself is used for global function. Method and constructor are also values and their classes
 * ({@link MethodValue}, {@link CtorValue}) are derived from this.
 * 
 * @author Ming Zhou
 */
public class FuncValue extends ObjectValue {
	
	private JValueKind funcKind;
	
	/**
	 * Create a function value that stores a global function.
	 * 
	 * @param memory
	 * @param funcType
	 * @return
	 */
	public static FuncValue createGlobalFuncValue(MemoryArea memory, JFunctionType funcType, boolean initFuncMembers){
		return new FuncValue(memory, funcType, JValueKind.FUNCTION, initFuncMembers);
	}
	
	@Override
	protected void initialize(JType type, MemoryArea memory) {
		initializeMembers(type, memory, false);
	}
	
	protected FuncValue(MemoryArea memory, JFunctionType funcType, JValueKind kind, boolean initFuncMembers) {
		super(memory, funcType, true);
		
		if(initFuncMembers){
			members.populateMethodMembersForFunctionType(memory, funcType, this);
		}
		
		if(memory != null){
			memory.allocate(this);
		}
		funcKind = kind;
	}
	
	@Override
	public JValueKind getBuiltInValueKind(){
		return JValueKind.FUNCTION;
	}

	/**
	 * Get the kind of this func value. The kind can be 
	 * {@link JValueKind#FUNCTION FUNCTION}, 
	 * {@link JValueKind#METHOD METHOD} or 
	 * {@link JValueKind#METHOD_GROUP METHOD_GROUP} or
	 * {@link JValueKind#CONSTRUCTOR CONSTRUCTOR}.
	 * @return
	 */
	public JValueKind getFuncValueKind(){
		return funcKind;
	}
	
	@Override
	public boolean isConst(){
		return true;
	}
}
