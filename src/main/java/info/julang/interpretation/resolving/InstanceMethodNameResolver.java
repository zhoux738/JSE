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
import info.julang.interpretation.context.ContextType;
import info.julang.interpretation.context.IContextAware;
import info.julang.interpretation.context.MethodContext;
import info.julang.memory.value.JValue;
import info.julang.memory.value.ObjectMember;
import info.julang.memory.value.ObjectValue;
import info.julang.memory.value.TempValueFactory;
import info.julang.typesystem.jclass.Accessibility;
import info.julang.typesystem.jclass.ICompoundType;
import info.julang.util.OneOrMoreList;

/**
 * {@link INameResolver Name resolver} used for instance method.
 * 
 * @author Ming Zhou
 */
public class InstanceMethodNameResolver implements INameResolver, IContextAware<MethodContext> {

	private IVariableTable vt;
	
	private ITypeTable tt;
	
	private ICompoundType type;
	
	private ObjectValue thisValue;
	
	private MethodContext context;
	
	public InstanceMethodNameResolver(IVariableTable vt, ITypeTable tt, ICompoundType type){
		this.vt = vt;
		this.tt = tt;
		this.type = type;
	}

	@Override
	public void setContext(MethodContext context) {
		this.context = context;
	}
	
	@Override
	public JValue resolve(String id){
		// 1) "this" or "super"?
		if("this".equals(id) || "super".equals(id)){
			//KnownTokens.THIS_T.getLiteral()
			//KnownTokens.SUPER_T.getLiteral()
			initThisValue();
			return thisValue;
		}
		
		// 2) try as variable; but do not query global variable table.
		JValue v = vt.getVariable(id, false);
		if(v != null){
			return v;
		}
	
		// 3) instance member?
		v = getMember(id);
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
	
	private void initThisValue() {
		if(thisValue == null) {
			thisValue = (ObjectValue) vt.getVariable("this", false);
		}
	}

	private JValue getMember(String name){
		initThisValue();		
		
		ICompoundType jct = Accessibility.checkMemberAccess(thisValue.getClassType(), name, type, context, ContextType.IMETHOD, false, false);
		
		OneOrMoreList<ObjectMember> mems = thisValue.getMemberValueByClass(name, jct, true);
		if(mems == null){
			return null;
		} else if(mems.hasOnlyOne()){
			return mems.getFirst().getValue();
		} else {
			return TempValueFactory.createTempMethodGroupValue(mems);
		}
	}
}
