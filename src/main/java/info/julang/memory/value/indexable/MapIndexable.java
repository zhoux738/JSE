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

import java.util.Map.Entry;

import info.julang.execution.Argument;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.interpretation.context.Context;
import info.julang.interpretation.internal.FuncCallExecutor;
import info.julang.interpretation.internal.NewObjExecutor;
import info.julang.interpretation.syntax.ParsedTypeName;
import info.julang.memory.value.HostedValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.ObjectValue;
import info.julang.typesystem.jclass.JClassConstructorMember;
import info.julang.typesystem.jclass.JClassType;
import info.julang.typesystem.jclass.builtin.JMethodType;
import info.julang.typesystem.jclass.jufc.System.Collection.JMap;
import info.julang.util.Pair;


/**
 * A {@link JIndexable} backed by a <font color="green">System.Collection.Map</font>.
 * <p/>
 * Do not pass this around as it contains the current context and is supposed to be 
 * used immediately.
 * 
 * @author Ming Zhou
 */
public class MapIndexable extends CollectionIndexable {
	
	private JMap map;
	
	private Pair<JValue, JValue>[] all;
	
	private JClassType entryClassType;
	
	private JClassConstructorMember entryClassCtor;
	
	protected final ParsedTypeName entryClassPTName = ParsedTypeName.makeFromFullName("System.Collection.Entry");
	
	public MapIndexable(ObjectValue ov, ThreadRuntime rt, Context context){
		super(ov, rt, context);
		map = (JMap)(((HostedValue)ov).getHostedObject());
	}

	@Override
	public JValue getCurrent() {
		if(all == null){
			all = map.getAll(); // Assumption: the size of this array is same to that returned by getLength()
		}
		
		NewObjExecutor noe = new NewObjExecutor(rt);
		if(entryClassCtor == null){
			entryClassType = getClassType(entryClassPTName);
			entryClassCtor = entryClassType.getClassConstructors()[0];
		}
		Pair<JValue, JValue> entry = all[index];
		
		ObjectValue val = noe.newObjectInternal(entryClassType, entryClassCtor, 
			new Argument[]{new Argument("key", entry.getFirst()), new Argument("value", entry.getSecond())});
		
		return val;
	}
	
	@Override
	public JValue getByIndex(JValue index) throws UnsupportedIndexTypeException {
		index = index.deref();

		FuncCallExecutor fce = new FuncCallExecutor(rt);
		
		JMethodType methodType = getMethodType(GetMethodName);
		JValue jvl = fce.invokeMethodInternal(
			methodType, 
			MethodNames[GetMethodName], 
			new JValue[]{index}, 
			ov);
		
		return jvl;
	}

	@Override
	protected String getTypeName() {
		return "System.Collection.Map";
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
}
