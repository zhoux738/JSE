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

import info.julang.execution.threading.ThreadRuntime;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.internal.FuncCallExecutor;
import info.julang.memory.value.ByteValue;
import info.julang.memory.value.HostedValue;
import info.julang.memory.value.IntValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.ObjectValue;
import info.julang.memory.value.TempValueFactory;
import info.julang.typesystem.jclass.builtin.JMethodType;
import info.julang.typesystem.jclass.jufc.System.Collection.JList;


/**
 * A {@link JIndexable} backed by a <font color="green">System.Collection.List</font>.
 * <p/>
 * Do not pass this around as it contains the current context and is supposed to be 
 * used immediately.
 * 
 * @author Ming Zhou
 */
public class ListIndexable extends CollectionIndexable {
		
	private JList list;
	
	public ListIndexable(ObjectValue ov, ThreadRuntime rt, Context context, boolean applyAccLock){
		super(ov, rt, context);
		list = (JList)((HostedValue)ov).getHostedObject();
		if(applyAccLock){
			list.applyWriteLock(rt);
		}
	}

	@Override
	public JValue getCurrent() {
		FuncCallExecutor fce = new FuncCallExecutor(rt);
		
		JMethodType methodType = getMethodType(GetMethodName);
		JValue jvl = fce.invokeMethodInternal(
			methodType, 
			MethodNames[GetMethodName], 
			new JValue[]{TempValueFactory.createTempIntValue(index)}, 
			ov);
		
		return jvl;
	}
	
	@Override
	public JValue getByIndex(JValue index) throws UnsupportedIndexTypeException {
		int ii = -1;
		switch(index.getKind()){
		case INTEGER:
			IntValue iv = (IntValue) index;
			ii = iv.getIntValue();
			break;
		case BYTE:
			ByteValue bv = (ByteValue) index;
			ii = bv.getByteValue();
			break;
		default:
			throw new UnsupportedIndexTypeException("The index for a " + JList.FullTypeName + " must be an integer.");		
		}
		
		return get(ii);
	}
	
	private JValue get(int index){
		FuncCallExecutor fce = new FuncCallExecutor(rt);
		
		JMethodType methodType = getMethodType(GetMethodName);
		JValue jvl = fce.invokeMethodInternal(
			methodType, 
			MethodNames[GetMethodName], 
			new JValue[]{TempValueFactory.createTempIntValue(index)}, 
			ov);
		
		return jvl;
	}

	@Override
	protected String getTypeName() {
		return "System.Collection.List";
	}

	@Override
	public JValue setByIndex(JValue index, JValue value) throws UnsupportedIndexTypeException {
		index = index.deref();

		FuncCallExecutor fce = new FuncCallExecutor(rt);
		
		JMethodType methodType = getMethodType(PutMethodName);
		JValue jvl = fce.invokeMethodInternal(
			methodType, 
			MethodNames[PutMethodName], 
			new JValue[]{index, value}, 
			ov);
		
		return jvl;
	}
	
	@Override
	public void dispose(){
		list.releaseWriteLock(rt);
	}
}
