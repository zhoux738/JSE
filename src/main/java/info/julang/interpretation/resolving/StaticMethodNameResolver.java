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
import info.julang.langspec.Keywords;
import info.julang.memory.value.JValue;
import info.julang.memory.value.MethodGroupValue;
import info.julang.memory.value.MethodValue;
import info.julang.memory.value.TempValueFactory;
import info.julang.memory.value.TypeValue;
import info.julang.memory.value.VoidValue;
import info.julang.typesystem.jclass.ICompoundType;

/**
 * {@link INameResolver Name resolver} used for static method.
 * 
 * @author Ming Zhou
 */
public class StaticMethodNameResolver implements INameResolver {

	private IVariableTable vt;
	
	private ITypeTable tt;
	
	private LocalBindingTable lbt;
	
	private ICompoundType type;
	
	private TypeValue tvalue;
	
	private IMemberNameResolver memResolver;
	
	public StaticMethodNameResolver(
		IVariableTable vt, ITypeTable tt, LocalBindingTable lbt, ICompoundType type, IMemberNameResolver memResolver){
		this.vt = vt;
		this.tt = tt;
		this.lbt = lbt;
		this.type = type;
		this.memResolver = memResolver != null ? memResolver : NoCacheMemberNameResolver.INSTANCE;
	}
	
	@Override
	public JValue resolve(String id){
		// 1) try pre-bindings 
		boolean isThis = "this".equals(id);
		if (isThis && lbt != null) {
			JValue v = lbt.getVariable(id);
			if(v != null){
				return v;
			}
		}
		
		// 2) try as variable; but do not query global variable table.
		JValue v = vt.getVariable(id, false);
		if(v != null){
			return v;
		} else if (isThis) {
			throw new RuntimeCheckException("'this' cannot be used in non-extension static method, which must name the first parameter as 'this'.");
		}
		
		// 3) static member?
		v = memResolver.resolve(id);
		if (v == null) {
			// Havn't resolved this id before
			v = getMember(id);
			memResolver.save(id, v);
		} else if (v == VoidValue.DEFAULT) {
			// It has been resolved to null
			v = null;
		}

		if(v != null){
			return v;
		}
		
		// 4) try as type
		v = tt.getValue(id);
		if(v != null){
			return v;
		}
		
		return null;
	}	
	
	private JValue getMember(String name){
		if(tvalue == null){
			tvalue = tt.getValue(type.getName());
		}
		
		JValue jval = tvalue.getMemberValue(name);
		
		// Handle method overloading
		if (jval != null && jval.deref() instanceof MethodValue) {
			MethodValue[] mvs = tvalue.getMethodMemberValues(name);
			if (mvs.length > 1){
				MethodGroupValue mgv = TempValueFactory.createTempMethodGroupValue(mvs);
				return mgv;
			}
		}
		
		return jval;
	}
}
