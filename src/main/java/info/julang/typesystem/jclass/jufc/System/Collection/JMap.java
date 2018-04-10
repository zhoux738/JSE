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

package info.julang.typesystem.jclass.jufc.System.Collection;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import info.julang.execution.Argument;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.hosting.HostedMethodProviderFactory;
import info.julang.hosting.SimpleHostedMethodProvider;
import info.julang.hosting.execution.CtorNativeExecutor;
import info.julang.hosting.execution.InstanceNativeExecutor;
import info.julang.memory.value.HostedValue;
import info.julang.memory.value.IntValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.ObjectValue;
import info.julang.memory.value.TempValueFactory;
import info.julang.memory.value.VoidValue;
import info.julang.util.Pair;

/**
 * The native implementation of <font color="green">System.Collection.Map</font>.
 * <p>
 * To use Java's {@link java.util.HashMap} as the storage backend,  we must hook it with hashCode()
 * and equals(Object) methods defined in Julian. To achieve this, we use a wrapper class, <code>
 * <font color="green">System.Collection.HashKey</font></code>, as the type of key on the side of 
 * Julian's API, regardless of the original key passed in by script composer. On the Java side, 
 * this class is wrapped by an interop class, {@link HashKeyWrapper}, which is used as the type of 
 * key for HashMap class. 
 * <p>
 * When invoked, HashMap class calls hashCode() and equals(Object) on the key object, which is a 
 * HashKeyWrapper. The latter forwards either call to <code><font color="green">
 * System.Collection.HashKey</font></code>, which, eventually, calls hashCode() and equals(Object) 
 * defined in the original key type.
 *  
 * @author Ming Zhou
 */
public class JMap {
	
	public final static String FullTypeName = "System.Collection.Map";
	
	//----------------- IRegisteredMethodProvider -----------------//
	
	public static HostedMethodProviderFactory Factory = new HostedMethodProviderFactory(FullTypeName){

		@Override
		protected void implementProvider(SimpleHostedMethodProvider provider) {
			provider
				.add("ctor", new InitExecutor())
				.add("hasKey", new HasKeyExecutor())
				.add("get", new GetExecutor())
				.add("put", new PutExecutor())
				.add("remove", new RemoveExecutor())
				.add("size", new SizeExecutor());
		}
		
	};
	
	//----------------- native executors -----------------//
	
	private static class InitExecutor extends CtorNativeExecutor<JMap> {

		@Override
		protected void initialize(ThreadRuntime rt, HostedValue hvalue, JMap jmap, Argument[] args) throws Exception {
			jmap.init();
		}
		
	}
	
	private static class HasKeyExecutor extends InstanceNativeExecutor<JMap> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, JMap jmap, Argument[] args) throws Exception {
			HashKeyWrapper k = getHashKey(rt, args);
			JValue result = jmap.hasKey(k);
			return result;
		}
		
	}
	
	private static class RemoveExecutor extends InstanceNativeExecutor<JMap> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, JMap jmap, Argument[] args) throws Exception {
			HashKeyWrapper k = getHashKey(rt, args);
			JValue result = jmap.remove(k);
			return result;
		}
		
	}
	
	private static class PutExecutor extends InstanceNativeExecutor<JMap> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, JMap jmap, Argument[] args) throws Exception {
			HashKeyWrapper k = getHashKey(rt, args);
			JValue v = args[1].getValue();
			jmap.put(k, v);
			return VoidValue.DEFAULT;
		}
		
	}
	
	private static class GetExecutor extends InstanceNativeExecutor<JMap> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, JMap jmap, Argument[] args) throws Exception {
			HashKeyWrapper k = getHashKey(rt, args);
			JValue jv = jmap.get(k);
			return jv;
		}
		
	}
	
	private static class SizeExecutor extends InstanceNativeExecutor<JMap> {
		
		@Override
		protected JValue apply(ThreadRuntime rt, JMap jmap, Argument[] args) throws Exception {
			IntValue iv = TempValueFactory.createTempIntValue(jmap.size());
			return iv;
		}
		
	}
	
	private static HashKeyWrapper getHashKey(ThreadRuntime rt, Argument[] args){
		ObjectValue val0 = (ObjectValue)args[0].getValue().deref();
		HashKeyWrapper wrapper = new HashKeyWrapper(rt, val0);
		return wrapper;
	}

	//----------------- implementation at native end -----------------//
	
	private Map<HashKeyWrapper, JValue> map;
	
	public void init(){
		this.map = new HashMap<HashKeyWrapper, JValue>();
	}
	
	public void put(HashKeyWrapper k, JValue v){
		map.put(k, v);
	}
	
	public JValue hasKey(HashKeyWrapper k){
		return TempValueFactory.createTempBoolValue(map.containsKey(k));
	}
	
	public JValue get(HashKeyWrapper k){
		JValue v = map.get(k);
		if(v == null){
			return TempValueFactory.createTempNullRefValue();
		} else {
			return v;
		}
	}
	
	public JValue remove(HashKeyWrapper k){
		JValue v = map.remove(k);
		if(v == null){
			return TempValueFactory.createTempNullRefValue();
		} else {
			return v;
		}
	}
	
	public int size(){
		return map.size();
	}
	
	/* the following methods are used internally. */
	
	public Pair<JValue, JValue>[] getAll(){
		Set<Entry<HashKeyWrapper, JValue>> set = map.entrySet();
		@SuppressWarnings("unchecked") // this is the best we can do in Java due to its array type not supporting covariance. 
		Pair<JValue, JValue>[] results = new Pair[set.size()];
		int i = 0;
		for(Entry<HashKeyWrapper, JValue> entry : set){
			JValue k = entry.getKey().getKey();
			Pair<JValue, JValue> e = new Pair<JValue, JValue>(k, entry.getValue());
			results[i] = e;
			i++;
		}
		
		return results;
	}
	
}
