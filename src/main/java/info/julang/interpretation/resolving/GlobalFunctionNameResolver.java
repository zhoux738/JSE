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

package info.julang.interpretation.resolving;

import info.julang.execution.symboltable.ITypeTable;
import info.julang.execution.symboltable.IVariableTable;
import info.julang.execution.symboltable.LocalBindingTable;
import info.julang.interpretation.RuntimeCheckException;
import info.julang.memory.value.JValue;

/**
 * {@link INameResolver Name resolver} used for global script.
 * 
 * @author Ming Zhou
 */
public class GlobalFunctionNameResolver implements INameResolver {

	private IVariableTable vt;
	
	private ITypeTable tt;
	
	private LocalBindingTable lbt;
	
	public GlobalFunctionNameResolver(IVariableTable vt, LocalBindingTable lbt, ITypeTable tt){
		this.vt = vt;
		this.lbt = lbt;
		this.tt = tt;
	}
	
	@Override
	public JValue resolve(String id){
		// 1) try pre-bindings for 'this', which is only available if the function has been explicitly bound.
		boolean isThis = "this".equals(id);
		if (isThis) {
			if (lbt != null) {
				JValue v = lbt.getVariable(id);
				if(v != null){
					return v;
				}
			}
		}
		
		// 2) try as local variable 
		JValue v = vt.getVariable(id);
		if(v != null){
			return v;
		}
		
		// 3) try as type
		v = tt.getValue(id);
		if(v != null){
			return v;
		}
		
		if (isThis) {
			throw new RuntimeCheckException("'this' cannot be used in script or global function unless it's been explicitly bound.");
		}
		
		return null;
	}
	
}
