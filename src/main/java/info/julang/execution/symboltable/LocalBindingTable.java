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

import java.util.HashMap;
import java.util.Map;

import info.julang.memory.value.JValue;

/**
 * A local binding table provides a simple storage for variables associated with their names.
 * 
 * @author Ming Zhou
 */
public abstract class LocalBindingTable {
	
	protected Map<String, JValue> map;
	
	/**
	 * Create an empty local binding table.
	 */
	protected LocalBindingTable() {
		map = new HashMap<String, JValue>();
	}
	
	/**
	 * Get variable bound with the specified name.
	 * 
	 * @param name
	 * @return null if it's not bound.
	 */
	public JValue getVariable(String name){		
		return map.get(name);
	}
	
	/**
	 * Get all variables stored in the table.
	 * @return
	 */
	public Map<String, JValue> getAll(){
		return map;
	}
}
