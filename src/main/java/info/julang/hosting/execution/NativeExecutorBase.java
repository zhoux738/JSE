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

package info.julang.hosting.execution;

import info.julang.execution.Argument;
import info.julang.execution.threading.ThreadRuntime;
import info.julang.memory.value.ArrayValue;
import info.julang.memory.value.BoolValue;
import info.julang.memory.value.ByteValue;
import info.julang.memory.value.EnumValue;
import info.julang.memory.value.FloatValue;
import info.julang.memory.value.FuncValue;
import info.julang.memory.value.HostedValue;
import info.julang.memory.value.IntValue;
import info.julang.memory.value.JValue;
import info.julang.memory.value.ObjectValue;
import info.julang.memory.value.RefValue;
import info.julang.memory.value.StringValue;
import info.julang.typesystem.JArgumentException;
import info.julang.util.Pair;

public abstract class NativeExecutorBase implements INativeExecutor {
	
	IPolicyChecker checker;
	
	/**
	 * Create a base instance without policy checking.
	 */
	protected NativeExecutorBase(){
		this(null);
	}
	
	/**
	 * Create a base instance with the customized policy checker.
	 * @param checker
	 */
	protected NativeExecutorBase(IPolicyChecker checker){
		this.checker = checker;
	}
	
	/**
	 * Create a base instance with the standard policy checking. It queries the policy engine 
	 * with the given category and operation name, and throws exception if the permission is
	 * not granted.
	 * 
	 * @param category
	 * @param operation
	 */
	protected NativeExecutorBase(String category, String operation){
		this(new PlatformAccessPolicyChecker(category, operation));
	}
	
	public void enforcePolicy(ThreadRuntime rt) {
		if (checker != null) {
			checker.checkPolicy(rt);
		}
	}
	
	//---------------- JValue generators ----------------//
	
	/**
	 * Create a hosted value in heap of the given type, optionally referred to by a ref value created in the current frame.
	 * <p/>
	 * Do not use this to create <code><font color="green">String</font></code> value. Use {@link #createString} instead.
	 * 
	 * @param fullTypeName
	 * @param rt thread runtime
	 * @param createRefValue if true, also create a ref value that refers to the hosted value.
	 * @return a pair of hosted value and ref value, the latter being null if <code>createRefValue</code> is false.
	 */
	protected Pair<HostedValue, RefValue> createHostedValue(String fullTypeName, ThreadRuntime rt, boolean createRefValue){
		HostedValue hv = new HostedValue(
			rt.getHeap(), 
			rt.getTypeTable().getType(fullTypeName));
		
		RefValue rv = null;
		if(createRefValue){
			rv = new RefValue(rt.getStackMemory().currentFrame(), hv);
		}
		
		return new Pair<HostedValue, RefValue>(hv, rv);
	}
	
	/**
	 * Create an int value in the current frame.
	 * 
	 * @param rt
	 * @param value
	 * @return
	 */
	protected IntValue createInt(ThreadRuntime rt, int value){
		return new IntValue(rt.getStackMemory().currentFrame(), value);
	}
	
	/**
	 * Create a boolean value in the current frame.
	 * 
	 * @param rt
	 * @param value
	 * @return
	 */
	protected BoolValue createBool(ThreadRuntime rt, boolean value){
		return new BoolValue(rt.getStackMemory().currentFrame(), value);
	}
	
	/**
	 * Create a string value in the current frame.
	 * 
	 * @param rt
	 * @param value
	 * @return
	 */
	protected StringValue createString(ThreadRuntime rt, String value) {
		return new StringValue(rt.getStackMemory().currentFrame(), value);
	}
	
	//---------------- Argument extractors ----------------//
	
	protected String getString(Argument[] args, int index){
		StringValue sv = StringValue.dereference(args[index].getValue(), true);
		return sv != null ? sv.getStringValue() : null;
	}
	
	protected int getInt(Argument[] args, int index){
		Argument a = args[index];
		try {
			return ((IntValue)a.getValue()).getIntValue();
		} catch (ClassCastException e) {
			throw new JArgumentException(a.getName());
		}
	}
	
	protected byte getByte(Argument[] args, int index){
		return ((ByteValue) args[index].getValue()).getByteValue();
	}
	
	protected boolean getBool(Argument[] args, int index){
		return ((BoolValue) args[index].getValue()).getBoolValue();
	}
	
	protected float getFloat(Argument[] args, int index){
		return ((FloatValue) args[index].getValue()).getFloatValue();
	}
	
	protected String getEnumAsLiteral(Argument[] args, int index){
		return ((EnumValue) RefValue.tryDereference(args[index].getValue())).getLiteral();
	}
	
	protected int getEnumAsOrdinal(Argument[] args, int index){
		return ((EnumValue) RefValue.tryDereference(args[index].getValue())).getOrdinal();
	}
	
	protected ArrayValue getArray(Argument[] args, int index){
		Argument a = args[index];
		try {
			JValue val = a.getValue().deref();
			return (ArrayValue) val;
		} catch (ClassCastException e) {
			throw new JArgumentException(a.getName());
		}
	}
	
	/**
	 * @return Return null if it dereferences to {@link RefValue#NULL NULL}.
	 */
	protected ObjectValue getObject(Argument[] args, int index){
		JValue val = args[index].getValue().deref();
		if (val == RefValue.NULL){
			return null;
		}
		
		return (ObjectValue) val;
	}
	
	protected JValue getValue(Argument[] args, int index){
		JValue val = args[index].getValue().deref();
		return val;
	}
	
	protected FuncValue getFunction(Argument[] args, int index){
		JValue val = args[index].getValue().deref();
		return (FuncValue) val;
	}
	
	protected HostedValue getHosted(Argument[] args, int index){
		JValue val = args[index].getValue().deref();
		return (HostedValue) val;
	}
	
}
