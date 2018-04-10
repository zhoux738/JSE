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

import info.julang.external.interfaces.IExtValue;
import info.julang.external.interfaces.IExtVariableTable;
import info.julang.memory.value.JValue;
import info.julang.memory.value.RefValue;
import info.julang.memory.value.UntypedValue;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Variable table stores the script variable information during runtime.
 * <p/>
 * Like other languages, Julian has the concept of scope. A variable in some inner scope will cloak the 
 * one with same name in outer scope. Scope boundary rules: <br/>
 * (1) function entry/exit or class entry/exit (via method invocation) forms the outermost scope <br/>
 * (2) block enclosed by curly braces { } forms a new scope <br/>
 * (3) for statement forms a new scope <br/>
 * 
 * @author Ming Zhou
 */
public class VariableTable implements IVariableTable {

	private VariableTable gvt;
	
	private Map<String, JValue> bindings;
	
	private Deque<Map<String, JValue>> scopes = new ArrayDeque<Map<String, JValue>>();
	
	/**
	 * [CFOW] Create a new VariableTable.
	 * 
	 * @param gvt the global variable table this var table can refer to. If it is null, it implies
	 * that this table is itself the global table. Nominally accepts an {@link IExtVariableTable}, 
	 * but in fact it <b>must</b> be an instance of {@link VariableTable}.
	 */
	public VariableTable(IExtVariableTable gvt){
		this.gvt = (VariableTable)gvt;
	}
	
	/**
	 * Add a new variable to current scope.
	 * 
	 * @param name
	 * @param value
	 * @throws SymbolBindingException if a variable with same name is defined in the current scope.
	 */
	public void addVariable(String name, JValue value) throws SymbolBindingException {
		Map<String, JValue> map = scopes.peek();
		if(map.containsKey(name)){
			throw new SymbolDuplicatedDefinitionException(name);
		}
		
		map.put(name, value);
	}
	
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
	public JValue getVariable(String name){		
		return getVariable(name, true);
	}
	
	/**
	 * Get the dereferenced variable for the given name.
	 * 
	 * @return the actual value that is dereferenced from the variable. So it cannot be a 
	 * {@link RefValue} or {@link UntypedValue}. null if not found.
	 */
	public IExtValue getValue(String name){		
		JValue val = getVariable(name);
		return val != null ? val.deref() : null;
	}
	
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
	public JValue getVariable(String name, boolean tryGlobal){
		// Deque's iterator is in stack order.
		for(Map<String, JValue> scope : scopes){
			JValue value = scope.get(name);
			if(value!=null){
				return value;
			}
		}
		
		// Query global variable table and bindings
		if(gvt == null && bindings != null){
			return bindings.get(name);
		} else if(tryGlobal && gvt != null){
			Map<String, JValue> scope = gvt.scopes.peekLast();
			if(scope != null){
				return scope.get(name);
			}
			
			if(gvt.bindings != null){
				return gvt.bindings.get(name);
			}
		}
		
		return null;
	}
	
	/**
	 * Add a new bound value to the variable table. This method should be used only by global variable table.
	 * <p/>
	 * Adding a binding with an existing name will overwrite the current one.
	 * @param name
	 * @param value
	 */
	public void addBinding(String name, JValue value) {
		if(bindings == null){
			bindings = new HashMap<String, JValue>();
		}

		bindings.put(name, value);
	}
	
	/**
	 * Get a bound value with specified name. This method should be used only by global variable table.
	 * @param name
	 * @return null if not found.
	 */
	public JValue getBinding(String name){
		return bindings != null ? bindings.get(name) : null;
	}
	
	/**
	 * Enter into a new scope.
	 */
	public void enterScope(){
		scopes.push(new HashMap<String, JValue>());
	}
	
	/**
	 * Exit from the current scope. The enclosing scope becomes the current scope.
	 */
	public void exitScope(){
		scopes.pop();
	}
	
	/**
	 * Clear everything from this variable table.
	 */
	public void clear(){
		scopes.clear();
		bindings.clear();
	}
	
	/**
	 * Traverse the variable table by scope, delegating actual processing of each scope to a specified traverser.
	 * 
	 * @param traverser
	 * @param topDown true if processing in top-down order; false bottom-up.
	 */
	public void traverse(IVariableTableTraverser traverser, boolean topDown){
		if(scopes != null){
			int size = scopes.size();
			@SuppressWarnings("unchecked")
			Map<String, JValue>[] array = new Map[size];
			scopes.toArray(array);
			
			int initial = 0;
			int termination = size;
			int step = 1;
			if(topDown){
				initial = size - 1;
				termination = -1;
				step = -1;
			}
			
			for(int i=initial;i!=termination;i+=step){
				if(traverser.processScope(i, array[i])){
					break;
				}
			}
		}
	}
	
	/**
	 * Get the count of scope level. Each call to {@link #enterScope()} increments one level, 
	 * each call to {@link #exitScope()} decrements one level.
	 * @return
	 */
	public int getNestLevel(){
		return scopes.size();
	}
	
	/**
	 * Get global variable table.
	 * @return If this table has global var table, return it; otherwise return this table itself.
	 */
	public IVariableTable getGlobal(){
		return gvt != null ? gvt : this;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		int i = 0;
		for(Map<String, JValue> map : scopes){
			sb.append("[scope " + i + "]\n");
			Set<Entry<String, JValue>> set = map.entrySet();
			for(Entry<String, JValue> entry : set){
				sb.append("  " + entry.getKey());
				sb.append(":  " + entry.getValue());	
				sb.append("\n");
			}
			i++;
		}
		return sb.toString();
	}
	
}
