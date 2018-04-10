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

package info.julang.memory.value.indexable;

import info.julang.execution.Argument;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.internal.FuncCallExecutor;
import info.julang.interpretation.syntax.ParsedTypeName;
import info.julang.memory.value.IntValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.ObjectValue;
import info.julang.typesystem.jclass.JClassMethodMember;
import info.julang.typesystem.jclass.JClassType;
import info.julang.typesystem.jclass.builtin.JMethodType;

// A base class used only in this package for shared code among indexable JuFCs.
abstract class CollectionIndexable implements JIndexable {
	
	protected static final String[] MethodNames = new String[]{"size", "get", "put"};
	protected static final int SizeMethodName = 0;	
	protected static final int GetMethodName = 1;
	protected static final int PutMethodName = 2;
	
	protected JClassType classType; 
	
	protected final JMethodType[] methodTypes = new JMethodType[MethodNames.length];
	
	protected ObjectValue ov;
	
	protected ThreadRuntime rt;
	
	protected Context context;
	
	protected final ParsedTypeName className = ParsedTypeName.makeFromFullName(getTypeName());
	
	public CollectionIndexable(ObjectValue ov, ThreadRuntime rt, Context context){
		this.ov = ov;
		this.rt = rt;
		this.context = context;
	}
	
	protected abstract String getTypeName();
	
	protected synchronized JMethodType getMethodType(int methodIndex){
		if(methodTypes[methodIndex] == null){
			if(classType == null){
				classType = getClassType(className);
			}
			JClassMethodMember jcmm = (JClassMethodMember) classType.getInstanceMemberByName(MethodNames[methodIndex]);
			methodTypes[methodIndex] = jcmm.getMethodType();
		}
		
		return methodTypes[methodIndex];
	}
	
	protected synchronized JClassType getClassType(ParsedTypeName className){
		return (JClassType) context.getTypeResolver().resolveType(className);
	}
	
	@Override
	public int getLength() {		
		FuncCallExecutor fce = new FuncCallExecutor(rt);
		
		JMethodType methodType = getMethodType(SizeMethodName);
		JValue jvl = fce.invokeFunction(methodType, MethodNames[SizeMethodName], Argument.CreateThisOnlyArguments(ov));
		
		return ((IntValue)jvl).getIntValue();
	}
	
	@Override
	public JValue getIndexableValue() {
		return ov;
	}
	
	protected int index;
	
	@Override
	public void setIndex(int index) {
		this.index = index;
	}

	@Override
	public int getIndex() {
		return index;
	}
	
	@Override
	public void dispose(){
		// Nothing to be done here.
	}
}
