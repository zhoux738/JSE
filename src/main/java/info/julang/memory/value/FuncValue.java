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

import info.julang.execution.symboltable.LocalBindingTable;
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
public class FuncValue extends ObjectValue implements IFuncValue {
	
	private JValueKind funcKind;
	
	private LocalBindingTable lbindings;
	
	/**
	 * Create a function value that stores a global function.
	 * 
	 * @param memory
	 * @param funcType
	 * @return
	 */
	public static FuncValue createGlobalFuncValue(MemoryArea memory, JFunctionType funcType){
		return new FuncValue(memory, funcType, JValueKind.FUNCTION, true);
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
	
	@Override
	public boolean isConst(){
		return true;
	}
	
	/**
	 * Set a local binding table. This will replace the old one.
	 * 
	 * @param lbt
	 */
	public void setLocalBindingTable(LocalBindingTable lbt) {
		this.lbindings = lbt;
	}
	
	//-------------- IFuncValue --------------//

	public JValueKind getFuncValueKind(){
		return funcKind;
	}

	@Override
	public LocalBindingTable getLocalBindings() {
		return lbindings;
	}
	
	/**
	 * A dummy function value that can be used where a real function value is not necessary.
	 * <p>
	 * As of 0.1.32, the sole purpose of having a function value available to the executable 
	 * is for getting the local bindings. If such bindings are not possible, then there is 
	 * no need for passing the func value.
	 */
	public static final IFuncValue DUMMY = BogusFunction.INSTANCE;
	
	private static class BogusFunction implements IFuncValue {

		private static final BogusFunction INSTANCE = new BogusFunction();
		
		private BogusFunction() {}
		
		@Override
		public JValueKind getFuncValueKind() {
			return JValueKind.FUNCTION;
		}

		@Override
		public LocalBindingTable getLocalBindings() {
			return null;
		}
		
	}
}
