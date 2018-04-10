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

package info.julang.execution.symboltable;

import info.julang.external.interfaces.IExtVariableTable;
import info.julang.memory.value.JValue;

public interface IVariableTable extends IExtVariableTable {

	/**
	 * Create a new VariableTable.
	 * 
	 * @param gvt the global variable table this var table can refer to. If it is null, it implies
	 * that this table is itself the global table.
	 */
	public void addVariable(String name, JValue value);
	
	/**
	 * Get variable with given name from the variable table.
	 * <p/>
	 * The search begins from the current scope. If it is not found in current scope, it will look up in the enclosing one. 
	 * The process is repeated up to the outermost scope. If still not found, try the global variable scope (the outermost
	 * scope in the global name space).
	 * 
	 * @param name
	 * @return null if no variable with given name is found.
	 */
	public JValue getVariable(String name);
	
	/**
	 * Get variable with given name from the variable table.
	 * <p/>
	 * The search begins from the current scope. If it is not found in current scope, it will look up in the enclosing one. 
	 * The process is repeated up to the outermost scope. If still not found, may optionally try the global variable scope 
	 * (the outermost scope in the global name space).
	 * @param name
	 * @param tryGlobal	true to try to get variable from the global variable scope as the last resort.
	 * @return
	 */
	public JValue getVariable(String name, boolean tryGlobal);
	
	/**
	 * Add a new bound value to the variable table. This method should be used only by global variable table.
	 * <p/>
	 * Adding a binding with an existing name will overwrite the current one.
	 * @param name
	 * @param value
	 */
	public void addBinding(String name, JValue value);
	
	/**
	 * Get a bound value with specified name. This method should be used only by global variable table.
	 * @param name
	 * @return null if not found.
	 */
	public JValue getBinding(String name);
	
	/**
	 * Traverse the variable table by scope, delegating actual processing of each scope to a specified traverser.
	 * 
	 * @param traverser
	 * @param topDown true if processing in top-down order; false bottom-up.
	 */
	public void traverse(IVariableTableTraverser traverser, boolean topDown);
	
	/**
	 * Get global variable table.
	 * @return If this table has global var table, return it; otherwise return this table itself.
	 */
	public IVariableTable getGlobal();
}
