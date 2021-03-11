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

import java.util.Map;
import java.util.Map.Entry;

import info.julang.memory.value.JValue;

/**
 * A referential environment which contains defined local variables (including arguments) at the 
 * time the lambda is defined.
 * <p>
 * In contrast to {@link VariableTable}, variables in Display are laid out flatly, with variables
 * coming in the original inner scopes overwriting those from outer scopes, when the names conflict.
 * 
 * @author Ming Zhou
 */
public class Display extends LocalBindingTable implements IVariableTableTraverser {

	/**
	 * Create a display that inherits from another, and also captures the current lexical context.
	 * 
	 * @param d A parent display.
	 * @param vt
	 */
	public Display(Display d, IVariableTable vt) {
		super();
		if (d != null && d.map != null) {
			map.putAll(d.map);
		}
		vt.traverse(this, false);
	}

	@Override
	public boolean processScope(int level, Map<String, JValue> scope) {
		for(Entry<String, JValue> entry : scope.entrySet()){
			map.put(entry.getKey(), entry.getValue());
		}
		return false;
	}
	
}
